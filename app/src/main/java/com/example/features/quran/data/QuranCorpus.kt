package com.example.features.quran.data

import com.example.features.quran.models.Ayah
import java.security.MessageDigest
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

/** Verbatim Tanzil Uthmani 1.1 text. See bundled quran/SOURCES.md and source notices. */
internal object QuranCorpus {
    const val TEXT_HASH = "6933e133dd56db778c801bf738848454e43648105a151e8d84d86a7cae39ec5f"
    const val METADATA_HASH = "8867c1d88191472adec9db694b3cd9f135b1a2ef580574d32cf888dcb22c5c7a"
    const val EN_SAHIH_HASH = "f090ed258e647a393ddaa0b48f1e81ab7a5ccd5ad7d72d0da498e151bc0dbc18"

    /** Edition id used for the bundled Saheeh International English translation. */
    const val ENGLISH_EDITION_ID = "sahih_en"

    private fun resource(name: String, hash: String): ByteArray {
        val bytes = requireNotNull(javaClass.getResourceAsStream("/quran/$name")) {
            "Missing Quran resource: $name"
        }.use { it.readBytes() }
        val message = integrityErrorFor(bytes, name, hash)
        require(message == null) { message!! }
        return bytes
    }

    /**
     * Pure SHA-256 integrity check over injected bytes. Returns null when the
     * bytes match `hash`, otherwise the fixed error message that names the
     * resource. Kept beside `resource` so the corruption path can be unit
     * tested with synthetic fixtures (same size, flipped byte) without ever
     * touching the bundled files (NUR-034).
     */
    internal fun integrityErrorFor(bytes: ByteArray, name: String, hash: String): String? {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it.toInt() and 255) }
        return if (digest == hash) null else "Quran resource integrity check failed: $name"
    }

    /**
     * NUR-033 / NUR-035: computes the real SHA-256 of the bundled
     * Uthmani text file at runtime — the hash the user sees on the Settings
     * source documentation / diagnostics cards is the *actual* digest of the
     * bundled bytes, never a re-typed copy of the constant. Returns null when
     * the file cannot be read or hashed (the card then reports the corpus as
     * not verifiable instead of inventing a value).
     */
    internal fun actualTextHash(): String? = runCatching {
        val bytes = javaClass.getResourceAsStream("/quran/uthmani.txt")
            ?.use { it.readBytes() }
            ?: return null
        MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it.toInt() and 255) }
    }.getOrNull()

    private data class Partition(val index: Int, val reference: Int)
    private fun reference(surah: Int, ayah: Int) = surah * 1000 + ayah

    private val metadata by lazy {
        DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(resource("metadata.xml", METADATA_HASH).inputStream())
    }

    private fun partitions(tag: String): List<Partition> {
        val nodes = metadata.getElementsByTagName(tag)
        return (0 until nodes.length).map { index ->
            val node = nodes.item(index) as Element
            Partition(node.getAttribute("index").toInt(),
                reference(node.getAttribute("sura").toInt(), node.getAttribute("aya").toInt()))
        }
    }

    private val pages by lazy { partitions("page").also { require(it.size == 604) } }
    private val juzs by lazy { partitions("juz").also { require(it.size == 30) } }
    private val quarters by lazy { partitions("quarter").also { require(it.size == 240) } }
    fun quarterFor(surah: Int, ayah: Int): Int =
        quarters.last { it.reference <= reference(surah, ayah) }.index

    private val englishByRef: Map<Int, String> by lazy {
        val text = resource("en_sahihintl.txt", EN_SAHIH_HASH).toString(Charsets.UTF_8)
        val map = HashMap<Int, String>(7000)
        text.lineSequence().forEach { line ->
            if (line.isBlank()) return@forEach
            val fields = line.split('|', limit = 3)
            require(fields.size == 3 && fields[2].isNotBlank()) { "Bad translation line: $line" }
            map[reference(fields[0].toInt(), fields[1].toInt())] = fields[2]
        }
        require(map.size == 6236) { "Expected 6236 translated verses, got ${map.size}" }
        map
    }

    val ayahs: List<Ayah> by lazy {
        // Force translation load first so a corrupt translation fails fast with a clear error.
        val translations = englishByRef
        // Tolerate a leading byte-order mark in the corpus. Written as an escape so the
        // character stays visible in this source file; a raw U+FEFF literal here is
        // invisible in most editors and trips lint's ByteOrderMark check.
        val text = resource("uthmani.txt", TEXT_HASH).toString(Charsets.UTF_8).removePrefix("\uFEFF")
        val verses = text.lineSequence().filter { it.firstOrNull()?.isDigit() == true }.map { line ->
            val fields = line.split('|', limit = 3)
            require(fields.size == 3 && fields[2].isNotBlank())
            val surah = fields[0].toInt()
            val ayah = fields[1].toInt()
            val ref = reference(surah, ayah)
            Ayah(surah, ayah, fields[2], pages.last { it.reference <= ref }.index,
                juzs.last { it.reference <= ref }.index,
                translations = translations[ref]?.let { mapOf(ENGLISH_EDITION_ID to it) } ?: emptyMap())
        }.toList()
        require(verses.size == 6236)
        val suras = metadata.getElementsByTagName("sura")
        require(suras.length == 114)
        val expected = (0 until suras.length).flatMap { i ->
            val node = suras.item(i) as Element
            val surah = node.getAttribute("index").toInt()
            (1..node.getAttribute("ayas").toInt()).map { reference(surah, it) }
        }
        require(verses.map { reference(it.surahNumber, it.ayahNumber) } == expected)
        verses
    }
}
