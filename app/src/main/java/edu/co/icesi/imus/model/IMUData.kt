package edu.co.icesi.imus.model

data class IMUData(
    val id: Long,
    val accelerometer: AccelerometerData,
    val gyroscope: GyroscopeData,
    val deviceId: String,
    val timestamp: Long
)

data class AccelerometerData(
    val x: Float,
    val y: Float,
    val z: Float
)

data class GyroscopeData(
    val x: Float,
    val y: Float,
    val z: Float
)