package edu.co.icesi.imus.screens

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.co.icesi.imus.components.SignalVisualization
import edu.co.icesi.imus.viewmodel.DataCollectionViewModel

@Composable
fun DataCollectionScreen(
    viewModel: DataCollectionViewModel,
    onFinish: () -> Unit,
    onDeviceDisconnected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDisconnectDialog by remember { mutableStateOf(false) }

    if (!uiState.allDevicesConnected && uiState.isCollecting) {
        showDisconnectDialog = true
        viewModel.stopDataCollection()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Recolección de Datos",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (uiState.isCollecting) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .weight(1f)
            ) {
                items(uiState.connectedDevices) { device ->
                    Text(device.name)
                    SignalVisualization(
                        data = uiState.imuData,
                        device = device.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        } else {
            Text(
                text = "Listo para iniciar la recolección de datos",
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (!uiState.isCollecting && !uiState.isPaused) {
                Button(
                    onClick = { viewModel.startDataCollection() },
                    enabled = uiState.allDevicesConnected,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Iniciar")
                }
                Button(
                    onClick = onFinish,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
            } else if (uiState.isCollecting && !uiState.isPaused) {
                Button(
                    onClick = { viewModel.pauseDataCollection() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pausar")
                }
            } else if (uiState.isPaused) {
                Button(
                    onClick = { viewModel.resumeDataCollection() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reanudar")
                }
                Button(
                    onClick = onFinish,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = { viewModel.saveAndFinish(onFinish) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar")
                }
            }
        }

        if (showDisconnectDialog) {
            AlertDialog(
                onDismissRequest = { showDisconnectDialog = false },
                title = { Text("Dispositivo Desconectado") },
                text = { Text("Un dispositivo se ha desconectado. La prueba será descartada.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDisconnectDialog = false
                        onDeviceDisconnected()
                    }) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}