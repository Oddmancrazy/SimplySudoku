package no.oddman.simplysudoku.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PanelOuter = Color(0xFF8C5624)
private val PanelMid = Color(0xFFB6702E)
private val PanelLight = Color(0xFFE7B36E)
private val PanelInner = Color(0xFFF8F1E6)

private val KeyWoodDark = Color(0xFF8A4C17)
private val KeyWoodMid = Color(0xFFC97926)
private val KeyWoodLight = Color(0xFFE9A44A)

private val KeyWoodSelectedTop = Color(0xFFF3C777)
private val KeyWoodSelectedBottom = Color(0xFFD8892F)

private val KeyWoodCompletedTop = Color(0xFFFBF7F0)
private val KeyWoodCompletedBottom = Color(0xFFE8DCCB)

private val KeyText = Color(0xFF3B210E)
private val KeyTextCompleted = Color(0xFF7B685A)

@Composable
fun NumberPad(
    completedNumbers: Set<Int>,
    selectedNumber: Int?,
    onNumberClick: (Int) -> Unit,
    onEraseClick: () -> Unit,
    panelWidth: Dp = 286.dp,
    compact: Boolean = false,
    isTablet: Boolean = false
) {
    val spacing = when {
        isTablet -> 10.dp
        compact -> 5.dp
        else -> 6.dp
    }
    val keyWidth = when {
        isTablet -> 80.dp
        compact -> 52.dp
        else -> 56.dp
    }
    val keyHeight = when {
        isTablet -> 68.dp
        compact -> 46.dp
        else -> 48.dp
    }
    val eraseWidth = when {
        isTablet -> 84.dp
        compact -> 56.dp
        else -> 60.dp
    }

    WoodPanel(
        modifier = Modifier.width(panelWidth)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NumberRow(
                    numbers = listOf(1, 2, 3),
                    completedNumbers = completedNumbers,
                    selectedNumber = selectedNumber,
                    onNumberClick = onNumberClick,
                    spacing = spacing,
                    keyWidth = keyWidth,
                    keyHeight = keyHeight
                )
                NumberRow(
                    numbers = listOf(4, 5, 6),
                    completedNumbers = completedNumbers,
                    selectedNumber = selectedNumber,
                    onNumberClick = onNumberClick,
                    spacing = spacing,
                    keyWidth = keyWidth,
                    keyHeight = keyHeight
                )
                NumberRow(
                    numbers = listOf(7, 8, 9),
                    completedNumbers = completedNumbers,
                    selectedNumber = selectedNumber,
                    onNumberClick = onNumberClick,
                    spacing = spacing,
                    keyWidth = keyWidth,
                    keyHeight = keyHeight
                )
            }

            Spacer(modifier = Modifier.width(spacing))

            EraseKey(
                onClick = onEraseClick,
                width = eraseWidth,
                height = (keyHeight * 3) + (spacing * 2)
            )
        }
    }
}

@Composable
fun LandscapeNumberPad(
    completedNumbers: Set<Int>,
    selectedNumber: Int?,
    onNumberClick: (Int) -> Unit,
    onEraseClick: () -> Unit,
    isTablet: Boolean = false
) {
    val leftColumn = listOf(1, 3, 5, 7, 9)
    val rightColumn = listOf(2, 4, 6, 8)
    
    val keySize = if (isTablet) 72.dp else 56.dp
    val spacing = if (isTablet) 12.dp else 8.dp
    val panelWidth = if (isTablet) 190.dp else 154.dp

    WoodPanel(
        modifier = Modifier.width(panelWidth)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                leftColumn.forEach { number ->
                    SquareNumberKey(
                        number = number,
                        isSelected = selectedNumber == number,
                        isCompleted = completedNumbers.contains(number),
                        onClick = { onNumberClick(number) },
                        size = keySize
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(spacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                rightColumn.forEach { number ->
                    SquareNumberKey(
                        number = number,
                        isSelected = selectedNumber == number,
                        isCompleted = completedNumbers.contains(number),
                        onClick = { onNumberClick(number) },
                        size = keySize
                    )
                }

                SquareEraseKey(onClick = onEraseClick, size = keySize)
            }
        }
    }
}

@Composable
private fun NumberRow(
    numbers: List<Int>,
    completedNumbers: Set<Int>,
    selectedNumber: Int?,
    onNumberClick: (Int) -> Unit,
    spacing: Dp,
    keyWidth: Dp,
    keyHeight: Dp
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        numbers.forEach { number ->
            NumberKey(
                number = number,
                isSelected = selectedNumber == number,
                isCompleted = completedNumbers.contains(number),
                onClick = { onNumberClick(number) },
                width = keyWidth,
                height = keyHeight
            )
        }
    }
}

