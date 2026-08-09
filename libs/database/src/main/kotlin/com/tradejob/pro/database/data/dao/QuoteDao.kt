package com.tradejob.pro.database.data.dao

import androidx.room.*
import com.tradejob.pro.database.data.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes ORDER BY createdAt DESC")
    fun getAllQuotes(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getQuotesByClient(clientId: Long): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getQuoteById(id: Long): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE status = :status ORDER BY createdAt DESC")
    fun getQuotesByStatus(status: String): Flow<List<QuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quote: QuoteEntity): Long

    @Update
    suspend fun update(quote: QuoteEntity)

    @Delete
    suspend fun delete(quote: QuoteEntity)

    @Query("SELECT * FROM quotes WHERE isSynced = 0")
    suspend fun getUnsyncedQuotes(): List<QuoteEntity>

    @Query("UPDATE quotes SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markQuotesAsSynced(ids: List<Long>)
}
