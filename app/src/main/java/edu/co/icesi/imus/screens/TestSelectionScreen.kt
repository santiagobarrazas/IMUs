package edu.co.icesi.imus.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.co.icesi.imus.model.Patient
import edu.co.icesi.imus.model.TestType

@Composable
fun TestSelectionScreen(
    navController: NavController,
    patient: Patient?,
    onTestSelected: (TestType) -> Unit,
    onChangePatient: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Selección de Prueba",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        patient?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Paciente Actual: ${it.name}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(text = "Cédula: ${it.id}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TestType.entries.forEach { testType ->
                Button(
                    onClick = { onTestSelected(testType) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(testType.displayName)
                }
            }
        }

        Button(
            onClick = onChangePatient,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Cambiar Paciente")
        }
    }
}