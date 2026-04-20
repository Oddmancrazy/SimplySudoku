package com.simplysudoku.app.model

import com.simplysudoku.app.R

enum class Difficulty(val nameRes: Int, val shortRes: Int) {
    VERY_EASY(R.string.difficulty_very_easy, R.string.diff_very_easy),
    EASY(R.string.difficulty_easy, R.string.diff_easy),
    MEDIUM(R.string.difficulty_medium, R.string.diff_medium),
    HARD(R.string.difficulty_hard, R.string.diff_hard),
    VERY_HARD(R.string.difficulty_very_hard, R.string.diff_very_hard),
    EXPERT(R.string.difficulty_expert, R.string.diff_expert)
}
