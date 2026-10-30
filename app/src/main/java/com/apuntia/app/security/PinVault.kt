package com.apuntia.app.security

import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

private const val KEY_SALT = "salt"
private const val KEY_WRAPPED = "wrapped_key"
private const val KEY_FAILED = "failed_attempts"
private const val KEY_LOCKED_UNTIL = "locked_until"
private const val KEY_BYTES = 32
private const val SALT_BYTES = 16
private const val PBKDF2_ITERATIONS = 210_000
private const val ATTEMPTS_BEFORE_LOCKOUT = 5
private const val LOCKOUT_MS = 2 * 60_000L
private const val MAX_FAILURES = 8
private const val BITS_PER_BYTE = 8

/**
 * Bóveda protegida por un PIN.
 *
 * Una llave aleatoria cifra las notas. Esa llave se guarda cifrada dos veces: con una llave sacada
 * del PIN (PBKDF2) y con una llave del Android Keystore. Para abrirla hacen falta el PIN y este teléfono.
 * Tras 5 intentos fallidos hay que esperar 2 minutos; después quedan 3 intentos más y, si todos
 * fallan, se borran para siempre las notas confidenciales y se quita el PIN.
 *
 * @param storage dónde se guardan los datos protegidos.
 * @param wrapper llave del teléfono que envuelve la llave de la bóveda.
 * @param clock hora actual en milisegundos; se cambia en las pruebas.
 * @param iterations vueltas de PBKDF2; las pruebas usan menos para ir rápido.
 * @param onWipe borra las notas confidenciales de la base de datos cuando se agotan los intentos.
 */
class PinVault(
    private val storage: VaultStorage,
    private val wrapper: KeyWrapper,
    private val clock: () -> Long = System::currentTimeMillis,
    private val iterations: Int = PBKDF2_ITERATIONS,
    private val onWipe: suspend () -> Unit = {},
) : Vault {
    private val random = SecureRandom()

    @Volatile
    private var key: ByteArray? = null

    private val mutableState = MutableStateFlow(
        if (storage.read(KEY_WRAPPED) == null) VaultState.NO_PIN else VaultState.LOCKED,
    )

    override val state: StateFlow<VaultState> = mutableState.asStateFlow()

    override suspend fun createPin(pin: String) {
        check(mutableState.value == VaultState.NO_PIN) { "Ya existe un PIN" }
        require(PinRules.isValid(pin)) { "El PIN no cumple las reglas" }
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val vaultKey = ByteArray(KEY_BYTES).also(random::nextBytes)
        val wrapped = withContext(Dispatchers.Default) {
            wrapper.wrap(AesGcm.encrypt(derive(pin, salt, iterations), vaultKey))
        }
        val saved = storage.write(
            mapOf(
                KEY_SALT to encode(salt),
                KEY_WRAPPED to encode(wrapped),
                KEY_FAILED to "0",
                KEY_LOCKED_UNTIL to "0",
            ),
        )
        check(saved) { "No se pudo guardar el PIN" }
        key = vaultKey
        mutableState.value = VaultState.UNLOCKED
    }

    override suspend fun unlock(pin: String): UnlockResult {
        val remaining = readLong(KEY_LOCKED_UNTIL) - clock()
        if (remaining > 0) return UnlockResult.LockedOut(remaining)
        val salt = decode(checkNotNull(storage.read(KEY_SALT)) { "No hay PIN" })
        val wrapped = decode(checkNotNull(storage.read(KEY_WRAPPED)) { "No hay PIN" })
        val vaultKey = withContext(Dispatchers.Default) {
            val inner = wrapper.unwrap(wrapped)
            try {
                AesGcm.decrypt(derive(pin, salt, iterations), inner)
            } catch (_: GeneralSecurityException) {
                null
            }
        }
        return if (vaultKey == null) registerFailure() else registerSuccess(vaultKey)
    }

    private fun registerSuccess(vaultKey: ByteArray): UnlockResult {
        storage.write(mapOf(KEY_FAILED to "0", KEY_LOCKED_UNTIL to "0"))
        key = vaultKey
        mutableState.value = VaultState.UNLOCKED
        return UnlockResult.Success
    }

    private suspend fun registerFailure(): UnlockResult {
        val failed = readLong(KEY_FAILED).toInt() + 1
        if (failed >= MAX_FAILURES) {
            wipe()
            return UnlockResult.Wiped
        }
        val lockedOut = failed == ATTEMPTS_BEFORE_LOCKOUT
        val lockedUntil = if (lockedOut) clock() + LOCKOUT_MS else 0L
        storage.write(mapOf(KEY_FAILED to failed.toString(), KEY_LOCKED_UNTIL to lockedUntil.toString()))
        return if (lockedOut) {
            UnlockResult.LockedOut(LOCKOUT_MS)
        } else {
            val afterLockout = failed > ATTEMPTS_BEFORE_LOCKOUT
            val left = if (afterLockout) MAX_FAILURES - failed else ATTEMPTS_BEFORE_LOCKOUT - failed
            UnlockResult.WrongPin(left, willWipe = afterLockout)
        }
    }

    // Primero se borran las notas; solo si eso salió bien se quita el PIN. Así, si algo falla,
    // el siguiente intento fallido vuelve a intentar el borrado.
    private suspend fun wipe() {
        onWipe()
        check(storage.clear()) { "No se pudo quitar el PIN" }
        key = null
        mutableState.value = VaultState.NO_PIN
    }

    override fun finalAttemptsLeft(): Int? {
        val failed = readLong(KEY_FAILED).toInt()
        return if (failed >= ATTEMPTS_BEFORE_LOCKOUT) MAX_FAILURES - failed else null
    }

    override fun lock() {
        key = null
        if (mutableState.value == VaultState.UNLOCKED) mutableState.value = VaultState.LOCKED
    }

    override fun encrypt(plain: String): ByteArray = AesGcm.encrypt(openKey(), plain.toByteArray(Charsets.UTF_8))

    override fun decrypt(blob: ByteArray): String = String(AesGcm.decrypt(openKey(), blob), Charsets.UTF_8)

    private fun openKey(): ByteArray = checkNotNull(key) { "La bóveda está cerrada" }

    private fun readLong(name: String): Long = storage.read(name)?.toLongOrNull() ?: 0L

    private fun derive(pin: String, salt: ByteArray, rounds: Int): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, rounds, KEY_BYTES * BITS_PER_BYTE)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    private fun encode(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    private fun decode(text: String): ByteArray = Base64.getDecoder().decode(text)
}
