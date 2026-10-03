package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ClipItem::class], version = 1, exportSchema = false)
abstract class ClipDatabase : RoomDatabase() {

    abstract fun clipDao(): ClipDao

    companion object {
        @Volatile
        private var INSTANCE: ClipDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): ClipDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClipDatabase::class.java,
                    "clipvault_database"
                )
                    .addCallback(ClipDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class ClipDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialClips(database.clipDao())
                    }
                }
            }
        }

        suspend fun populateInitialClips(dao: ClipDao) {
            val now = System.currentTimeMillis()
            val initialClips = listOf(
                ClipItem(
                    content = "ssh -i ~/.ssh/id_ed25519 deploy@prod",
                    type = ClipType.CODE,
                    isPinned = true,
                    isMasked = false,
                    createdAt = now - 1000 * 60 * 12, // 12 mins ago
                    characterCount = 37,
                    copyCount = 3
                ),
                ClipItem(
                    content = "const API_KEY = \"sk-proj-abc1234987xyz...\"",
                    type = ClipType.SECRET,
                    isPinned = false,
                    isMasked = true,
                    createdAt = now - 1000 * 60 * 28, // 28 mins ago
                    characterCount = 42,
                    copyCount = 1
                ),
                ClipItem(
                    content = "https://polar.sh/dashboard/ailooplabs-llc",
                    type = ClipType.URL,
                    isPinned = false,
                    isMasked = false,
                    createdAt = now - 1000 * 60 * 60, // 1 hour ago
                    characterCount = 41,
                    copyCount = 2
                ),
                ClipItem(
                    content = "#6366F1",
                    type = ClipType.COLOR_HEX,
                    isPinned = true,
                    isMasked = false,
                    createdAt = now - 1000 * 60 * 120, // 2 hours ago
                    characterCount = 7,
                    copyCount = 5
                ),
                ClipItem(
                    content = "SELECT * FROM users WHERE status = 'active' ORDER BY created_at DESC LIMIT 50;",
                    type = ClipType.CODE,
                    isPinned = false,
                    isMasked = false,
                    createdAt = now - 1000 * 60 * 240, // 4 hours ago
                    characterCount = 79,
                    copyCount = 0
                ),
                ClipItem(
                    content = "Meeting notes: Q1 roadmap review, zero-tracking security verification & performance testing.",
                    type = ClipType.TEXT,
                    isPinned = false,
                    isMasked = false,
                    createdAt = now - 1000 * 60 * 60 * 24, // 1 day ago
                    characterCount = 94,
                    copyCount = 1
                )
            )
            dao.insertAll(initialClips)
        }
    }
}
