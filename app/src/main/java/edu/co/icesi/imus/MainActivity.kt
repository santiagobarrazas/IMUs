package edu.co.icesi.imus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.co.icesi.imus.di.AppContainer
import edu.co.icesi.imus.model.TestType
import edu.co.icesi.imus.screens.DataCollectionScreen
import edu.co.icesi.imus.screens.TestSelectionScreen
import edu.co.icesi.imus.ui.theme.IMUAppTheme
import edu.co.icesi.imus.viewmodel.DataCollectionViewModel
import edu.co.icesi.imus.viewmodel.DataCollectionViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: AppContainer
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    @RequiresApi(Build.VERSION_CODES.S)
    private val requiredPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = (application as IMUApplication).appContainer

        // Register permission launcher
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                setupContent()
            } else {
                Toast.makeText(
                    this,
                    "Permissions are required for Bluetooth functionality",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        checkPermissionsAndStart()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkPermissionsAndStart() {
        if (requiredPermissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            setupContent()
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    private fun setupContent() {
        setContent {
            IMUAppTheme {
                var currentTest by remember { mutableStateOf<TestType?>(null) }

                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (currentTest == null) {
                        TestSelectionScreen(
                            onTestSelected = { selectedTest ->
                                currentTest = selectedTest
                            }
                        )
                    } else {
                        val viewModel: DataCollectionViewModel = viewModel(
                            factory = DataCollectionViewModelFactory(
                                imuRepository = appContainer.imuRepository,
                                testType = currentTest!!
                            )
                        )
                        DataCollectionScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
