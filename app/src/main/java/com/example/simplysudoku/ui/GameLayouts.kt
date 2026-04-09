package com.example.simplysudoku.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplysudoku.R
import com.example.simplysudoku.model.Difficulty
import com.example.simplysudoku.model.GameMode
import com.example.simplysudoku.model.GameUiState
import com.example.simplysudoku.viewmodel.GameViewModel

private enum class PendingOpenType {
    MODE_MENU,
    DIFFICULTY_MENU
}

private val PanelOuter = Color(0xFF9D744B)
private val PanelInner = Color(0xFFF5E8D8)
private val PanelFill = Color(0xEEFFF9F0)

private val WoodButtonBase = Color(0xFFC88B4A)
private val WoodButtonDark = Color(0xFF8B5A2B)
private val WoodButtonLight = Color(0xFFE8B777)
private val WoodButtonText = Color(0xFF3E2512)

@Composable
fun PortraitGameContent(
    uiState: GameUiState,
    viewModel: GameViewModel,
    onTitleClick: () -> Unit
) {
    var difficultyExpanded by remember { mutableStateOf(false) }
    var modeExpanded by remember { mutableStateOf(false) }
    var pendingOpenType by remember { mutableStateOf<PendingOpenType?>(null) }

    fun requestOpenModeMenu() {
        if (uiState.hasStarted && !uiState.isCompleted) {
            pendingOpenType = PendingOpenType.MODE_MENU
        } else {
            modeExpanded = true
        }
    }

    fun requestOpenDifficultyMenu() {
        if (uiState.hasStarted && !uiState.isCompleted) {
            pendingOpenType = PendingOpenType.DIFFICULTY_MENU
        } else {
            difficultyExpanded = true
        }
    }

    GameBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PanelCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WoodButton(
                        text = "SimplySudoku",
                        onClick = onTitleClick,
                        modifier = Modifier.width(300.dp),
                        textFontSize = 24.sp,
                        textFontFamily = FontFamily.Cursive
                    )

                    PortraitTopControls(
                        uiState = uiState,
                        modeExpanded = modeExpanded,
                        difficultyExpanded = difficultyExpanded,
                        onOpenMode = ::requestOpenModeMenu,
                        onOpenDifficulty = ::requestOpenDifficultyMenu,
                        onDismissMode = { modeExpanded = false },
                        onDismissDifficulty = { difficultyExpanded = false },
                        onSelectMode = {
                            modeExpanded = false
                            viewModel.setGameMode(it)
                            viewModel.startNewGame()
                        },
                        onSelectDifficulty = {
                            difficultyExpanded = false
                            viewModel.setDifficulty(it)
                            viewModel.startNewGame()
                        },
                        onNewGame = viewModel::startNewGame
                    )

                    if (uiState.isCompleted) {
                        Text("Gratulerer! Du løste brettet!")
                    }
                }
            }

            BoardPanelWithTimer(
                elapsedSeconds = uiState.elapsedSeconds
            ) {
                BoardContainer(
                    uiState = uiState,
                    pendingOpenType = pendingOpenType,
                    onConfirmPending = {
                        when (pendingOpenType) {
                            PendingOpenType.MODE_MENU -> modeExpanded = true
                            PendingOpenType.DIFFICULTY_MENU -> difficultyExpanded = true
                            null -> {}
                        }
                        pendingOpenType = null
                    },
                    onDismissPending = { pendingOpenType = null },
                    onCellClick = viewModel::onCellClicked
                )
            }

            TightPanelCard(
                modifier = Modifier.widthIn(max = 360.dp)
            ) {
                NumberPad(
                    completedNumbers = uiState.completedNumbers,
                    gameMode = uiState.gameMode,
                    selectedNumber = uiState.selectedNumber,
                    onNumberClick = viewModel::onNumberInput,
                    onEraseClick = viewModel::onEraseInput
                )
            }
        }
    }
}

