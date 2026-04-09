package com.example.simplysudoku.model

data class GameUiState(
    val board: List<SudokuCell> = emptyList(),
    val selectedRow: Int? = null,
    val selectedCol: Int? = null,
    val selectedNumber: Int? = null,
    val difficulty: Difficulty = Difficulty.EASY,
    val gameMode: GameMode = GameMode.MODERN,
    val elapsedSeconds: Int = 0,
    val isCompleted: Boolean = false,
    val hasStarted: Boolean = false,
    val completedNumbers: Set<Int> = emptySet(),
    val completedRows: Set<Int> = emptySet(),
    val completedColumns: Set<Int> = emptySet(),
    val completedBoxes: Set<Int> = emptySet()
)