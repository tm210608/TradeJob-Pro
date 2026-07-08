package com.tradejob.pro.login.di

import com.tradejob.pro.login.data.LoginRepositoryImpl
import com.tradejob.pro.login.data.UserDataSourceImpl
import com.tradejob.pro.login.domain.LoginRepository
import com.tradejob.pro.login.domain.UserDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoginModule {

    @Binds
    @Singleton
    abstract fun bindLoginRepository(impl: LoginRepositoryImpl): LoginRepository

    @Binds
    @Singleton
    abstract fun bindUserDataSource(impl: UserDataSourceImpl): UserDataSource
}
