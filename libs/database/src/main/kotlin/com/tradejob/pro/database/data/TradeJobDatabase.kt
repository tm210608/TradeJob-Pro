package com.tradejob.pro.database.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tradejob.pro.database.data.dao.ClientDao
import com.tradejob.pro.database.data.dao.JobDao
import com.tradejob.pro.database.data.dao.JobPhotoDao
import com.tradejob.pro.database.data.dao.UserDao
import com.tradejob.pro.database.data.entity.ClientEntity
import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.database.data.entity.JobPhotoEntity
import com.tradejob.pro.database.data.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ClientEntity::class,
        JobEntity::class,
        JobPhotoEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class TradeJobDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun clientDao(): ClientDao
    abstract fun jobDao(): JobDao
    abstract fun jobPhotoDao(): JobPhotoDao
}
