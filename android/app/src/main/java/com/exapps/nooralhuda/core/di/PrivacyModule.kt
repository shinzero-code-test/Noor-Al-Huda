package com.exapps.nooralhuda.core.di

import com.exapps.nooralhuda.core.data.prefs.PreferencesStore
import com.exapps.nooralhuda.core.privacy.PrivacyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PrivacyModule {

    @Provides
    @Singleton
    fun providePrivacyManager(store: PreferencesStore): PrivacyManager =
        PrivacyManager(store.privacyMode)
}
