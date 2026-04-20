package com.simplysudoku.app.data.backup

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.content.FileProvider
import com.simplysudoku.app.data.local.RecordEntity
import com.simplysudoku.app.model.AppLanguage
import com.simplysudoku.app.model.AppSettings
import com.simplysudoku.app.model.BackupSnapshot
import com.simplysudoku.app.model.Difficulty
import com.simplysudoku.app.model.GameMode
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object BackupFileManager {

    private const val BACKUP_FILE_NAME = "simplysudoku_backup.json"

    fun writeBackupToTreeUri(
        context: Context,
        treeUriString: String,
        records: List<RecordEntity>,
        settings: AppSettings
    ): Result<Unit> {
        return runCatching {
            val treeUri = Uri.parse(treeUriString)
            val documentUri = findOrCreateBackupDocument(context, treeUri)
                ?: error("Kunne ikke opprette backupfil.")

            val backupJson = buildBackupJson(records, settings)

            context.contentResolver.openOutputStream(documentUri, "wt")?.bufferedWriter()?.use {
                it.write(backupJson)
            } ?: error("Kunne ikke åpne backupfil for skriving.")
        }
    }

    fun importBackupFromFileUri(
        context: Context,
        fileUriString: String
    ): Result<BackupSnapshot> {
        return runCatching {
            val fileUri = Uri.parse(fileUriString)

            val text = context.contentResolver.openInputStream(fileUri)
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: error("Kunne ikke lese backupfil.")

            parseBackupSnapshot(text)
        }
    }

    fun createShareBackupUri(
        context: Context,
        records: List<RecordEntity>,
        settings: AppSettings
    ): Result<Uri> {
        return runCatching {
            val backupJson = buildBackupJson(records, settings)
            val backupFile = File(context.cacheDir, BACKUP_FILE_NAME)

            backupFile.writeText(backupJson)

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )
        }
    }

    fun writeBackupToFileUri(
        context: Context,
        fileUriString: String,
        records: List<RecordEntity>,
        settings: AppSettings
    ): Result<Unit> {
        return runCatching {
            val fileUri = Uri.parse(fileUriString)
            val backupJson = buildBackupJson(records, settings)

            context.contentResolver.openOutputStream(fileUri, "wt")?.bufferedWriter()?.use {
                it.write(backupJson)
            } ?: error("Kunne ikke åpne fil for skriving.")
        }
    }

    private fun buildBackupJson(
        records: List<RecordEntity>,
        settings: AppSettings
    ): String {
        return JSONObject().apply {
            put("version", 1)
            put("records", JSONArray().apply {
                records.forEach { put(recordToJson(it)) }
            })
            put("settings", settingsToJson(settings))
        }.toString()
    }

    private fun parseBackupSnapshot(text: String): BackupSnapshot {
        val root = JSONObject(text)
        val recordsJson = root.optJSONArray("records") ?: JSONArray()
        val settingsJson = root.optJSONObject("settings") ?: JSONObject()

        val records = buildList {
            for (index in 0 until recordsJson.length()) {
                add(jsonToRecord(recordsJson.getJSONObject(index)))
            }
        }

        val settings = jsonToSettings(settingsJson)

        return BackupSnapshot(
            records = records,
            settings = settings
        )
    }

    private fun findOrCreateBackupDocument(
        context: Context,
        treeUri: Uri
    ): Uri? {
        val resolver = context.contentResolver
        val treeDocumentId = DocumentsContract.getTreeDocumentId(treeUri)
        val treeDocumentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocumentId)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocumentId)

        resolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ),
            null,
            null,
            null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val documentId = cursor.getString(idIndex)
                val displayName = cursor.getString(nameIndex)

                if (displayName == BACKUP_FILE_NAME) {
                    return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                }
            }
        }

        return DocumentsContract.createDocument(
            resolver,
            treeDocumentUri,
            "application/json",
            BACKUP_FILE_NAME
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

    private fun settingsToJson(settings: AppSettings): JSONObject {
        return JSONObject().apply {
            put("difficulty", settings.difficulty.name)
            put("gameMode", settings.gameMode.name)
            put("language", settings.language.storageValue)
            put("autoBackupEnabled", settings.autoBackupEnabled)
            put("backupUri", settings.backupUri ?: JSONObject.NULL)
        }
    }

    private fun jsonToSettings(obj: JSONObject): AppSettings {
        return AppSettings(
            difficulty = Difficulty.entries.firstOrNull {
                it.name == obj.optString("difficulty", Difficulty.EASY.name)
            } ?: Difficulty.EASY,
            gameMode = GameMode.entries.firstOrNull {
                it.name == obj.optString("gameMode", GameMode.MODERN.name)
            } ?: GameMode.MODERN,
            language = AppLanguage.fromStorageValue(
                obj.optString("language", AppLanguage.SYSTEM.storageValue)
            ),
            autoBackupEnabled = obj.optBoolean("autoBackupEnabled", false),
            backupUri = if (obj.isNull("backupUri")) null else obj.optString("backupUri", "")
                .takeIf { it.isNotBlank() }
        )
    }
}
