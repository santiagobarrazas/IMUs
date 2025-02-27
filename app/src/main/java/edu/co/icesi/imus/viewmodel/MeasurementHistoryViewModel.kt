package edu.co.icesi.imus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.co.icesi.imus.model.Measurement
import edu.co.icesi.imus.repository.MeasurementRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MeasurementHistoryViewModel(
    private val repository: MeasurementRepository
) : ViewModel() {

    val measurements: StateFlow<List<Measurement>> = repository.getAllMeasurements()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}