package com.tradejob.pro.home.di

import com.tradejob.pro.home.data.ClientRepositoryImpl
import com.tradejob.pro.home.data.JobRepositoryImpl
import com.tradejob.pro.home.data.UserRepositoryImpl
import com.tradejob.pro.home.domain.ClientRepository
import com.tradejob.pro.home.domain.JobRepository
import com.tradejob.pro.home.domain.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {
    @Binds
    abstract fun bindClientRepository(
        clientRepositoryImpl: ClientRepositoryImpl
    ): ClientRepository

    @Binds
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    abstract fun bindJobRepository(
        jobRepositoryImpl: JobRepositoryImpl
    ): JobRepository
}
