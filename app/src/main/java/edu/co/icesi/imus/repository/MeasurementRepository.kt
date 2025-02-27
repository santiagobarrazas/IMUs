package edu.co.icesi.imus.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import edu.co.icesi.imus.model.IMUData
import edu.co.icesi.imus.model.Measurement
import edu.co.icesi.imus.model.Patient
import edu.co.icesi.imus.model.TestType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class MeasurementRepository(context: Context) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val measurementsDir: File = File(context.filesDir, "measurements")

    init {

        if (!measurementsDir.exists()) {
            measurementsDir.mkdirs()
        }
    }

    suspend fun saveMeasurement(
        testType: TestType,
        patient: Patient,
        imuData: List<IMUData>
    ): String {
        return withContext(Dispatchers.IO) {
            val measurement = Measurement(
                testType = testType,
                patient = patient,
                imuData = imuData
            )

            val file = File(measurementsDir, "${measurement.id}.json")
            try {
                FileWriter(file).use { writer ->
                    gson.toJson(measurement, writer)
                }
                Log.d("IMU", "Data saved to JSON: ${file.absolutePath}")
                measurement.id
            } catch (e: Exception) {
                Log.e("IMU", "Error saving measurement to JSON", e)
                throw e
            }
        }
    }

    fun getAllMeasurements(): Flow<List<Measurement>> = flow {
        val measurements = mutableListOf<Measurement>()

        measurementsDir.listFiles()?.filter { it.extension == "json" }?.forEach { file ->
            try {
                FileReader(file).use { reader ->
                    val measurement = gson.fromJson(reader, Measurement::class.java)
                    measurements.add(measurement)
                }
            } catch (e: Exception) {
                Log.e("IMU", "Error reading measurement file: ${file.name}", e)
            }
        }

        measurements.sortByDescending { it.timestamp }
        emit(measurements)
    }.flowOn(Dispatchers.IO)

    suspend fun getMeasurementById(id: String): Measurement? {
        return withContext(Dispatchers.IO) {
            val file = File(measurementsDir, "$id.json")
            if (!file.exists()) return@withContext null

            try {
                FileReader(file).use { reader ->
                    gson.fromJson(reader, Measurement::class.java)
                }
            } catch (e: Exception) {
                Log.e("IMU", "Error reading measurement file: $id", e)
                null
            }
        }
    }
}