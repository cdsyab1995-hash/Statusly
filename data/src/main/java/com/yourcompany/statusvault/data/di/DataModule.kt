package com.yourcompany.statusvault.data.di

import android.content.Context
import androidx.room.Room
import com.yourcompany.statusvault.data.local.db.AppDatabase
import com.yourcompany.statusvault.data.repository.DirectoryRepositoryImpl
import com.yourcompany.statusvault.data.repository.StatusRepositoryImpl
import com.yourcompany.statusvault.domain.repository.DirectoryRepository
import com.yourcompany.statusvault.domain.repository.StatusRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindDirectoryRepository(
        impl: DirectoryRepositoryImpl,
    ): DirectoryRepository

    @Binds
    abstract fun bindStatusRepository(
        impl: StatusRepositoryImpl,
    ): StatusRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "status_vault.db",
        ).build()
    }

    @Provides
    fun provideSavedItemDao(database: AppDatabase) = database.savedItemDao()

    @Provides
    fun provideDirectoryGrantDao(database: AppDatabase) = database.directoryGrantDao()
}
