package com.example.simplysudoku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.simplysudoku.model.GameMode
import com.example.simplysudoku.model.SudokuCell

@Composable
fun SudokuBoard(
    board: List<SudokuCell>,
    gameMode: GameMode,
    selectedNumber: Int?,
    completedRows: Set<Int>,
    completedColumns: Set<Int>,
    completedBoxes: Set<Int>,
    onCellClick: (Int, Int) -> Unit
) {
    val selectedCell = board.firstOrNull { it.isSelected }
    val selectedRow = selectedCell?.row
    val selectedCol = selectedCell?.col

    val boardByPosition = board.associateBy { cell -> cell.row to cell.col }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            for (row in 0..8) {
                Row(
                    modifier = Modifier.weight(1f)
                ) {
                    for (col in 0..8) {
                        val cell = boardByPosition[row to col] ?: continue

                        SudokuCellView(
                            cell = cell,
                            gameMode = gameMode,
                            selectedRow = selectedRow,
                            selectedCol = selectedCol,
                            selectedNumber = selectedNumber,
                            completedRows = completedRows,
                            completedColumns = completedColumns,
                            completedBoxes = completedBoxes,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            onClick = { onCellClick(cell.row, cell.col) }
                        )
                    }
                }
            }
        }

        BoardGridOverlay()
    }
}

@Composable
private fun SudokuCellView(
    cell: SudokuCell,
    gameMode: GameMode,
    selectedRow: Int?,
    selectedCol: Int?,
    selectedNumber: Int?,
    completedRows: Set<Int>,
    completedColumns: Set<Int>,
    completedBoxes: Set<Int>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = cellBackgroundColor(
        cell = cell,
        gameMode = gameMode,
        selectedRow = selectedRow,
        selectedCol = selectedCol,
        selectedNumber = selectedNumber,
        completedRows = completedRows,
        completedColumns = completedColumns,
        completedBoxes = completedBoxes
    )

    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = cell.value?.toString() ?: "",
            color = if (cell.isFixed) Color(0xFF5F6368) else Color(0xFF111111),
            fontWeight = if (cell.isFixed) FontWeight.Bold else FontWeight.ExtraBold,
            fontSize = if (cell.isFixed) 20.sp else 19.sp
        )
    }
}

@Composable
private fun BoardGridOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cellWidth = size.width / 9f
        val cellHeight = size.height / 9f

        for (i in 0..9) {
            val stroke = if (i % 3 == 0) 6f else 2f

            drawLine(
                color = Color.Black,
                start = Offset(i * cellWidth, 0f),
                end = Offset(i * cellWidth, size.height),
                strokeWidth = stroke
            )

            drawLine(
                color = Color.Black,
                start = Offset(0f, i * cellHeight),
                end = Offset(size.width, i * cellHeight),
                strokeWidth = stroke
            )
        }
    }
}

private fun cellBackgroundColor(
    cell: SudokuCell,
    gameMode: GameMode,
    selectedRow: Int?,
    selectedCol: Int?,
    selectedNumber: Int?,
    completedRows: Set<Int>,
    completedColumns: Set<Int>,
    completedBoxes: Set<Int>
): Color {
    val isModernMode = gameMode == GameMode.MODERN

    val isInSelectedRow = selectedRow != null && cell.row == selectedRow
    val isInSelectedCol = selectedCol != null && cell.col == selectedCol

    val isInSelectedBox =
        selectedRow != null && selectedCol != null &&
                cell.row / 3 == selectedRow / 3 &&
                cell.col / 3 == selectedCol / 3

    val boxIndex = (cell.row / 3) * 3 + (cell.col / 3)

    val isInCompletedArea =
        cell.row in completedRows ||
                cell.col in completedColumns ||
                boxIndex in completedBoxes

    val isSameNumber =
        isModernMode &&
                selectedNumber != null &&
                cell.value != null &&
                cell.value == selectedNumber

    return when {
        isModernMode && cell.hasError -> Color(0xFFFFCDD2)
        cell.isSelected -> Color(0xFF90CAF9)
        isSameNumber -> Color(0xFFBBDEFB)
        isModernMode && (isInSelectedRow || isInSelectedCol || isInSelectedBox) -> Color(0xFFEAF4FF)
        isInCompletedArea -> Color(0xFFF1F8F1)
        else -> Color.White
    }
}
