package com.example.simplysudoku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    Box {
        LazyVerticalGrid(
            columns = GridCells.Fixed(9),
            modifier = Modifier.size(360.dp)
        ) {
            items(board) { cell ->
                SudokuCellView(
                    cell = cell,
                    gameMode = gameMode,
                    selectedRow = selectedRow,
                    selectedCol = selectedCol,
                    selectedNumber = selectedNumber,
                    completedRows = completedRows,
                    completedColumns = completedColumns,
                    completedBoxes = completedBoxes,
                    onClick = { onCellClick(cell.row, cell.col) }
                )
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
        modifier = Modifier
            .aspectRatio(1f)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = cell.value?.toString() ?: "",
            color = Color(0xFF111111),
            fontWeight = if (cell.isFixed) FontWeight.ExtraBold else FontWeight.Bold
        )
    }
}

@Composable
private fun BoardGridOverlay() {
    Canvas(modifier = Modifier.size(360.dp)) {
        val cellSize = size.width / 9f

        for (i in 0..9) {
            val stroke = if (i % 3 == 0) 6f else 2f

            drawLine(
                color = Color.Black,
                start = Offset(i * cellSize, 0f),
                end = Offset(i * cellSize, size.height),
                strokeWidth = stroke
            )

            drawLine(
                color = Color.Black,
                start = Offset(0f, i * cellSize),
                end = Offset(size.width, i * cellSize),
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

    val isInCompletedRow = completedRows.contains(cell.row)
    val isInCompletedColumn = completedColumns.contains(cell.col)
    val isInCompletedBox = completedBoxes.contains(boxIndex)

    val isSameNumber =
        isModernMode &&
                selectedNumber != null &&
                cell.value != null &&
                cell.value == selectedNumber

    val baseBackground = when {
        cell.isFixed -> Color(0xFFE0E0E0)
        cell.value != null -> Color(0xFFF1F1F1)
        else -> Color.White
    }

    val completedTint = isInCompletedRow || isInCompletedColumn || isInCompletedBox
    val completedBackground = when {
        completedTint && cell.isFixed -> Color(0xFFE6F3E6)
        completedTint && cell.value != null -> Color(0xFFEDF7ED)
        completedTint -> Color(0xFFF6FBF6)
        else -> baseBackground
    }

    return when {
        isModernMode && cell.hasError -> Color(0xFFFFCDD2)
        cell.isSelected -> Color(0xFF90CAF9)
        isSameNumber -> Color(0xFFBBDEFB)
        completedTint -> completedBackground
        isModernMode && (isInSelectedRow || isInSelectedCol || isInSelectedBox) ->
            Color(0xFFF3F8FF)
        else -> baseBackground
    }
}