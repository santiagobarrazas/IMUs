package edu.co.icesi.imus.model

import java.util.UUID

data class Measurement(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val testType: TestType,
    val patient: Patient,
    val imuData: List<IMUData>
)