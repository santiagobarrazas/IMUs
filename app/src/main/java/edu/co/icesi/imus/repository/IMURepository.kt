package edu.co.icesi.imus.repository

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import edu.co.icesi.imus.bluetooth.BLEManager
import edu.co.icesi.imus.model.IMUData
import edu.co.icesi.imus.model.IMUDevice
import edu.co.icesi.imus.model.TestType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class IMURepository(
    private val bleManager: BLEManager,
    private val dataStore: DataStore<Preferences>
) {
    val connectedDevices: StateFlow<List<BluetoothDevice>> = bleManager.connectedDevices
    val imuData: StateFlow<List<IMUData>> = bleManager.imuData
    var isCollecting = false

    private var targetDevices: List<IMUDevice> = emptyList()
    private var targetDeviceNames: List<String> = emptyList()
    private val _allTargetDevicesConnected = MutableStateFlow(false)
    val allTargetDevicesConnected: StateFlow<Boolean> = _allTargetDevicesConnected.asStateFlow()

    init {
        monitorConnections()
    }

    @SuppressLint("MissingPermission")
    fun scanForDevices(testType: TestType) {
        if (isCollecting) {
            stopTest()
        }
        disconnectAllDevices()

        targetDevices = when (testType) {
            TestType.GAIT -> listOf(IMUDevice.LEFT_HAND, IMUDevice.RIGHT_HAND, IMUDevice.BASE_SPINE)
            TestType.TOPOLOGICAL_GAIT_ANALYSIS -> listOf(IMUDevice.LEFT_ANKLE, IMUDevice.RIGHT_ANKLE)
            TestType.DYNAMIC_GAIT_INDEX -> listOf(IMUDevice.LEFT_ANKLE, IMUDevice.RIGHT_ANKLE, IMUDevice.BASE_SPINE)
            TestType.TIMED_UP_AND_GO -> listOf(IMUDevice.LEFT_HAND, IMUDevice.RIGHT_HAND, IMUDevice.LEFT_ANKLE, IMUDevice.RIGHT_ANKLE, IMUDevice.BASE_SPINE)
            TestType.ONLY_RIGHT_HAND -> listOf(IMUDevice.RIGHT_HAND)
        }

        targetDeviceNames = targetDevices.map { it.bluetoothName }
        _allTargetDevicesConnected.value = false
        bleManager.startScan(targetDevices)
    }

    @SuppressLint("MissingPermission")
    fun startTest() {
        if (!allTargetDevicesConnected.value) return
        connectedDevices.value.forEach { device ->
            if (targetDeviceNames.contains(device.name)) {
                bleManager.sendStartSignal(device)
            }
        }
        isCollecting = true
    }

    @SuppressLint("MissingPermission")
    fun pauseTest() {
        connectedDevices.value.forEach { device ->
            if (targetDeviceNames.contains(device.name)) {
                bleManager.sendStopSignal(device)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun resumeTest() {
        connectedDevices.value.forEach { device ->
            if (targetDeviceNames.contains(device.name)) {
                bleManager.sendStartSignal(device)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopTest() {
        if (isCollecting) {
            connectedDevices.value.forEach { device ->
                if (targetDeviceNames.contains(device.name)) {
                    bleManager.sendStopSignal(device)
                }
            }
            isCollecting = false
            bleManager.clearData()
        } else {
            bleManager.stopScan()
        }
    }

    fun getTargetDeviceNames(testType: TestType): List<String> {
        return when (testType) {
            TestType.GAIT -> listOf("LEFT-HAND", "RIGHT-HAND", "BASE-SPINE")
            TestType.TOPOLOGICAL_GAIT_ANALYSIS -> listOf("LEFT-ANKLE", "RIGHT-ANKLE")
            TestType.DYNAMIC_GAIT_INDEX -> listOf("LEFT-ANKLE", "RIGHT-ANKLE", "BASE-SPINE")
            TestType.TIMED_UP_AND_GO -> listOf("LEFT-HAND", "RIGHT-HAND", "LEFT-ANKLE", "RIGHT-ANKLE", "BASE-SPINE")
            TestType.ONLY_RIGHT_HAND -> listOf("RIGHT-HAND")
        }
    }

    @SuppressLint("MissingPermission")
    private fun disconnectAllDevices() {
        bleManager.disconnectAllDevices()
        bleManager.coroutineScope.launch {
            delay(500)
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
                    _allTargetDevicesConnected.value = allConnected
                }
            }
        }
    }
}