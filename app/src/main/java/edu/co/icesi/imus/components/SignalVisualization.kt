package edu.co.icesi.imus.components

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import edu.co.icesi.imus.model.IMUData
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

@Composable
fun SignalVisualization(
    data: List<IMUData>,
    device: String,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Accelerometer Plot
        Text(
            text = "Accelerometer (g)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Y-axis labels for Accelerometer
            Column(
                modifier = Modifier.width(40.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(text = "2", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "0", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "-2", style = MaterialTheme.typography.bodySmall)
            }

            // Accelerometer Plot
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.White)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { canvasSize = it }
                ) {
                    drawPlotBackground()

                    val lastPoints = data.takeLast(100)
                    if (lastPoints.isNotEmpty()) {
                        drawAccelerometerData(lastPoints, size.height, device)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Gyroscope Plot
        Text(
            text = "Gyroscope (deg/s)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Y-axis labels for Gyroscope
            Column(
                modifier = Modifier.width(40.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(text = "250", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "0", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "-250", style = MaterialTheme.typography.bodySmall)
            }

            // Gyroscope Plot
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.White)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { canvasSize = it }
                ) {
                    drawPlotBackground()

                    val lastPoints = data.takeLast(100)
                    if (lastPoints.isNotEmpty()) {
                        drawGyroscopeData(lastPoints, size.height, device)
                    }
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendItem(color = Color.Red, text = "X-axis")
            Spacer(modifier = Modifier.width(16.dp))
            LegendItem(color = Color.Green, text = "Y-axis")
            Spacer(modifier = Modifier.width(16.dp))
            LegendItem(color = Color.Blue, text = "Z-axis")
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}

private fun DrawScope.drawPlotBackground() {
    // Draw grid
    val gridColor = Color.LightGray.copy(alpha = 0.5f)
    val verticalLines = 10
    val horizontalLines = 6

    val verticalStep = size.width / verticalLines
    val horizontalStep = size.height / horizontalLines

    // Vertical grid lines
    for (i in 0..verticalLines) {
        val x = i * verticalStep
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
    }

    // Horizontal grid lines
    for (i in 0..horizontalLines) {
        val y = i * horizontalStep
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
    }

    // Draw center line
    drawLine(
        color = gridColor.copy(alpha = 0.8f),
        start = Offset(0f, size.height / 2),
        end = Offset(size.width, size.height / 2),
        strokeWidth = 2f
    )
}

private fun DrawScope.drawAccelerometerData(data: List<IMUData>, height: Float, device: String) {
    val xPoints = mutableListOf<Offset>()
    val yPoints = mutableListOf<Offset>()
    val zPoints = mutableListOf<Offset>()

    val scaleX = size.width / (data.size - 1)
    val scaleY = height / 4  // Scale for ±2g range

    data.forEachIndexed { index, imuData ->
        if (imuData.deviceId == device){
            val x = index * scaleX
            val centerY = height / 2

            val ax = (centerY - (imuData.accelerometer.x * scaleY)).coerceIn(0f, height)
            val ay = (centerY - (imuData.accelerometer.y * scaleY)).coerceIn(0f, height)
            val az = (centerY - (imuData.accelerometer.z * scaleY)).coerceIn(0f, height)

            xPoints.add(Offset(x, ax))
            yPoints.add(Offset(x, ay))
            zPoints.add(Offset(x, az))
        }
    }

    // Draw paths
    drawLines(points = xPoints, color = Color.Red, strokeWidth = 2f)
    drawLines(points = yPoints, color = Color.Green, strokeWidth = 2f)
    drawLines(points = zPoints, color = Color.Blue, strokeWidth = 2f)
}

private fun DrawScope.drawGyroscopeData(data: List<IMUData>, height: Float, device: String) {
    val xPoints = mutableListOf<Offset>()
    val yPoints = mutableListOf<Offset>()
    val zPoints = mutableListOf<Offset>()

    val scaleX = size.width / (data.size - 1)
    val scaleY = height / 500  // Scale for ±250 deg/s range

    data.forEachIndexed { index, imuData ->
        if (imuData.deviceId == device){
            val x = index * scaleX
            val centerY = height / 2

            val gx = (centerY - (imuData.gyroscope.x * scaleY)).coerceIn(0f, height)
            val gy = (centerY - (imuData.gyroscope.y * scaleY)).coerceIn(0f, height)
            val gz = (centerY - (imuData.gyroscope.z * scaleY)).coerceIn(0f, height)

            xPoints.add(Offset(x, gx))
            yPoints.add(Offset(x, gy))
            zPoints.add(Offset(x, gz))
        }
    }

    // Draw paths
    drawLines(points = xPoints, color = Color.Red, strokeWidth = 2f)
    drawLines(points = yPoints, color = Color.Green, strokeWidth = 2f)
    drawLines(points = zPoints, color = Color.Blue, strokeWidth = 2f)
}

private fun DrawScope.drawLines(points: List<Offset>, color: Color, strokeWidth: Float) {
    for (i in 0 until points.size - 1) {
        drawLine(
            color = color,
            start = points[i],
            end = points[i + 1],
            strokeWidth = strokeWidth
        )
    }
}