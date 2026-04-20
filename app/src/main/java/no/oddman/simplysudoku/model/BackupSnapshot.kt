package no.oddman.simplysudoku.model

import no.oddman.simplysudoku.data.local.RecordEntity

data class BackupSnapshot(
    val records: List<RecordEntity>,
    val settings: AppSettings
)

