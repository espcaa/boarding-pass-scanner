package eu.espcaa.boardingpassscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eu.espcaa.boardingpassscanner.screens.HomeScreen
import eu.espcaa.boardingpassscanner.screens.ScanScreen
import eu.espcaa.boardingpassscanner.ui.theme.BoardingPassScannerTheme
import eu.espcaa.boardingpassscanner.utils.AirlineManager
import kotlinx.serialization.Serializable
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

@Serializable
object HomeRoute

@Serializable
data class ScanRoute(val scannerId: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoardingPassScannerTheme {
                BoardingPassApp()
            }
        }
        startKoin {
            androidLogger()
            androidContext(this@MainActivity)
            modules(appModule)
        }
    }
}

val appModule = module {
    single { AirlineManager(androidContext()) }
}

@Composable
fun BoardingPassApp() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            enterTransition = {
                scaleIn(initialScale = 0.9f, transformOrigin = TransformOrigin.Center) + fadeIn() +
                        slideInHorizontally(initialOffsetX = { it / 4 })
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() +
                        scaleOut(targetScale = 0.9f, transformOrigin = TransformOrigin.Center)
            },

            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it }) + fadeIn() +
                        scaleIn(initialScale = 0.9f, transformOrigin = TransformOrigin.Center)
            },
            popExitTransition = {
                scaleOut(targetScale = 0.9f, transformOrigin = TransformOrigin.Center) + fadeOut() +
                        slideOutHorizontally(targetOffsetX = { it / 4 })
            }
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    onScanClick = {
                        navController.navigate(ScanRoute)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            composable<ScanRoute> {
                ScanScreen()
            }
        }
    }
}


