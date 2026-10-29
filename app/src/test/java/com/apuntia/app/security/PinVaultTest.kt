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

    private fun newVault() = PinVault(storage, IdentityWrapper(), clock = { now }, iterations = TEST_ITERATIONS)

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
        assertEquals(UnlockResult.WrongPin(attemptsLeft = 4), vault.unlock("0000"))
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
        now += 31_000
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
    fun secondRoundOfFailuresWaitsLonger() = runBlocking {
        val vault = newVault().also { it.createPin("1234") }
        vault.lock()
        repeat(5) { vault.unlock("0000") }
        now += 31_000
        repeat(4) { vault.unlock("0000") }
        val result = vault.unlock("0000") as UnlockResult.LockedOut
        assertEquals(60_000L, result.remainingMillis)
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
}

private class IdentityWrapper : KeyWrapper {
    override fun wrap(data: ByteArray): ByteArray = data
    override fun unwrap(data: ByteArray): ByteArray = data
}
