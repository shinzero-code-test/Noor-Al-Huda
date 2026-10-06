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
    abstract fun bindAudioPlayer(impl: AudioRepository): AudioPlayer

    @Binds
    @Singleton
    abstract fun bindAudioDownloads(impl: AudioDownloadRepository): AudioDownloads
}
