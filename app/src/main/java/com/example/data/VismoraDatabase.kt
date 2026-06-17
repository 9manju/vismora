package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun getProgressFlow(): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress WHERE id = 1")
    suspend fun getProgress(): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: UserProgress)
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badges ORDER BY earnedTime DESC")
    fun getAllBadgesFlow(): Flow<List<Badge>>

    @Query("SELECT * FROM badges")
    suspend fun getAllBadges(): List<Badge>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: Badge)
}

@Dao
interface ChatHistoryDao {
    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getChatHistoryFlow(): Flow<List<ChatHistory>>

    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    suspend fun getChatHistory(): List<ChatHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(chat: ChatHistory)

    @Query("DELETE FROM chat_history")
    suspend fun clearHistory()
}

@Database(entities = [UserProgress::class, Badge::class, ChatHistory::class], version = 1, exportSchema = false)
abstract class VismoraDatabase : RoomDatabase() {
    abstract fun userProgressDao(): UserProgressDao
    abstract fun badgeDao(): BadgeDao
    abstract fun chatHistoryDao(): ChatHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: VismoraDatabase? = null

        fun getDatabase(context: Context): VismoraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VismoraDatabase::class.java,
                    "vismora_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
