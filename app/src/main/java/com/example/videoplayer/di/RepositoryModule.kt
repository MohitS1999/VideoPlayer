package com.example.videoplayer.di

import com.example.videoplayer.repository.VideoRepository
import com.example.videoplayer.repository.VideoRepositoryImp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    @Singleton
    fun providesVideoSongsRepository(
        database:FirebaseFirestore
    ) : VideoRepository {
        return VideoRepositoryImp(database)
    }

}