package eu.espcaa.boardingpassscanner

import android.content.Context
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eu.espcaa.boardingpassscanner.screens.HomeScreen
import eu.espcaa.boardingpassscanner.screens.SettingsScreen
import eu.espcaa.boardingpassscanner.screens.TestScanner
import eu.espcaa.boardingpassscanner.screens.WelcomeScreen
import eu.espcaa.boardingpassscanner.ui.theme.BoardingPassScannerTheme
import eu.espcaa.boardingpassscanner.utils.AirlineManager
import eu.espcaa.boardingpassscanner.utils.AirportManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

val Context.dataStore by preferencesDataStore(name = "settings")
val SETUP_COMPLETED = booleanPreferencesKey("setup_completed")

@Serializable
object WelcomeRoute

@Serializable
data class TestScanRoute(val scannerId: String)

@Serializable
object HomeRoute

@Serializable
object SettingsRoute

class MainActivity : ComponentActivity() {

    // Use the standard Koin delegate for Kotlin
    private val airportManager: AirportManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {
            androidLogger()
            androidContext(this@MainActivity)
            modules(appModule)
        }

        CoroutineScope(Dispatchers.IO).launch {
            airportManager.loadAirports()
        }

        enableEdgeToEdge()
        setContent {
            BoardingPassScannerTheme {
                BoardingPassApp()
            }
        }
    }
}

val appModule = module {
    single { AirlineManager(androidContext()).also { it.init(androidContext()) } }
    single { AirportManager(androidContext()) }

}

@Composable
fun BoardingPassApp() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    val setupCompleted by context.dataStore.data
        .map { it[SETUP_COMPLETED] ?: false }
        .collectAsState(initial = null)

    if (setupCompleted == null) return

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (setupCompleted == true) HomeRoute else WelcomeRoute,
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
                        scope.launch {
                            context.dataStore.edit { it[SETUP_COMPLETED] = true }
                        }
                        navController.navigate(HomeRoute) {
                            popUpTo(WelcomeRoute) { inclusive = true }
                        }
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
                    },
                    onSettingsClick = {
                        navController.navigate(SettingsRoute)
                    }
                )
            }

            composable<SettingsRoute> {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
