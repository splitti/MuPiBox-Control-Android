package de.mupibox.control.ui.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mupibox.control.data.api.ApiError
import de.mupibox.control.data.api.BluetoothDeviceDto
import de.mupibox.control.data.api.PlayerStatus
import de.mupibox.control.data.api.SpotifyStatus
import de.mupibox.control.data.api.SystemStatus
import de.mupibox.control.data.repository.ControlRepository
import de.mupibox.control.model.BoxEndpoint
import de.mupibox.control.model.PlaybackSource
import de.mupibox.control.model.PlaybackSourceSelector
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BoxControlUiState(
    val box: BoxEndpoint,
    val player: PlayerStatus = PlayerStatus(),
    val spotify: SpotifyStatus = SpotifyStatus(),
    val system: SystemStatus = SystemStatus(),
    val bluetoothDevices: List<BluetoothDeviceDto>? = null,
    val loading: Boolean = true,
    val actionBusy: Boolean = false,
    val error: String? = null,
    /** null = not yet confirmed either way. */
    val connected: Boolean? = null,
) {
    val spotifyActive: Boolean
        get() = PlaybackSourceSelector.active(player, spotify) == PlaybackSource.SPOTIFY

    val title: String
        get() = if (spotifyActive) spotify.track?.name.orEmpty()
        else player.currentTrack?.title ?: player.folder.ifBlank { "Bereit" }

    val subtitle: String
        get() = if (spotifyActive) spotify.track?.artists?.joinToString(", ").orEmpty()
        else if (player.folder.isNotBlank()) player.folder else box.name

    val playing: Boolean get() = if (spotifyActive) spotify.playing else player.isPlaying
    val volumePercent: Int
        get() = if (spotifyActive) {
            ((spotify.volume.toDouble() / spotify.volumeSteps.coerceAtLeast(1)) * 100).toInt().coerceIn(0, 100)
        } else {
            ((player.volume.toDouble() / player.maxVolume.coerceAtLeast(1)) * 100).toInt().coerceIn(0, 100)
        }
}

class BoxControlViewModel(
    private val box: BoxEndpoint,
    private val repository: ControlRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BoxControlUiState(box = box))
    val uiState: StateFlow<BoxControlUiState> = _uiState.asStateFlow()

    private var polling: Job? = null

    fun startPolling() {
        polling?.cancel()
        polling = viewModelScope.launch {
            var tick = 0
            while (true) {
                refreshPlayer()
                if (tick % 5 == 0) refreshSystem()
                tick++
                delay(1_000)
            }
        }
    }

    fun stopPolling() {
        polling?.cancel()
        polling = null
    }

    private suspend fun refreshPlayer() {
        runCatching {
            val player = repository.player(box)
            val spotify = runCatching { repository.spotify(box) }.getOrDefault(SpotifyStatus())
            player to spotify
        }.onSuccess { (player, spotify) ->
            _uiState.value = _uiState.value.copy(
                player = player,
                spotify = spotify,
                loading = false,
                error = null,
                connected = true,
            )
        }.onFailure {
            _uiState.value = _uiState.value.copy(
                loading = false,
                error = it.message ?: "Box nicht erreichbar",
                connected = false,
            )
        }
    }

    private suspend fun refreshSystem() {
        runCatching { repository.system(box) }.onSuccess { status ->
            _uiState.value = _uiState.value.copy(system = status)
        }
    }

    fun playPause() = action { state -> repository.playPause(box, state.player, state.spotify) }
    fun previous() = action { state -> repository.previous(box, state.player, state.spotify) }
    fun next() = action { state -> repository.next(box, state.player, state.spotify) }
    fun setVolumePercent(value: Int) = action { state -> repository.setVolumePercent(box, state.player, state.spotify, value) }
    fun speak(text: String) = action { repository.speak(box, text) }

    fun refreshBluetooth() = action {
        try {
            val response = repository.bluetoothOnDemand(box)
            _uiState.value = _uiState.value.copy(bluetoothDevices = response.devices)
        } catch (e: ApiError) {
            // Give the two documented Bluetooth failure codes (mupibox-api-current.md) their own
            // readable message instead of surfacing a raw "HTTP 401"/"HTTP 409" to the user.
            val message = when (e.code) {
                401 -> "Anmeldung erforderlich."
                409 -> "Bluetooth ist deaktiviert."
                else -> e.message
            }
            throw ApiError(e.code, message)
        }
    }

    private fun action(block: suspend (BoxControlUiState) -> Unit) {
        if (_uiState.value.actionBusy) return
        viewModelScope.launch {
            val before = _uiState.value
            _uiState.value = before.copy(actionBusy = true, error = null)
            runCatching { block(before) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(actionBusy = false)
                    refreshPlayer()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        actionBusy = false,
                        error = it.message ?: "Aktion fehlgeschlagen",
                    )
                }
        }
    }
}
