package com.example.simplysudoku.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.simplysudoku.model.DifficultyStats
import com.example.simplysudoku.model.ModeSummaryStats
import com.example.simplysudoku.model.RecordsOverview
import com.example.simplysudoku.viewmodel.RecordsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordsScreen(
    viewModel: RecordsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val showFirstDeleteDialog = remember { mutableStateOf(false) }
    val showSecondDeleteDialog = remember { mutableStateOf(false) }

    if (showFirstDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showFirstDeleteDialog.value = false },
            title = { Text("Slette historikk?") },
            text = { Text("Er du sikker på at du vil slette all historikk?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFirstDeleteDialog.value = false
                        showSecondDeleteDialog.value = true
                    }
                ) {
                    Text("Ja")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFirstDeleteDialog.value = false }) {
                    Text("Avbryt")
                }
            }
        )
    }

    if (showSecondDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showSecondDeleteDialog.value = false },
            title = { Text("Bekreft sletting") },
            text = { Text("Dette kan ikke angres. Vil du virkelig slette alt?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSecondDeleteDialog.value = false
                        viewModel.deleteAllHistory()
                    }
                ) {
                    Text("Slett alt")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSecondDeleteDialog.value = false }) {
                    Text("Avbryt")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBackClick) {
                Text("Tilbake")
            }

            Text("Historikk og statistikk")

            Button(onClick = viewModel::refresh) {
                Text("Oppdater")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            Text("Laster statistikk...")
            return@Column
        }

        val overview = uiState.overview
        if (overview == null) {
            Text("Ingen data tilgjengelig.")
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummarySection("Samlet", overview.combinedSummary)
            SummarySection("Klassisk", overview.classicSummary)
            SummarySection("Moderne", overview.modernSummary)

            DifficultySection(
                title = "Samlet per vanskelighetsgrad",
                stats = overview.combinedByDifficulty
            )

            DifficultySection(
                title = "Klassisk per vanskelighetsgrad",
                stats = overview.classicByDifficulty
            )

            DifficultySection(
                title = "Moderne per vanskelighetsgrad",
                stats = overview.modernByDifficulty
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showFirstDeleteDialog.value = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Slett all historikk")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SummarySection(
    title: String,
    summary: ModeSummaryStats
) {
    Card(
        colors = CardDefaults.cardColors(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title)
            Text("Fullførte spill: ${summary.completedCount}")
            Text("Fullført uten feil: ${summary.perfectCount}")
            Text("Total tid spilt: ${formatLongTime(summary.totalSecondsPlayed)}")
        }
    }
}

@Composable
private fun DifficultySection(
    title: String,
    stats: List<DifficultyStats>
) {
    Card(
        colors = CardDefaults.cardColors(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title)

            stats.forEachIndexed { index, stat ->
                DifficultyRow(stat)

                if (index != stats.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun DifficultyRow(
    stat: DifficultyStats
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(stat.difficulty.displayName)
        Text("Startet: ${stat.startedCount}")
        Text("Fullført: ${stat.completedCount}")
        Text("Fullført uten feil: ${stat.perfectCount}")
        Text("Tid spilt: ${formatLongTime(stat.totalSecondsPlayed)}")

        val bestSeconds = stat.bestTime.fastestSeconds
        val bestDate = stat.bestTime.achievedAtMillis

        if (bestSeconds != null) {
            Text("Beste tid: ${formatShortTime(bestSeconds)}")
        } else {
            Text("Beste tid: –")
        }

        if (bestDate != null) {
            Text("Satt: ${formatDate(bestDate)}")
        } else {
            Text("Satt: –")
        }
    }
}

private fun formatShortTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun formatLongTime(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        "%dt %02dm %02ds".format(hours, minutes, seconds)
    } else {
        "%02dm %02ds".format(minutes, seconds)
    }
}

private fun formatDate(timeMillis: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timeMillis))
}