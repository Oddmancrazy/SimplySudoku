package com.example.simplysudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.simplysudoku.theme.SimplySudokuTheme
import com.example.simplysudoku.ui.GameScreen
import com.example.simplysudoku.ui.HomeScreen
import com.example.simplysudoku.ui.RecordsScreen
import com.example.simplysudoku.viewmodel.GameViewModel
import com.example.simplysudoku.viewmodel.RecordsViewModel
import com.example.simplysudoku.viewmodel.SettingsViewModel

private enum class AppScreen {
    GAME,
    HOME,
    RECORDS
}

class MainActivity : AppCompatActivity() {

    private val gameViewModel: GameViewModel by viewModels()
    private val recordsViewModel: RecordsViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SimplySudokuTheme {
                var currentScreen by rememberSaveable { mutableStateOf(AppScreen.GAME) }

                when (currentScreen) {
                    AppScreen.GAME -> {
                        GameScreen(
                            viewModel = gameViewModel,
                            onTitleClick = {
                                recordsViewModel.refresh()
                                settingsViewModel.refresh()
                                currentScreen = AppScreen.HOME
                            }
                        )
                    }

                    AppScreen.HOME -> {
                        HomeScreen(
                            recordsViewModel = recordsViewModel,
                            settingsViewModel = settingsViewModel,
                            onBackToGame = {
                                currentScreen = AppScreen.GAME
                            },
                            onOpenAllHistory = {
                                recordsViewModel.refresh()
                                currentScreen = AppScreen.RECORDS
                            }
                        )
                    }

                    AppScreen.RECORDS -> {
                        RecordsScreen(
                            viewModel = recordsViewModel,
                            onBackClick = {
                                settingsViewModel.refresh()
                                currentScreen = AppScreen.HOME
                            }
                        )
                    }
                }
            }
        }
    }
}