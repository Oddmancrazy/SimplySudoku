package com.example.simplysudoku.logic

import com.example.simplysudoku.model.Difficulty

class SudokuDifficultyEvaluator(
    private val analyzer: SudokuAnalyzer = SudokuAnalyzer()
) {

    data class EvaluationResult(
        val difficulty: Difficulty,
        val analysis: SudokuAnalysis,
        val score: Int
    )

    fun evaluate(puzzle: Array<IntArray>): EvaluationResult {
        val analysis = analyzer.analyze(puzzle)
        val clueCount = puzzle.sumOf { row -> row.count { it != 0 } }
        val difficulty = classify(analysis, clueCount)
        val score = calculateScore(analysis, clueCount)

        return EvaluationResult(
            difficulty = difficulty,
            analysis = analysis,
            score = score
        )
    }

    private fun classify(
        analysis: SudokuAnalysis,
        clueCount: Int
    ): Difficulty {
        return when {
            analysis.solved &&
                    analysis.hiddenSinglesUsed == 0 &&
                    analysis.nakedSinglesUsed >= 40 &&
                    clueCount >= 40 ->
                Difficulty.VERY_EASY

            analysis.solved &&
                    analysis.hiddenSinglesUsed <= 4 &&
                    analysis.nakedSinglesUsed >= 28 &&
                    clueCount >= 34 ->
                Difficulty.EASY

            analysis.solved &&
                    analysis.hiddenSinglesUsed <= 12 ->
                Difficulty.MEDIUM

            analysis.solved ->
                Difficulty.HARD

            analysis.unresolvedCells <= 8 &&
                    clueCount <= 28 ->
                Difficulty.VERY_HARD

            analysis.unresolvedCells >= 9 &&
                    clueCount <= 25 &&
                    analysis.nakedSinglesUsed <= 12 ->
                Difficulty.EXPERT

            else ->
                Difficulty.VERY_HARD
        }
    }

    private fun calculateScore(
        analysis: SudokuAnalysis,
        clueCount: Int
    ): Int {
        var score = 0

        score += analysis.hiddenSinglesUsed * 5
        score += analysis.unresolvedCells * 6
        score += (30 - clueCount).coerceAtLeast(0) * 2

        if (!analysis.solved) {
            score += 20
        }

        score -= analysis.nakedSinglesUsed.coerceAtMost(30)

        return score.coerceAtLeast(0)
    }

    fun difficultyDistance(a: Difficulty, b: Difficulty): Int {
        return difficultyRank(a) - difficultyRank(b)
    }

    fun absoluteDifficultyDistance(a: Difficulty, b: Difficulty): Int {
        return kotlin.math.abs(difficultyDistance(a, b))
    }

    fun difficultyRank(difficulty: Difficulty): Int {
        return when (difficulty) {
            Difficulty.VERY_EASY -> 0
            Difficulty.EASY -> 1
            Difficulty.MEDIUM -> 2
            Difficulty.HARD -> 3
            Difficulty.VERY_HARD -> 4
            Difficulty.EXPERT -> 5
        }
    }
}