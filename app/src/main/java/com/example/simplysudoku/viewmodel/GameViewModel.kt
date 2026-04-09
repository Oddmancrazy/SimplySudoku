package com.example.simplysudoku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class GameViewModel : ViewModel() {

    private val gameEngine = GameEngine()

    private lateinit var solutionBoard: Array<IntArray>

    private val _uiState = MutableStateFlow(
        GameUiState(
            board = emptyList(),
            difficulty = Difficulty.EASY,
            gameMode = GameMode.MODERN,
            elapsedSeconds = 0,
            isCompleted = false,
            hasStarted = false,
            selectedRow = null,
            selectedCol = null,
            selectedNumber = null
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState

    init {
        startNewGame()
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { state ->
                    if (!state.hasStarted || state.isCompleted) {
                        state
                    } else {
                        state.copy(elapsedSeconds = state.elapsedSeconds + 1)
                    }
                }
            }
        }
    }

    fun onCellClicked(row: Int, col: Int) {
        _uiState.update { state ->
            gameEngine.selectCell(state, row, col)
        }
    }

    fun onNumberInput(number: Int) {
        if (!::solutionBoard.isInitialized) return

        _uiState.update { state ->
            gameEngine.inputNumber(state, solutionBoard, number)
        }
    }

    fun onEraseInput() {
        if (!::solutionBoard.isInitialized) return

        _uiState.update { state ->
            gameEngine.eraseNumber(state, solutionBoard)
        }
    }

    fun setDifficulty(difficulty: Difficulty) {
        _uiState.update { state ->
            state.copy(difficulty = difficulty)
        }
    }

    fun setGameMode(mode: GameMode) {
        if (!::solutionBoard.isInitialized) return

        _uiState.update { state ->
            val updatedState = state.copy(gameMode = mode)
            gameEngine.changeMode(updatedState, solutionBoard)
        }
    }

    fun startNewGame() {
        val difficulty = _uiState.value.difficulty
        val gameMode = _uiState.value.gameMode

        viewModelScope.launch(Dispatchers.Default) {
            val result = gameEngine.createNewGame(
                difficulty = difficulty,
                gameMode = gameMode
            )

            solutionBoard = result.solution
            _uiState.value = result.uiState
        }
    }
}