package com.simplysudoku.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.simplysudoku.app.theme.SimplySudokuTheme
import com.simplysudoku.app.ui.GameScreen
import com.simplysudoku.app.ui.HomeScreen
import com.simplysudoku.app.ui.LanguageSelectionDialog
import com.simplysudoku.app.ui.RecordsScreen
import com.simplysudoku.app.viewmodel.GameViewModel
import com.simplysudoku.app.viewmodel.RecordsViewModel
import com.simplysudoku.app.viewmodel.SettingsViewModel
import com.simplysudoku.app.data.repository.SettingsRepository

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

        // Aktiver Immersive Mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            SimplySudokuTheme {
                var currentScreen by rememberSaveable { mutableStateOf(AppScreen.GAME) }
                val uiState by gameViewModel.uiState.collectAsState()
                val lifecycleOwner = LocalLifecycleOwner.current
                
                // Sjekk for første kjøring (språkvalg)
                val settingsRepo = remember { SettingsRepository(applicationContext) }
                var showLanguagePicker by rememberSaveable { mutableStateOf(!settingsRepo.hasSelectedLanguage()) }

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_PAUSE) {
                            if (currentScreen == AppScreen.GAME) {
                                gameViewModel.pauseGame()
                            } else {
                                // Hvis man er på HomeScreen eller Records, avbrytes spillet helt ved app-pause
                                gameViewModel.cancelGame()
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                when (currentScreen) {
                    AppScreen.GAME -> {
                        GameScreen(
                            viewModel = gameViewModel,
                            onTitleClick = {
                                recordsViewModel.refresh()
                                settingsViewModel.refresh()
                                gameViewModel.pauseGame() // Pause spillet når man går til Home
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

                if (showLanguagePicker) {
                    LanguageSelectionDialog(
                        onLanguageSelected = { language ->
                            settingsViewModel.setLanguage(language)
                            showLanguagePicker = false
                        }
                    )
                }
            }
        }
    }
}
