package edu.co.icesi.imus.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.co.icesi.imus.model.IMUData
import edu.co.icesi.imus.model.TestType
import edu.co.icesi.imus.repository.IMURepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DataCollectionViewModel(
    private val imuRepository: IMURepository,
    private val testType: TestType
) : ViewModel() {

    private val _uiState = MutableStateFlow(DataCollectionUiState())
    val uiState: StateFlow<DataCollectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            imuRepository.connectedDevices.collect { devices ->
                _uiState.update { it.copy(connectedDevices = devices) }
            }
        }

        viewModelScope.launch {
            imuRepository.imuData.collect { data ->
                _uiState.update { it.copy(imuData = data) }
            }
        }

        viewModelScope.launch {
            imuRepository.allTargetDevicesConnected.collect { allConnected ->
                _uiState.update { it.copy(allTargetDevicesConnected = allConnected) }
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCollecting = imuRepository.isCollecting) }
        }
    }

    fun scanForDevices() {
        viewModelScope.launch {
            imuRepository.scanForDevices(testType)
        }
    }

    fun startDataCollection() {
        viewModelScope.launch {
            imuRepository.startTest()
            _uiState.update { it.copy(isCollecting = imuRepository.isCollecting) }
        }
    }

    fun stopDataCollection() {
        viewModelScope.launch {
            imuRepository.stopTest()
            _uiState.update { it.copy(isCollecting = imuRepository.isCollecting) }
        }
    }
}

data class DataCollectionUiState(
    val connectedDevices: List<BluetoothDevice> = emptyList(),
    val imuData: List<IMUData> = emptyList(),
    val isCollecting: Boolean = false,
    val allTargetDevicesConnected: Boolean = false
)