package com.exapps.nooralhuda.feature.seerah.domain

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.StateFlow

/** One biography chapter: summary lede + reflection + lessons. */
@Immutable
data class SeerahChapter(
    val id: String,
    val title: String,
    val summary: String,
    val reflection: String,
    val lessons: List<String>
)

interface SeerahRepository {
    val chapters: StateFlow<List<SeerahChapter>>
    /** Seed Room from the static corpus on first run; reload afterwards. */
    suspend fun warm()
    suspend fun chapter(id: String): SeerahChapter?
}

/**
 * Static corpus (stories ported from the legacy client; reflections
 * authored for this release). Content data, not UI text.
 */
val STATIC_SEERAH_CHAPTERS = listOf(
    SeerahChapter(
        id = "muhammad",
        title = "سيرة النبي محمد ﷺ",
        summary = "من مكة إلى المدينة، سيرة الرحمة والثبات والدعوة بالحكمة حتى اكتمال الرسالة.",
        reflection = "في كل مرحلة من سيرته ﷺ درس في الصبر على الأذى والثبات على المبدأ: ثلاث عشرة سنة في مكة يدعو بالحكمة، ثم هجرة وبناء مجتمع يقوم على الأخوة والعدل.",
        lessons = listOf("الصدق والأمانة", "الرحمة بالناس", "الصبر في الدعوة")
    ),
    SeerahChapter(
        id = "ibrahim",
        title = "قصة إبراهيم عليه السلام",
        summary = "إمام التوحيد الذي جادل قومه بالحكمة وبنى البيت الحرام مع ابنه إسماعيل عليهما السلام.",
        reflection = "قصة إبراهيم عليه السلام مدرسة في اليقين: يترك أهله في واد غير ذي زرع ثقة بالله، ويرفع القواعد من البيت طاعة وتسليما.",
        lessons = listOf("التوحيد الخالص", "اليقين", "التسليم لأمر الله")
    ),
    SeerahChapter(
        id = "musa",
        title = "قصة موسى عليه السلام",
        summary = "رسول الله إلى فرعون، وقصة النجاة والعبور والصبر الطويل مع بني إسرائيل.",
        reflection = "في مواجهة الطغيان يقف موسى عليه السلام وحيدا إلا من أخيه، فيشق الله له البحر؛ تذكير بأن النصر مع الصبر وأن مع العسر يسرا.",
        lessons = listOf("نصرة الحق", "الثقة بالله", "الصبر في القيادة")
    ),
    SeerahChapter(
        id = "isa",
        title = "قصة عيسى عليه السلام",
        summary = "نبي كريم دعا إلى الرحمة والطهارة وصدق العبادة، ورفعه الله إليه.",
        reflection = "كلامه في المهد براءة لأمه، ومعجزاته رحمة بالناس؛ سيرته دعوة إلى طهارة القلب وصدق التوجه إلى الله وحده.",
        lessons = listOf("الرفق", "طهارة القلب", "الإخلاص")
    ),
    SeerahChapter(
        id = "yusuf",
        title = "قصة يوسف عليه السلام",
        summary = "من الجبّ إلى التمكين، قصة عفة وصبر وتفسير للأحلام ورحمة بالأهل.",
        reflection = "يبتلى بالجب والسجن والغربة فيحفظ الله دينه وأمانته، ثم يعفو عن إخوته قائلا: لا تثريب عليكم اليوم؛ قمة العفو عند المقدرة.",
        lessons = listOf("العفة", "العفو", "الأمل بعد الشدة")
    ),
    SeerahChapter(
        id = "yunus",
        title = "قصة يونس عليه السلام",
        summary = "قصة الرجوع والإنابة والدعاء العظيم في الظلمات.",
        reflection = "في بطن الحوت وفي ظلمات ثلاث ينادي: لا إله إلا أنت سبحانك إني كنت من الظالمين؛ فينجيه الله، وباب التوبة مفتوح لكل منيب.",
        lessons = listOf("التوبة", "الافتقار إلى الله", "أثر الدعاء")
    )
)
