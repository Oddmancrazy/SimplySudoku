package no.oddman.simplysudoku.logic

data class SudokuAnalysis(
    val solved: Boolean,
    val nakedSinglesUsed: Int,
    val hiddenSinglesUsed: Int,
    val unresolvedCells: Int,
    val totalSteps: Int
)

class SudokuAnalyzer {

    fun analyze(puzzle: Array<IntArray>): SudokuAnalysis {
        val board = copyBoard(puzzle)

        var nakedSinglesUsed = 0
        var hiddenSinglesUsed = 0

        while (true) {
            var progress = false

            while (true) {
                val nakedMoves = findNakedSingles(board)
                if (nakedMoves.isEmpty()) break

                for ((row, col, value) in nakedMoves) {
                    if (board[row][col] == 0) {
                        board[row][col] = value
                        nakedSinglesUsed++
                        progress = true
                    }
                }
            }

            val hiddenMoves = findHiddenSingles(board)
            if (hiddenMoves.isNotEmpty()) {
                for ((row, col, value) in hiddenMoves) {
                    if (board[row][col] == 0) {
                        board[row][col] = value
                        hiddenSinglesUsed++
                        progress = true
                    }
                }
            }

            if (!progress) break
        }

        val unresolvedCells = board.sumOf { row -> row.count { it == 0 } }

        return SudokuAnalysis(
            solved = unresolvedCells == 0,
            nakedSinglesUsed = nakedSinglesUsed,
            hiddenSinglesUsed = hiddenSinglesUsed,
            unresolvedCells = unresolvedCells,
            totalSteps = nakedSinglesUsed + hiddenSinglesUsed
        )
    }

    fun getCandidates(board: Array<IntArray>, row: Int, col: Int): Set<Int> {
        if (board[row][col] != 0) return emptySet()

        val candidates = mutableSetOf<Int>()

        for (number in 1..9) {
            if (isValid(board, row, col, number)) {
                candidates.add(number)
            }
        }

        return candidates
    }

    private fun findNakedSingles(board: Array<IntArray>): List<Triple<Int, Int, Int>> {
        val moves = mutableListOf<Triple<Int, Int, Int>>()

        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] != 0) continue

                val candidates = getCandidates(board, row, col)
                if (candidates.size == 1) {
                    moves.add(Triple(row, col, candidates.first()))
                }
            }
        }

        return moves
    }

    private fun findHiddenSingles(board: Array<IntArray>): List<Triple<Int, Int, Int>> {
        val moves = linkedSetOf<Triple<Int, Int, Int>>()

        for (row in 0..8) {
            for (number in 1..9) {
                val possibleCells = mutableListOf<Pair<Int, Int>>()

                for (col in 0..8) {
                    if (board[row][col] == 0 && isValid(board, row, col, number)) {
                        possibleCells.add(row to col)
                    }
                }

                if (possibleCells.size == 1) {
                    val (r, c) = possibleCells.first()
                    moves.add(Triple(r, c, number))
                }
            }
        }

        for (col in 0..8) {
            for (number in 1..9) {
                val possibleCells = mutableListOf<Pair<Int, Int>>()

                for (row in 0..8) {
                    if (board[row][col] == 0 && isValid(board, row, col, number)) {
                        possibleCells.add(row to col)
                    }
                }

                if (possibleCells.size == 1) {
                    val (r, c) = possibleCells.first()
                    moves.add(Triple(r, c, number))
                }
            }
        }

        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                val rowStart = boxRow * 3
                val colStart = boxCol * 3

                for (number in 1..9) {
                    val possibleCells = mutableListOf<Pair<Int, Int>>()

                    for (row in rowStart until rowStart + 3) {
                        for (col in colStart until colStart + 3) {
                            if (board[row][col] == 0 && isValid(board, row, col, number)) {
                                possibleCells.add(row to col)
                            }
                        }
                    }

                    if (possibleCells.size == 1) {
                        val (r, c) = possibleCells.first()
                        moves.add(Triple(r, c, number))
                    }
                }
            }
        }

        return moves.toList()
    }

    private fun isValid(board: Array<IntArray>, row: Int, col: Int, number: Int): Boolean {
        for (i in 0..8) {
            if (board[row][i] == number) return false
            if (board[i][col] == number) return false
        }

        val boxRowStart = (row / 3) * 3
        val boxColStart = (col / 3) * 3

        for (r in boxRowStart until boxRowStart + 3) {
            for (c in boxColStart until boxColStart + 3) {
                if (board[r][c] == number) return false
            }
        }

        return true
    }

    private fun copyBoard(board: Array<IntArray>): Array<IntArray> {
        return Array(9) { row -> board[row].clone() }
    }
}

