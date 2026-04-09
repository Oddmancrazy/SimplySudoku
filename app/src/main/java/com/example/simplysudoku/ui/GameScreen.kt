package com.example.simplysudoku.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import com.example.simplysudoku.viewmodel.GameViewModel

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeGameContent(
            uiState = uiState,
            viewModel = viewModel
        )
    } else {
        PortraitGameContent(
            uiState = uiState,
            viewModel = viewModel
        )
    }
}