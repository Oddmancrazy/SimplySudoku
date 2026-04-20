package com.simplysudoku.app.model

import com.simplysudoku.app.data.local.RecordEntity

data class BackupSnapshot(
    val records: List<RecordEntity>,
    val settings: AppSettings
)