@Composable
fun LandscapeGameContent(
    uiState: GameUiState,
    viewModel: GameViewModel,
    onTitleClick: () -> Unit
) {
    var difficultyExpanded by remember { mutableStateOf(false) }
    var modeExpanded by remember { mutableStateOf(false) }
    var pendingOpenType by remember { mutableStateOf<PendingOpenType?>(null) }
    var isLeftHanded by rememberSaveable { mutableStateOf(false) }

    fun requestOpenModeMenu() {
        if (uiState.hasStarted && !uiState.isCompleted) {
            pendingOpenType = PendingOpenType.MODE_MENU
        } else {
            modeExpanded = true
        }
    }

    fun requestOpenDifficultyMenu() {
        if (uiState.hasStarted && !uiState.isCompleted) {
            pendingOpenType = PendingOpenType.DIFFICULTY_MENU
        } else {
            difficultyExpanded = true
        }
    }

    GameBackground {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLeftHanded) {
                TightPanelCard {
                    LandscapeNumberPadColumn(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }

                BoardPanelWithTimer(
                    elapsedSeconds = uiState.elapsedSeconds
                ) {
                    BoardContainer(
                        uiState = uiState,
                        pendingOpenType = pendingOpenType,
                        onConfirmPending = {
                            when (pendingOpenType) {
                                PendingOpenType.MODE_MENU -> modeExpanded = true
                                PendingOpenType.DIFFICULTY_MENU -> difficultyExpanded = true
                                null -> {}
                            }
                            pendingOpenType = null
                        },
                        onDismissPending = { pendingOpenType = null },
                        onCellClick = viewModel::onCellClicked
                    )
                }

                PanelCard {
                    LandscapeControlColumn(
                        uiState = uiState,
                        modeExpanded = modeExpanded,
                        difficultyExpanded = difficultyExpanded,
                        onTitleClick = onTitleClick,
                        onOpenMode = ::requestOpenModeMenu,
                        onOpenDifficulty = ::requestOpenDifficultyMenu,
                        onDismissMode = { modeExpanded = false },
                        onDismissDifficulty = { difficultyExpanded = false },
                        onSelectMode = {
                            modeExpanded = false
                            viewModel.setGameMode(it)
                            viewModel.startNewGame()
                        },
                        onSelectDifficulty = {
                            difficultyExpanded = false
                            viewModel.setDifficulty(it)
                            viewModel.startNewGame()
                        },
                        onNewGame = viewModel::startNewGame,
                        onSwapSides = { isLeftHanded = !isLeftHanded }
                    )
                }
            } else {
                PanelCard {
                    LandscapeControlColumn(
                        uiState = uiState,
                        modeExpanded = modeExpanded,
                        difficultyExpanded = difficultyExpanded,
                        onTitleClick = onTitleClick,
                        onOpenMode = ::requestOpenModeMenu,
                        onOpenDifficulty = ::requestOpenDifficultyMenu,
                        onDismissMode = { modeExpanded = false },
                        onDismissDifficulty = { difficultyExpanded = false },
                        onSelectMode = {
                            modeExpanded = false
                            viewModel.setGameMode(it)
                            viewModel.startNewGame()
                        },
                        onSelectDifficulty = {
                            difficultyExpanded = false
                            viewModel.setDifficulty(it)
                            viewModel.startNewGame()
                        },
                        onNewGame = viewModel::startNewGame,
                        onSwapSides = { isLeftHanded = !isLeftHanded }
                    )
                }

                BoardPanelWithTimer(
                    elapsedSeconds = uiState.elapsedSeconds
                ) {
                    BoardContainer(
                        uiState = uiState,
                        pendingOpenType = pendingOpenType,
                        onConfirmPending = {
                            when (pendingOpenType) {
                                PendingOpenType.MODE_MENU -> modeExpanded = true
                                PendingOpenType.DIFFICULTY_MENU -> difficultyExpanded = true
                                null -> {}
                            }
                            pendingOpenType = null
                        },
                        onDismissPending = { pendingOpenType = null },
                        onCellClick = viewModel::onCellClicked
                    )
                }

                TightPanelCard {
                    LandscapeNumberPadColumn(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun GameBackground(
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.sudoku_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x55FFF7EB))
        )

        content()
    }
}

@Composable
private fun PanelCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(PanelOuter)
            .padding(2.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(PanelInner)
            .padding(1.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(PanelFill)
            .padding(14.dp)
    ) {
        content()
    }
}

@Composable
private fun TightPanelCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(PanelOuter)
            .padding(2.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(PanelInner)
            .padding(1.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(PanelFill)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        content()
    }
}

@Composable
private fun BoardPanelWithTimer(
    elapsedSeconds: Int,
    content: @Composable () -> Unit
) {
    PanelCard(
        modifier = Modifier.widthIn(max = 420.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = formatTime(elapsedSeconds),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF5C4126)
                )
            }

            Box(
                modifier = Modifier.padding(top = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}

@Composable
private fun WoodButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textFontSize: TextUnit = 18.sp,
    textFontFamily: FontFamily = FontFamily.Serif
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, WoodButtonDark),
        colors = ButtonDefaults.buttonColors(
            containerColor = WoodButtonBase,
            contentColor = WoodButtonText
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            WoodButtonLight.copy(alpha = 0.55f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 4.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = textFontSize,
                fontFamily = textFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PortraitTopControls(
    uiState: GameUiState,
    modeExpanded: Boolean,
    difficultyExpanded: Boolean,
    onOpenMode: () -> Unit,
    onOpenDifficulty: () -> Unit,
    onDismissMode: () -> Unit,
    onDismissDifficulty: () -> Unit,
    onSelectMode: (GameMode) -> Unit,
    onSelectDifficulty: (Difficulty) -> Unit,
    onNewGame: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WoodButton(
                text = shortModeLabel(uiState.gameMode),
                onClick = onOpenMode,
                modifier = Modifier.width(92.dp),
                textFontSize = 16.sp,
                textFontFamily = FontFamily.Cursive
            )

            DropdownMenu(
                expanded = modeExpanded,
                onDismissRequest = onDismissMode
            ) {
                GameMode.values().forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(mode.displayName) },
                        onClick = { onSelectMode(mode) }
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WoodButton(
                text = shortDifficultyLabel(uiState.difficulty),
                onClick = onOpenDifficulty,
                modifier = Modifier.width(92.dp),
                textFontSize = 16.sp,
                textFontFamily = FontFamily.Cursive
            )

            DropdownMenu(
                expanded = difficultyExpanded,
                onDismissRequest = onDismissDifficulty
            ) {
                Difficulty.values().forEach { difficulty ->
                    DropdownMenuItem(
                        text = { Text(difficulty.displayName) },
                        onClick = { onSelectDifficulty(difficulty) }
                    )
                }
            }
        }

        WoodButton(
            text = "Nytt",
            onClick = onNewGame,
            modifier = Modifier.width(108.dp),
            textFontSize = 16.sp,
            textFontFamily = FontFamily.Cursive
        )
    }
}

@Composable
private fun LandscapeControlColumn(
    uiState: GameUiState,
    modeExpanded: Boolean,
    difficultyExpanded: Boolean,
    onTitleClick: () -> Unit,
    onOpenMode: () -> Unit,
    onOpenDifficulty: () -> Unit,
    onDismissMode: () -> Unit,
    onDismissDifficulty: () -> Unit,
    onSelectMode: (GameMode) -> Unit,
    onSelectDifficulty: (Difficulty) -> Unit,
    onNewGame: () -> Unit,
    onSwapSides: () -> Unit
) {
    Column(
        modifier = Modifier.size(width = 190.dp, height = 370.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WoodButton(
                text = "SimplySudoku",
                onClick = onTitleClick,
                modifier = Modifier.width(168.dp),
                textFontSize = 22.sp,
                textFontFamily = FontFamily.Cursive
            )

            WoodButton(
                text = shortModeLabel(uiState.gameMode),
                onClick = onOpenMode,
                modifier = Modifier.width(140.dp),
                textFontSize = 16.sp,
                textFontFamily = FontFamily.Cursive
            )

            DropdownMenu(
                expanded = modeExpanded,
                onDismissRequest = onDismissMode
            ) {
                GameMode.values().forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(mode.displayName) },
                        onClick = { onSelectMode(mode) }
                    )
                }
            }

            WoodButton(
                text = shortDifficultyLabel(uiState.difficulty),
                onClick = onOpenDifficulty,
                modifier = Modifier.width(140.dp),
                textFontSize = 16.sp,
                textFontFamily = FontFamily.Cursive
            )

            DropdownMenu(
                expanded = difficultyExpanded,
                onDismissRequest = onDismissDifficulty
            ) {
                Difficulty.values().forEach { difficulty ->
                    DropdownMenuItem(
                        text = { Text(difficulty.displayName) },
                        onClick = { onSelectDifficulty(difficulty) }
                    )
                }
            }

            WoodButton(
                text = "Nytt",
                onClick = onNewGame,
                modifier = Modifier.width(140.dp),
                textFontSize = 16.sp,
                textFontFamily = FontFamily.Cursive
            )

            if (uiState.isCompleted) {
                Text("Ferdig!")
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WoodButton(
                text = "Bytt",
                onClick = onSwapSides,
                modifier = Modifier.width(128.dp),
                textFontSize = 16.sp,
                textFontFamily = FontFamily.Cursive
            )
        }
    }
}

