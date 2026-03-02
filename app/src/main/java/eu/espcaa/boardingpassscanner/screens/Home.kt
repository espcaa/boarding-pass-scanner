package eu.espcaa.boardingpassscanner.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun HomeScreen(onScanClick: () -> Unit) {
    Button(onClick = onScanClick) {
        Text("Start Scanning")
    }
}