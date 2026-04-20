package com.simplysudoku.app.model

data class SudokuCell(
    val row: Int,
    val col: Int,
    val value: Int?,
    val isFixed: Boolean,
    val isSelected: Boolean = false,
    val hasError: Boolean = false
)
