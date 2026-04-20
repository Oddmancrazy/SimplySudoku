package com.simplysudoku.app.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplysudoku.app.R
import com.simplysudoku.app.model.AppLanguage
import com.simplysudoku.app.model.Difficulty
import com.simplysudoku.app.model.RecordsOverview
import com.simplysudoku.app.viewmodel.RecordsViewModel
import com.simplysudoku.app.viewmodel.SettingsViewModel

private val HomePanelOuter = Color(0xFF8C5624)
private val HomePanelMid = Color(0xFFB6702E)
private val HomePanelLight = Color(0xFFE7B36E)
private val HomePanelInner = Color(0xFFF8F1E6)

private val HomeKeyWoodDark = Color(0xFF8A4C17)
private val HomeKeyWoodMid = Color(0xFFC97926)
private val HomeKeyWoodLight = Color(0xFFE9A44A)
private val HomeKeyText = Color(0xFF3B210E)

@Composable
fun HomeScreen(
    recordsViewModel: RecordsViewModel,
    settingsViewModel: SettingsViewModel,
    onBackToGame: () -> Unit,
    onOpenAllHistory: () -> Unit
) {
    val context = LocalContext.current
    val recordsUiState by recordsViewModel.uiState.collectAsState()
    val settingsUiState by settingsViewModel.uiState.collectAsState()

    val shareTitle = stringResource(R.string.share_backup_title)
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: SecurityException) {
            }

            settingsViewModel.setBackupUri(uri.toString())
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
            }

            settingsViewModel.importBackupFromFile(uri.toString())
            recordsViewModel.refresh()
        }
    }

    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        if (uri != null) {
            settingsViewModel.exportToSingleFile(uri.toString())
        }
    }

    val importedMessage = stringResource(R.string.backup_imported)
    LaunchedEffect(settingsUiState.statusMessage) {
        val msg = settingsUiState.statusMessage
        if (msg == "Backup importert." || msg == "Backup flettet inn." || msg == importedMessage) {
            recordsViewModel.refresh()
        }
    }

    GameLikeBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeWoodFramePanel(
                modifier = Modifier.widthIn(max = 600.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeTitleBanner()

                    HomeWideButton(
                        text = stringResource(R.string.to_game),
                        onClick = onBackToGame,
                        modifier = Modifier.width(220.dp)
                    )
                }
            }

            HomeWoodFramePanel(
                modifier = Modifier.widthIn(max = 600.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.highlights),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        color = HomeKeyText
                    )

                    if (recordsUiState.isLoading) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator()
                            Text(stringResource(R.string.loading_stats))
                        }
                    } else {
                        val overview = recordsUiState.overview
                        if (overview == null) {
                            Text(stringResource(R.string.no_stats))
                        } else {
                            HighlightsSection(overview)

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                HomeHistoryButton(
                                    text = stringResource(R.string.all_history),
                                    onClick = onOpenAllHistory,
                                    modifier = Modifier.width(180.dp)
                                )
                            }
                        }
                    }
                }
            }

            HomeWoodFramePanel(
                modifier = Modifier.widthIn(max = 600.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        color = HomeKeyText
                    )

                    LanguageSection(
                        selectedLanguage = settingsUiState.settings.language,
                        onSelectLanguage = settingsViewModel::setLanguage
                    )

                    BackupSection(
                        autoBackupEnabled = settingsUiState.settings.autoBackupEnabled,
                        backupUri = settingsUiState.settings.backupUri,
                        isWorking = settingsUiState.isWorking,
                        statusMessage = settingsUiState.statusMessage,
                        onToggleAutoBackup = settingsViewModel::setAutoBackupEnabled,
                        onConnectDrive = {
                            exportFileLauncher.launch("simplysudoku_backup.json")
                        },
                        onImportBackup = {
                            importFileLauncher.launch(
                                arrayOf("application/json", "text/plain", "*/*")
                            )
                        },
                        onClearStatus = settingsViewModel::clearStatusMessage
                    )
                }
            }

            HomeWoodFramePanel(
                modifier = Modifier.widthIn(max = 600.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.about_app),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        color = HomeKeyText
                    )

                    // 1. Goal
                    AboutLine(
                        title = stringResource(R.string.goal),
                        text = stringResource(R.string.goal_desc)
                    )

                    // 2. Game Modes
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.about_modes_title),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = HomeKeyText,
                            fontFamily = FontFamily.Serif
                        )
                        AboutLine(
                            title = stringResource(R.string.classic),
                            text = stringResource(R.string.classic_desc)
                        )
                        AboutLine(
                            title = stringResource(R.string.modern),
                            text = stringResource(R.string.modern_desc)
                        )
                    }

                    // 3. Features
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.about_features_title),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = HomeKeyText,
                            fontFamily = FontFamily.Serif
                        )
                        AboutLine(
                            title = stringResource(R.string.feature_pause_title),
                            text = stringResource(R.string.feature_pause_desc)
                        )
                        AboutLine(
                            title = stringResource(R.string.feature_layout_title),
                            text = stringResource(R.string.feature_layout_desc)
                        )
                        AboutLine(
                            title = stringResource(R.string.feature_history_title),
                            text = stringResource(R.string.feature_history_desc)
                        )
                    }

                    // 4. Story
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.about_story_title),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = HomeKeyText,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = stringResource(R.string.about_story_desc),
                            fontSize = 16.sp,
                            color = Color(0xFF4E3822),
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.app_by, "Oddman"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF5C4126),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun HighlightsSection(
    overview: RecordsOverview
) {
    StatLine(
        label = stringResource(R.string.total_time_played),
        value = formatLongTime(overview.combinedSummary.totalSecondsPlayed)
    )

    StatLine(
        label = stringResource(R.string.completed_games),
        value = overview.combinedSummary.completedCount.toString()
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = stringResource(R.string.best_time_per_difficulty),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = HomeKeyText
    )

    Difficulty.entries.forEach { difficulty ->
        val stat = overview.combinedByDifficulty.firstOrNull { it.difficulty == difficulty }
        val best = stat?.bestTime?.fastestSeconds

        StatLine(
            label = stringResource(difficulty.nameRes),
            value = if (best != null) formatShortTime(best) else "–"
        )
    }
}

