package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "user_progress")
@JsonClass(generateAdapter = true)
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val xp: Int = 0,
    val level: Int = 1,
    val streakCount: Int = 1,
    val lastActiveTime: Long = System.currentTimeMillis(),
    val currentTheme: String = "netflix", // netflix, developer, light, cyberpunk, galaxy, nature
    val fontSizeMultiplier: Float = 1.0f,
    val highContrastMode: Boolean = false,
    val colorBlindMode: Boolean = false,
    val activeCursor: String = "default" // default, stack, queue, list, sort, search, ai
)

@Entity(tableName = "badges")
@JsonClass(generateAdapter = true)
data class Badge(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String,
    val iconName: String,
    val earnedTime: Long
)

@Entity(tableName = "chat_history")
@JsonClass(generateAdapter = true)
data class ChatHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user" or "model"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class RoadmapNode(
    val id: String,
    val title: String,
    val description: String,
    val skills: List<String>,
    val projects: List<String>,
    val difficulty: String,
    val xpReward: Int,
    val isCompleted: Boolean = false
)

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIdx: Int,
    val explanation: String
)

data class CodeChallenge(
    val id: String,
    val title: String,
    val problem: String,
    val startingCode: String,
    val language: String,
    val tests: List<Pair<String, String>>, // input, expected output
    val difficulty: String,
    val xpReward: Int
)
