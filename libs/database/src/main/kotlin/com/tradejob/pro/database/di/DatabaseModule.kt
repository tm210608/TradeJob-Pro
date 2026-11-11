package com.tradejob.pro.database.di

import android.content.Context
import androidx.room.Room
import com.tradejob.pro.database.data.TradeJobDatabase
import com.tradejob.pro.database.data.dao.ClientDao
import com.tradejob.pro.database.data.dao.JobDao
import com.tradejob.pro.database.data.dao.JobPhotoDao
import com.tradejob.pro.database.data.dao.QuoteDao
import com.tradejob.pro.database.data.dao.QuoteItemDao
import com.tradejob.pro.database.data.dao.InvoiceDao
import com.tradejob.pro.database.data.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideRoomDatabase(@ApplicationContext context: Context): TradeJobDatabase {
        return Room.databaseBuilder(
            context,
            TradeJobDatabase::class.java,
            "trade_job_database"
        )
            .addMigrations(TradeJobDatabase.MIGRATION_1_2, TradeJobDatabase.MIGRATION_2_3, TradeJobDatabase.MIGRATION_3_4, TradeJobDatabase.MIGRATION_4_5, TradeJobDatabase.MIGRATION_5_6, TradeJobDatabase.MIGRATION_6_7)
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: TradeJobDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideClientDao(database: TradeJobDatabase): ClientDao = database.clientDao()

    @Provides
    @Singleton
    fun provideJobDao(database: TradeJobDatabase): JobDao = database.jobDao()

    @Provides
    @Singleton
    fun provideJobPhotoDao(database: TradeJobDatabase): JobPhotoDao = database.jobPhotoDao()

    @Provides
    @Singleton
    fun provideQuoteDao(database: TradeJobDatabase): QuoteDao = database.quoteDao()

    @Provides
    @Singleton
    fun provideQuoteItemDao(database: TradeJobDatabase): QuoteItemDao = database.quoteItemDao()

    @Provides
    @Singleton
    fun provideInvoiceDao(database: TradeJobDatabase): InvoiceDao = database.invoiceDao()
}
