package edu.co.icesi.imus.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import edu.co.icesi.imus.model.IMUData
import edu.co.icesi.imus.model.IMUDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

class BLEManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val _connectedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val connectedDevices: StateFlow<List<BluetoothDevice>> = _connectedDevices.asStateFlow()

    private val _scanResults = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val scanResults: StateFlow<List<BluetoothDevice>> = _scanResults.asStateFlow()

    private val _imuData = MutableStateFlow<List<IMUData>>(emptyList())
    val imuData: StateFlow<List<IMUData>> = _imuData.asStateFlow()

    private val deviceGattMap = mutableMapOf<String, BluetoothGatt>()

    @SuppressLint("MissingPermission")
    fun startScan(targetDevices: List<IMUDevice>) {
        val filters = targetDevices.map { device ->
            ScanFilter.Builder()
                .setDeviceName(device.bluetoothName)
                .build()
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothAdapter.bluetoothLeScanner.startScan(
            filters,
            settings,
            scanCallback
        )
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val currentList = _scanResults.value.toMutableList()
            if (!currentList.contains(result.device)) {
                currentList.add(result.device)
                _scanResults.value = currentList
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        val gatt = device.connectGatt(
            context,
            false,
            gattCallback,
            BluetoothDevice.TRANSPORT_LE
        )
        deviceGattMap[device.address] = gatt
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    val currentList = _connectedDevices.value.toMutableList()
                    currentList.add(gatt.device)
                    _connectedDevices.value = currentList
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    val currentList = _connectedDevices.value.toMutableList()
                    currentList.remove(gatt.device)
                    _connectedDevices.value = currentList
                }
            }
        }

        @SuppressLint("MissingPermission")
        private fun setupNotifications(gatt: BluetoothGatt) {
            gatt.services.forEach { service ->
                service.characteristics.forEach { characteristic ->
                    if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
                        gatt.setCharacteristicNotification(characteristic, true)
                    }
                }
            }
        }


        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                setupNotifications(gatt)
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            parseIMUData(characteristic.value, gatt.device.address)
        }
    }

    private fun parseIMUData(data: ByteArray, deviceAddress: String) {
        try {
            val jsonString = String(data)
            val imuData = Json.decodeFromString<IMUData>(jsonString)
            val currentList = _imuData.value.toMutableList()
            currentList.add(imuData.copy(deviceId = deviceAddress))
            _imuData.value = currentList
        } catch (e: Exception) {
            Log.e("BLEManager", "Error parsing IMU data", e)
        }
    }
}