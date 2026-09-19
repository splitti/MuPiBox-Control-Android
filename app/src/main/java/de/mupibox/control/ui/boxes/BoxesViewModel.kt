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

    /** Per-box reachability, keyed by [BoxEndpoint.id]. Absent = not yet checked. */
    private val _onlineStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val onlineStates: StateFlow<Map<String, Boolean>> = _onlineStates.asStateFlow()

    private val _uiState = MutableStateFlow(BoxesUiState())
    val uiState: StateFlow<BoxesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            boxes.collect { refreshOnlineStates(it) }
        }
    }

    /** Re-probes every saved box's `/api/health`. Cheap and safe to call whenever the list is shown. */
    fun refreshOnlineStates() = refreshOnlineStates(boxes.value)

    private fun refreshOnlineStates(list: List<BoxEndpoint>) {
        list.forEach { box ->
            viewModelScope.launch {
                val reachable = runCatching { repository.probe(box) }.getOrNull()?.status == "ok"
                _onlineStates.value = _onlineStates.value + (box.id to reachable)
            }
        }
    }

    fun add(name: String, host: String, port: Int, onAdded: () -> Unit) {
        save(
            BoxEndpoint(name = name.trim().ifBlank { "MuPiBox" }, host = host.trim(), port = port),
            onAdded,
            "MuPiBox konnte nicht hinzugefügt werden.",
        )
    }

    /**
     * Updates an existing box in place. Preserving [id] is what makes this an update rather than
     * a duplicate: [BoxRepository.add] validates and health-probes the candidate exactly like a
     * new box, then [de.mupibox.control.data.local.BoxStore.upsert] replaces the entry with a
     * matching id instead of appending a new one.
     */
    fun update(id: String, name: String, host: String, port: Int, onUpdated: () -> Unit) {
        save(
            BoxEndpoint(id = id, name = name.trim().ifBlank { "MuPiBox" }, host = host.trim(), port = port),
            onUpdated,
            "MuPiBox konnte nicht aktualisiert werden.",
        )
    }

    private fun save(box: BoxEndpoint, onSaved: () -> Unit, failureMessage: String) {
        if (_uiState.value.busy) return
        viewModelScope.launch {
            _uiState.value = BoxesUiState(busy = true)
            runCatching { repository.add(box) }
                .onSuccess {
                    _uiState.value = BoxesUiState()
                    onSaved()
                }
                .onFailure {
                    _uiState.value = BoxesUiState(error = it.message ?: failureMessage)
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
