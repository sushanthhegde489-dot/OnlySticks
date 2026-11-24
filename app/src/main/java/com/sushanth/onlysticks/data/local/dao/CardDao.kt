package com.sushanth.onlysticks.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sushanth.onlysticks.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE quarantined = 0 ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id")
    fun observeById(id: String): Flow<CardEntity?>

    @Query("SELECT * FROM cards ORDER BY createdAt ASC")
    suspend fun getAll(): List<CardEntity>

    @Upsert
    suspend fun upsert(card: CardEntity)

    @Upsert
    suspend fun upsertAll(list: List<CardEntity>)

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM cards")
    suspend fun deleteAll()

    @Query("UPDATE cards SET quarantined = :quarantined WHERE id = :id")
    suspend fun setQuar(id: String, quarantined: Boolean)

    @Query("SELECT * FROM cards WHERE quarantined = 1 ORDER BY createdAt ASC")
    fun observeQuar(): Flow<List<CardEntity>>
}
