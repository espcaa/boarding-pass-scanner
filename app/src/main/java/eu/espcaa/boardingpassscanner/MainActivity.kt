package eu.espcaa.boardingpassscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eu.espcaa.boardingpassscanner.screens.HomeScreen
import eu.espcaa.boardingpassscanner.ui.theme.BoardingPassScannerTheme
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class DetailsRoute(val scannerId: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoardingPassScannerTheme {
                BoardingPassApp()
            }
        }
    }
}

@Composable
fun BoardingPassApp() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    onScanClick = {
                        navController.navigate(DetailsRoute(scannerId = "Scanner_01"))
                    }
                )
            }

            composable<DetailsRoute> {
                DetailsScreen()
            }
        }
    }
}



@Composable
fun DetailsScreen() {
    Text("Scanning Details Screen")
}