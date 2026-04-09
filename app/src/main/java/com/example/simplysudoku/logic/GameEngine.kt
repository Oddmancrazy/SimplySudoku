package com.example.simplysudoku.logic

import com.example.simplysudoku.model.Difficulty
import com.example.simplysudoku.model.GameMode
import com.example.simplysudoku.model.GameUiState
import com.example.simplysudoku.model.SudokuCell

class GameEngine(
    private val generator: SudokuGenerator = SudokuGenerator()
) {

    data class NewGameResult(
        val uiState: GameUiState,
        val solution: Array<IntArray>
    )

    fun createNewGame(
        difficulty: Difficulty,
        gameMode: GameMode
    ): NewGameResult {
        val generated = generator.generate(difficulty)
        val board = puzzleToCells(generated.puzzle)

        val completedNumbers = findCompletedNumbers(board, generated.solution)
        val completedRows = findCompletedRows(board, generated.solution)
        val completedColumns = findCompletedColumns(board, generated.solution)
        val completedBoxes = findCompletedBoxes(board, generated.solution)

        return NewGameResult(
            uiState = GameUiState(
                board = board,
                difficulty = difficulty,
                gameMode = gameMode,
                elapsedSeconds = 0,
                isCompleted = false,
                hasStarted = false,
                selectedRow = null,
                selectedCol = null,
                selectedNumber = null,
                completedNumbers = completedNumbers,
                completedRows = completedRows,
                completedColumns = completedColumns,
                completedBoxes = completedBoxes
            ),
            solution = generated.solution
        )
    }

    fun selectCell(
        state: GameUiState,
        row: Int,
        col: Int
    ): GameUiState {
        val clickedCell = state.board.firstOrNull { it.row == row && it.col == col }

        return state.copy(
            selectedRow = row,
            selectedCol = col,
            selectedNumber = clickedCell?.value,
            board = state.board.map { cell ->
                cell.copy(isSelected = cell.row == row && cell.col == col)
            }
        )
    }

    fun inputNumber(
        state: GameUiState,
        solutionBoard: Array<IntArray>,
        number: Int
    ): GameUiState {
        val row = state.selectedRow ?: return state.copy(selectedNumber = number)
        val col = state.selectedCol ?: return state.copy(selectedNumber = number)

        val selectedCell = state.board.firstOrNull {
            it.row == row && it.col == col
        } ?: return state.copy(selectedNumber = number)

        if (selectedCell.isFixed || state.isCompleted) {
            return state.copy(selectedNumber = number)
        }

        val updatedBoard = state.board.map { cell ->
            if (cell.row == row && cell.col == col) {
                val hasError = state.gameMode == GameMode.MODERN &&
                        number != solutionBoard[row][col]

                cell.copy(
                    value = number,
                    hasError = hasError
                )
            } else {
                cell
            }
        }

        val completed = isBoardCompleted(updatedBoard, solutionBoard)

        return state.copy(
            board = updatedBoard,
            selectedNumber = number,
            isCompleted = completed,
            hasStarted = true,
            completedNumbers = findCompletedNumbers(updatedBoard, solutionBoard),
            completedRows = findCompletedRows(updatedBoard, solutionBoard),
            completedColumns = findCompletedColumns(updatedBoard, solutionBoard),
            completedBoxes = findCompletedBoxes(updatedBoard, solutionBoard)
        )
    }

    fun eraseNumber(
        state: GameUiState,
        solutionBoard: Array<IntArray>
    ): GameUiState {
        val row = state.selectedRow ?: return state
        val col = state.selectedCol ?: return state

        if (state.isCompleted) return state

        val updatedBoard = state.board.map { cell ->
            if (cell.row == row && cell.col == col && !cell.isFixed) {
                cell.copy(
                    value = null,
                    hasError = false
                )
            } else {
                cell
            }
        }

        return state.copy(
            board = updatedBoard,
            isCompleted = false,
            selectedNumber = null,
            completedNumbers = findCompletedNumbers(updatedBoard, solutionBoard),
            completedRows = findCompletedRows(updatedBoard, solutionBoard),
            completedColumns = findCompletedColumns(updatedBoard, solutionBoard),
            completedBoxes = findCompletedBoxes(updatedBoard, solutionBoard)
        )
    }

    fun changeMode(
        state: GameUiState,
        solutionBoard: Array<IntArray>
    ): GameUiState {
        val updatedBoard = when (state.gameMode) {
            GameMode.CLASSIC -> state.board.map { cell ->
                cell.copy(hasError = false)
            }

            GameMode.MODERN -> state.board.map { cell ->
                if (cell.value == null || cell.isFixed) {
                    cell.copy(hasError = false)
                } else {
                    cell.copy(
                        hasError = cell.value != solutionBoard[cell.row][cell.col]
                    )
                }
            }
        }

        return state.copy(
            board = updatedBoard,
            completedNumbers = findCompletedNumbers(updatedBoard, solutionBoard),
            completedRows = findCompletedRows(updatedBoard, solutionBoard),
            completedColumns = findCompletedColumns(updatedBoard, solutionBoard),
            completedBoxes = findCompletedBoxes(updatedBoard, solutionBoard)
        )
    }

    private fun isBoardCompleted(
        board: List<SudokuCell>,
        solutionBoard: Array<IntArray>
    ): Boolean {
        return board.all { cell ->
            val value = cell.value ?: return@all false
            value == solutionBoard[cell.row][cell.col]
        }
    }

    private fun findCompletedNumbers(
        board: List<SudokuCell>,
        solutionBoard: Array<IntArray>
    ): Set<Int> {
        val completed = mutableSetOf<Int>()

        for (number in 1..9) {
            val matchingCells = board.filter { cell ->
                cell.value == number && solutionBoard[cell.row][cell.col] == number
            }

            if (matchingCells.size == 9) {
                completed.add(number)
            }
        }

        return completed
    }

    private fun findCompletedRows(
        board: List<SudokuCell>,
        solutionBoard: Array<IntArray>
    ): Set<Int> {
        val completedRows = mutableSetOf<Int>()

        for (row in 0..8) {
            val isComplete = (0..8).all { col ->
                val cell = board.first { it.row == row && it.col == col }
                cell.value != null && cell.value == solutionBoard[row][col]
            }

            if (isComplete) {
                completedRows.add(row)
            }
        }

        return completedRows
    }

    private fun findCompletedColumns(
        board: List<SudokuCell>,
        solutionBoard: Array<IntArray>
    ): Set<Int> {
        val completedColumns = mutableSetOf<Int>()

        for (col in 0..8) {
            val isComplete = (0..8).all { row ->
                val cell = board.first { it.row == row && it.col == col }
                cell.value != null && cell.value == solutionBoard[row][col]
            }

            if (isComplete) {
                completedColumns.add(col)
            }
        }

        return completedColumns
    }

    private fun findCompletedBoxes(
        board: List<SudokuCell>,
        solutionBoard: Array<IntArray>
    ): Set<Int> {
        val completedBoxes = mutableSetOf<Int>()

        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                val boxIndex = boxRow * 3 + boxCol

                val isComplete = (boxRow * 3 until boxRow * 3 + 3).all { row ->
                    (boxCol * 3 until boxCol * 3 + 3).all { col ->
                        val cell = board.first { it.row == row && it.col == col }
                        cell.value != null && cell.value == solutionBoard[row][col]
                    }
                }

                if (isComplete) {
                    completedBoxes.add(boxIndex)
                }
            }
        }

        return completedBoxes
    }

    private fun puzzleToCells(puzzle: Array<IntArray>): List<SudokuCell> {
        val cells = mutableListOf<SudokuCell>()

        for (row in 0..8) {
            for (col in 0..8) {
                val value = puzzle[row][col]
                cells.add(
                    SudokuCell(
                        row = row,
                        col = col,
                        value = if (value == 0) null else value,
                        isFixed = value != 0
                    )
                )
            }
        }

        return cells
    }
}