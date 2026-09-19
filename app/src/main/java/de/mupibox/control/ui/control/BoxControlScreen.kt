package de.mupibox.control.ui.control

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api\nimport androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxControlScreen(
    state: BoxControlUiState,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onVolume: (Int) -> Unit,
    onSpeak: (String) -> Unit,
    onBluetoothRefresh: () -> Unit,
) {
    var tts by remember { mutableStateOf("") }
    var slider by remember(state.volumePercent) { mutableFloatStateOf(state.volumePercent.toFloat()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.box.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val battery = state.system.battery
                AssistChip(
                    onClick = {},
                    label = { Text(if (battery.available) "${battery.percent ?: 0}%" else "Akku —") },
                    leadingIcon = {
                        Icon(
                            if (battery.charging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                            contentDescription = null,
                        )
                    },
                )
                val wifi = state.system.wifi
                AssistChip(
                    onClick = {},
                    label = { Text(if (wifi.connected) "WLAN ${wifi.qualityPercent ?: 0}%" else "WLAN —") },
                    leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                )
                AssistChip(
                    onClick = onBluetoothRefresh,
                    label = {
                        val connected = state.bluetoothDevices?.count { it.connected }
                        Text(if (connected == null) "Bluetooth ?" else "Bluetooth $connected")
                    },
                    leadingIcon = { Icon(Icons.Default.Bluetooth, contentDescription = null) },
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(state.title, style = MaterialTheme.typography.headlineSmall)
                    Text(state.subtitle, style = MaterialTheme.typography.bodyMedium)
                    Text(if (state.spotifyActive) "Spotify" else "Lokale Wiedergabe")
                    Spacer(Modifier.height(18.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(22.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onPrevious) { Icon(Icons.Default.SkipPrevious, "Zurück") }
                        IconButton(onClick = onPlayPause) {
                            Icon(if (state.playing) Icons.Default.Pause else Icons.Default.PlayArrow, "Play/Pause")
                        }
                        IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, "Weiter") }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null)
                        Text(" Lautstärke ${slider.roundToInt()} %")
                    }
                    Slider(
                        value = slider.coerceIn(0f, 100f),
                        onValueChange = { slider = it },
                        onValueChangeFinished = { onVolume(slider.roundToInt()) },
                        valueRange = 0f..100f,
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Text an die Box", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = tts,
                        onValueChange = { tts = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("TTS") },
                        minLines = 2,
                    )
                    Button(
                        onClick = { onSpeak(tts); tts = "" },
                        enabled = tts.isNotBlank() && !state.actionBusy,
                    ) { Text("Vorlesen") }
                }
            }

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
