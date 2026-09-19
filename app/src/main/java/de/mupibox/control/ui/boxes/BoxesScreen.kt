package de.mupibox.control.ui.boxes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.ExperimentalMaterial3Api\nimport androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mupibox.control.model.BoxEndpoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxesScreen(
    boxes: List<BoxEndpoint>,
    uiState: BoxesUiState,
    onOpen: (BoxEndpoint) -> Unit,
    onAdd: (String, String, Int, () -> Unit) -> Unit,
    onClearError: () -> Unit,
) {
    var addOpen by remember { mutableStateOf(false) }

    fun openAdd() {
        onClearError()
        addOpen = true
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("MuPiBox Control") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = ::openAdd) {
                Icon(Icons.Default.Add, contentDescription = "Box hinzufügen")
            }
        },
    ) { padding ->
        if (boxes.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Default.Speaker, contentDescription = null)
                Spacer(Modifier.height(12.dp))
                Text("Noch keine MuPiBox gespeichert", style = MaterialTheme.typography.titleMedium)
                Text("Für den MVP kann eine Box per Hostname oder privater IP hinzugefügt werden.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = ::openAdd) { Text("Box hinzufügen") }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                items(boxes, key = { it.id }) { box ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onOpen(box) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(box.name, style = MaterialTheme.typography.titleMedium)
                                Text("${box.host}:${box.port}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Icon(Icons.Default.Speaker, contentDescription = null)
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (addOpen) {
        AddBoxDialog(
            uiState = uiState,
            onDismiss = {
                if (!uiState.busy) {
                    addOpen = false
                    onClearError()
                }
            },
            onAdd = { name, host, port ->
                onAdd(name, host, port) {
                    addOpen = false
                    onClearError()
                }
            },
        )
    }
}

@Composable
private fun AddBoxDialog(
    uiState: BoxesUiState,
    onDismiss: () -> Unit,
    onAdd: (String, String, Int) -> Unit,
) {
    var name by remember { mutableStateOf("MuPiBox") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("8090") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("MuPiBox hinzufügen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    enabled = !uiState.busy,
                    label = { Text("Name") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    enabled = !uiState.busy,
                    label = { Text("Hostname / IP") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it.filter(Char::isDigit) },
                    enabled = !uiState.busy,
                    label = { Text("Port") },
                    singleLine = true,
                )
                Text("Erlaubt sind lokale Ziele, z. B. 192.168.x.x, mupibox.local oder ein lokaler Hostname.")
                uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (uiState.busy) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator()
                        Text("Verbindung wird geprüft …")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !uiState.busy && host.isNotBlank() && port.toIntOrNull() in 1..65535,
                onClick = { onAdd(name, host, port.toInt()) },
            ) { Text("Hinzufügen") }
        },
        dismissButton = {
            TextButton(enabled = !uiState.busy, onClick = onDismiss) { Text("Abbrechen") }
        },
    )
}
