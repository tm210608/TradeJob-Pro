package com.tradejob.pro.database.data.dao

import androidx.room.*
import com.tradejob.pro.database.data.entity.QuoteItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteItemDao {
    @Query("SELECT * FROM quote_items WHERE quoteId = :quoteId ORDER BY sortOrder ASC")
    fun getItemsByQuote(quoteId: Long): Flow<List<QuoteItemEntity>>

    @Query("SELECT * FROM quote_items WHERE quoteId = :quoteId ORDER BY sortOrder ASC")
    suspend fun getItemsByQuoteSync(quoteId: Long): List<QuoteItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: QuoteItemEntity): Long

    @Update
    suspend fun update(item: QuoteItemEntity)

    @Delete
    suspend fun delete(item: QuoteItemEntity)

    @Query("DELETE FROM quote_items WHERE quoteId = :quoteId")
    suspend fun deleteByQuote(quoteId: Long)
}
