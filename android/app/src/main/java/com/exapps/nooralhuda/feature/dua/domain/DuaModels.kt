package com.exapps.nooralhuda.feature.dua.domain

import androidx.compose.runtime.Immutable

/** One supplication in the offline catalog. */
@Immutable
data class DuaEntry(
    val id: String,
    val category: DuaCategory,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val repeat: Int,
    val source: String,
    val favourite: Boolean = false
)

enum class DuaCategory(val titleEn: String, val titleAr: String) {
    MORNING("Morning", "الصباح"),
    EVENING("Evening", "المساء"),
    PRAYER("Prayer", "الصلاة"),
    FOOD("Food", "الطعام"),
    TRAVEL("Travel", "السفر"),
    SICKNESS("Sickness", "المرض")
}

/**
 * Curated offline catalog (famous, stable texts with sources). The AI
 * generator lands with provider keys; until then this catalog plus the
 * locked teaser card is the whole feature. Content data, not UI text.
 */
val STATIC_DUA_CATALOG = listOf(
    DuaEntry(
        id = "morning-protection",
        category = DuaCategory.MORNING,
        arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
        transliteration = "Bismillahilladhi la yadurru ma‘asmihi shay’un fil-ardi wa la fis-sama’i wa huwas-Sami‘ul-‘Alim",
        translation = "In the name of Allah, with whose name nothing on earth or in heaven can cause harm, and He is the All-Hearing, the All-Knowing.",
        repeat = 3,
        source = "Sunan Abi Dawud"
    ),
    DuaEntry(
        id = "morning-asbahna",
        category = DuaCategory.MORNING,
        arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
        transliteration = "Asbahna wa asbahal-mulku lillah, wal-hamdu lillah, la ilaha illallah wahdahu la sharika lah",
        translation = "We have reached the morning, and dominion belongs to Allah. Praise is due to Allah. None has the right to be worshipped but Allah alone, without partner.",
        repeat = 1,
        source = "Sahih Muslim"
    ),
    DuaEntry(
        id = "morning-ridha",
        category = DuaCategory.MORNING,
        arabic = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
        transliteration = "Raditu billahi rabban, wa bil-islami dinan, wa bi Muhammadin sallallahu ‘alayhi wa sallama nabiyya",
        translation = "I am pleased with Allah as Lord, with Islam as religion, and with Muhammad (peace be upon him) as Prophet.",
        repeat = 3,
        source = "Sunan Abi Dawud"
    ),
    DuaEntry(
        id = "evening-amsayna",
        category = DuaCategory.EVENING,
        arabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
        transliteration = "Amsayna wa amsal-mulku lillah, wal-hamdu lillah, la ilaha illallah wahdahu la sharika lah",
        translation = "We have reached the evening, and dominion belongs to Allah. Praise is due to Allah. None has the right to be worshipped but Allah alone, without partner.",
        repeat = 1,
        source = "Sahih Muslim"
    ),
    DuaEntry(
        id = "evening-kalimat",
        category = DuaCategory.EVENING,
        arabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
        transliteration = "A‘udhu bi kalimatillahit-tammati min sharri ma khalaq",
        translation = "I seek refuge in the perfect words of Allah from the evil of what He has created.",
        repeat = 3,
        source = "Sahih Muslim"
    ),
    DuaEntry(
        id = "istighfar",
        category = DuaCategory.EVENING,
        arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ",
        transliteration = "Allahumma anta rabbi la ilaha illa ant, khalaqtani wa ana ‘abduk, wa ana ‘ala ‘ahdika wa wa‘dika mastata‘t",
        translation = "O Allah, You are my Lord, none has the right to be worshipped but You. You created me and I am Your servant, and I honour my covenant with You as best I can.",
        repeat = 1,
        source = "Sahih al-Bukhari"
    ),
    DuaEntry(
        id = "prayer-ainni",
        category = DuaCategory.PRAYER,
        arabic = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ",
        transliteration = "Allahumma a‘inni ‘ala dhikrika wa shukrika wa husni ‘ibadatik",
        translation = "O Allah, help me to remember You, to thank You, and to worship You in the best manner.",
        repeat = 1,
        source = "Sunan Abi Dawud"
    ),
    DuaEntry(
        id = "prayer-la-mani",
        category = DuaCategory.PRAYER,
        arabic = "اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ، وَلَا مُعْطِيَ لِمَا مَنَعْتَ، وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ",
        transliteration = "Allahumma la mani‘a lima a‘tayt, wa la mu‘tiya lima mana‘t, wa la yanfa‘u dhal-jaddi minkal-jadd",
        translation = "O Allah, none can withhold what You give, and none can give what You withhold, and the fortune of the fortunate avails him nothing against You.",
        repeat = 1,
        source = "Sahih al-Bukhari, Sahih Muslim"
    ),
    DuaEntry(
        id = "food-before",
        category = DuaCategory.FOOD,
        arabic = "بِسْمِ اللَّهِ",
        transliteration = "Bismillah",
        translation = "In the name of Allah.",
        repeat = 1,
        source = "Sunan Abi Dawud"
    ),
    DuaEntry(
        id = "food-after",
        category = DuaCategory.FOOD,
        arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
        transliteration = "Alhamdu lillahilladhi at‘amani hadha wa razaqanihi min ghayri hawlin minni wa la quwwah",
        translation = "Praise is due to Allah who fed me this and provided it for me, without any power or might from me.",
        repeat = 1,
        source = "Jami‘ at-Tirmidhi"
    ),
    DuaEntry(
        id = "travel",
        category = DuaCategory.TRAVEL,
        arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
        transliteration = "Subhanalladhi sakhkhara lana hadha wa ma kunna lahu muqrinin, wa inna ila rabbina lamunqalibun",
        translation = "Glory unto Him who has subjected this to us, and we were not capable of it. And indeed, to our Lord we shall return.",
        repeat = 1,
        source = "Quran 43:13-14"
    ),
    DuaEntry(
        id = "healing-seven",
        category = DuaCategory.SICKNESS,
        arabic = "أَسْأَلُ اللَّهَ الْعَظِيمَ رَبَّ الْعَرْشِ الْعَظِيمِ أَنْ يَشْفِيَكَ",
        transliteration = "As’alullahal-‘azim rabbal-‘arshil-‘azim an yashfiyak",
        translation = "I ask Allah the Almighty, Lord of the Magnificent Throne, to cure you.",
        repeat = 7,
        source = "Sunan Abi Dawud, Jami‘ at-Tirmidhi"
    ),
    DuaEntry(
        id = "healing-rabb",
        category = DuaCategory.SICKNESS,
        arabic = "اللَّهُمَّ رَبَّ النَّاسِ أَذْهِبِ الْبَأْسَ، اشْفِ أَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ، شِفَاءً لَا يُغَادِرُ سَقَمًا",
        transliteration = "Allahumma rabban-nas adh’hibil-ba’s, ishfi antash-shafi, la shifa’a illa shifa’uk, shifa’an la yughadiru saqama",
        translation = "O Allah, Lord of mankind, remove the hardship. Cure, for You are the Healer; there is no cure but Your cure, a cure that leaves no illness.",
        repeat = 1,
        source = "Sahih al-Bukhari, Sahih Muslim"
    )
)
