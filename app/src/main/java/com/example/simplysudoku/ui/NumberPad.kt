package com.example.simplysudoku.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.simplysudoku.model.GameMode

@Composable
fun NumberPad(
    completedNumbers: Set<Int>,
    gameMode: GameMode,
    selectedNumber: Int?,
    onNumberClick: (Int) -> Unit,
    onEraseClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NumberRow(
            numbers = listOf(1, 2, 3),
            completedNumbers = completedNumbers,
            gameMode = gameMode,
            selectedNumber = selectedNumber,
            onNumberClick = onNumberClick
        )

        NumberRow(
            numbers = listOf(4, 5, 6),
            completedNumbers = completedNumbers,
            gameMode = gameMode,
            selectedNumber = selectedNumber,
            onNumberClick = onNumberClick
        )

        NumberRow(
            numbers = listOf(7, 8, 9),
            completedNumbers = completedNumbers,
            gameMode = gameMode,
            selectedNumber = selectedNumber,
            onNumberClick = onNumberClick
        )

        Button(
            onClick = onEraseClick,
            modifier = Modifier
                .width(180.dp)
                .height(44.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Slett")
        }
    }
}

@Composable
private fun NumberRow(
    numbers: List<Int>,
    completedNumbers: Set<Int>,
    gameMode: GameMode,
    selectedNumber: Int?,
    onNumberClick: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        numbers.forEach { number ->
            NumberButton(
                number = number,
                completedNumbers = completedNumbers,
                gameMode = gameMode,
                selectedNumber = selectedNumber,
                onNumberClick = onNumberClick
            )
        }
    }
}

@Composable
private fun NumberButton(
    number: Int,
    completedNumbers: Set<Int>,
    gameMode: GameMode,
    selectedNumber: Int?,
    onNumberClick: (Int) -> Unit
) {
    val isSelected = selectedNumber == number
    val isComplete = completedNumbers.contains(number)
    val isModernMode = gameMode == GameMode.MODERN

    val buttonColors = when {
        isModernMode && isComplete -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFFC8E6C9)
        )
        isModernMode && isSelected -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFFBBDEFB)
        )
        else -> ButtonDefaults.buttonColors()
    }

    Button(
        onClick = { onNumberClick(number) },
        modifier = Modifier
            .width(64.dp)
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = buttonColors
    ) {
        Text(number.toString())
    }
}