@Composable
private fun LandscapeNumberPadColumn(
    uiState: GameUiState,
    viewModel: GameViewModel
) {
    Column(
        modifier = Modifier.size(width = 165.dp, height = 350.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LandscapeNumberPad(
            completedNumbers = uiState.completedNumbers,
            gameMode = uiState.gameMode,
            selectedNumber = uiState.selectedNumber,
            onNumberClick = viewModel::onNumberInput,
            onEraseClick = viewModel::onEraseInput
        )
    }
}

@Composable
private fun LandscapeNumberPad(
    completedNumbers: Set<Int>,
    gameMode: GameMode,
    selectedNumber: Int?,
    onNumberClick: (Int) -> Unit,
    onEraseClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LandscapeNumberRow(1, 2, completedNumbers, gameMode, selectedNumber, onNumberClick)
        LandscapeNumberRow(3, 4, completedNumbers, gameMode, selectedNumber, onNumberClick)
        LandscapeNumberRow(5, 6, completedNumbers, gameMode, selectedNumber, onNumberClick)
        LandscapeNumberRow(7, 8, completedNumbers, gameMode, selectedNumber, onNumberClick)

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LandscapeSingleNumberButton(
                number = 9,
                completedNumbers = completedNumbers,
                gameMode = gameMode,
                selectedNumber = selectedNumber,
                onNumberClick = onNumberClick
            )

            WoodButton(
                text = "⌫",
                onClick = onEraseClick,
                modifier = Modifier.size(width = 60.dp, height = 46.dp),
                textFontSize = 22.sp
            )
        }
    }
}

