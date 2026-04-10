package com.example.simplysudoku.ui

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale.Companion.Crop
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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

private val PanelOuter = Color(0xFF8C5624)
private val PanelMid = Color(0xFFB6702E)
private val PanelLight = Color(0xFFE7B36E)
private val PanelInner = Color(0xFFF8F1E6)

private val KeyWoodDark = Color(0xFF8A4C17)
private val KeyWoodMid = Color(0xFFC97926)
private val KeyWoodLight = Color(0xFFE9A44A)
private val KeyText = Color(0xFF3B210E)

private val LandscapeSidePanelWidth = 220.dp
private val LandscapeNumberPadWidth = 154.dp
private val LandscapeGap = 16.dp

@Composable
fun PortraitGameContent(
    uiState: GameUiState,
    viewModel: GameViewModel,
    onTitleClick: () -> Unit
) {
    // Layout calculations based on screen width
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val topPanelWidth = clampDp(screenWidth - 28.dp, 320.dp, 370.dp)
    val titleWidth = clampDp(screenWidth * 0.62f, 220.dp, 320.dp)
    val boardSize = clampDp(screenWidth * 0.80f, 300.dp, 348.dp)
    val numberPadWidth = clampDp(boardSize * 0.82f, 250.dp, 286.dp)
    val compactPad = numberPadWidth < 280.dp

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
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WoodFramePanel(
                modifier = Modifier.width(topPanelWidth)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(if (screenWidth > 400.dp) 16.dp else 10.dp)
                ) {
                    TitleImageButton(
                        onClick = onTitleClick,
                        width = titleWidth
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
                }
            }

            BoardPanelWithTimer(
                elapsedSeconds = uiState.elapsedSeconds,
                isCompleted = uiState.isCompleted
            ) {
                BoardContainer(
                    uiState = uiState,
                    pendingOpenType = pendingOpenType,
                    boardSize = boardSize,
                    onConfirmPending = {
                        when (pendingOpenType) {
                            PendingOpenType.MODE_MENU -> modeExpanded = true
                            PendingOpenType.DIFFICULTY_MENU -> difficultyExpanded = true
                            null -> {}
                        }
                        pendingOpenType = null
                    },
                    onDismissPending = { pendingOpenType = null },
                    onCellClick = viewModel::onCellClicked,
                    onResume = viewModel::resumeGame
                )
            }

            NumberPad(
                completedNumbers = uiState.completedNumbers,
                selectedNumber = uiState.selectedNumber,
                onNumberClick = viewModel::onNumberInput,
                onEraseClick = viewModel::onEraseInput,
                panelWidth = numberPadWidth,
                compact = compactPad
            )
        }
    }
}

@Composable
fun LandscapeGameContent(
    uiState: GameUiState,
    viewModel: GameViewModel,
    onTitleClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // Dynamically calculate board size based on screen height for landscape
    // We want the board to fit comfortably within the height with padding
    val boardSize = clampDp(screenHeight - 80.dp, 340.dp, 440.dp)
    val boardPanelWidth = boardSize + 44.dp // Add some extra for the WoodFramePanel

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
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(
                LandscapeGap,
                Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(2.dp))

            if (isLeftHanded) {
                Box(
                    modifier = Modifier.width(LandscapeNumberPadWidth),
                    contentAlignment = Alignment.Center
                ) {
                    LandscapeNumberPad(
                        completedNumbers = uiState.completedNumbers,
                        selectedNumber = uiState.selectedNumber,
                        onNumberClick = viewModel::onNumberInput,
                        onEraseClick = viewModel::onEraseInput
                    )
                }

                Box(
                    modifier = Modifier.width(boardPanelWidth),
                    contentAlignment = Alignment.Center
                ) {
                    BoardPanelWithTimer(
                        elapsedSeconds = uiState.elapsedSeconds,
                        isCompleted = uiState.isCompleted,
                        maxWidth = boardPanelWidth,
                        compactFrame = true
                    ) {
                        BoardContainer(
                            uiState = uiState,
                            pendingOpenType = pendingOpenType,
                            boardSize = boardSize,
                            onConfirmPending = {
                                when (pendingOpenType) {
                                    PendingOpenType.MODE_MENU -> modeExpanded = true
                                    PendingOpenType.DIFFICULTY_MENU -> difficultyExpanded = true
                                    null -> {}
                                }
                                pendingOpenType = null
                            },
                            onDismissPending = { pendingOpenType = null },
                            onCellClick = viewModel::onCellClicked,
                            onResume = viewModel::resumeGame
                        )
                    }
                }

                Box(
                    modifier = Modifier.width(LandscapeSidePanelWidth),
                    contentAlignment = Alignment.Center
                ) {
                    WoodFramePanel {
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
                }
            } else {
                Box(
                    modifier = Modifier.width(LandscapeSidePanelWidth),
                    contentAlignment = Alignment.Center
                ) {
                    WoodFramePanel {
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
                }

                Box(
                    modifier = Modifier.width(boardPanelWidth),
                    contentAlignment = Alignment.Center
                ) {
                    BoardPanelWithTimer(
                        elapsedSeconds = uiState.elapsedSeconds,
                        isCompleted = uiState.isCompleted,
                        maxWidth = boardPanelWidth,
                        compactFrame = true
                    ) {
                        BoardContainer(
                            uiState = uiState,
                            pendingOpenType = pendingOpenType,
                            boardSize = boardSize,
                            onConfirmPending = {
                                when (pendingOpenType) {
                                    PendingOpenType.MODE_MENU -> modeExpanded = true
                                    PendingOpenType.DIFFICULTY_MENU -> difficultyExpanded = true
                                    null -> {}
                                }
                                pendingOpenType = null
                            },
                            onDismissPending = { pendingOpenType = null },
                            onCellClick = viewModel::onCellClicked,
                            onResume = viewModel::resumeGame
                        )
                    }
                }

                Box(
                    modifier = Modifier.width(LandscapeNumberPadWidth),
                    contentAlignment = Alignment.Center
                ) {
                    LandscapeNumberPad(
                        completedNumbers = uiState.completedNumbers,
                        selectedNumber = uiState.selectedNumber,
                        onNumberClick = viewModel::onNumberInput,
                        onEraseClick = viewModel::onEraseInput
                    )
                }
            }

            Spacer(modifier = Modifier.width(2.dp))
        }
    }
}

