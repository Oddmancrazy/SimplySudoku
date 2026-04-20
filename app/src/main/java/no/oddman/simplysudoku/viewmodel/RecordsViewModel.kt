package no.oddman.simplysudoku.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import no.oddman.simplysudoku.data.repository.RecordRepository
import no.oddman.simplysudoku.model.RecordsOverview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RecordsUiState(
    val isLoading: Boolean = true,
    val overview: RecordsOverview? = null
)

class RecordsViewModel(application: Application) : AndroidViewModel(application) {

    private val recordRepository = RecordRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = RecordsUiState(isLoading = true)
            val overview = recordRepository.buildOverview()
            _uiState.value = RecordsUiState(
                isLoading = false,
                overview = overview
            )
        }
    }

    fun deleteAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            recordRepository.deleteAllHistory()
            val overview = recordRepository.buildOverview()
            _uiState.value = RecordsUiState(
                isLoading = false,
                overview = overview
            )
        }
    }
}

