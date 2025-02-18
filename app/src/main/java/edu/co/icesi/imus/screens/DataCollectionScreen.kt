package edu.co.icesi.imus.screens

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.co.icesi.imus.components.DeviceCard
import edu.co.icesi.imus.components.SignalVisualization
import edu.co.icesi.imus.viewmodel.DataCollectionViewModel

@SuppressLint("MissingPermission")
@Composable
fun DataCollectionScreen(
    viewModel: DataCollectionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Connected Devices",
            style = MaterialTheme.typography.headlineSmall
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            items(uiState.connectedDevices) { device ->
                DeviceCard(device = device)
            }
        }

        LazyColumn (
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            items(uiState.connectedDevices) { device ->
                Text(device.name)
                SignalVisualization(
                    data = uiState.imuData,
                    device = device.name,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { viewModel.startDataCollection() },
                enabled = !uiState.isCollecting
            ) {
                Text("Start")
            }

            Button(
                onClick = { viewModel.stopDataCollection() },
                enabled = uiState.isCollecting
            ) {
                Text("Stop")
            }
        }
    }
}