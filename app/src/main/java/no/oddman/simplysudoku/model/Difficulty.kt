package no.oddman.simplysudoku.model

import no.oddman.simplysudoku.R

enum class Difficulty(val nameRes: Int) {
    VERY_EASY(R.string.difficulty_very_easy),
    EASY(R.string.difficulty_easy),
    MEDIUM(R.string.difficulty_medium),
    HARD(R.string.difficulty_hard),
    VERY_HARD(R.string.difficulty_very_hard),
    EXPERT(R.string.difficulty_expert)
}

