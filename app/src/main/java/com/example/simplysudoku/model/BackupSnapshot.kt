package com.example.simplysudoku.model

import com.example.simplysudoku.data.local.RecordEntity

data class BackupSnapshot(
    val records: List<RecordEntity>,
    val settings: AppSettings
)