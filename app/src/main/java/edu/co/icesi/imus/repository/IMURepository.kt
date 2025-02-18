package edu.co.icesi.imus.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.util.Log
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import edu.co.icesi.imus.bluetooth.BLEManager
import edu.co.icesi.imus.model.IMUData
import edu.co.icesi.imus.model.IMUDevice
import edu.co.icesi.imus.model.TestType
import kotlinx.coroutines.delay
import java.io.File

class IMURepository(
    private val bleManager: BLEManager,
    private val dataStore: DataStore<Preferences>
) {
    val connectedDevices: StateFlow<List<BluetoothDevice>> = bleManager.connectedDevices
    val imuData: StateFlow<List<IMUData>> = bleManager.imuData

    private var targetDeviceNames: List<String> = emptyList()
    private var canStartMeasurement = false

    suspend fun startTest(testType: TestType) {
        val targetDevices = when (testType) {
            TestType.GAIT -> listOf(IMUDevice.LEFT_HAND, IMUDevice.RIGHT_HAND, IMUDevice.BASE_SPINE)
            TestType.TOPOLOGICAL_GAIT_ANALYSIS -> listOf(IMUDevice.LEFT_ANKLE, IMUDevice.RIGHT_ANKLE, IMUDevice.BASE_SPINE)
            TestType.DYNAMIC_GAIT_INDEX -> listOf(IMUDevice.LEFT_ANKLE, IMUDevice.RIGHT_ANKLE)
            TestType.TIMED_UP_AND_GO -> listOf(IMUDevice.LEFT_HAND, IMUDevice.RIGHT_HAND, IMUDevice.LEFT_ANKLE, IMUDevice.RIGHT_ANKLE, IMUDevice.BASE_SPINE)
            TestType.ONLY_RIGHT_HAND -> listOf(IMUDevice.RIGHT_HAND)
        }

        bleManager.startScan(targetDevices)

        while (!bleManager.areAllDevicesConnected()) {
            delay(1000)
        }

        Log.e("TEST", "All devices connected. Starting test...")
    }


    @SuppressLint("MissingPermission")
    private fun monitorConnections() {
        bleManager.coroutineScope.launch {
            connectedDevices.collectLatest { devices ->
                val connectedNames = devices.mapNotNull { it.name }
                canStartMeasurement = targetDeviceNames.all { it in connectedNames }
            }
        }
    }

    fun stopTest() {
        bleManager.stopScan()
        exportDataToCSV()
    }

    private fun exportDataToCSV() {
        val file = File("/storage/emulated/0/Documents/imu_data.csv")
        file.bufferedWriter().use { writer ->
            writer.write("Device ID, Accel X, Accel Y, Accel Z, Gyro X, Gyro Y, Gyro Z, Timestamp\n")
            imuData.value.forEach { data ->
                writer.write("${data.deviceId}, ${data.accelerometer.x}, ${data.accelerometer.y}, ${data.accelerometer.z}, " +
                        "${data.gyroscope.x}, ${data.gyroscope.y}, ${data.gyroscope.z}, ${data.timestamp}\n")
            }
        }
    }
}
