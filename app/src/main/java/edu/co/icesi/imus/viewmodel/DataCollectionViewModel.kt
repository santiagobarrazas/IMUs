package edu.co.icesi.imus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.co.icesi.imus.model.IMUData
import edu.co.icesi.imus.model.Patient
import edu.co.icesi.imus.model.TestType
import edu.co.icesi.imus.repository.IMURepository
import edu.co.icesi.imus.repository.MeasurementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DataCollectionViewModel(
    private val imuRepository: IMURepository,
    private val measurementRepository: MeasurementRepository,
    private val testType: TestType,
    private val patient: Patient
) : ViewModel() {
    private val _uiState = MutableStateFlow(DataCollectionUiState())
    val uiState: StateFlow<DataCollectionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            imuRepository.connectedDevices.collectLatest { devices ->
                val allConnected = imuRepository.getTargetDeviceNames(testType).all { name ->
                    devices.any { it.name == name }
                }
                _uiState.value = _uiState.value.copy(
                    connectedDevices = devices,
                    allDevicesConnected = allConnected
                )
            }
        }
        viewModelScope.launch {
            imuRepository.imuData.collectLatest { data ->
                _uiState.value = _uiState.value.copy(imuData = data)
            }
        }
    }

    fun startDataCollection() {
        viewModelScope.launch {
            imuRepository.startTest()
            _uiState.value = _uiState.value.copy(isCollecting = true, isPaused = false)
        }
    }

    fun pauseDataCollection() {
        viewModelScope.launch {
            imuRepository.pauseTest()
            _uiState.value = _uiState.value.copy(isPaused = true)
        }
    }

    fun resumeDataCollection() {
        viewModelScope.launch {
            imuRepository.resumeTest()
            _uiState.value = _uiState.value.copy(isPaused = false)
        }
    }

    fun stopDataCollection() {
        viewModelScope.launch {
            imuRepository.stopTest()
            _uiState.value = _uiState.value.copy(isCollecting = false, isPaused = false)
        }
    }

    fun saveAndFinish(onFinish: () -> Unit) {
        viewModelScope.launch {
            val collectedData = imuRepository.imuData.value
            if (collectedData.isNotEmpty()) {
                measurementRepository.saveMeasurement(testType, patient, collectedData)
            }
            imuRepository.stopTest()
            onFinish()
        }
    }

    class Factory(
        private val imuRepository: IMURepository,
        private val measurementRepository: MeasurementRepository,
        private val testType: TestType,
        private val patient: Patient
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DataCollectionViewModel(imuRepository, measurementRepository, testType, patient) as T
        }
    }
}

data class DataCollectionUiState(
    val connectedDevices: List<android.bluetooth.BluetoothDevice> = emptyList(),
    val imuData: List<IMUData> = emptyList(),
    val isCollecting: Boolean = false,
    val isPaused: Boolean = false,
    val allDevicesConnected: Boolean = true
)