package edu.co.icesi.imus.model

data class Patient(
    val id: String = "",
    val cedula: String = "",
    val nombre: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class TestSession(
    val id: String = System.currentTimeMillis().toString(),
    val patientId: String,
    val patientName: String,
    val testType: TestType,
    val startTimestamp: Long = System.currentTimeMillis(),
    val endTimestamp: Long? = null,
    val imuData: List<IMUData> = emptyList()
)