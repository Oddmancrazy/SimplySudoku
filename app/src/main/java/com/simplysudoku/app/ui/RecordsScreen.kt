package com.simplysudoku.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplysudoku.app.R
import com.simplysudoku.app.model.DifficultyStats
import com.simplysudoku.app.model.ModeSummaryStats
import com.simplysudoku.app.viewmodel.RecordsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val RecordsPanelOuter = Color(0xFF8C5624)
private val RecordsPanelMid = Color(0xFFB6702E)
private val RecordsPanelLight = Color(0xFFE7B36E)
private val RecordsPanelInner = Color(0xFFF8F1E6)

private val RecordsKeyWoodDark = Color(0xFF8A4C17)
private val RecordsKeyWoodMid = Color(0xFFC97926)
private val RecordsKeyWoodLight = Color(0xFFE9A44A)
private val RecordsKeyText = Color(0xFF3B210E)

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
            title = { Text(stringResource(R.string.delete_history_title)) },
            text = { Text(stringResource(R.string.delete_history_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFirstDeleteDialog.value = false
                        showSecondDeleteDialog.value = true
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFirstDeleteDialog.value = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showSecondDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showSecondDeleteDialog.value = false },
            title = { Text(stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.confirm_delete_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSecondDeleteDialog.value = false
                        viewModel.deleteAllHistory()
                    }
                ) {
                    Text(stringResource(R.string.delete_all_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSecondDeleteDialog.value = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    RecordsBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RecordsWoodFramePanel(
                modifier = Modifier.widthIn(max = 600.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RecordsTitleBanner()

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RecordsWideButton(
                            text = stringResource(R.string.back),
                            onClick = onBackClick,
                            modifier = Modifier.width(150.dp)
                        )

                        RecordsWideButton(
                            text = stringResource(R.string.refresh),
                            onClick = viewModel::refresh,
                            modifier = Modifier.width(150.dp)
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                RecordsWoodFramePanel(
                    modifier = Modifier.widthIn(max = 600.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator()
                        Text(stringResource(R.string.loading_stats))
                    }
                }
            } else {
                val overview = uiState.overview

                if (overview == null) {
                    RecordsWoodFramePanel(
                        modifier = Modifier.widthIn(max = 600.dp)
                    ) {
                        Text(stringResource(R.string.no_data))
                    }
                } else {
                    RecordsWoodFramePanel(
                        modifier = Modifier.widthIn(max = 600.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SectionTitle(stringResource(R.string.overview))

                            SummaryCard(stringResource(R.string.combined), overview.combinedSummary)
                            SummaryCard(stringResource(R.string.classic), overview.classicSummary)
                            SummaryCard(stringResource(R.string.modern), overview.modernSummary)
                        }
                    }

                    RecordsWoodFramePanel(
                        modifier = Modifier.widthIn(max = 600.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SectionTitle(stringResource(R.string.classic_per_difficulty))
                            DifficultySection(stats = overview.classicByDifficulty)
                        }
                    }

                    RecordsWoodFramePanel(
                        modifier = Modifier.widthIn(max = 600.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SectionTitle(stringResource(R.string.modern_per_difficulty))
                            DifficultySection(stats = overview.modernByDifficulty)
                        }
                    }

                    RecordsWoodFramePanel(
                        modifier = Modifier.widthIn(max = 600.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RecordsWideButton(
                                text = stringResource(R.string.delete_all_history),
                                onClick = { showFirstDeleteDialog.value = true },
                                modifier = Modifier.width(240.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    summary: ModeSummaryStats
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.45f))
            .border(
                width = 1.dp,
                color = Color(0xFFD8B58A),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(14.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = RecordsKeyText
            )

            StatLine(stringResource(R.string.completed_games), summary.completedCount.toString())
            StatLine(stringResource(R.string.perfect_games), summary.perfectCount.toString())
            StatLine(stringResource(R.string.total_time_played), formatLongTime(summary.totalSecondsPlayed))
        }
    }
}

@Composable
private fun DifficultySection(
    stats: List<DifficultyStats>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        stats.forEachIndexed { index, stat ->
            DifficultyRow(stat)

            if (index != stats.lastIndex) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color(0xFFD8B58A)
                )
            }
        }
    }
}

@Composable
private fun DifficultyRow(
    stat: DifficultyStats
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(stat.difficulty.nameRes),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = RecordsKeyText
        )

        StatLine(stringResource(R.string.started), stat.startedCount.toString())
        StatLine(stringResource(R.string.completed), stat.completedCount.toString())
        StatLine(stringResource(R.string.perfect_games), stat.perfectCount.toString())
        StatLine(stringResource(R.string.time_played), formatLongTime(stat.totalSecondsPlayed))

        val bestSeconds = stat.bestTime.fastestSeconds
        val bestDate = stat.bestTime.achievedAtMillis

        StatLine(
            stringResource(R.string.best_time),
            if (bestSeconds != null) formatShortTime(bestSeconds) else "–"
        )

        StatLine(
            stringResource(R.string.achieved),
            if (bestDate != null) formatDate(bestDate) else "–"
        )
    }
}

@Composable
private fun SectionTitle(
    text: String
) {
    Text(
        text = text,
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Serif,
        color = RecordsKeyText
    )
}

@Composable
private fun StatLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color(0xFF4E3822)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = RecordsKeyText
        )
    }
}

@Composable
private fun RecordsBackground(
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.insp01),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x30FFF7EB))
        )

        content()
    }
}

@Composable
private fun RecordsWoodFramePanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(30.dp), clip = false)
            .clip(RoundedCornerShape(30.dp))
            .background(RecordsPanelOuter)
            .padding(bottom = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(RecordsPanelLight, RecordsPanelMid)
                    )
                )
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(26.dp))
                    .background(RecordsPanelInner)
                    .padding(14.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun RecordsTitleBanner() {
    Box(
        modifier = Modifier
            .width(320.dp)
            .height(86.dp)
            .clip(RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.sudoku_title02),
            contentDescription = "SimplySudoku",
            modifier = Modifier.fillMaxSize(),
            contentScale = Crop
        )
    }
}

@Composable
private fun RecordsWideButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp), clip = false)
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(RecordsKeyWoodDark)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(bottom = if (isPressed) 2.dp else 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (isPressed) Color(0xFFF3C777) else RecordsKeyWoodLight,
                            if (isPressed) Color(0xFFD8892F) else RecordsKeyWoodMid
                        )
                    )
                )
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Serif,
                color = RecordsKeyText
            )
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
