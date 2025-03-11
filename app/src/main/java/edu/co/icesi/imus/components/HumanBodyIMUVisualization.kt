package edu.co.icesi.imus.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import edu.co.icesi.imus.model.IMUDevice
import edu.co.icesi.imus.model.TestType

@Composable
fun HumanBodyIMUVisualization(
    testType: TestType,
    connectedDevices: List<String>,
    modifier: Modifier = Modifier
) {
    val requiredDevices = getRequiredDevicesForTest(testType)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.6f)
            .padding(8.dp)
    ) {
        val width = size.width
        val height = size.height

        drawRealisticHumanFigure(width, height)

        requiredDevices.forEach { device ->
            val position = getIMUPosition(device, width, height)
            val isConnected = connectedDevices.contains(device.bluetoothName)
            val color = if (isConnected) Color.Green else Color.Red

            drawCircle(
                color = color,
                radius = width * 0.025f,
                center = position
            )

            drawCircle(
                color = Color.Black,
                radius = width * 0.025f,
                center = position,
                style = Stroke(width = 2f)
            )

            val labelColor = if (isConnected) Color.DarkGray else Color.Gray
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = width * 0.015f,
                center = Offset(position.x, position.y)
            )
        }
    }
}

private fun getRequiredDevicesForTest(testType: TestType): List<IMUDevice> {
    return when (testType) {
        TestType.GAIT -> listOf(IMUDevice.LEFT_HAND, IMUDevice.RIGHT_HAND, IMUDevice.BASE_SPINE)
        TestType.TOPOLOGICAL_GAIT_ANALYSIS -> listOf(IMUDevice.LEFT_ANKLE, IMUDevice.RIGHT_ANKLE)
        TestType.DYNAMIC_GAIT_INDEX -> listOf(IMUDevice.LEFT_ANKLE, IMUDevice.RIGHT_ANKLE, IMUDevice.BASE_SPINE)
        TestType.TIMED_UP_AND_GO -> listOf(
            IMUDevice.LEFT_HAND,
            IMUDevice.RIGHT_HAND,
            IMUDevice.LEFT_ANKLE,
            IMUDevice.RIGHT_ANKLE,
            IMUDevice.BASE_SPINE
        )
        TestType.ONLY_RIGHT_HAND -> listOf(IMUDevice.RIGHT_HAND)
    }
}

