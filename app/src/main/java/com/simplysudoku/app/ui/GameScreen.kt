package com.simplysudoku.app.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import com.simplysudoku.app.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onTitleClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeGameContent(
            uiState = uiState,
            viewModel = viewModel,
            onTitleClick = onTitleClick
        )
    } else {
        PortraitGameContent(
            uiState = uiState,
            viewModel = viewModel,
            onTitleClick = onTitleClick
        )
    }
}
