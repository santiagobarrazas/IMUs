package edu.co.icesi.imus.repository

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.StateFlow
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import edu.co.icesi.imus.bluetooth.BLEManager
import edu.co.icesi.imus.model.IMUData
import edu.co.icesi.imus.model.IMUDevice
import edu.co.icesi.imus.model.TestType
import java.io.File

class IMURepository(
    private val bleManager: BLEManager,
    private val dataStore: DataStore<Preferences>
) {
    val connectedDevices: StateFlow<List<BluetoothDevice>> = bleManager.connectedDevices
    val imuData: StateFlow<List<IMUData>> = bleManager.imuData

    suspend fun startTest(testType: TestType) {
        val targetDevices = when (testType) {
            TestType.GAIT -> listOf(
                IMUDevice.LEFT_HAND,
                IMUDevice.RIGHT_HAND,
                IMUDevice.BASE_SPINE
            )
            TestType.TOPOLOGICAL_GAIT_ANALYSIS -> listOf(
                IMUDevice.LEFT_ANKLE,
                IMUDevice.RIGHT_ANKLE,
                IMUDevice.BASE_SPINE
            )
            TestType.DYNAMIC_GAIT_INDEX -> listOf(
                IMUDevice.LEFT_ANKLE,
                IMUDevice.RIGHT_ANKLE
            )
            TestType.TIMED_UP_AND_GO -> listOf(
                IMUDevice.LEFT_HAND,
                IMUDevice.RIGHT_HAND,
                IMUDevice.LEFT_ANKLE,
                IMUDevice.RIGHT_ANKLE,
                IMUDevice.BASE_SPINE
            )
        }

        bleManager.startScan(targetDevices)
    }

    suspend fun stopTest() {
        bleManager.stopScan()
        exportDataToCSV()
    }

    private suspend fun exportDataToCSV() {
        val imuDataList = imuData.value
        val file = File("/storage/emulated/0/Documents/imu_data.csv")
        file.bufferedWriter().use { writer ->
            writer.write("Device ID,Acceleration X,Acceleration Y,Acceleration Z,Gyroscope X,Gyroscope Y,Gyroscope Z,Timestamp\n")
            imuDataList.forEach { data ->
                writer.write("${data.deviceId},${data.accelerometer.x},${data.accelerometer.y},${data.accelerometer.z},${data.gyroscope.x},${data.gyroscope.y},${data.gyroscope.z},${data.timestamp}\n")
            }
        }
    }
}