@Composable
private fun LandscapeNumberRow(
    leftNumber: Int,
    rightNumber: Int,
    completedNumbers: Set<Int>,
    gameMode: GameMode,
    selectedNumber: Int?,
    onNumberClick: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LandscapeSingleNumberButton(
            number = leftNumber,
            completedNumbers = completedNumbers,
            gameMode = gameMode,
            selectedNumber = selectedNumber,
            onNumberClick = onNumberClick
        )

        LandscapeSingleNumberButton(
            number = rightNumber,
            completedNumbers = completedNumbers,
            gameMode = gameMode,
            selectedNumber = selectedNumber,
            onNumberClick = onNumberClick
        )
    }
}

@Composable
private fun LandscapeSingleNumberButton(
    number: Int,
    completedNumbers: Set<Int>,
    gameMode: GameMode,
    selectedNumber: Int?,
    onNumberClick: (Int) -> Unit
) {
    val isSelected = selectedNumber == number
    val isComplete = completedNumbers.contains(number)
    val isModernMode = gameMode == GameMode.MODERN

    val buttonColors = when {
        isModernMode && isComplete -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFFC8E6C9),
            contentColor = WoodButtonText
        )
        isModernMode && isSelected -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFFBBDEFB),
            contentColor = WoodButtonText
        )
        else -> ButtonDefaults.buttonColors(
            containerColor = WoodButtonBase,
            contentColor = WoodButtonText
        )
    }

    Button(
        onClick = { onNumberClick(number) },
        modifier = Modifier.size(width = 60.dp, height = 46.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, WoodButtonDark),
        colors = buttonColors
    ) {
        Text(
            text = number.toString(),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )
    }
}

@Composable
private fun BoardContainer(
    uiState: GameUiState,
    pendingOpenType: PendingOpenType?,
    onConfirmPending: () -> Unit,
    onDismissPending: () -> Unit,
    onCellClick: (Int, Int) -> Unit
) {
    Box(
        modifier = Modifier.size(360.dp),
        contentAlignment = Alignment.Center
    ) {
        SudokuBoard(
            board = uiState.board,
            gameMode = uiState.gameMode,
            selectedNumber = uiState.selectedNumber,
            completedRows = uiState.completedRows,
            completedColumns = uiState.completedColumns,
            completedBoxes = uiState.completedBoxes,
            onCellClick = onCellClick
        )

        if (pendingOpenType != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99FFFFFF))
            )

            ConfirmRestartOverlay(
                message = "Å endre dette starter et nytt spill. Vil du fortsette?",
                onConfirm = onConfirmPending,
                onDismiss = onDismissPending
            )
        }

        if (uiState.isGenerating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCCFFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                MiniSudokuLoader()
            }
        }
    }
}

@Composable
private fun ConfirmRestartOverlay(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.82f)
            .background(
                color = Color(0xFFFFF8E1),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFFFCC80),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message)

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WoodButton(
                text = "Avbryt",
                onClick = onDismiss,
                modifier = Modifier.width(110.dp),
                textFontSize = 15.sp,
                textFontFamily = FontFamily.Cursive
            )
            WoodButton(
                text = "Fortsett",
                onClick = onConfirm,
                modifier = Modifier.width(110.dp),
                textFontSize = 15.sp,
                textFontFamily = FontFamily.Cursive
            )
        }
    }
}

private fun shortModeLabel(mode: GameMode): String {
    return when (mode) {
        GameMode.MODERN -> "M"
        GameMode.CLASSIC -> "K"
    }
}

private fun shortDifficultyLabel(difficulty: Difficulty): String {
    return when (difficulty) {
        Difficulty.VERY_EASY -> "EL"
        Difficulty.EASY -> "L"
        Difficulty.MEDIUM -> "M"
        Difficulty.HARD -> "V"
        Difficulty.VERY_HARD -> "VV"
        Difficulty.EXPERT -> "EX"
    }
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}