package com.simplysudoku.app.logic

import com.simplysudoku.app.model.Difficulty
import kotlin.math.abs
import kotlin.random.Random

class SudokuGenerator(
    private val solver: SudokuSolver = SudokuSolver(),
    private val evaluator: SudokuDifficultyEvaluator = SudokuDifficultyEvaluator()
) {

    fun generate(difficulty: Difficulty): GeneratedSudoku {
        val maxAttempts = maxAttemptsFor(difficulty)

        var bestCandidate: GeneratedSudoku? = null
        var bestDistance = Int.MAX_VALUE
        var bestScoreDelta = Int.MAX_VALUE

        repeat(maxAttempts) {
            val solution = generateSolvedBoard()
            val puzzle = solver.copyBoard(solution)

            removeNumbersKeepingUniqueSolution(
                board = puzzle,
                targetClues = getTargetClues(difficulty)
            )

            val evaluation = evaluator.evaluate(puzzle)
            val distance = evaluator.absoluteDifficultyDistance(
                evaluation.difficulty,
                difficulty
            )
            val scoreDelta = abs(evaluation.score - targetScoreFor(difficulty))

            if (isGoodEnoughMatch(difficulty, distance, scoreDelta)) {
                return GeneratedSudoku(
                    puzzle = puzzle,
                    solution = solution
                )
            }

            val shouldReplaceBest =
                distance < bestDistance ||
                        (distance == bestDistance && scoreDelta < bestScoreDelta)

            if (shouldReplaceBest) {
                bestCandidate = GeneratedSudoku(
                    puzzle = solver.copyBoard(puzzle),
                    solution = solver.copyBoard(solution)
                )
                bestDistance = distance
                bestScoreDelta = scoreDelta
            }
        }

        return bestCandidate ?: fallbackGenerate(difficulty)
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
                positions.add(row to col)
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

            if (solutionCount == 1) {
                cluesLeft--
            } else {
                board[row][col] = backup
            }
        }
    }

    private fun fallbackGenerate(difficulty: Difficulty): GeneratedSudoku {
        val solution = generateSolvedBoard()
        val puzzle = solver.copyBoard(solution)

        removeNumbersKeepingUniqueSolution(
            board = puzzle,
            targetClues = getTargetClues(difficulty)
        )

        return GeneratedSudoku(
            puzzle = puzzle,
            solution = solution
        )
    }

    private fun maxAttemptsFor(difficulty: Difficulty): Int {
        return when (difficulty) {
            Difficulty.VERY_EASY -> 10
            Difficulty.EASY -> 12
            Difficulty.MEDIUM -> 14
            Difficulty.HARD -> 16
            Difficulty.VERY_HARD -> 14
            Difficulty.EXPERT -> 12
        }
    }

    private fun getTargetClues(difficulty: Difficulty): Int {
        return when (difficulty) {
            Difficulty.VERY_EASY -> 46
            Difficulty.EASY -> 40
            Difficulty.MEDIUM -> 35
            Difficulty.HARD -> 30
            Difficulty.VERY_HARD -> 27
            Difficulty.EXPERT -> 24
        }
    }

    private fun targetScoreFor(difficulty: Difficulty): Int {
        return when (difficulty) {
            Difficulty.VERY_EASY -> 0
            Difficulty.EASY -> 12
            Difficulty.MEDIUM -> 28
            Difficulty.HARD -> 46
            Difficulty.VERY_HARD -> 64
            Difficulty.EXPERT -> 82
        }
    }

    private fun isGoodEnoughMatch(
        difficulty: Difficulty,
        distance: Int,
        scoreDelta: Int
    ): Boolean {
        return when (difficulty) {
            Difficulty.VERY_EASY ->
                distance == 0 && scoreDelta <= 12

            Difficulty.EASY ->
                distance == 0 && scoreDelta <= 14

            Difficulty.MEDIUM ->
                distance == 0 && scoreDelta <= 16

            Difficulty.HARD ->
                distance <= 1 && scoreDelta <= 18

            Difficulty.VERY_HARD ->
                distance <= 1 && scoreDelta <= 22

            Difficulty.EXPERT ->
                distance <= 1 && scoreDelta <= 26
        }
    }
}
