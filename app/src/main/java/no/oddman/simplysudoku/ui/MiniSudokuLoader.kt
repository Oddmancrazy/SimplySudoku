package no.oddman.simplysudoku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun MiniSudokuLoader() {
    var visibleCount by remember { mutableIntStateOf(0) }
    val numbers = (1..9).toList()

    LaunchedEffect(Unit) {
        while (true) {
            for (count in 0..9) {
                visibleCount = count
                delay(90)
            }
            delay(180)
            visibleCount = 0
            delay(120)
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 8.dp,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .size(width = 152.dp, height = 182.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.size(96.dp),
                    userScrollEnabled = false
                ) {
                    itemsIndexed(numbers) { index, number ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .background(
                                    if (index < visibleCount) Color(0xFFEAF6EA) else Color.White
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (index < visibleCount) number.toString() else "",
                                color = Color(0xFF1A1A1A),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Canvas(modifier = Modifier.size(96.dp)) {
                    val cellSize = size.width / 3f

                    for (i in 0..3) {
                        drawLine(
                            color = Color.Black,
                            start = Offset(i * cellSize, 0f),
                            end = Offset(i * cellSize, size.height),
                            strokeWidth = 2.5f
                        )
                        drawLine(
                            color = Color.Black,
                            start = Offset(0f, i * cellSize),
                            end = Offset(size.width, i * cellSize),
                            strokeWidth = 2.5f
                        )
                    }
                }
            }

            Text(
                text = "Lager brett...",
                fontWeight = FontWeight.Medium
            )
        }
    }
}