@Composable
private fun GameBackground(
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
private fun WoodFramePanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(30.dp), clip = false)
            .clip(RoundedCornerShape(30.dp))
            .background(PanelOuter)
            .padding(bottom = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(PanelLight, PanelMid)
                    )
                )
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(26.dp))
                    .background(PanelInner)
                    .padding(14.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun BoardPanelWithTimer(
    elapsedSeconds: Int,
    isCompleted: Boolean,
    maxWidth: Dp = 420.dp,
    compactFrame: Boolean = false,
    content: @Composable () -> Unit
) {
    val innerPadding = if (compactFrame) 9.dp else 14.dp
    val midPadding = if (compactFrame) 3.dp else 4.dp
    val bottomLip = if (compactFrame) 4.dp else 5.dp

    Box(
        modifier = Modifier
            .widthIn(max = maxWidth)
            .shadow(10.dp, RoundedCornerShape(30.dp), clip = false)
            .clip(RoundedCornerShape(30.dp))
            .background(PanelOuter)
            .padding(bottom = bottomLip)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(PanelLight, PanelMid)
                    )
                )
                .padding(midPadding)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(26.dp))
                    .background(PanelInner)
                    .padding(innerPadding)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCompleted) {
                            Text(
                                text = stringResource(R.string.congratulations),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF5C4126)
                            )
                        } else {
                            Box(modifier = Modifier.width(80.dp))
                        }

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
    }
}

@Composable
private fun TitleImageButton(
    onClick: () -> Unit,
    width: Dp = 190.dp
) {
    TopWoodKey(
        onClick = onClick,
        modifier = Modifier.width(width),
        cornerRadius = 18.dp,
        keyHeight = 64.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.sudoku_title02),
                contentDescription = "SimplySudoku",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentScale = Crop
            )
        }
    }
}

