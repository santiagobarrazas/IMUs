package edu.co.icesi.imus

import android.app.Application
import edu.co.icesi.imus.di.AppContainer

class IMUApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}