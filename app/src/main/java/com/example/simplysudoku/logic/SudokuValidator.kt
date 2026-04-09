package com.example.simplysudoku.logic

class SudokuValidator {

    fun isValidMove(board: Array<IntArray>, row: Int, col: Int, value: Int): Boolean {
        for (i in 0..8) {
            if (i != col && board[row][i] == value) return false
            if (i != row && board[i][col] == value) return false
        }

        val boxRowStart = (row / 3) * 3
        val boxColStart = (col / 3) * 3

        for (r in boxRowStart until boxRowStart + 3) {
            for (c in boxColStart until boxColStart + 3) {
                if ((r != row || c != col) && board[r][c] == value) return false
            }
        }

        return true
    }

    fun findConflicts(board: Array<IntArray>): Set<Pair<Int, Int>> {
        val conflicts = mutableSetOf<Pair<Int, Int>>()

        for (row in 0..8) {
            for (col in 0..8) {
                val value = board[row][col]
                if (value == 0) continue

                board[row][col] = 0
                val valid = isValidMove(board, row, col, value)
                board[row][col] = value

                if (!valid) {
                    conflicts.add(row to col)
                }
            }
        }

        return conflicts
    }
}