package edu.co.icesi.imus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.co.icesi.imus.model.Patient
import edu.co.icesi.imus.model.TestType
import edu.co.icesi.imus.repository.IMURepository
import edu.co.icesi.imus.repository.MeasurementRepository

class DataCollectionViewModelFactory(
    private val imuRepository: IMURepository,
    private val measurementRepository: MeasurementRepository,
    private val testType: TestType,
    private val patient: Patient
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DataCollectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DataCollectionViewModel(
                imuRepository,
                measurementRepository,
                testType,
                patient
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}