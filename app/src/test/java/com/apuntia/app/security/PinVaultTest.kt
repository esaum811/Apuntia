package com.apuntia.app.security

import java.security.GeneralSecurityException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/** Pruebas de la bóveda: PIN, cifrado y bloqueo por intentos fallidos. */
class PinVaultTest {
    private val storage = FakeStorage()
    private var now = 1_000_000L

    private fun newVault(onWipe: suspend (String?) -> Unit = {}) =
        PinVault(storage, IdentityWrapper(), clock = { now }, iterations = TEST_ITERATIONS, onWipe = onWipe)

    @Test
    fun startsWithoutPin() {
        assertEquals(VaultState.NO_PIN, newVault().state.value)
    }

    @Test
    fun createPinOpensVaultAndEncryptsRoundTrip() = runBlocking {
        val vault = newVault()
        vault.createPin("1234")
        assertEquals(VaultState.UNLOCKED, vault.state.value)
        assertEquals("secreto ñ", vault.decrypt(vault.encrypt("secreto ñ")))
    }

    @Test
    fun encryptedBlobDoesNotContainThePlainText() = runBlocking {
        val vault = newVault()
        vault.createPin("1234")
        val blob = vault.encrypt("texto muy secreto")
        assertFalse(String(blob, Charsets.ISO_8859_1).contains("secreto"))
    }

    @Test
    fun lockedVaultRefusesToDecrypt() = runBlocking {
        val vault = newVault()
        vault.createPin("1234")
        val blob = vault.encrypt("hola")
        vault.lock()
        assertEquals(VaultState.LOCKED, vault.state.value)
        try {
            vault.decrypt(blob)
            fail("Debió lanzar IllegalStateException")
        } catch (_: IllegalStateException) {
            // Esperado.
        }
    }

    @Test
    fun correctPinReadsDataEncryptedBeforeInANewSession() = runBlocking {
        val first = newVault()
        first.createPin("4321")
        val blob = first.encrypt("nota guardada")
        val second = newVault()
        assertEquals(VaultState.LOCKED, second.state.value)
        assertEquals(UnlockResult.Success, second.unlock("4321"))
        assertEquals("nota guardada", second.decrypt(blob))
    }

    @Test
    fun wrongPinKeepsTheVaultLockedAndCountsAttempts() = runBlocking {
        val vault = newVault().also { it.createPin("1234") }
        vault.lock()
        assertEquals(UnlockResult.WrongPin(attemptsLeft = 4, willWipe = false), vault.unlock("0000"))
        assertEquals(VaultState.LOCKED, vault.state.value)
    }

    @Test
    fun fiveWrongPinsLockOutEvenTheCorrectPinUntilTheWaitEnds() = runBlocking {
        val vault = newVault().also { it.createPin("1234") }
        vault.lock()
        repeat(4) { vault.unlock("0000") }
        val result = vault.unlock("0000")
        assertTrue(result is UnlockResult.LockedOut)
        assertTrue(vault.unlock("1234") is UnlockResult.LockedOut)
        assertEquals(120_000L, (result as UnlockResult.LockedOut).remainingMillis)
        now += 121_000
        assertEquals(UnlockResult.Success, vault.unlock("1234"))
    }

    @Test
    fun lockoutSurvivesRestartingTheApp() = runBlocking {
        val vault = newVault().also { it.createPin("1234") }
        vault.lock()
        repeat(5) { vault.unlock("0000") }
        assertTrue(newVault().unlock("1234") is UnlockResult.LockedOut)
    }

    @Test
    fun withoutATargetThreeMoreFailuresAfterTheWaitWipeEverything() = runBlocking {
        var wiped = 0
        val vault = newVault(onWipe = { wiped++ }).also { it.createPin("1234") }
        vault.lock()
        repeat(5) { vault.unlock("0000") }
        now += 121_000
        assertEquals(UnlockResult.WrongPin(attemptsLeft = 2, willWipe = true), vault.unlock("0000"))
        assertEquals(UnlockResult.WrongPin(attemptsLeft = 1, willWipe = true), vault.unlock("0000"))
        assertEquals(0, wiped)
        assertEquals(UnlockResult.Wiped(everything = true), vault.unlock("0000"))
        assertEquals(1, wiped)
        assertEquals(VaultState.NO_PIN, vault.state.value)
        assertEquals(VaultState.NO_PIN, newVault().state.value)
    }

    @Test
    fun withATargetOnlyThatNoteIsWipedAndThePinStays() = runBlocking {
        val deleted = mutableListOf<String?>()
        val vault = newVault(onWipe = { deleted += it }).also { it.createPin("1234") }
        vault.lock()
        repeat(5) { vault.unlock("0000", "nota-1") }
        now += 121_000
        repeat(2) { vault.unlock("0000", "nota-1") }
        assertEquals(UnlockResult.Wiped(everything = false), vault.unlock("0000", "nota-1"))
        assertEquals(listOf<String?>("nota-1"), deleted)
        assertEquals(VaultState.LOCKED, vault.state.value)
        // Vuelve la espera de 2 minutos y los 3 intentos para la siguiente nota.
        assertTrue(vault.unlock("1234", "nota-2") is UnlockResult.LockedOut)
        assertEquals(3, vault.finalAttemptsLeft())
        now += 121_000
        assertEquals(UnlockResult.Success, vault.unlock("1234", "nota-2"))
    }

    @Test
    fun aNewPinCanBeCreatedAfterAWipe() = runBlocking {
        val vault = newVault().also { it.createPin("1234") }
        vault.lock()
        repeat(5) { vault.unlock("0000") }
        now += 121_000
        repeat(3) { vault.unlock("0000") }
        vault.createPin("5678")
        assertEquals(VaultState.UNLOCKED, vault.state.value)
        assertEquals(null, vault.finalAttemptsLeft())
    }

