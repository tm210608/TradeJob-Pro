package com.tradejob.pro.database.di

import android.content.Context
import androidx.room.Room
import com.tradejob.pro.database.data.TradeJobDatabase
import com.tradejob.pro.database.data.dao.ClientDao
import com.tradejob.pro.database.data.dao.JobDao
import com.tradejob.pro.database.data.dao.JobPhotoDao
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
            .fallbackToDestructiveMigration()
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
}
