package edu.co.icesi.imus.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import edu.co.icesi.imus.model.AccelerometerData
import edu.co.icesi.imus.model.GyroscopeData
import edu.co.icesi.imus.model.IMUData
import edu.co.icesi.imus.model.IMUDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import java.util.UUID

class BLEManager(
    private val context: Context,
    val coroutineScope: CoroutineScope
) {
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val _connectedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val connectedDevices: StateFlow<List<BluetoothDevice>> = _connectedDevices.asStateFlow()

    private val _imuData = MutableStateFlow<List<IMUData>>(emptyList())
    val imuData: StateFlow<List<IMUData>> = _imuData.asStateFlow()

    private val deviceGattMap = mutableMapOf<String, BluetoothGatt>()
    private val requiredDevices = mutableSetOf<String>()

    private val IMU_SERVICE_UUID = UUID.fromString("12345678-1234-5678-1234-56789abcdef0")
    private val IMU_DATA_UUID = UUID.fromString("12345678-1234-5678-1234-56789abcdef1")
    private val COMMAND_UUID = UUID.fromString("12345678-1234-5678-1234-56789abcdef2")
    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private var descriptorWriteInProgress = false
    private var characteristicWriteInProgress = false

    @SuppressLint("MissingPermission")
    fun startScan(targetDevicesList: List<IMUDevice>) {
        stopScan()

        requiredDevices.clear()
        requiredDevices.addAll(targetDevicesList.map { it.bluetoothName })

        val filters = requiredDevices.map { name ->
            ScanFilter.Builder()
                .setDeviceName(name)
                .build()
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothAdapter.bluetoothLeScanner?.startScan(filters, settings, scanCallback)
        Log.d("BLE", "Started scanning for devices: ${requiredDevices.joinToString()}")
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        try {
            bluetoothAdapter.bluetoothLeScanner?.stopScan(scanCallback)
            Log.d("BLE", "Scan stopped")
        } catch (e: Exception) {
            Log.e("BLE", "Error stopping scan", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnectAllDevices() {
        Log.d("BLE", "Disconnecting all devices")

        stopScan()

        val deviceAddresses = deviceGattMap.keys.toList()

        for (address in deviceAddresses) {
            val gatt = deviceGattMap[address]
            if (gatt != null) {
                try {
                    Log.d("BLE", "Disconnecting device: ${gatt.device.name ?: "Unknown"}")
                    gatt.disconnect()
                    gatt.close()
                } catch (e: Exception) {
                    Log.e("BLE", "Error disconnecting device", e)
                }
                deviceGattMap.remove(address)
            }
        }

        _connectedDevices.value = emptyList()

        clearData()

        Log.d("BLE", "All devices disconnected")
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (device.name in requiredDevices && !deviceGattMap.containsKey(device.address)) {
                connect(device)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connect(device: BluetoothDevice) {
        if (!deviceGattMap.containsKey(device.address)) {
            val gatt =
                device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            deviceGattMap[device.address] = gatt
            Log.e("BLE", "Connecting to ${device.name}")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceName = gatt.device.name ?: "Unknown"
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d("BLE", "Connected to $deviceName")
                    _connectedDevices.value += gatt.device
                    gatt.requestMtu(517)
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d("BLE", "Disconnected from $deviceName")
                    _connectedDevices.value -= gatt.device
                    deviceGattMap.remove(gatt.device.address)
                    gatt.close()
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE", "MTU changed to $mtu for ${gatt.device.name}")
                gatt.discoverServices()
            } else {
                Log.e("BLE", "MTU change failed for ${gatt.device.name}: $status")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE", "Services discovered for ${gatt.device.name}")
                setupNotifications(gatt)
            } else {
                Log.e("BLE", "Service discovery failed for ${gatt.device.name}: $status")
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE", "Descriptor write successful for ${gatt.device.name}")
                descriptorWriteInProgress = false
                if (!characteristicWriteInProgress) {
                    sendStartSignal(gatt.device)
                }
            } else {
                Log.e("BLE", "Descriptor write failed for ${gatt.device.name}: $status")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE", "Characteristic write successful for ${gatt.device.name}")
                characteristicWriteInProgress = false
            } else {
                Log.e("BLE", "Characteristic write failed for ${gatt.device.name}: $status")
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == IMU_DATA_UUID) {
                val data = characteristic.value

                parseIMUData(data, gatt.device.address)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupNotifications(gatt: BluetoothGatt) {
        val service = gatt.getService(IMU_SERVICE_UUID)
        val characteristic = service?.getCharacteristic(IMU_DATA_UUID)

        if (characteristic != null) {
            if (gatt.setCharacteristicNotification(characteristic, true)) {
                Log.d("BLE", "Notification enabled for ${gatt.device.name}")

                val descriptor = characteristic.getDescriptor(CCCD_UUID)
                descriptor?.let {
                    descriptorWriteInProgress = true
                    it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    if (!gatt.writeDescriptor(it)) {
                        Log.e("BLE", "Failed to write descriptor for ${gatt.device.name}")
                    }
                }
            } else {
                Log.e("BLE", "Failed to enable notifications for ${gatt.device.name}")
            }
        } else {
            Log.e("BLE", "Characteristic not found for ${gatt.device.name}")
        }
    }

    private fun parseIMUData(data: ByteArray, deviceAddress: String) {
        try {
            val jsonString = String(data)


            val jsonElement = Json.parseToJsonElement(jsonString)
            val jsonObj = jsonElement.jsonObject


            val accelObj = jsonObj["accelerometer"]?.jsonObject
            val accelerometer = AccelerometerData(
                x = accelObj?.get("x")?.jsonPrimitive?.float ?: 0f,
                y = accelObj?.get("y")?.jsonPrimitive?.float ?: 0f,
                z = accelObj?.get("z")?.jsonPrimitive?.float ?: 0f
            )

            val gyroObj = jsonObj["gyroscope"]?.jsonObject
            val gyroscope = GyroscopeData(
                x = gyroObj?.get("x")?.jsonPrimitive?.float ?: 0f,
                y = gyroObj?.get("y")?.jsonPrimitive?.float ?: 0f,
                z = gyroObj?.get("z")?.jsonPrimitive?.float ?: 0f
            )


            val imuData = IMUData(
                deviceId = jsonObj["deviceId"]?.toString()?.replace("\"", "") ?: "",
                accelerometer = accelerometer,
                gyroscope = gyroscope,
                timestamp = jsonObj["timestamp"]?.jsonPrimitive?.long ?: 0L
            )

            _imuData.value += imuData
        } catch (e: Exception) {
            Log.e("BLE", "Error parsing IMU data", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun sendStartSignal(device: BluetoothDevice) {
        val gatt = deviceGattMap[device.address] ?: return
        val characteristic =
            gatt.getService(IMU_SERVICE_UUID)?.getCharacteristic(COMMAND_UUID) ?: return

        characteristicWriteInProgress = true
        characteristic.value = byteArrayOf(1)
        if (!gatt.writeCharacteristic(characteristic)) {
            Log.e("BLE", "Failed to write start signal to ${device.name}")
            characteristicWriteInProgress = false
        } else {
            Log.d("BLE", "Start signal sent to ${device.name}")
        }
    }

    @SuppressLint("MissingPermission")
    fun sendStopSignal(device: BluetoothDevice) {
        val gatt = deviceGattMap[device.address] ?: return
        val characteristic =
            gatt.getService(IMU_SERVICE_UUID)?.getCharacteristic(COMMAND_UUID) ?: return

        characteristicWriteInProgress = true
        characteristic.value = byteArrayOf(0)
        if (!gatt.writeCharacteristic(characteristic)) {
            Log.e("BLE", "Failed to write stop signal to ${device.name}")
            characteristicWriteInProgress = false
        } else {
            Log.d("BLE", "Stop signal sent to ${device.name}")
        }
    }

    fun clearData() {
        _imuData.value = emptyList()
        Log.d("BLE", "Data cleared")
    }
}
