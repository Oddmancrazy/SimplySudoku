package com.example.simplysudoku.data.repository

import android.content.Context
import com.example.simplysudoku.data.backup.BackupFileManager
import com.example.simplysudoku.data.local.RecordEntity
import com.example.simplysudoku.model.BestTimeStat
import com.example.simplysudoku.model.Difficulty
import com.example.simplysudoku.model.DifficultyStats
import com.example.simplysudoku.model.GameMode
import com.example.simplysudoku.model.ModeSummaryStats
import com.example.simplysudoku.model.RecordsOverview
import org.json.JSONArray
import org.json.JSONObject

class RecordRepository(
    private val context: Context
) {

    private val fileName = "records.json"

    suspend fun createStartedGame(
        difficulty: Difficulty,
        gameMode: GameMode,
        startedAtMillis: Long
    ): Long {
        val records = readRecords().toMutableList()
        val nextId = (records.maxOfOrNull { it.id } ?: 0L) + 1L

        val record = RecordEntity(
            id = nextId,
            difficulty = difficulty.name,
            gameMode = gameMode.name,
            startedAtMillis = startedAtMillis,
            completedAtMillis = null,
            elapsedSeconds = 0,
            isCompleted = false,
            isPerfectGame = false
        )

        records.add(record)
        writeRecords(records)
        return nextId
    }

    suspend fun completeGame(
        recordId: Long,
        elapsedSeconds: Int,
        completedAtMillis: Long,
        isPerfectGame: Boolean
    ) {
        val updatedRecords = readRecords().map { record ->
            if (record.id == recordId) {
                record.copy(
                    completedAtMillis = completedAtMillis,
                    elapsedSeconds = elapsedSeconds,
                    isCompleted = true,
                    isPerfectGame = isPerfectGame
                )
            } else {
                record
            }
        }

        writeRecords(updatedRecords)
    }

    suspend fun deleteAllHistory() {
        writeRecords(emptyList())
    }

    suspend fun getAllRecords(): List<RecordEntity> {
        return readRecords()
    }

    fun getAllRecordsSync(): List<RecordEntity> {
        return readRecords()
    }

    suspend fun replaceAllRecords(
        records: List<RecordEntity>,
        triggerAutoBackup: Boolean = true
    ) {
        writeRecords(records, triggerAutoBackup = triggerAutoBackup)
    }

    suspend fun buildOverview(): RecordsOverview {
        val allRecords = readRecords()

        return RecordsOverview(
            combinedSummary = buildSummary(
                label = "Samlet",
                records = allRecords
            ),
            classicSummary = buildSummary(
                label = "Klassisk",
                records = allRecords.filter { it.gameMode == GameMode.CLASSIC.name }
            ),
            modernSummary = buildSummary(
                label = "Moderne",
                records = allRecords.filter { it.gameMode == GameMode.MODERN.name }
            ),
            combinedByDifficulty = buildDifficultyStats(allRecords),
            classicByDifficulty = buildDifficultyStats(
                allRecords.filter { it.gameMode == GameMode.CLASSIC.name }
            ),
            modernByDifficulty = buildDifficultyStats(
                allRecords.filter { it.gameMode == GameMode.MODERN.name }
            )
        )
    }

    private fun buildSummary(
        label: String,
        records: List<RecordEntity>
    ): ModeSummaryStats {
        return ModeSummaryStats(
            label = label,
            completedCount = records.count { it.isCompleted },
            perfectCount = records.count { it.isCompleted && it.isPerfectGame },
            totalSecondsPlayed = records.sumOf { it.elapsedSeconds.toLong() }
        )
    }

    private fun buildDifficultyStats(
        records: List<RecordEntity>
    ): List<DifficultyStats> {
        return Difficulty.values().map { difficulty ->
            val difficultyRecords = records.filter { it.difficulty == difficulty.name }
            val completedRecords = difficultyRecords.filter { it.isCompleted }
            val perfectRecords = completedRecords.filter { it.isPerfectGame }
            val bestCompleted = completedRecords.minByOrNull { it.elapsedSeconds }

            DifficultyStats(
                difficulty = difficulty,
                startedCount = difficultyRecords.size,
                completedCount = completedRecords.size,
                perfectCount = perfectRecords.size,
                totalSecondsPlayed = difficultyRecords.sumOf { it.elapsedSeconds.toLong() },
                bestTime = BestTimeStat(
                    fastestSeconds = bestCompleted?.elapsedSeconds,
                    achievedAtMillis = bestCompleted?.completedAtMillis
                )
            )
        }
    }

    private fun readRecords(): List<RecordEntity> {
        val file = context.getFileStreamPath(fileName)
        if (!file.exists()) return emptyList()

        val text = context.openFileInput(fileName).bufferedReader().use { it.readText() }
        if (text.isBlank()) return emptyList()

        val jsonArray = JSONArray(text)
        val records = mutableListOf<RecordEntity>()

        for (index in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(index)
            records.add(jsonToRecord(obj))
        }

        return records.sortedByDescending { it.startedAtMillis }
    }

    private fun writeRecords(
        records: List<RecordEntity>,
        triggerAutoBackup: Boolean = true
    ) {
        val jsonArray = JSONArray()

        records.forEach { record ->
            jsonArray.put(recordToJson(record))
        }

        context.openFileOutput(fileName, Context.MODE_PRIVATE).bufferedWriter().use { writer ->
            writer.write(jsonArray.toString())
        }

        if (triggerAutoBackup) {
            triggerAutoBackup(records)
        }
    }

    private fun triggerAutoBackup(records: List<RecordEntity>) {
        val settingsRepository = SettingsRepository(context)
        val settings = settingsRepository.getSettings()

        if (!settings.autoBackupEnabled || settings.backupUri.isNullOrBlank()) return

        BackupFileManager.writeBackupToTreeUri(
            context = context,
            treeUriString = settings.backupUri,
            records = records,
            settings = settings
        )
    }

    private fun recordToJson(record: RecordEntity): JSONObject {
        return JSONObject().apply {
            put("id", record.id)
            put("difficulty", record.difficulty)
            put("gameMode", record.gameMode)
            put("startedAtMillis", record.startedAtMillis)
            put("completedAtMillis", record.completedAtMillis ?: JSONObject.NULL)
            put("elapsedSeconds", record.elapsedSeconds)
            put("isCompleted", record.isCompleted)
            put("isPerfectGame", record.isPerfectGame)
        }
    }

    private fun jsonToRecord(obj: JSONObject): RecordEntity {
        return RecordEntity(
            id = obj.optLong("id", 0L),
            difficulty = obj.optString("difficulty", Difficulty.EASY.name),
            gameMode = obj.optString("gameMode", GameMode.MODERN.name),
            startedAtMillis = obj.optLong("startedAtMillis", 0L),
            completedAtMillis = if (obj.isNull("completedAtMillis")) null else obj.optLong("completedAtMillis"),
            elapsedSeconds = obj.optInt("elapsedSeconds", 0),
            isCompleted = obj.optBoolean("isCompleted", false),
            isPerfectGame = obj.optBoolean("isPerfectGame", false)
        )
    }
}