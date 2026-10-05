package com.exapps.nooralhuda.core.di

import com.exapps.nooralhuda.core.data.prefs.PendingEmailStore
import com.exapps.nooralhuda.core.data.prefs.PreferencesStore
import com.exapps.nooralhuda.feature.auth.data.FirebaseAuthRepository
import com.exapps.nooralhuda.feature.auth.domain.AuthRepository
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
    abstract fun bindPendingEmailStore(impl: PreferencesStore): PendingEmailStore
}
