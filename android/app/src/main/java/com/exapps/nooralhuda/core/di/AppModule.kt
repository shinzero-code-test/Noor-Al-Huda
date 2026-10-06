package com.exapps.nooralhuda.core.di

import android.content.Context
import androidx.room.Room
import com.exapps.nooralhuda.core.data.db.MIGRATION_1_2
import com.exapps.nooralhuda.core.data.db.MIGRATION_2_3
import com.exapps.nooralhuda.core.data.db.MIGRATION_3_4
import com.exapps.nooralhuda.core.data.db.MIGRATION_4_5
import com.exapps.nooralhuda.core.data.db.MIGRATION_5_6
import com.exapps.nooralhuda.core.data.db.MIGRATION_6_7
import com.exapps.nooralhuda.core.data.db.NoorDatabase
import com.exapps.nooralhuda.core.datetime.HijriDateProvider
import com.exapps.nooralhuda.core.datetime.HijriMonthProvider
import com.exapps.nooralhuda.core.datetime.IslamicHijriDateProvider
import com.exapps.nooralhuda.core.datetime.IslamicHijriMonthProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

/** OAuth web client ID for Google Sign-In (public client identifier, BuildConfig). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleWebClientId

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NoorDatabase =
        Room.databaseBuilder(context, NoorDatabase::class.java, "noor.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .build()

    @Provides
    fun provideBookmarkDao(db: NoorDatabase) = db.bookmarkDao()

    @Provides
    fun provideContentCacheDao(db: NoorDatabase) = db.contentCacheDao()

    @Provides
    fun provideSurahDao(db: NoorDatabase) = db.surahDao()

    @Provides
    fun provideVerseDao(db: NoorDatabase) = db.verseDao()

    @Provides
    fun provideReciterDownloadDao(db: NoorDatabase) = db.reciterDownloadDao()

    @Provides
    fun providePrayerDayDao(db: NoorDatabase) = db.prayerDayDao()

    @Provides
    fun provideWorshipLogDao(db: NoorDatabase) = db.worshipLogDao()

    @Provides
    fun provideAzkarDao(db: NoorDatabase) = db.azkarDao()

    @Provides
    fun provideHadithCollectionDao(db: NoorDatabase) = db.hadithCollectionDao()

    @Provides
    fun provideHadithItemDao(db: NoorDatabase) = db.hadithItemDao()

    @Provides
    fun provideHadithDetailDao(db: NoorDatabase) = db.hadithDetailDao()

    @Provides
    fun provideRadioStationDao(db: NoorDatabase) = db.radioStationDao()

    @Provides
    fun provideDuaEntryDao(db: NoorDatabase) = db.duaEntryDao()

    @Provides
    fun provideCalendarEventDao(db: NoorDatabase) = db.calendarEventDao()

    @Provides
    fun provideSeerahChapterDao(db: NoorDatabase) = db.seerahChapterDao()

    @Provides
    fun provideKnowledgeEntryDao(db: NoorDatabase) = db.knowledgeEntryDao()

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @GoogleWebClientId
    fun provideGoogleWebClientId(): String = com.exapps.nooralhuda.BuildConfig.GOOGLE_WEB_CLIENT_ID

    @Provides
    @Singleton
    fun provideHijriDates(impl: IslamicHijriDateProvider): HijriDateProvider = impl

    @Provides
    @Singleton
    fun provideHijriMonths(impl: IslamicHijriMonthProvider): HijriMonthProvider = impl
}
