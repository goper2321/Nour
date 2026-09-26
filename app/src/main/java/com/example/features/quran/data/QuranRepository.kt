package com.example.features.quran.data

import com.example.features.quran.models.Ayah
import com.example.features.quran.models.MushafPage
import com.example.features.quran.models.RevelationType
import com.example.features.quran.models.Surah
import com.example.features.quran.models.TafsirEdition
import com.example.features.quran.models.TranslationEdition
import java.util.Locale

object QuranRepository {

    // Precompiled once: search normalizes all 6,236 verses per keystroke, so
    // compiling these patterns per ayah caused visible input lag on low-end
    // devices. Hoisting is behavior-preserving (same patterns, same order).
    private val HarakatRegex = Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")
    private val AlifRegex = Regex("[\\u0622\\u0623\\u0625\\u0671]")

    val TRANSLATION_EDITIONS = listOf(
        TranslationEdition("sahih_en", "Sahih International", "English", "Saheeh Int."),
        TranslationEdition("pickthall_en", "Pickthall", "English", "Marmaduke Pickthall"),
        TranslationEdition("hamidullah_fr", "Muhammad Hamidullah", "French", "Muhammad Hamidullah"),
        TranslationEdition("bernstrom_sv", "Knut Bernström", "Swedish", "Knut Bernström"),
        TranslationEdition("diyanet_tr", "Diyanet İşleri", "Turkish", "Diyanet"),
        TranslationEdition("jalandhry_ur", "Fateh Muhammad Jalandhry", "Urdu", "Jalandhry"),
        TranslationEdition("bubenheim_de", "Frank Bubenheim", "German", "Bubenheim & Elyas"),
        TranslationEdition("cortes_es", "Julio Cortés", "Spanish", "Julio Cortés"),
        TranslationEdition("indonesian_id", "Indonesian Ministry", "Indonesian", "Kemenag"),
        TranslationEdition("rahman_bn", "Mujibur Rahman", "Bengali", "Mujibur Rahman"),
        TranslationEdition("khan_hi", "Suhel Farooq Khan", "Hindi", "Farooq Khan & Nadwi"),
        TranslationEdition("kuliev_ru", "Elmir Kuliev", "Russian", "Elmir Kuliev"),
        TranslationEdition("jian_zh", "Ma Jian", "Chinese", "Ma Jian")
    )

    val TAFSIR_EDITIONS = listOf(
        TafsirEdition("muyassar", "Tafsir al-Muyassar (الميسر)", "Arabic", "King Fahd Complex"),
        TafsirEdition("kathir", "Tafsir Ibn Kathir (ابن كثير)", "Arabic / English", "Imam Ibn Kathir"),
        TafsirEdition("maarif", "Ma'arif-ul-Quran", "English / Urdu", "Mufti Muhammad Shafi")
    )

