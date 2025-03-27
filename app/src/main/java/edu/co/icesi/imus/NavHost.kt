package edu.co.icesi.imus

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import edu.co.icesi.imus.model.Patient
import edu.co.icesi.imus.model.TestType
import edu.co.icesi.imus.repository.IMURepository
import edu.co.icesi.imus.repository.MeasurementRepository
import edu.co.icesi.imus.screens.DataCollectionScreen
import edu.co.icesi.imus.screens.HomeScreen
import edu.co.icesi.imus.screens.IMUConnectionScreen
import edu.co.icesi.imus.screens.MeasurementDetailScreen
import edu.co.icesi.imus.screens.MeasurementHistoryScreen
import edu.co.icesi.imus.screens.PatientFormScreen
import edu.co.icesi.imus.screens.TestSelectionScreen
import edu.co.icesi.imus.viewmodel.DataCollectionViewModel
import edu.co.icesi.imus.viewmodel.IMUConnectionViewModel
import edu.co.icesi.imus.viewmodel.MeasurementHistoryViewModel

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun IMUNavigationApp(
    imuRepository: IMURepository,
    measurementRepository: MeasurementRepository,
    navController: NavHostController = rememberNavController()
) {
    val patientState = remember { androidx.compose.runtime.mutableStateOf<Patient?>(null) }
    val testTypeState = remember { androidx.compose.runtime.mutableStateOf<TestType?>(null) }
    val measurementHistoryViewModel = MeasurementHistoryViewModel(measurementRepository)

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController)
        }

        composable("patient_form") {
            PatientFormScreen(
                navController = navController,
                onPatientInfoSubmitted = { patient ->
                    patientState.value = patient
                    navController.navigate("test_selection")
                }
            )
        }

        composable("test_selection") {
            TestSelectionScreen(
                navController = navController,
                patient = patientState.value,
                onTestSelected = { testType ->
                    testTypeState.value = testType
                    navController.navigate("imu_connection")
                },
                onChangePatient = {
                    patientState.value = null
                    navController.navigate("patient_form")
                }
            )
        }

        composable("imu_connection") {
            val viewModel: IMUConnectionViewModel = viewModel(
                factory = IMUConnectionViewModel.Factory(imuRepository, testTypeState.value!!)
            )
            IMUConnectionScreen(
                viewModel = viewModel,
                onStartTest = {
                    navController.navigate("data_collection")
                }
            )
        }

        composable("data_collection") {
            val viewModel: DataCollectionViewModel = viewModel(
                factory = DataCollectionViewModel.Factory(
                    imuRepository,
                    measurementRepository,
                    testTypeState.value!!,
                    patientState.value!!
                )
            )
            DataCollectionScreen(
                viewModel = viewModel,
                onFinish = {
                    navController.popBackStack("test_selection", false)
                },
                onDeviceDisconnected = {
                    navController.popBackStack("test_selection", false)
                }
            )
        }

        composable("measurement_history") {
            MeasurementHistoryScreen(
                viewModel = measurementHistoryViewModel,
                navController = navController
            )
        }

        composable(
            route = "measurement_detail/{measurementId}",
            arguments = listOf(navArgument("measurementId") { type = NavType.StringType })
        ) { backStackEntry ->
            val measurementId = backStackEntry.arguments?.getString("measurementId") ?: ""
            MeasurementDetailScreen(
                measurementId = measurementId,
                measurementRepository = measurementRepository,
                navController = navController
            )
        }
    }
}