private fun getIMUPosition(device: IMUDevice, width: Float, height: Float): Offset {
    val centerX = width / 2f

    return when (device) {
        IMUDevice.LEFT_HAND -> Offset(centerX - width * 0.28f, height * 0.32f)
        IMUDevice.RIGHT_HAND -> Offset(centerX + width * 0.28f, height * 0.32f)
        IMUDevice.LEFT_ANKLE -> Offset(centerX - width * 0.15f, height * 0.85f)
        IMUDevice.RIGHT_ANKLE -> Offset(centerX + width * 0.15f, height * 0.85f)
        IMUDevice.BASE_SPINE -> Offset(centerX, height * 0.45f)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRealisticHumanFigure(width: Float, height: Float) {
    val centerX = width / 2f
    val strokeWidth = width * 0.008f
    val bodyColor = Color(0xFF607D8B)

    val headRadius = width * 0.07f
    drawCircle(
        color = bodyColor,
        radius = headRadius,
        center = Offset(centerX, height * 0.09f),
    )

    drawLine(
        color = bodyColor,
        start = Offset(centerX, height * 0.09f + headRadius),
        end = Offset(centerX, height * 0.18f),
        strokeWidth = strokeWidth * 2f,
        cap = StrokeCap.Round
    )

    val shoulderY = height * 0.2f
    val shoulderLeft = centerX - width * 0.15f
    val shoulderRight = centerX + width * 0.15f

    drawLine(
        color = bodyColor,
        start = Offset(shoulderLeft, shoulderY),
        end = Offset(shoulderRight, shoulderY),
        strokeWidth = strokeWidth * 2.2f,
        cap = StrokeCap.Round
    )

    drawLine(
        color = bodyColor,
        start = Offset(shoulderLeft, shoulderY),
        end = Offset(centerX - width * 0.22f, height * 0.32f),
        strokeWidth = strokeWidth * 2f,
        cap = StrokeCap.Round
    )

    drawLine(
        color = bodyColor,
        start = Offset(shoulderRight, shoulderY),
        end = Offset(centerX + width * 0.22f, height * 0.32f),
        strokeWidth = strokeWidth * 2f,
        cap = StrokeCap.Round
    )

    drawLine(
        color = bodyColor,
        start = Offset(centerX - width * 0.22f, height * 0.32f),
        end = Offset(centerX - width * 0.28f, height * 0.42f),
        strokeWidth = strokeWidth * 1.8f,
        cap = StrokeCap.Round
    )

    drawLine(
        color = bodyColor,
        start = Offset(centerX + width * 0.22f, height * 0.32f),
        end = Offset(centerX + width * 0.28f, height * 0.42f),
        strokeWidth = strokeWidth * 1.8f,
        cap = StrokeCap.Round
    )

    drawCircle(
        color = bodyColor,
        radius = width * 0.02f,
        center = Offset(centerX - width * 0.28f, height * 0.42f)
    )

    drawCircle(
        color = bodyColor,
        radius = width * 0.02f,
        center = Offset(centerX + width * 0.28f, height * 0.42f)
    )

    val torsoPath = Path().apply {
        moveTo(shoulderLeft, shoulderY)
        lineTo(shoulderRight, shoulderY)
        lineTo(centerX + width * 0.13f, height * 0.5f)
        lineTo(centerX + width * 0.1f, height * 0.6f)
        lineTo(centerX - width * 0.1f, height * 0.6f)
        lineTo(centerX - width * 0.13f, height * 0.5f)
        close()
    }

    drawPath(
        path = torsoPath,
        color = bodyColor,
    )

    val hipY = height * 0.6f
    val hipLeft = centerX - width * 0.1f
    val hipRight = centerX + width * 0.1f

    drawLine(
        color = bodyColor,
        start = Offset(hipLeft, hipY),
        end = Offset(centerX - width * 0.13f, height * 0.75f),
        strokeWidth = strokeWidth * 2.5f,
        cap = StrokeCap.Round
    )

    drawLine(
        color = bodyColor,
        start = Offset(hipRight, hipY),
        end = Offset(centerX + width * 0.13f, height * 0.75f),
        strokeWidth = strokeWidth * 2.5f,
        cap = StrokeCap.Round
    )

    drawLine(
        color = bodyColor,
        start = Offset(centerX - width * 0.13f, height * 0.75f),
        end = Offset(centerX - width * 0.15f, height * 0.93f),
        strokeWidth = strokeWidth * 2.2f,
        cap = StrokeCap.Round
    )

    drawLine(
        color = bodyColor,
        start = Offset(centerX + width * 0.13f, height * 0.75f),
        end = Offset(centerX + width * 0.15f, height * 0.93f),
        strokeWidth = strokeWidth * 2.2f,
        cap = StrokeCap.Round
    )

    val footPath1 = Path().apply {
        moveTo(centerX - width * 0.15f, height * 0.93f)
        lineTo(centerX - width * 0.22f, height * 0.93f)
        lineTo(centerX - width * 0.22f, height * 0.96f)
        lineTo(centerX - width * 0.15f, height * 0.96f)
        close()
    }

    val footPath2 = Path().apply {
        moveTo(centerX + width * 0.15f, height * 0.93f)
        lineTo(centerX + width * 0.22f, height * 0.93f)
        lineTo(centerX + width * 0.22f, height * 0.96f)
        lineTo(centerX + width * 0.15f, height * 0.96f)
        close()
    }

    drawPath(
        path = footPath1,
        color = bodyColor,
    )

    drawPath(
        path = footPath2,
        color = bodyColor,
    )
}