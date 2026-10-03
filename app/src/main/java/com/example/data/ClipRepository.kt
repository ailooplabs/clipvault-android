package com.example.data

import kotlinx.coroutines.flow.Flow

class ClipRepository(private val clipDao: ClipDao) {

    val allClips: Flow<List<ClipItem>> = clipDao.getAllClips()
    val pinnedClips: Flow<List<ClipItem>> = clipDao.getPinnedClips()
    val totalCount: Flow<Int> = clipDao.getCount()
    val pinnedCount: Flow<Int> = clipDao.getPinnedCount()

    fun searchClips(query: String): Flow<List<ClipItem>> {
        return clipDao.searchClips(query)
    }

    fun getClipsByType(type: ClipType): Flow<List<ClipItem>> {
        return clipDao.getClipsByType(type)
    }

    suspend fun insert(content: String, isPinned: Boolean = false): Long {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return -1

        // Check if duplicate exists
        val existing = clipDao.findByContent(trimmed)
        return if (existing != null) {
            // Update timestamp to bring to top, and retain pin status
            val updated = existing.copy(
                createdAt = System.currentTimeMillis(),
                isPinned = if (isPinned) true else existing.isPinned
            )
            clipDao.update(updated)
            existing.id
        } else {
            val item = ClipItem.create(trimmed, isPinned = isPinned)
            clipDao.insert(item)
        }
    }

    suspend fun update(clip: ClipItem) {
        clipDao.update(clip)
    }

    suspend fun delete(clip: ClipItem) {
        clipDao.delete(clip)
    }

    suspend fun deleteById(id: Long) {
        clipDao.deleteById(id)
    }

    suspend fun clearUnpinned() {
        clipDao.clearUnpinned()
    }

    suspend fun clearAll() {
        clipDao.clearAll()
    }

    suspend fun togglePin(id: Long, currentPinState: Boolean) {
        clipDao.setPinned(id, !currentPinState)
    }

    suspend fun toggleMask(id: Long, currentMaskState: Boolean) {
        clipDao.setMasked(id, !currentMaskState)
    }

    suspend fun recordCopy(id: Long) {
        clipDao.incrementCopyCount(id)
    }

    suspend fun populateStarterPack() {
        ClipDatabase.populateInitialClips(clipDao)
    }
}
