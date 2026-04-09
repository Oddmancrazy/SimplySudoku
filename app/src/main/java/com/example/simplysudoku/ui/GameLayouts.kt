package com.example.simplysudoku.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.simplysudoku.model.Difficulty
import com.example.simplysudoku.model.GameMode
import com.example.simplysudoku.model.GameUiState
import com.example.simplysudoku.model.SudokuCell
import com.example.simplysudoku.viewmodel.GameViewModel

private enum class PendingOpenType {
    MODE_MENU,
    DIFFICULTY_MENU
}

@Composable
fun PortraitGameContent(
    uiState: GameUiState,
    viewModel: GameViewModel
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = { }) {
            Text("SimplySudoku")
        }

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

        NumberPad(
            completedNumbers = uiState.completedNumbers,
            gameMode = uiState.gameMode,
            selectedNumber = uiState.selectedNumber,
            onNumberClick = viewModel::onNumberInput,
            onEraseClick = viewModel::onEraseInput
        )

        Text("Tid: ${formatTime(uiState.elapsedSeconds)}")
    }
}

@Composable
fun LandscapeGameContent(
    uiState: GameUiState,
    viewModel: GameViewModel
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

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLeftHanded) {
            LandscapeNumberPadColumn(
                uiState = uiState,
                viewModel = viewModel
            )

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

            LandscapeControlColumn(
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
                onNewGame = viewModel::startNewGame,
                onSwapSides = { isLeftHanded = !isLeftHanded },
                isLeftHanded = isLeftHanded
            )
        } else {
            LandscapeControlColumn(
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
                onNewGame = viewModel::startNewGame,
                onSwapSides = { isLeftHanded = !isLeftHanded },
                isLeftHanded = isLeftHanded
            )

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

            LandscapeNumberPadColumn(
                uiState = uiState,
                viewModel = viewModel
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
            Button(onClick = onOpenMode) {
                Text(uiState.gameMode.displayName)
            }

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
            Button(onClick = onOpenDifficulty) {
                Text(uiState.difficulty.displayName)
            }

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

        Button(onClick = onNewGame) {
            Text("Nytt spill")
        }
    }
}

@Composable
private fun LandscapeControlColumn(
    uiState: GameUiState,
    modeExpanded: Boolean,
    difficultyExpanded: Boolean,
    onOpenMode: () -> Unit,
    onOpenDifficulty: () -> Unit,
    onDismissMode: () -> Unit,
    onDismissDifficulty: () -> Unit,
    onSelectMode: (GameMode) -> Unit,
    onSelectDifficulty: (Difficulty) -> Unit,
    onNewGame: () -> Unit,
    onSwapSides: () -> Unit,
    isLeftHanded: Boolean
) {
    Column(
        modifier = Modifier.size(width = 180.dp, height = 360.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(onClick = { }) {
                Text("SimplySudoku")
            }

            Button(onClick = onOpenMode) {
                Text(uiState.gameMode.displayName)
            }

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

            Button(onClick = onOpenDifficulty) {
                Text(uiState.difficulty.displayName)
            }

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

            Button(onClick = onNewGame) {
                Text("Nytt spill")
            }

            if (uiState.isCompleted) {
                Text("Ferdig!")
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSwapSides,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Bytt side")
            }

            Text("Tid: ${formatTime(uiState.elapsedSeconds)}")
        }
    }
}

@Composable
private fun LandscapeNumberPadColumn(
    uiState: GameUiState,
    viewModel: GameViewModel
) {
    Column(
        modifier = Modifier.size(width = 170.dp, height = 360.dp),
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

            Button(
                onClick = onEraseClick,
                modifier = Modifier.size(width = 60.dp, height = 46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("⌫")
            }
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
        isModernMode && isComplete -> androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = Color(0xFFC8E6C9)
        )

        isModernMode && isSelected -> androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = Color(0xFFBBDEFB)
        )

        else -> androidx.compose.material3.ButtonDefaults.buttonColors()
    }

    Button(
        onClick = { onNumberClick(number) },
        modifier = Modifier.size(width = 60.dp, height = 46.dp),
        shape = RoundedCornerShape(12.dp),
        colors = buttonColors
    ) {
        Text(number.toString())
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
            Button(onClick = onDismiss) {
                Text("Avbryt")
            }
            Button(onClick = onConfirm) {
                Text("Fortsett")
            }
        }
    }
}

private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}