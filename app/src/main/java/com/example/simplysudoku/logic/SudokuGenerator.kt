package com.example.simplysudoku.logic

import com.example.simplysudoku.model.Difficulty
import kotlin.random.Random

class SudokuGenerator(
    private val solver: SudokuSolver = SudokuSolver()
) {

    fun generate(difficulty: Difficulty): GeneratedSudoku {
        val solution = generateSolvedBoard()
        val puzzle = solver.copyBoard(solution)

        val targetClues = getTargetClues(difficulty)
        removeNumbersKeepingUniqueSolution(puzzle, targetClues)

        return GeneratedSudoku(
            puzzle = puzzle,
            solution = solution
        )
    }

    fun generateSolvedBoard(): Array<IntArray> {
        val board = Array(9) { IntArray(9) { 0 } }
        solver.solve(board)
        return board
    }

    private fun removeNumbersKeepingUniqueSolution(
        board: Array<IntArray>,
        targetClues: Int
    ) {
        val positions = mutableListOf<Pair<Int, Int>>()

        for (row in 0..8) {
            for (col in 0..8) {
                positions.add(Pair(row, col))
            }
        }

        positions.shuffle(Random)

        var cluesLeft = 81

        for ((row, col) in positions) {
            if (cluesLeft <= targetClues) break

            val backup = board[row][col]
            board[row][col] = 0

            val boardCopy = solver.copyBoard(board)
            val solutionCount = solver.countSolutions(boardCopy, limit = 2)

            if (solutionCount != 1) {
                board[row][col] = backup
            } else {
                cluesLeft--
            }
        }
    }

    private fun getTargetClues(difficulty: Difficulty): Int {
        return when (difficulty) {
            Difficulty.VERY_EASY -> 44
            Difficulty.EASY -> 38
            Difficulty.MEDIUM -> 34
            Difficulty.HARD -> 30
            Difficulty.VERY_HARD -> 26
            Difficulty.EXPERT -> 23
        }
    }
}