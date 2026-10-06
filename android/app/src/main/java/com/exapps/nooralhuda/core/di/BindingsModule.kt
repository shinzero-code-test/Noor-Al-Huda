package com.exapps.nooralhuda.core.di

import com.exapps.nooralhuda.core.data.prefs.PendingEmailStore
import com.exapps.nooralhuda.core.data.prefs.PreferencesStore
import com.exapps.nooralhuda.core.media.AudioPlayer
import com.exapps.nooralhuda.core.media.AudioRepository
import com.exapps.nooralhuda.core.network.BackendApi
import com.exapps.nooralhuda.core.network.NoorBackendApi
import com.exapps.nooralhuda.feature.auth.data.FirebaseAuthRepository
import com.exapps.nooralhuda.feature.auth.data.GoogleSignIn
import com.exapps.nooralhuda.feature.auth.data.GoogleSignInManager
import com.exapps.nooralhuda.feature.auth.domain.AuthRepository
import com.exapps.nooralhuda.feature.bookmarks.data.BookmarkRepository
import com.exapps.nooralhuda.feature.bookmarks.data.BookmarkRepositoryImpl
import com.exapps.nooralhuda.feature.hadith.data.RoomHadithRepository
import com.exapps.nooralhuda.feature.hadith.domain.HadithRepository as HadithRepositoryApi
import com.exapps.nooralhuda.feature.radio.data.RoomRadioRepository
import com.exapps.nooralhuda.feature.radio.domain.RadioRepository as RadioRepositoryApi
import com.exapps.nooralhuda.feature.dua.data.RoomDuaRepository
import com.exapps.nooralhuda.feature.dua.domain.DuaRepository as DuaRepositoryApi
import com.exapps.nooralhuda.feature.calendar.data.RoomCalendarRepository
import com.exapps.nooralhuda.feature.calendar.domain.CalendarRepository as CalendarRepositoryApi
import com.exapps.nooralhuda.feature.daily.data.RoomDailyContentRepository
import com.exapps.nooralhuda.feature.daily.domain.DailyContentRepository as DailyContentRepositoryApi
import com.exapps.nooralhuda.feature.seerah.data.RoomSeerahRepository
import com.exapps.nooralhuda.feature.seerah.domain.SeerahRepository as SeerahRepositoryApi
import com.exapps.nooralhuda.feature.knowledge.data.RoomKnowledgeRepository
import com.exapps.nooralhuda.feature.knowledge.domain.KnowledgeRepository as KnowledgeRepositoryApi
import com.exapps.nooralhuda.feature.quran.data.AudioDownloadRepository
import com.exapps.nooralhuda.feature.quran.data.AudioDownloads
import com.exapps.nooralhuda.feature.quran.data.RoomQuranRepository
import com.exapps.nooralhuda.feature.quran.domain.QuranRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindGoogleSignIn(impl: GoogleSignInManager): GoogleSignIn

    @Binds
    @Singleton
    abstract fun bindPendingEmailStore(impl: PreferencesStore): PendingEmailStore

    @Binds
    @Singleton
    abstract fun bindBackendApi(impl: NoorBackendApi): BackendApi

    @Binds
    @Singleton
    abstract fun bindQuranRepository(impl: RoomQuranRepository): QuranRepository

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindHadithRepository(impl: RoomHadithRepository): HadithRepositoryApi

    @Binds
    @Singleton
    abstract fun bindRadioRepository(impl: RoomRadioRepository): RadioRepositoryApi

    @Binds
    @Singleton
    abstract fun bindDuaRepository(impl: RoomDuaRepository): DuaRepositoryApi

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(impl: RoomCalendarRepository): CalendarRepositoryApi

    @Binds
    @Singleton
    abstract fun bindDailyContentRepository(
        impl: RoomDailyContentRepository
    ): DailyContentRepositoryApi

    @Binds
    @Singleton
    abstract fun bindSeerahRepository(impl: RoomSeerahRepository): SeerahRepositoryApi

    @Binds
    @Singleton
    abstract fun bindKnowledgeRepository(impl: RoomKnowledgeRepository): KnowledgeRepositoryApi

    @Binds
    @Singleton
    abstract fun bindAudioPlayer(impl: AudioRepository): AudioPlayer

    @Binds
    @Singleton
    abstract fun bindAudioDownloads(impl: AudioDownloadRepository): AudioDownloads
}