@Composable
private fun LanguageSection(
    selectedLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val displayText = if (selectedLanguage == AppLanguage.SYSTEM) {
        "${selectedLanguage.flag} " + stringResource(R.string.system)
    } else {
        "${selectedLanguage.flag} ${selectedLanguage.displayName}"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.language),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = HomeKeyText
        )

        Text(
            text = stringResource(R.string.language_desc),
            fontSize = 15.sp,
            color = Color(0xFF4E3822)
        )

        Box {
            LanguageChoiceButton(
                text = displayText,
                isSelected = expanded,
                onClick = { expanded = true }
            )

            // Tre-tema Dropdown
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(220.dp)
                    .heightIn(max = (48 * 6).dp) // Plass til ca 6 elementer (48dp per item)
                    .background(HomePanelInner)
                    .border(2.dp, HomePanelOuter, RoundedCornerShape(8.dp))
            ) {
                // Vi viser bare de faktiske språkene i listen, ikke "System"
                AppLanguage.entries.filter { it != AppLanguage.SYSTEM }.forEach { language ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = language.flag,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Text(
                                    text = language.displayName,
                                    fontSize = 16.sp,
                                    fontWeight = if (selectedLanguage == language) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = HomeKeyText
                                )
                            }
                        },
                        onClick = {
                            onSelectLanguage(language)
                            expanded = false
                        },
                        modifier = Modifier.background(
                            if (selectedLanguage == language) Color(0x20D8892F) else Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun BackupSection(
    autoBackupEnabled: Boolean,
    backupUri: String?,
    isWorking: Boolean,
    statusMessage: String?,
    onToggleAutoBackup: (Boolean) -> Unit,
    onConnectDrive: () -> Unit,
    onImportBackup: () -> Unit,
    onClearStatus: () -> Unit
) {
    var infoExpanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.storage_and_backup),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = HomeKeyText
        )

        Text(
            text = stringResource(R.string.storage_desc),
            fontSize = 15.sp,
            color = Color(0xFF4E3822)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.auto_backup),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = HomeKeyText
            )

            Switch(
                checked = autoBackupEnabled,
                onCheckedChange = onToggleAutoBackup
            )
        }

        StatLine(
            label = stringResource(R.string.selected_location),
            value = when {
                backupUri == null -> stringResource(R.string.none_selected)
                backupUri.contains("com.google.android.apps.docs") -> stringResource(R.string.google_drive)
                else -> stringResource(R.string.cloud_file)
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallWoodButton(
                text = stringResource(R.string.choose_folder),
                onClick = onConnectDrive,
                modifier = Modifier.weight(1f)
            )

            SmallWoodButton(
                text = stringResource(R.string.import_label),
                onClick = onImportBackup,
                modifier = Modifier.weight(1f)
            )
        }

        if (autoBackupEnabled && backupUri != null) {
            Text(
                text = stringResource(R.string.auto_backup_active),
                fontSize = 13.sp,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (isWorking) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator()
                Text(stringResource(R.string.working))
            }
        }

        if (!statusMessage.isNullOrBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusMessage,
                    fontSize = 14.sp,
                    color = Color(0xFF6B5238),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Info-knapp og ekspanderende tekst
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (infoExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.backup_info_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = HomeKeyText
                    )
                    Text(
                        text = stringResource(R.string.backup_info_desc),
                        fontSize = 15.sp,
                        color = Color(0xFF4E3822),
                        lineHeight = 22.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(36.dp)
                    .shadow(2.dp, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .background(HomeKeyWoodDark)
                    .clickable { infoExpanded = !infoExpanded }
                    .padding(bottom = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(HomeKeyWoodLight, HomeKeyWoodMid)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (infoExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = HomeKeyText,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallWoodButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(14.dp), clip = false)
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(HomeKeyWoodDark)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(bottom = if (isPressed) 2.dp else 4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (isPressed) Color(0xFFF3C777) else HomeKeyWoodLight,
                            if (isPressed) Color(0xFFD8892F) else HomeKeyWoodMid
                        )
                    )
                )
                .padding(horizontal = 14.dp)
                .fillMaxSize()
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = HomeKeyText,
                maxLines = 1,
                softWrap = false,
                onTextLayout = { textLayoutResult ->
                    // Valgfritt: Hvis vi ville ha auto-scaling her, men Compose 
                    // håndterer det best via biblioteker. Vi bruker en enklere løsning:
                },
                style = LocalTextStyle.current.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        }
    }
}

