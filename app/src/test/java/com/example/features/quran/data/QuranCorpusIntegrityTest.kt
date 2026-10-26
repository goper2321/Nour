package com.example.features.quran.data

import com.example.localization.AppStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * NUR-034: exercises the bundled-Quran corpus corruption failure path with
 * synthetic in-memory fixtures only — no file under app/src/main/resources/
 * quran/ is ever modified. The verifier seam (QuranCorpus.integrityErrorFor)
 * is exactly what production's resource loader consults before any Quran data
 * is built, so a failure here must surface as an error state, never invented
 * content and never a crash loop.
 */
class QuranCorpusIntegrityTest {

    private fun sha256(bytes: ByteArray): String =
        java.security.MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it.toInt() and 255) }

    private val fixture = "1|1|corpus fixture line\n1|2|corpus fixture line\n".toByteArray(Charsets.UTF_8)

    @Test
    fun matchingBytesVerifyClean() {
        assertNull(QuranCorpus.integrityErrorFor(fixture, "fixture.txt", sha256(fixture)))
    }

    @Test
    fun sameSizeDifferentBytesReturnTheErrorState() {
        val corrupted = fixture.clone()
        corrupted[3] = (corrupted[3].toInt() xor 0x01).toByte() // same length, one flipped byte

        val error = QuranCorpus.integrityErrorFor(corrupted, "fixture.txt", sha256(fixture))

        assertEquals("Quran resource integrity check failed: fixture.txt", error)
    }

    @Test
    fun integrityMessageIsUserMeaningfulNotBareNull() {
        val corrupted = ByteArray(fixture.size) { (it % 97).toByte() } // same shape, wrong content
        val error = QuranCorpus.integrityErrorFor(corrupted, "fixture.txt", "deadbeef")

        assertTrue(error != null)
        assertTrue(error!!.contains("integrity"))
        assertTrue(error.contains("fixture.txt"))
    }

    @Test
    fun bundledUthmaniTextStillVerifiesAgainstTheCommittedHash() {
        // The test classpath exposes the bundled resources; this proves the
        // refactored verifier's contract on the real artifact byte-for-byte.
        val bytes = javaClass.getResourceAsStream("/quran/uthmani.txt")!!.use { it.readBytes() }
        assertNull(QuranCorpus.integrityErrorFor(bytes, "uthmani.txt", QuranCorpus.TEXT_HASH))
    }

    @Test
    fun failureSurfacesUserStringsInsteadOfInventedContent() {
        // These are the exact strings the Quran library renders while the
        // corpus error is active (QuranScreen "quran_corpus_error" empty
        // state): an explicit honest message, not fabricated verses or tafsir.
        val strings = AppStrings()
        assertTrue(strings.corpusErrorTitle.isNotBlank())
        val message = strings.corpusErrorMessage
        assertTrue(message.isNotBlank())
        assertTrue(message.contains("integrity"))
        assertTrue(message.contains("reinstall"))
    }
}
