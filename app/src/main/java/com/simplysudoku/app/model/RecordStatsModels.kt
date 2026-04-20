package com.simplysudoku.app.model

data class BestTimeStat(
    val fastestSeconds: Int?,
    val achievedAtMillis: Long?
)

data class DifficultyStats(
    val difficulty: Difficulty,
    val startedCount: Int,
    val completedCount: Int,
    val perfectCount: Int,
    val totalSecondsPlayed: Long,
    val bestTime: BestTimeStat
)

data class ModeSummaryStats(
    val label: String,
    val completedCount: Int,
    val perfectCount: Int,
    val totalSecondsPlayed: Long
)

data class RecordsOverview(
    val combinedSummary: ModeSummaryStats,
    val classicSummary: ModeSummaryStats,
    val modernSummary: ModeSummaryStats,
    val combinedByDifficulty: List<DifficultyStats>,
    val classicByDifficulty: List<DifficultyStats>,
    val modernByDifficulty: List<DifficultyStats>
)
