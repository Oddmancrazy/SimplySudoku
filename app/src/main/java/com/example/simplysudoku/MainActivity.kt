package com.example.simplysudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.simplysudoku.theme.SimplySudokuTheme
import com.example.simplysudoku.ui.GameScreen
import com.example.simplysudoku.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimplySudokuTheme {
                GameScreen(viewModel = gameViewModel)
            }
        }
    }
}