@Composable
private fun NumberKey(
    number: Int,
    isSelected: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit,
    width: Dp,
    height: Dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topColor = when {
        isPressed -> KeyWoodSelectedTop
        isSelected -> KeyWoodSelectedTop
        isCompleted -> KeyWoodCompletedTop
        else -> KeyWoodLight
    }

    val bottomColor = when {
        isPressed -> KeyWoodSelectedBottom
        isSelected -> KeyWoodSelectedBottom
        isCompleted -> KeyWoodCompletedBottom
        else -> KeyWoodMid
    }

    Box(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(16.dp), clip = false)
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(KeyWoodDark)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(bottom = if (isPressed) 2.dp else 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .height(14.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(
                                alpha = when {
                                    isPressed || isSelected -> 0.28f
                                    isCompleted -> 0.32f
                                    else -> 0.20f
                                }
                            ),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = if (isCompleted) KeyTextCompleted else KeyText,
                fontSize = 18.sp,
                fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.ExtraBold,
                fontFamily = FontFamily.Serif
            )
        }
    }
}

@Composable
private fun SquareNumberKey(
    number: Int,
    isSelected: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit,
    size: Dp = 56.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topColor = when {
        isPressed -> KeyWoodSelectedTop
        isSelected -> KeyWoodSelectedTop
        isCompleted -> KeyWoodCompletedTop
        else -> KeyWoodLight
    }

    val bottomColor = when {
        isPressed -> KeyWoodSelectedBottom
        isSelected -> KeyWoodSelectedBottom
        isCompleted -> KeyWoodCompletedBottom
        else -> KeyWoodMid
    }

    Box(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(16.dp), clip = false)
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(KeyWoodDark)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(bottom = if (isPressed) 2.dp else 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .height(if (size > 60.dp) 18.dp else 14.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(
                                alpha = when {
                                    isPressed || isSelected -> 0.28f
                                    isCompleted -> 0.32f
                                    else -> 0.20f
                                }
                            ),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = if (isCompleted) KeyTextCompleted else KeyText,
                fontSize = if (size > 60.dp) 26.sp else 20.sp,
                fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.ExtraBold,
                fontFamily = FontFamily.Serif
            )
        }
    }
}

@Composable
private fun EraseKey(
    onClick: () -> Unit,
    width: Dp,
    height: Dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(20.dp), clip = false)
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(20.dp))
            .background(KeyWoodDark)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(bottom = if (isPressed) 2.dp else 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (isPressed) KeyWoodSelectedTop else KeyWoodLight,
                            if (isPressed) KeyWoodSelectedBottom else KeyWoodMid
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .height(18.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (isPressed) 0.28f else 0.20f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⌫",
                color = KeyText,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun SquareEraseKey(
    onClick: () -> Unit,
    size: Dp = 56.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(16.dp), clip = false)
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(KeyWoodDark)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(bottom = if (isPressed) 2.dp else 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (isPressed) KeyWoodSelectedTop else KeyWoodLight,
                            if (isPressed) KeyWoodSelectedBottom else KeyWoodMid
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .height(if (size > 60.dp) 18.dp else 14.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (isPressed) 0.28f else 0.20f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⌫",
                color = KeyText,
                fontSize = if (size > 60.dp) 30.sp else 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun WoodPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(30.dp), clip = false)
            .clip(RoundedCornerShape(30.dp))
            .background(PanelOuter)
            .padding(bottom = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(PanelLight, PanelMid)
                    )
                )
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(26.dp))
                    .background(PanelInner)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                content()
            }
        }
    }
}

