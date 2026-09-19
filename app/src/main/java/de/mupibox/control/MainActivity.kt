package de.mupibox.control

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mupibox.control.model.BoxEndpoint
import de.mupibox.control.ui.boxes.BoxesScreen
import de.mupibox.control.ui.boxes.BoxesViewModel
import de.mupibox.control.ui.control.BoxControlScreen
import de.mupibox.control.ui.control.BoxControlViewModel
import de.mupibox.control.ui.theme.MuPiBoxControlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as MuPiBoxControlApplication).container
        setContent {
            MuPiBoxControlTheme {
                MuPiBoxControlApp(container)
            }
        }
    }
}

@Composable
private fun MuPiBoxControlApp(container: AppContainer) {
    var selected by remember { mutableStateOf<BoxEndpoint?>(null) }

    if (selected == null) {
        val boxesVm: BoxesViewModel = viewModel {
            BoxesViewModel(container.boxRepository)
        }
        val boxes by boxesVm.boxes.collectAsStateWithLifecycle()
        val boxesState by boxesVm.uiState.collectAsStateWithLifecycle()
        val onlineStates by boxesVm.onlineStates.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) { boxesVm.refreshOnlineStates() }
        BoxesScreen(
            boxes = boxes,
            uiState = boxesState,
            onlineStates = onlineStates,
            onOpen = { selected = it },
            onAdd = boxesVm::add,
            onUpdate = boxesVm::update,
            onClearError = boxesVm::clearError,
        )
    } else {
        val box = selected!!
        val controlVm: BoxControlViewModel = viewModel(key = "control:${box.id}") {
            BoxControlViewModel(box, container.controlRepository)
        }
        val state by controlVm.uiState.collectAsStateWithLifecycle()
        DisposableEffect(controlVm) {
            controlVm.startPolling()
            onDispose { controlVm.stopPolling() }
        }
        BoxControlScreen(
            state = state,
            onBack = { selected = null },
            onPlayPause = controlVm::playPause,
            onPrevious = controlVm::previous,
            onNext = controlVm::next,
            onVolume = controlVm::setVolumePercent,
            onSpeak = controlVm::speak,
            onBluetoothRefresh = controlVm::refreshBluetooth,
        )
    }
}
