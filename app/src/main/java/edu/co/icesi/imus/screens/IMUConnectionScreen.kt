package edu.co.icesi.imus.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import edu.co.icesi.imus.R
import edu.co.icesi.imus.components.DeviceCard
import edu.co.icesi.imus.viewmodel.IMUConnectionViewModel

@Composable
fun IMUConnectionScreen(
    viewModel: IMUConnectionViewModel,
    onStartTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Conexión de IMUs",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(modifier = Modifier.height(300.dp)) {
            Image(
                painter = painterResource(id = R.drawable.human_body),
                contentDescription = "Cuerpo humano",
                modifier = Modifier.fillMaxSize()
            )

            uiState.deviceConnectionStatus.forEach { (device, isConnected) ->
                val position = when (device) {
                    "LEFT-HAND" -> Modifier.align(Alignment.TopStart).padding(start = 140.dp, top = 150.dp)
                    "RIGHT-HAND" -> Modifier.align(Alignment.TopEnd).padding(end = 140.dp, top = 150.dp)
                    "LEFT-ANKLE" -> Modifier.align(Alignment.BottomStart).padding(start = 170.dp, bottom = 10.dp)
                    "RIGHT-ANKLE" -> Modifier.align(Alignment.BottomEnd).padding(end = 170.dp, bottom = 10.dp)
                    "BASE-SPINE" -> Modifier.align(Alignment.Center).padding(bottom = 60.dp)
                    else -> Modifier
                }
                Box(
                    modifier = position
                        .size(20.dp)
                        .padding(2.dp)
                        .background(if (isConnected) Color.Green else Color.Red, shape = MaterialTheme.shapes.small)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = if (uiState.allDevicesConnected) "Todos los dispositivos conectados" else "Escaneando dispositivos...",
                style = MaterialTheme.typography.bodyLarge,
                color = if (uiState.allDevicesConnected) Color.Green else Color.Red
            )
            if (!uiState.allDevicesConnected) {
                CircularProgressIndicator(modifier = Modifier.padding(start = 8.dp))
            }
        }

        LazyColumn(
            modifier = Modifier
                .height(150.dp)
                .padding(vertical = 8.dp)
        ) {
            items(uiState.connectedDevices) { device ->
                DeviceCard(device = device)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onStartTest,
                enabled = uiState.allDevicesConnected,
                modifier = Modifier.weight(1f)
            ) {
                Text("Iniciar Prueba")
            }
            Button(
                onClick = { viewModel.scanForDevices() },
                enabled = !uiState.allDevicesConnected,
                modifier = Modifier.weight(1f)
            ) {
                Text("Reescanear")
            }
        }
    }
}