    @Test
    fun correctPinAfterTheWaitResetsTheCounters() = runBlocking {
        val vault = newVault().also { it.createPin("1234") }
        vault.lock()
        repeat(5) { vault.unlock("0000") }
        now += 121_000
        assertEquals(UnlockResult.Success, vault.unlock("1234"))
        vault.lock()
        assertEquals(UnlockResult.WrongPin(attemptsLeft = 4, willWipe = false), vault.unlock("0000"))
        assertEquals(null, vault.finalAttemptsLeft())
    }

    @Test
    fun finalAttemptsLeftAppearsOnlyAfterTheFifthFailure() = runBlocking {
        val vault = newVault().also { it.createPin("1234") }
        vault.lock()
        repeat(4) { vault.unlock("0000") }
        assertEquals(null, vault.finalAttemptsLeft())
        vault.unlock("0000")
        assertEquals(3, vault.finalAttemptsLeft())
        now += 121_000
        vault.unlock("0000")
        assertEquals(2, vault.finalAttemptsLeft())
    }

    @Test
    fun ifTheWipeFailsThePinIsNotRemoved() = runBlocking {
        val vault = newVault(onWipe = { error("falló el borrado") }).also { it.createPin("1234") }
        vault.lock()
        repeat(5) { vault.unlock("0000") }
        now += 121_000
        repeat(2) { vault.unlock("0000") }
        try {
            vault.unlock("0000")
            fail("Debió lanzar la excepción del borrado")
        } catch (_: IllegalStateException) {
            // Esperado.
        }
        assertEquals(VaultState.LOCKED, vault.state.value)
        assertEquals(UnlockResult.Success, vault.unlock("1234"))
    }

    @Test
    fun invalidPinsAreRejected() = runBlocking {
        for (pin in listOf("", "123", "123456789", "12a4", "١٢٣٤")) {
            try {
                newVault().createPin(pin)
                fail("Debió rechazar el PIN '$pin'")
            } catch (_: IllegalArgumentException) {
                // Esperado.
            }
        }
    }

    @Test
    fun tamperedContentIsDetected() = runBlocking {
        val vault = newVault().also { it.createPin("1234") }
        val blob = vault.encrypt("hola")
        blob[blob.size - 1] = (blob[blob.size - 1].toInt() xor 1).toByte()
        try {
            vault.decrypt(blob)
            fail("Debió detectar la alteración")
        } catch (_: GeneralSecurityException) {
            // Esperado.
        }
    }

    @Test
    fun biometricSealRoundTripOpensTheVault() = runBlocking {
        val vault = newVault().also { it.createPin("1234") }
        val blob = vault.encrypt("con huella")
        assertEquals(null, vault.biometricSeal())
        vault.enableBiometric { raw -> BiometricSeal(byteArrayOf(1, 2, 3), raw.reversedArray()) }
        vault.lock()
        vault.unlockWithBiometric { seal -> seal.blob.reversedArray() }
        assertEquals(VaultState.UNLOCKED, vault.state.value)
        assertEquals("con huella", vault.decrypt(blob))
    }

    @Test
    fun disablingBiometricRemovesTheSeal() = runBlocking {
        val vault = newVault().also { it.createPin("1234") }
        vault.enableBiometric { raw -> BiometricSeal(byteArrayOf(1), raw) }
        vault.disableBiometric()
        assertEquals(null, vault.biometricSeal())
    }

    @Test
    fun biometricUnlockClearsFailedAttempts() = runBlocking {
        val vault = newVault().also { it.createPin("1234") }
        vault.enableBiometric { raw -> BiometricSeal(byteArrayOf(1), raw) }
        vault.lock()
        repeat(5) { vault.unlock("0000") }
        vault.unlockWithBiometric { it.blob }
        assertEquals(null, vault.finalAttemptsLeft())
    }

    @Test
    fun changePinKeepsOldDataReadableAndSwitchesThePin() = runBlocking {
        val vault = newVault().also { it.createPin("1234") }
        val blob = vault.encrypt("dato")
        vault.lock()
        assertEquals(UnlockResult.Success, vault.changePin("1234", "9876"))
        assertEquals(VaultState.LOCKED, vault.state.value)
        assertEquals(UnlockResult.WrongPin(attemptsLeft = 4, willWipe = false), vault.unlock("1234"))
        assertEquals(UnlockResult.Success, vault.unlock("9876"))
        assertEquals("dato", vault.decrypt(blob))
    }

    @Test
    fun changePinWithWrongCurrentPinChangesNothing() = runBlocking {
        val vault = newVault().also { it.createPin("1234") }
        vault.lock()
        assertEquals(UnlockResult.WrongPin(attemptsLeft = 4, willWipe = false), vault.changePin("0000", "9876"))
        assertEquals(UnlockResult.Success, vault.unlock("1234"))
    }

    private companion object {
        const val TEST_ITERATIONS = 1_000
    }
}

private class FakeStorage : VaultStorage {
    private val data = mutableMapOf<String, String>()
    override fun read(key: String): String? = data[key]
    override fun write(values: Map<String, String>): Boolean {
        data += values
        return true
    }
    override fun remove(keys: Set<String>): Boolean {
        data.keys.removeAll(keys)
        return true
    }
    override fun clear(): Boolean {
        data.clear()
        return true
    }
}

private class IdentityWrapper : KeyWrapper {
    override fun wrap(data: ByteArray): ByteArray = data
    override fun unwrap(data: ByteArray): ByteArray = data
}
