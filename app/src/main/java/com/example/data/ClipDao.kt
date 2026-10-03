package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipDao {

    @Query("SELECT * FROM clips ORDER BY isPinned DESC, createdAt DESC")
    fun getAllClips(): Flow<List<ClipItem>>

    @Query("SELECT * FROM clips WHERE isPinned = 1 ORDER BY createdAt DESC")
    fun getPinnedClips(): Flow<List<ClipItem>>

    @Query("SELECT * FROM clips WHERE type = :type ORDER BY isPinned DESC, createdAt DESC")
    fun getClipsByType(type: ClipType): Flow<List<ClipItem>>

    @Query("SELECT * FROM clips WHERE content LIKE '%' || :query || '%' ORDER BY isPinned DESC, createdAt DESC")
    fun searchClips(query: String): Flow<List<ClipItem>>

    @Query("SELECT * FROM clips WHERE content = :content LIMIT 1")
    suspend fun findByContent(content: String): ClipItem?

    @Query("SELECT COUNT(*) FROM clips")
    fun getCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM clips WHERE isPinned = 1")
    fun getPinnedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clip: ClipItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clips: List<ClipItem>)

    @Update
    suspend fun update(clip: ClipItem)

    @Delete
    suspend fun delete(clip: ClipItem)

    @Query("DELETE FROM clips WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM clips WHERE isPinned = 0")
    suspend fun clearUnpinned()

    @Query("UPDATE clips SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean)

    @Query("UPDATE clips SET isMasked = :isMasked WHERE id = :id")
    suspend fun setMasked(id: Long, isMasked: Boolean)

    @Query("UPDATE clips SET copyCount = copyCount + 1 WHERE id = :id")
    suspend fun incrementCopyCount(id: Long)

    @Query("DELETE FROM clips")
    suspend fun clearAll()
}
