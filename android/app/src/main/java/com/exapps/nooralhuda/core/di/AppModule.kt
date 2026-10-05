package com.exapps.nooralhuda.core.di

import android.content.Context
import androidx.room.Room
import com.exapps.nooralhuda.core.data.db.MIGRATION_1_2
import com.exapps.nooralhuda.core.data.db.NoorDatabase
import com.exapps.nooralhuda.core.datetime.HijriDateProvider
import com.exapps.nooralhuda.core.datetime.IslamicHijriDateProvider
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

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NoorDatabase =
        Room.databaseBuilder(context, NoorDatabase::class.java, "noor.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideBookmarkDao(db: NoorDatabase) = db.bookmarkDao()

    @Provides
    fun provideContentCacheDao(db: NoorDatabase) = db.contentCacheDao()

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    fun provideHijriDates(impl: IslamicHijriDateProvider): HijriDateProvider = impl
}
