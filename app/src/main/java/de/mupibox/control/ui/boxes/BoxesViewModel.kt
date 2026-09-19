package de.mupibox.control.ui.boxes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mupibox.control.data.repository.BoxRepository
import de.mupibox.control.model.BoxEndpoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BoxesUiState(
    val busy: Boolean = false,
    val error: String? = null,
)

class BoxesViewModel(private val repository: BoxRepository) : ViewModel() {
    val boxes: StateFlow<List<BoxEndpoint>> = repository.savedBoxes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _uiState = MutableStateFlow(BoxesUiState())
    val uiState: StateFlow<BoxesUiState> = _uiState.asStateFlow()

    fun add(name: String, host: String, port: Int, onAdded: () -> Unit) {
        if (_uiState.value.busy) return
        viewModelScope.launch {
            _uiState.value = BoxesUiState(busy = true)
            runCatching {
                repository.add(
                    BoxEndpoint(
                        name = name.trim().ifBlank { "MuPiBox" },
                        host = host.trim(),
                        port = port,
                    )
                )
            }.onSuccess {
                _uiState.value = BoxesUiState()
                onAdded()
            }.onFailure {
                _uiState.value = BoxesUiState(error = it.message ?: "MuPiBox konnte nicht hinzugefügt werden.")
            }
        }
    }

    fun clearError() {
        if (!_uiState.value.busy) _uiState.value = _uiState.value.copy(error = null)
    }

    fun remove(id: String) {
        viewModelScope.launch { repository.remove(id) }
    }
}
