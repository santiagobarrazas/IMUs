package edu.co.icesi.imus.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import edu.co.icesi.imus.model.IMUData
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

@Composable
fun SignalVisualization(
    data: List<IMUData>,
    modifier: Modifier = Modifier
) {
    val canvasSize = remember { mutableStateOf(IntSize.Zero) }

    Canvas(
        modifier = modifier
            .onSizeChanged { canvasSize.value = it }
    ) {
        val width = canvasSize.value.width.toFloat()
        val height = canvasSize.value.height.toFloat()

        // Draw grid
        drawGrid(width, height)

        // Draw signals
        data.takeLast(50).forEachIndexed { index, imuData ->
            if (index > 0) {
                val prevData = data[index - 1]
                drawSignalLine(
                    prevData,
                    imuData,
                    index - 1,
                    index,
                    width,
                    height
                )
            }
        }
    }
}

fun DrawScope.drawGrid(width: Float, height: Float) {
    val stepX = width / 10
    val stepY = height / 10
    val gridColor = Color.Gray.copy(alpha = 0.3f)

    for (i in 1 until 10) {
        val x = i * stepX
        drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(x, 0f), end = androidx.compose.ui.geometry.Offset(x, height))
    }

    for (i in 1 until 10) {
        val y = i * stepY
        drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(width, y))
    }
}

fun DrawScope.drawSignalLine(
    prevData: IMUData,
    imuData: IMUData,
    prevIndex: Int,
    index: Int,
    width: Float,
    height: Float
) {
    val scaleX = width / 50
    val scaleY = height / 4

    val prevX = prevIndex * scaleX
    val currX = index * scaleX

    val prevAccY = height / 2 - prevData.accelerometer.y * scaleY
    val currAccY = height / 2 - imuData.accelerometer.y * scaleY
    drawLine(
        color = Color.Blue,
        start = androidx.compose.ui.geometry.Offset(prevX, prevAccY),
        end = androidx.compose.ui.geometry.Offset(currX, currAccY),
        strokeWidth = 2f
    )

    val prevGyroY = height / 2 - prevData.gyroscope.y * scaleY
    val currGyroY = height / 2 - imuData.gyroscope.y * scaleY
    drawLine(
        color = Color.Red,
        start = androidx.compose.ui.geometry.Offset(prevX, prevGyroY),
        end = androidx.compose.ui.geometry.Offset(currX, currGyroY),
        strokeWidth = 2f
    )
}
