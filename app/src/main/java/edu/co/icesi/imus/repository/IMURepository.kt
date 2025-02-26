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
    var isCollecting = false

    // Estado para seguimiento de dispositivos objetivo
    private var targetDevices: List<IMUDevice> = emptyList()
    private var targetDeviceNames: List<String> = emptyList()
    private val _allTargetDevicesConnected = MutableStateFlow(false)
    val allTargetDevicesConnected: StateFlow<Boolean> = _allTargetDevicesConnected.asStateFlow()

    init {
        // Iniciar el monitoreo de conexiones
        monitorConnections()
    }

    @SuppressLint("MissingPermission")
    fun scanForDevices(testType: TestType) {
        // Detener cualquier proceso en curso primero
        if (isCollecting) {
            stopTest()
        }

        // Desconectar todos los dispositivos existentes antes de iniciar un nuevo escaneo
        disconnectAllDevices()

        // Definir los dispositivos objetivo según el tipo de prueba
        targetDevices = when (testType) {
            TestType.GAIT -> listOf(IMUDevice.LEFT_HAND, IMUDevice.RIGHT_HAND, IMUDevice.BASE_SPINE)
            TestType.TOPOLOGICAL_GAIT_ANALYSIS -> listOf(IMUDevice.LEFT_ANKLE, IMUDevice.RIGHT_ANKLE)
            TestType.DYNAMIC_GAIT_INDEX -> listOf(IMUDevice.LEFT_ANKLE, IMUDevice.RIGHT_ANKLE, IMUDevice.BASE_SPINE)
            TestType.TIMED_UP_AND_GO -> listOf(IMUDevice.LEFT_HAND, IMUDevice.RIGHT_HAND, IMUDevice.LEFT_ANKLE, IMUDevice.RIGHT_ANKLE, IMUDevice.BASE_SPINE)
            TestType.ONLY_RIGHT_HAND -> listOf(IMUDevice.RIGHT_HAND)
        }

        targetDeviceNames = targetDevices.map { it.bluetoothName }

        Log.d("IMU", "Scanning for devices: $targetDeviceNames")

        // Reset del estado de conexión
        _allTargetDevicesConnected.value = false

        // Iniciar el escaneo para los dispositivos objetivo
        bleManager.startScan(targetDevices)
    }

    @SuppressLint("MissingPermission")
    private fun disconnectAllDevices() {
        Log.d("IMU", "Disconnecting all existing devices before new scan")

        // Implementar en el BLEManager una forma de desconectar dispositivos actuales
        bleManager.disconnectAllDevices()

        // Esperar un momento para asegurar que los dispositivos se desconecten
        bleManager.coroutineScope.launch {
            delay(500) // Pequeño delay para asegurar que las desconexiones se completen
        }
    }

    @SuppressLint("MissingPermission")
    private fun monitorConnections() {
        bleManager.coroutineScope.launch {
            connectedDevices.collectLatest { devices ->
                if (targetDeviceNames.isNotEmpty()) {
                    val connectedNames = devices.mapNotNull { it.name }
                    val allConnected = targetDeviceNames.all { targetName ->
                        connectedNames.contains(targetName)
                    }

                    // Actualizar el estado de conexión
                    _allTargetDevicesConnected.value = allConnected

                    Log.d("IMU", "Device connection status updated: $allConnected")
                    Log.d("IMU", "Connected devices: $connectedNames")
                    Log.d("IMU", "Target devices: $targetDeviceNames")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startTest() {
        // Verificar que todos los dispositivos objetivo estén conectados
        if (!allTargetDevicesConnected.value) {
            Log.e("IMU", "Cannot start test: not all target devices are connected")
            return
        }

        Log.d("IMU", "All devices connected. Starting test...")

        // Enviar señal de inicio a todos los dispositivos objetivo
        connectedDevices.value.forEach { device ->
            if (targetDeviceNames.contains(device.name)) {
                bleManager.sendStartSignal(device)
                Log.d("IMU", "Sent start signal to device: ${device.name}")
            }
        }

        isCollecting = true
    }

    @SuppressLint("MissingPermission")
    fun stopTest() {
        if (isCollecting) {
            // Detener la recolección de datos en todos los dispositivos objetivo
            connectedDevices.value.forEach { device ->
                if (targetDeviceNames.contains(device.name)) {
                    bleManager.sendStopSignal(device)
                    Log.d("IMU", "Sent stop signal to device: ${device.name}")
                }
            }

            isCollecting = false
            exportDataToCSV()
            bleManager.clearData()
        } else {
            // Si no estamos recolectando, simplemente detener el escaneo
            bleManager.stopScan()
        }
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
        Log.d("IMU", "Data exported to CSV: ${file.absolutePath}")
    }
}