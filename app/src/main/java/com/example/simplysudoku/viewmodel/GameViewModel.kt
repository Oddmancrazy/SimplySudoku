package com.example.simplysudoku.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplysudoku.data.repository.RecordRepository
import com.example.simplysudoku.data.repository.SettingsRepository
import com.example.simplysudoku.logic.GameEngine
import com.example.simplysudoku.model.Difficulty
import com.example.simplysudoku.model.GameMode
import com.example.simplysudoku.model.GameUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val gameEngine = GameEngine()
    private val recordRepository = RecordRepository(application.applicationContext)
    private val settingsRepository = SettingsRepository(application.applicationContext)

    private val initialSettings = settingsRepository.getSettings()

    private lateinit var solutionBoard: Array<IntArray>

    private val _uiState = MutableStateFlow(
        GameUiState(
            board = emptyList(),
            difficulty = initialSettings.difficulty,
            gameMode = initialSettings.gameMode,
            elapsedSeconds = 0,
            isCompleted = false,
            hasStarted = false,
            selectedRow = null,
            selectedCol = null,
            selectedNumber = null,
            completedNumbers = emptySet(),
            completedRows = emptySet(),
            completedColumns = emptySet(),
            completedBoxes = emptySet(),
            isGenerating = false
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState

    private var currentRecordId: Long? = null
    private var hasMadeMistakeInCurrentGame: Boolean = false

    init {
        startNewGame()
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { state ->
                    if (!state.hasStarted || state.isCompleted || state.isGenerating || state.isPaused) {
                        state
                    } else {
                        state.copy(elapsedSeconds = state.elapsedSeconds + 1)
                    }
                }
            }
        }
    }

    fun pauseGame() {
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resumeGame() {
        _uiState.update { it.copy(isPaused = false) }
    }

    fun cancelGame() {
        _uiState.update { state ->
            state.copy(
                hasStarted = false,
                isPaused = false,
                elapsedSeconds = 0,
                selectedRow = null,
                selectedCol = null,
                selectedNumber = null
            )
        }
        currentRecordId = null
    }

    fun onCellClicked(row: Int, col: Int) {
        if (_uiState.value.isGenerating) return

        _uiState.update { state ->
            gameEngine.selectCell(state, row, col)
        }
    }

    fun onNumberInput(number: Int) {
        if (!::solutionBoard.isInitialized) return
        if (_uiState.value.isGenerating) return

        val previousState = _uiState.value
        val updatedState = gameEngine.inputNumber(previousState, solutionBoard, number)

        val selectedRow = updatedState.selectedRow
        val selectedCol = updatedState.selectedCol

        val selectedCellAfterInput = if (selectedRow != null && selectedCol != null) {
            updatedState.board.firstOrNull { it.row == selectedRow && it.col == selectedCol }
        } else {
            null
        }

        if (updatedState.gameMode == GameMode.MODERN && selectedCellAfterInput?.hasError == true) {
            hasMadeMistakeInCurrentGame = true
        }

        _uiState.value = updatedState

        val startedNow = !previousState.hasStarted && updatedState.hasStarted
        if (startedNow && currentRecordId == null) {
            createRecordForStartedGame(
                difficulty = updatedState.difficulty,
                gameMode = updatedState.gameMode
            )
        }

        val completedNow = !previousState.isCompleted && updatedState.isCompleted
        if (completedNow) {
            completeCurrentGame(updatedState.elapsedSeconds)
        }
    }

    fun onEraseInput() {
        if (!::solutionBoard.isInitialized) return
        if (_uiState.value.isGenerating) return

        _uiState.update { state ->
            gameEngine.eraseNumber(state, solutionBoard)
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        if (_uiState.value.isGenerating) return

        settingsRepository.saveDifficulty(difficulty)

        _uiState.update { state ->
            state.copy(difficulty = difficulty)
        }
    }

    fun setGameMode(mode: GameMode) {
        if (!::solutionBoard.isInitialized) return
        if (_uiState.value.isGenerating) return

        settingsRepository.saveGameMode(mode)

        _uiState.update { state ->
            val updatedState = state.copy(gameMode = mode)
            gameEngine.changeMode(updatedState, solutionBoard)
        }
    }

    fun startNewGame() {
        if (_uiState.value.isGenerating) return

        val currentState = _uiState.value
        val difficulty = currentState.difficulty
        val gameMode = currentState.gameMode

        _uiState.update { state ->
            state.copy(isGenerating = true)
        }

        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                gameEngine.createNewGame(
                    difficulty = difficulty,
                    gameMode = gameMode
                )
            }

            currentRecordId = null
            hasMadeMistakeInCurrentGame = false
            solutionBoard = result.solution
            _uiState.value = result.uiState.copy(isGenerating = false)
        }
    }

    private fun createRecordForStartedGame(
        difficulty: Difficulty,
        gameMode: GameMode
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val recordId = recordRepository.createStartedGame(
                difficulty = difficulty,
                gameMode = gameMode,
                startedAtMillis = System.currentTimeMillis()
            )
            currentRecordId = recordId
        }
    }

    private fun completeCurrentGame(elapsedSeconds: Int) {
        val recordId = currentRecordId ?: return
        val isPerfectGame = !hasMadeMistakeInCurrentGame

        viewModelScope.launch(Dispatchers.IO) {
            recordRepository.completeGame(
                recordId = recordId,
                elapsedSeconds = elapsedSeconds,
                completedAtMillis = System.currentTimeMillis(),
                isPerfectGame = isPerfectGame
            )
        }
    }
}