@Composable
private fun TopWoodKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    keyHeight: Dp = 52.dp,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(cornerRadius), clip = false)
            .height(keyHeight)
            .clip(RoundedCornerShape(cornerRadius))
            .background(KeyWoodDark)
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
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (isPressed) Color(0xFFF3C777) else KeyWoodLight,
                            if (isPressed) Color(0xFFD8892F) else KeyWoodMid
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = cornerRadius,
                        topEnd = cornerRadius
                    )
                )
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (isPressed) 0.28f else 0.20f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun TopWoodTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopWoodKey(
        onClick = onClick,
        modifier = modifier,
        cornerRadius = 16.dp,
        keyHeight = 52.dp
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Serif,
            color = KeyText
        )
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
            TopWoodTextButton(
                text = shortModeLabel(uiState.gameMode),
                onClick = onOpenMode,
                modifier = Modifier.width(92.dp)
            )

            DropdownMenu(
                expanded = modeExpanded,
                onDismissRequest = onDismissMode,
                modifier = Modifier
                    .width(180.dp)
                    .heightIn(max = (48 * 6).dp)
                    .background(PanelInner)
                    .border(2.dp, PanelOuter, RoundedCornerShape(8.dp))
            ) {
                GameMode.entries.forEach { mode ->
                    val isSelected = uiState.gameMode == mode
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(mode.nameRes),
                                color = KeyText,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        onClick = { onSelectMode(mode) },
                        modifier = Modifier.background(
                            if (isSelected) Color(0x20D8892F) else Color.Transparent
                        )
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TopWoodTextButton(
                text = shortDifficultyLabel(uiState.difficulty),
                onClick = onOpenDifficulty,
                modifier = Modifier.width(92.dp)
            )

            DropdownMenu(
                expanded = difficultyExpanded,
                onDismissRequest = onDismissDifficulty,
                modifier = Modifier
                    .width(180.dp)
                    .heightIn(max = (48 * 6).dp)
                    .background(PanelInner)
                    .border(2.dp, PanelOuter, RoundedCornerShape(8.dp))
            ) {
                Difficulty.entries.forEach { difficulty ->
                    val isSelected = uiState.difficulty == difficulty
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(difficulty.nameRes),
                                color = KeyText,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        onClick = { onSelectDifficulty(difficulty) },
                        modifier = Modifier.background(
                            if (isSelected) Color(0x20D8892F) else Color.Transparent
                        )
                    )
                }
            }
        }

        TopWoodTextButton(
            text = stringResource(R.string.new_game_short),
            onClick = onNewGame,
            modifier = Modifier.width(108.dp)
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
            TitleImageButton(onClick = onTitleClick, width = 190.dp)

            TopWoodTextButton(
                text = shortModeLabel(uiState.gameMode),
                onClick = onOpenMode,
                modifier = Modifier.width(190.dp)
            )

            DropdownMenu(
                expanded = modeExpanded,
                onDismissRequest = onDismissMode,
                modifier = Modifier
                    .width(190.dp)
                    .heightIn(max = (48 * 6).dp)
                    .background(PanelInner)
                    .border(2.dp, PanelOuter, RoundedCornerShape(8.dp))
            ) {
                GameMode.entries.forEach { mode ->
                    val isSelected = uiState.gameMode == mode
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(mode.nameRes),
                                color = KeyText,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        onClick = { onSelectMode(mode) },
                        modifier = Modifier.background(
                            if (isSelected) Color(0x20D8892F) else Color.Transparent
                        )
                    )
                }
            }

            TopWoodTextButton(
                text = shortDifficultyLabel(uiState.difficulty),
                onClick = onOpenDifficulty,
                modifier = Modifier.width(190.dp)
            )

            DropdownMenu(
                expanded = difficultyExpanded,
                onDismissRequest = onDismissDifficulty,
                modifier = Modifier
                    .width(190.dp)
                    .heightIn(max = (48 * 6).dp)
                    .background(PanelInner)
                    .border(2.dp, PanelOuter, RoundedCornerShape(8.dp))
            ) {
                Difficulty.entries.forEach { difficulty ->
                    val isSelected = uiState.difficulty == difficulty
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(difficulty.nameRes),
                                color = KeyText,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        onClick = { onSelectDifficulty(difficulty) },
                        modifier = Modifier.background(
                            if (isSelected) Color(0x20D8892F) else Color.Transparent
                        )
                    )
                }
            }

            TopWoodTextButton(
                text = stringResource(R.string.new_game_short),
                onClick = onNewGame,
                modifier = Modifier.width(190.dp)
            )
        }

        TopWoodTextButton(
            text = stringResource(R.string.swap_sides),
            onClick = onSwapSides,
            modifier = Modifier.width(190.dp)
        )
    }
}

@Composable
private fun BoardContainer(
    uiState: GameUiState,
    pendingOpenType: PendingOpenType?,
    boardSize: Dp,
    onConfirmPending: () -> Unit,
    onDismissPending: () -> Unit,
    onCellClick: (Int, Int) -> Unit,
    onResume: () -> Unit
) {
    Box(
        modifier = Modifier.size(boardSize),
        contentAlignment = Alignment.Center
    ) {
        if (!uiState.isPaused) {
            SudokuBoard(
                board = uiState.board,
                gameMode = uiState.gameMode,
                selectedNumber = uiState.selectedNumber,
                completedRows = uiState.completedRows,
                completedColumns = uiState.completedColumns,
                completedBoxes = uiState.completedBoxes,
                onCellClick = onCellClick
            )
        }

        if (uiState.isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PanelInner)
                    .border(2.dp, PanelOuter, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.game_paused),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = KeyText
                    )
                    TopWoodTextButton(
                        text = stringResource(R.string.resume),
                        onClick = onResume,
                        modifier = Modifier.width(160.dp)
                    )
                }
            }
        }

        if (pendingOpenType != null && !uiState.isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99FFFFFF))
            )

            ConfirmRestartOverlay(
                message = stringResource(R.string.confirm_restart),
                onConfirm = onConfirmPending,
                onDismiss = onDismissPending
            )
        }

        if (uiState.isGenerating && !uiState.isPaused) {
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
            TopWoodTextButton(
                text = stringResource(R.string.cancel),
                onClick = onDismiss,
                modifier = Modifier.width(110.dp)
            )
            TopWoodTextButton(
                text = stringResource(R.string.continue_label),
                onClick = onConfirm,
                modifier = Modifier.width(110.dp)
            )
        }
    }
}

@Composable
private fun shortModeLabel(mode: GameMode): String {
    return stringResource(when (mode) {
        GameMode.MODERN -> R.string.mode_modern_short
        GameMode.CLASSIC -> R.string.mode_classic_short
    })
}

@Composable
private fun shortDifficultyLabel(difficulty: Difficulty): String {
    return stringResource(difficulty.shortRes)
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun clampDp(value: Dp, min: Dp, max: Dp): Dp {
    return when {
        value < min -> min
        value > max -> max
        else -> value
    }
}
