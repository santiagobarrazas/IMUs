package edu.co.icesi.imus.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.co.icesi.imus.model.TestType
import edu.co.icesi.imus.repository.IMURepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class IMUConnectionViewModel(
    private val imuRepository: IMURepository,
    private val testType: TestType
) : ViewModel() {
    private val _uiState = MutableStateFlow(IMUConnectionUiState())
    val uiState: StateFlow<IMUConnectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            imuRepository.connectedDevices.collectLatest { devices ->
                val deviceStatus = imuRepository.getTargetDeviceNames(testType).associateWith { name ->
                    devices.any { it.name == name }
                }
                _uiState.value = _uiState.value.copy(
                    connectedDevices = devices,
                    deviceConnectionStatus = deviceStatus,
                    allDevicesConnected = deviceStatus.all { it.value }
                )
            }
        }
        scanForDevices()
    }

    fun scanForDevices() {
        viewModelScope.launch {
            imuRepository.scanForDevices(testType)
        }
    }

    class Factory(
        private val imuRepository: IMURepository,
        private val testType: TestType
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return IMUConnectionViewModel(imuRepository, testType) as T
        }
    }
}

data class IMUConnectionUiState(
    val connectedDevices: List<android.bluetooth.BluetoothDevice> = emptyList(),
    val deviceConnectionStatus: Map<String, Boolean> = emptyMap(),
    val allDevicesConnected: Boolean = false
)