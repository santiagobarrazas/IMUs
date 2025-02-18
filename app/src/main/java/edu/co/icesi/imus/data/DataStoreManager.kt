package edu.co.icesi.imus.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile

class DataStoreManager(private val context: Context) {
    val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("settings") }
    )
}