    // Complete index of all 114 Surahs
    val ALL_SURAHS: List<Surah> = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "The Opening", RevelationType.MECCAN, 7, 1, 1),
        Surah(2, "البقرة", "Al-Baqarah", "The Cow", RevelationType.MEDINAN, 286, 2, 1),
        Surah(3, "آل عمران", "Ali 'Imran", "Family of Imran", RevelationType.MEDINAN, 200, 50, 3),
        Surah(4, "النساء", "An-Nisa", "The Women", RevelationType.MEDINAN, 176, 77, 4),
        Surah(5, "المائدة", "Al-Ma'idah", "The Table Spread", RevelationType.MEDINAN, 120, 106, 6),
        Surah(6, "الأنعام", "Al-An'am", "The Cattle", RevelationType.MECCAN, 165, 128, 7),
        Surah(7, "الأعراف", "Al-A'raf", "The Heights", RevelationType.MECCAN, 206, 151, 8),
        Surah(8, "الأنفال", "Al-Anfal", "The Spoils of War", RevelationType.MEDINAN, 75, 177, 9),
        Surah(9, "التوبة", "At-Tawbah", "The Repentance", RevelationType.MEDINAN, 129, 187, 10),
        Surah(10, "يونس", "Yunus", "Jonah", RevelationType.MECCAN, 109, 208, 11),
        Surah(11, "هود", "Hud", "Hud", RevelationType.MECCAN, 123, 221, 11),
        Surah(12, "يوسف", "Yusuf", "Joseph", RevelationType.MECCAN, 111, 235, 12),
        Surah(13, "الرعد", "Ar-Ra'd", "The Thunder", RevelationType.MEDINAN, 43, 249, 13),
        Surah(14, "إبراهيم", "Ibrahim", "Abraham", RevelationType.MECCAN, 52, 255, 13),
        Surah(15, "الحجر", "Al-Hijr", "The Rocky Tract", RevelationType.MECCAN, 99, 262, 14),
        Surah(16, "النحل", "An-Nahl", "The Bee", RevelationType.MECCAN, 128, 267, 14),
        Surah(17, "الإسراء", "Al-Isra", "The Night Journey", RevelationType.MECCAN, 111, 282, 15),
        Surah(18, "الكهف", "Al-Kahf", "The Cave", RevelationType.MECCAN, 110, 293, 15),
        Surah(19, "مريم", "Maryam", "Mary", RevelationType.MECCAN, 98, 305, 16),
        Surah(20, "طه", "Ta-Ha", "Ta-Ha", RevelationType.MECCAN, 135, 312, 16),
        Surah(21, "الأنبياء", "Al-Anbiya", "The Prophets", RevelationType.MECCAN, 112, 322, 17),
        Surah(22, "الحج", "Al-Hajj", "The Pilgrimage", RevelationType.MEDINAN, 78, 332, 17),
        Surah(23, "المؤمنون", "Al-Mu'minun", "The Believers", RevelationType.MECCAN, 118, 342, 18),
        Surah(24, "النور", "An-Nur", "The Light", RevelationType.MEDINAN, 64, 350, 18),
        Surah(25, "الفرقان", "Al-Furqan", "The Criterion", RevelationType.MECCAN, 77, 359, 18),
        Surah(26, "الشعراء", "Ash-Shu'ara", "The Poets", RevelationType.MECCAN, 227, 367, 19),
        Surah(27, "النمل", "An-Naml", "The Ant", RevelationType.MECCAN, 93, 377, 19),
        Surah(28, "القصص", "Al-Qasas", "The Stories", RevelationType.MECCAN, 88, 385, 20),
        Surah(29, "العنكبوت", "Al-'Ankabut", "The Spider", RevelationType.MECCAN, 69, 396, 20),
        Surah(30, "الروم", "Ar-Rum", "The Romans", RevelationType.MECCAN, 60, 404, 21),
        Surah(31, "لقمان", "Luqman", "Luqman", RevelationType.MECCAN, 34, 411, 21),
        Surah(32, "السجدة", "As-Sajdah", "The Prostration", RevelationType.MECCAN, 30, 415, 21),
        Surah(33, "الأحزاب", "Al-Ahzab", "The Combined Forces", RevelationType.MEDINAN, 73, 418, 21),
        Surah(34, "سبأ", "Saba", "Sheba", RevelationType.MECCAN, 54, 428, 22),
        Surah(35, "فاطر", "Fatir", "Originator", RevelationType.MECCAN, 45, 434, 22),
        Surah(36, "يس", "Ya-Sin", "Ya-Sin", RevelationType.MECCAN, 83, 440, 22),
        Surah(37, "الصافات", "As-Saffat", "Those Who Set The Ranks", RevelationType.MECCAN, 182, 446, 23),
        Surah(38, "ص", "Sad", "The Letter Sad", RevelationType.MECCAN, 88, 453, 23),
        Surah(39, "الزمر", "Az-Zumar", "The Troops", RevelationType.MECCAN, 75, 458, 23),
        Surah(40, "غافر", "Ghafir", "The Forgiver", RevelationType.MECCAN, 85, 467, 24),
        Surah(41, "فصلت", "Fussilat", "Explained In Detail", RevelationType.MECCAN, 54, 477, 24),
        Surah(42, "الشورى", "Ash-Shura", "The Consultation", RevelationType.MECCAN, 53, 483, 25),
        Surah(43, "الزخرف", "Az-Zukhruf", "The Ornaments of Gold", RevelationType.MECCAN, 89, 489, 25),
        Surah(44, "الدخان", "Ad-Dukhan", "The Smoke", RevelationType.MECCAN, 59, 496, 25),
        Surah(45, "الجاثية", "Al-Jathiyah", "The Crouching", RevelationType.MECCAN, 37, 499, 25),
        Surah(46, "الأحقاف", "Al-Ahqaf", "The Wind-Curved Sandhills", RevelationType.MECCAN, 35, 502, 26),
        Surah(47, "محمد", "Muhammad", "Muhammad", RevelationType.MEDINAN, 38, 507, 26),
        Surah(48, "الفتح", "Al-Fath", "The Victory", RevelationType.MEDINAN, 29, 511, 26),
        Surah(49, "الحجرات", "Al-Hujurat", "The Rooms", RevelationType.MEDINAN, 18, 515, 26),
        Surah(50, "ق", "Qaf", "The Letter Qaf", RevelationType.MECCAN, 45, 518, 26),
        Surah(51, "الذاريات", "Adh-Dhariyat", "The Winnowing Winds", RevelationType.MECCAN, 60, 520, 26),
        Surah(52, "الطور", "At-Tur", "The Mount", RevelationType.MECCAN, 49, 523, 27),
        Surah(53, "النجم", "An-Najm", "The Star", RevelationType.MECCAN, 62, 526, 27),
        Surah(54, "القمر", "Al-Qamar", "The Moon", RevelationType.MECCAN, 55, 528, 27),
        Surah(55, "الرحمن", "Ar-Rahman", "The Beneficent", RevelationType.MEDINAN, 78, 531, 27),
        Surah(56, "الواقعة", "Al-Waqi'ah", "The Inevitable", RevelationType.MECCAN, 96, 534, 27),
        Surah(57, "الحديد", "Al-Hadid", "The Iron", RevelationType.MEDINAN, 29, 537, 27),
        Surah(58, "المجادلة", "Al-Mujadila", "The Pleading Woman", RevelationType.MEDINAN, 22, 542, 28),
        Surah(59, "الحشر", "Al-Hashr", "The Exile", RevelationType.MEDINAN, 24, 545, 28),
        Surah(60, "الممتحنة", "Al-Mumtahanah", "She That Is Examined", RevelationType.MEDINAN, 13, 549, 28),
        Surah(61, "الصف", "As-Saff", "The Ranks", RevelationType.MEDINAN, 14, 551, 28),
        Surah(62, "الجمعة", "Al-Jumu'ah", "The Congregation, Friday", RevelationType.MEDINAN, 11, 553, 28),
        Surah(63, "المنافقون", "Al-Munafiqun", "The Hypocrites", RevelationType.MEDINAN, 11, 554, 28),
        Surah(64, "التغابن", "At-Taghabun", "Mutual Disillusion", RevelationType.MEDINAN, 18, 556, 28),
        Surah(65, "الطلاق", "At-Talaq", "The Divorce", RevelationType.MEDINAN, 12, 558, 28),
        Surah(66, "التحريم", "At-Tahrim", "The Prohibition", RevelationType.MEDINAN, 12, 560, 28),
        Surah(67, "الملك", "Al-Mulk", "The Sovereignty", RevelationType.MECCAN, 30, 562, 29),
        Surah(68, "القلم", "Al-Qalam", "The Pen", RevelationType.MECCAN, 52, 564, 29),
        Surah(69, "الحاقة", "Al-Haqqah", "The Reality", RevelationType.MECCAN, 52, 566, 29),
        Surah(70, "المعارج", "Al-Ma'arij", "The Ascending Stairways", RevelationType.MECCAN, 44, 568, 29),
        Surah(71, "نوح", "Nuh", "Noah", RevelationType.MECCAN, 28, 570, 29),
        Surah(72, "الجن", "Al-Jinn", "The Jinn", RevelationType.MECCAN, 28, 572, 29),
        Surah(73, "المزمل", "Al-Muzzammil", "The Enshrouded One", RevelationType.MECCAN, 20, 574, 29),
        Surah(74, "المدثر", "Al-Muddaththir", "The Cloaked One", RevelationType.MECCAN, 56, 575, 29),
        Surah(75, "القيامة", "Al-Qiyamah", "The Resurrection", RevelationType.MECCAN, 40, 577, 29),
        Surah(76, "الإنسان", "Al-Insan", "The Human", RevelationType.MEDINAN, 31, 578, 29),
        Surah(77, "المرسلات", "Al-Mursalat", "The Emissaries", RevelationType.MECCAN, 50, 580, 29),
        Surah(78, "النبأ", "An-Naba", "The Tidings", RevelationType.MECCAN, 40, 582, 30),
        Surah(79, "النازعات", "An-Nazi'at", "Those Who Drag Forth", RevelationType.MECCAN, 46, 583, 30),
        Surah(80, "عبس", "'Abasa", "He Frowned", RevelationType.MECCAN, 42, 585, 30),
        Surah(81, "التكوير", "At-Takwir", "The Overthrowing", RevelationType.MECCAN, 29, 586, 30),
        Surah(82, "الانفطار", "Al-Infitar", "The Cleaving", RevelationType.MECCAN, 19, 587, 30),
        Surah(83, "المطففين", "Al-Mutaffifin", "Defrauding", RevelationType.MECCAN, 36, 587, 30),
        Surah(84, "الانشقاق", "Al-Inshiqaq", "The Splitting Open", RevelationType.MECCAN, 25, 589, 30),
        Surah(85, "البروج", "Al-Buruj", "The Mansions of the Stars", RevelationType.MECCAN, 22, 590, 30),
        Surah(86, "الطارق", "At-Tariq", "The Morning Star", RevelationType.MECCAN, 17, 591, 30),
        Surah(87, "الأعلى", "Al-A'la", "The Most High", RevelationType.MECCAN, 19, 591, 30),
        Surah(88, "الغاشية", "Al-Ghashiyah", "The Overwhelming", RevelationType.MECCAN, 26, 592, 30),
        Surah(89, "الفجر", "Al-Fajr", "The Dawn", RevelationType.MECCAN, 30, 593, 30),
        Surah(90, "البلد", "Al-Balad", "The City", RevelationType.MECCAN, 20, 594, 30),
        Surah(91, "الشمس", "Ash-Shams", "The Sun", RevelationType.MECCAN, 15, 595, 30),
        Surah(92, "الليل", "Al-Layl", "The Night", RevelationType.MECCAN, 21, 595, 30),
        Surah(93, "الضحى", "Ad-Duha", "The Morning Hours", RevelationType.MECCAN, 11, 596, 30),
        Surah(94, "الشرح", "Ash-Sharh", "The Relief", RevelationType.MECCAN, 8, 596, 30),
        Surah(95, "التين", "At-Tin", "The Fig", RevelationType.MECCAN, 8, 597, 30),
        Surah(96, "العلق", "Al-'Alaq", "The Clot", RevelationType.MECCAN, 19, 597, 30),
        Surah(97, "القدر", "Al-Qadr", "The Power", RevelationType.MECCAN, 5, 598, 30),
        Surah(98, "البينة", "Al-Bayyinah", "The Clear Proof", RevelationType.MEDINAN, 8, 598, 30),
        Surah(99, "الزلزلة", "Az-Zalzalah", "The Earthquake", RevelationType.MEDINAN, 8, 599, 30),
        Surah(100, "العاديات", "Al-'Adiyat", "The Courser", RevelationType.MECCAN, 11, 599, 30),
        Surah(101, "القارعة", "Al-Qari'ah", "The Calamity", RevelationType.MECCAN, 11, 600, 30),
        Surah(102, "التكاثر", "At-Takathur", "The Rivalry In World Increase", RevelationType.MECCAN, 8, 600, 30),
        Surah(103, "العصر", "Al-'Asr", "The Declining Day", RevelationType.MECCAN, 3, 601, 30),
        Surah(104, "الهمزة", "Al-Humazah", "The Traducer", RevelationType.MECCAN, 9, 601, 30),
        Surah(105, "الفيل", "Al-Fil", "The Elephant", RevelationType.MECCAN, 5, 601, 30),
        Surah(106, "قريش", "Quraysh", "Quraysh", RevelationType.MECCAN, 4, 602, 30),
        Surah(107, "الماعون", "Al-Ma'un", "The Small Kindness", RevelationType.MECCAN, 7, 602, 30),
        Surah(108, "الكوثر", "Al-Kawthar", "The Abundance", RevelationType.MECCAN, 3, 602, 30),
        Surah(109, "الكافرون", "Al-Kafirun", "The Disbelievers", RevelationType.MECCAN, 6, 603, 30),
        Surah(110, "النصر", "An-Nasr", "The Divine Support", RevelationType.MEDINAN, 3, 603, 30),
        Surah(111, "المسد", "Al-Masad", "The Palm Fiber", RevelationType.MECCAN, 5, 603, 30),
        Surah(112, "الإخلاص", "Al-Ikhlas", "The Sincerity", RevelationType.MECCAN, 4, 604, 30),
        Surah(113, "الفلق", "Al-Falaq", "The Daybreak", RevelationType.MECCAN, 5, 604, 30),
        Surah(114, "الناس", "An-Nas", "Mankind", RevelationType.MECCAN, 6, 604, 30)
    )

    fun getSurah(number: Int): Surah {
        return ALL_SURAHS.firstOrNull { it.number == number } ?: ALL_SURAHS.first()
    }

    /**
     * Converts an integer to Eastern Arabic-Indic numerals (٠, ١, ٢, ٣, ٤, ٥, ٦, ٧, ٨, ٩)
     * for authentic Uthmani Quranic ayah end markers.
     */
    fun toArabicDigits(number: Int): String {
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return number.toString().map { arabicDigits[it - '0'] }.joinToString("")
    }

    /**
     * Retrieves all Ayahs for a given Surah from the verified corpus.
     */
    fun getAyahsForSurah(surahNumber: Int): List<Ayah> {
        return QuranCorpus.ayahs.filter { it.surahNumber == surahNumber }
    }

    /**
     * Resolves a single verse reference from the verified corpus, or null when
     * the reference does not exist. Used to display bookmarks from live text.
     */
    fun resolveAyah(surahNumber: Int, ayahNumber: Int): Ayah? {
        val surah = getSurah(surahNumber)
        if (surah.number != surahNumber) return null
        if (ayahNumber < 1 || ayahNumber > surah.ayahCount) return null
        return QuranCorpus.ayahs.firstOrNull {
            it.surahNumber == surahNumber && it.ayahNumber == ayahNumber
        }
    }

    /**
     * Resolves the Surah and Ayahs belonging to a specific Madinah Mushaf page (1 to 604).
     */
    fun getMushafPage(pageNumber: Int): MushafPage {
        val page = pageNumber.coerceIn(1, 604)
        val ayahs = QuranCorpus.ayahs.filter { it.pageNumber == page }
        val first = ayahs.firstOrNull()
        val surahHeader = ALL_SURAHS.lastOrNull { it.number == first?.surahNumber }
        return MushafPage(
            pageNumber = page,
            juzNumber = first?.juzNumber ?: 1,
            hizbQuarter = if (first != null) QuranCorpus.quarterFor(first.surahNumber, first.ayahNumber) else 1,
            surahHeader = if (first?.ayahNumber == 1) surahHeader else null,
            ayahs = ayahs
        )
    }

    /**
     * Normalizes Arabic text by removing tashkeel (diacritics), tatweel, and normalizing alif/hamza
     * for seamless full-text offline search.
     */
    fun normalizeArabic(text: String): String {
        return text
            // Remove Harakat (Tashkeel: Fathah, Dammah, Kasrah, Sukun, Shaddah, Tanween, etc.)
            .replace(HarakatRegex, "")
            // Remove Tatweel / Kashida
            .replace("\u0640", "")
            // Normalize Alif forms (أ, إ, آ, ٱ -> ا)
            .replace(AlifRegex, "\u0627")
            // Normalize Taa Marbuta (ة -> ه)
            .replace("\u0629", "\u0647")
            // Normalize Yaa (ى -> ي)
            .replace("\u0649", "\u064A")
            .trim()
    }

    /**
     * Offline search across Surahs and Arabic text of the verified corpus.
     */
    fun search(query: String): List<Ayah> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        val normalizedQuery = normalizeArabic(trimmed).lowercase(Locale.ROOT)

        return QuranCorpus.ayahs.filter { ayah ->
            val matchesArabic = normalizeArabic(ayah.arabicText).lowercase(Locale.ROOT).contains(normalizedQuery)
            val matchesSurah = getSurah(ayah.surahNumber).nameEnglish.contains(trimmed, ignoreCase = true) ||
                               getSurah(ayah.surahNumber).nameArabic.contains(trimmed)
            matchesArabic || matchesSurah
        }
    }
}