@Composable
private fun LanguageChoiceButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topColor = when {
        isPressed -> Color(0xFFF3C777)
        isSelected -> Color(0xFFF3C777)
        else -> HomeKeyWoodLight
    }

    val bottomColor = when {
        isPressed -> Color(0xFFD8892F)
        isSelected -> Color(0xFFD8892F)
        else -> HomeKeyWoodMid
    }

    Box(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(16.dp), clip = false)
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(HomeKeyWoodDark)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(bottom = if (isPressed) 2.dp else 4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(listOf(topColor, bottomColor))
                )
                .padding(horizontal = 18.dp)
                .fillMaxSize()
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = HomeKeyText
            )
        }
    }
}

@Composable
private fun AboutLine(
    title: String,
    text: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = HomeKeyText
        )
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color(0xFF4E3822)
        )
    }
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
            color = HomeKeyText
        )
    }
}

@Composable
private fun GameLikeBackground(
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
private fun HomeWoodFramePanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(30.dp), clip = false)
            .clip(RoundedCornerShape(30.dp))
            .background(HomePanelOuter)
            .padding(bottom = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(HomePanelLight, HomePanelMid)
                    )
                )
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(26.dp))
                    .background(HomePanelInner)
                    .padding(14.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun HomeTitleBanner() {
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
private fun HomeHistoryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(12.dp), clip = false)
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(HomeKeyWoodDark)
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
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (isPressed) Color(0xFFF3C777) else HomeKeyWoodLight,
                            if (isPressed) Color(0xFFD8892F) else HomeKeyWoodMid
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
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = HomeKeyText
            )
        }
    }
}

@Composable
private fun HomeWideButton(
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
            .background(HomeKeyWoodDark)
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
                            if (isPressed) Color(0xFFF3C777) else HomeKeyWoodLight,
                            if (isPressed) Color(0xFFD8892F) else HomeKeyWoodMid
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
                color = HomeKeyText
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
