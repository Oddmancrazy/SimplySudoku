package com.example.simplysudoku.data.local

data class RecordEntity(
    val id: Long = 0,
    val difficulty: String,
    val gameMode: String,
    val startedAtMillis: Long,
    val completedAtMillis: Long? = null,
    val elapsedSeconds: Int = 0,
    val isCompleted: Boolean = false,
    val isPerfectGame: Boolean = false
)