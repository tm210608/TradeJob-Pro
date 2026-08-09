package com.tradejob.pro.home.di

import com.tradejob.pro.home.data.ClientRepositoryImpl
import com.tradejob.pro.home.data.InvoiceRepositoryImpl
import com.tradejob.pro.home.data.JobRepositoryImpl
import com.tradejob.pro.home.data.QuoteRepositoryImpl
import com.tradejob.pro.home.data.UserRepositoryImpl
import com.tradejob.pro.home.domain.ClientRepository
import com.tradejob.pro.home.domain.InvoiceRepository
import com.tradejob.pro.home.domain.JobRepository
import com.tradejob.pro.home.domain.QuoteRepository
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

    @Binds
    abstract fun bindQuoteRepository(
        quoteRepositoryImpl: QuoteRepositoryImpl
    ): QuoteRepository

    @Binds
    abstract fun bindInvoiceRepository(
        invoiceRepositoryImpl: InvoiceRepositoryImpl
    ): InvoiceRepository
}
