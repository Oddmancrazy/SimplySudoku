package com.simplysudoku.app.logic

import kotlin.random.Random

class SudokuSolver {

    fun solve(board: Array<IntArray>): Boolean {
        val emptyCell = findEmptyCell(board) ?: return true
        val row = emptyCell.first
        val col = emptyCell.second

        val numbers = (1..9).shuffled(Random)

        for (number in numbers) {
            if (isValid(board, row, col, number)) {
                board[row][col] = number

                if (solve(board)) {
                    return true
                }

                board[row][col] = 0
            }
        }

        return false
    }

    fun countSolutions(board: Array<IntArray>, limit: Int = 2): Int {
        var solutionCount = 0

        fun backtrack(): Boolean {
            val emptyCell = findEmptyCell(board) ?: run {
                solutionCount++
                return solutionCount >= limit
            }

            val row = emptyCell.first
            val col = emptyCell.second

            for (number in 1..9) {
                if (isValid(board, row, col, number)) {
                    board[row][col] = number

                    val shouldStop = backtrack()
                    if (shouldStop) {
                        board[row][col] = 0
                        return true
                    }

                    board[row][col] = 0
                }
            }

            return false
        }

        backtrack()
        return solutionCount
    }

    fun isValid(board: Array<IntArray>, row: Int, col: Int, number: Int): Boolean {
        for (i in 0..8) {
            if (board[row][i] == number && i != col) return false
            if (board[i][col] == number && i != row) return false
        }

        val boxRowStart = (row / 3) * 3
        val boxColStart = (col / 3) * 3

        for (r in boxRowStart until boxRowStart + 3) {
            for (c in boxColStart until boxColStart + 3) {
                if (board[r][c] == number && (r != row || c != col)) {
                    return false
                }
            }
        }

        return true
    }

    fun copyBoard(board: Array<IntArray>): Array<IntArray> {
        return Array(9) { row -> board[row].clone() }
    }

    private fun findEmptyCell(board: Array<IntArray>): Pair<Int, Int>? {
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] == 0) {
                    return Pair(row, col)
                }
            }
        }
        return null
    }
}
