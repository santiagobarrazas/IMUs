package edu.co.icesi.imus.di

import android.content.Context
import edu.co.icesi.imus.bluetooth.BLEManager
import edu.co.icesi.imus.data.DataStoreManager
import edu.co.icesi.imus.repository.IMURepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class AppContainer(private val context: Context) {
    private val bleManager by lazy {
        BLEManager(
            context = context,
            coroutineScope = CoroutineScope(Dispatchers.Default + Job())
        )
    }

    private val dataStoreManager by lazy {
        DataStoreManager(context)
    }

    val imuRepository by lazy {
        IMURepository(bleManager, dataStoreManager.dataStore)
    }
}