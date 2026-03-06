package eu.espcaa.boardingpassscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eu.espcaa.boardingpassscanner.screens.HomeScreen
import eu.espcaa.boardingpassscanner.screens.TestScanner
import eu.espcaa.boardingpassscanner.screens.WelcomeScreen
import eu.espcaa.boardingpassscanner.ui.theme.BoardingPassScannerTheme
import eu.espcaa.boardingpassscanner.utils.AirlineManager
import kotlinx.serialization.Serializable
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

@Serializable
object WelcomeRoute

@Serializable
data class TestScanRoute(val scannerId: String)

@Serializable
object HomeRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startKoin {
            androidLogger()
            androidContext(this@MainActivity)
            modules(appModule)
        }
        setContent {
            BoardingPassScannerTheme {
                BoardingPassApp()
            }
        }

    }
}

val appModule = module {
    single { AirlineManager(androidContext()).also { it.init(androidContext()) } }
}

@Composable
fun BoardingPassApp() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(700)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(700)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(700)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(700)
                )
            }
        ) {
            composable<WelcomeRoute> {
                WelcomeScreen(
                    onNextClick = {
                        navController.navigate(TestScanRoute(scannerId = "default"))
                    },
                )
            }

            composable<TestScanRoute> {
                TestScanner()
            }

            composable<HomeRoute> {
                HomeScreen(
                    innerPadding,
                    onScanClick = {
                        navController.navigate(TestScanRoute(scannerId = "default"))
                    }
                )
            }
        }
    }
}
