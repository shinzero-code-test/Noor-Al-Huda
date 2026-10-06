package com.exapps.nooralhuda.feature.knowledge.domain

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.StateFlow

enum class KnowledgeKind { NAME, RUQYAH, FAQ, EBOOK, STREAM }

/** One hub entry across all knowledge sections. */
@Immutable
data class KnowledgeEntry(
    val id: String,
    val kind: KnowledgeKind,
    val title: String,
    val subtitle: String,
    val body: String,
    /** Stream/book URL where applicable, else null. */
    val url: String? = null
)

interface KnowledgeRepository {
    val entries: StateFlow<List<KnowledgeEntry>>
    /** Seed Room from the static corpus on first run; reload afterwards. */
    suspend fun warm()

    /** Deterministic name of the day (rotates through the NAME entries). */
    fun nameOfTheDay(): KnowledgeEntry? {
        val names = entries.value.filter { it.kind == KnowledgeKind.NAME }
        if (names.isEmpty()) return null
        val day = java.time.temporal.ChronoUnit.DAYS.between(
            java.time.LocalDate.of(2026, 1, 1), java.time.LocalDate.now()
        )
        val index = (day % names.size).toInt().let { if (it < 0) it + names.size else it }
        return names[index]
    }
}

/**
 * Static corpus (ported from the legacy client). Content data,
 * not UI text — hence plain data, not string resources.
 */
val STATIC_KNOWLEDGE = listOf(
    KnowledgeEntry("allah", KnowledgeKind.NAME, "الله", "Allah", "المألوه بحق، الجامع لصفات الكمال. يملأ القلب تعظيماً ومحبة."),
    KnowledgeEntry("ar-rahman", KnowledgeKind.NAME, "الرحمن", "Ar-Rahman", "واسع الرحمة في الدنيا والآخرة. يبعث في النفس الرجاء."),
    KnowledgeEntry("ar-raheem", KnowledgeKind.NAME, "الرحيم", "Ar-Rahim", "الذي يخص عباده المؤمنين بمزيد الرحمة. يربط القلب بحسن الظن بالله."),
    KnowledgeEntry("al-malik", KnowledgeKind.NAME, "الملك", "Al-Malik", "المالك لكل شيء والمتصرف فيه. يغرس اليقين بأن الأمر كله لله."),
    KnowledgeEntry("as-salam", KnowledgeKind.NAME, "السلام", "As-Salam", "السالم من كل نقص والمانح للأمان. يبعث الطمأنينة والسكينة."),
    KnowledgeEntry("al-wadud", KnowledgeKind.NAME, "الودود", "Al-Wadud", "المحب لعباده الصالحين. يزرع الأنس بالقرب من الله."),
    KnowledgeEntry(
        "fatiha", KnowledgeKind.RUQYAH, "سورة الفاتحة", "Al-Fatihah",
        "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ...",
        url = "https://server8.mp3quran.net/afs/001.mp3"
    ),
    KnowledgeEntry(
        "baqarah255", KnowledgeKind.RUQYAH, "آية الكرسي", "Ayat al-Kursi",
        "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ...",
        url = "https://server8.mp3quran.net/afs/002.mp3"
    ),
    KnowledgeEntry(
        "muawwidhat", KnowledgeKind.RUQYAH, "المعوذات", "Al-Mu‘awwidhat",
        "قُلْ هُوَ اللَّهُ أَحَدٌ • قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ • قُلْ أَعُوذُ بِرَبِّ النَّاسِ",
        url = "https://server8.mp3quran.net/afs/112.mp3"
    ),
    KnowledgeEntry("wudu", KnowledgeKind.FAQ, "متى يبطل الوضوء؟", "فقه العبادات", "يبطل بخروج شيء من السبيلين، والنوم المستغرق، وزوال العقل ونحو ذلك."),
    KnowledgeEntry("salah-qada", KnowledgeKind.FAQ, "كيف أقضي الصلاة الفائتة؟", "الصلوات", "تبادر إلى قضائها عند التذكر مع الترتيب بحسب الاستطاعة."),
    KnowledgeEntry("zakat-money", KnowledgeKind.FAQ, "متى تجب زكاة المال؟", "الزكاة", "إذا بلغ المال النصاب وحال عليه الحول القمري."),
    KnowledgeEntry("transactions", KnowledgeKind.FAQ, "ما ضابط البيع الصحيح؟", "المعاملات", "أن يكون مباحاً، معلوماً، برضا الطرفين، خالياً من الغرر والربا."),
    KnowledgeEntry("riyad", KnowledgeKind.EBOOK, "رياض الصالحين", "نص", "مختارات من الأحاديث في تزكية النفس والأخلاق."),
    KnowledgeEntry("fortress", KnowledgeKind.EBOOK, "حصن المسلم", "نص", "أذكار ودعوات مأثورة مرتبة على الأبواب."),
    KnowledgeEntry("aqidah", KnowledgeKind.EBOOK, "متن العقيدة", "PDF", "مواد مختصرة في العقيدة وشرح أركان الإيمان."),
    KnowledgeEntry("quran-tv", KnowledgeKind.STREAM, "قناة القرآن الكريم", "بث مرئي", "تلاوات وبث مباشر.", url = "https://www.youtube.com/watch?v=9Auq9mYxFEE"),
    KnowledgeEntry("sunnah-tv", KnowledgeKind.STREAM, "قناة السنة النبوية", "بث مرئي", "دروس وتلاوات.", url = "https://www.youtube.com/watch?v=kwihSx9pN6I")
)
