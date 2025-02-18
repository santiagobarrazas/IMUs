package edu.co.icesi.imus.components

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DeviceCard(device: BluetoothDevice) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = device.name ?: "Unknown Device", style = MaterialTheme.typography.bodyLarge)
                Text(text = device.address, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}