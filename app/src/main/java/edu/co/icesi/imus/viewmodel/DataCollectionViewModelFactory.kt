package edu.co.icesi.imus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.co.icesi.imus.model.TestType
import edu.co.icesi.imus.repository.IMURepository

class DataCollectionViewModelFactory(
    private val imuRepository: IMURepository,
    private val testType: TestType
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DataCollectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DataCollectionViewModel(imuRepository, testType) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}