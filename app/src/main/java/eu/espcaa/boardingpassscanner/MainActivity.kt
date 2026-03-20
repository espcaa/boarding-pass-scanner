package eu.espcaa.boardingpassscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import eu.espcaa.boardingpassscanner.data.AppDatabase
import eu.espcaa.boardingpassscanner.screens.BoardingDetailScreen
import eu.espcaa.boardingpassscanner.screens.HomeScreen
import eu.espcaa.boardingpassscanner.screens.TestScanner
import eu.espcaa.boardingpassscanner.ui.theme.BoardingPassScannerTheme
import eu.espcaa.boardingpassscanner.utils.AirlineColorCache
import eu.espcaa.boardingpassscanner.utils.AirlineManager
import eu.espcaa.boardingpassscanner.utils.AirportManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.dsl.module

@Serializable
data class TestScanRoute(val scannerId: String) : NavKey

@Serializable
object HomeRoute : NavKey

@Serializable
data class BoardingPassDetailRoute(
    val rawBarcode: String,
    val year: Int,
) : NavKey

class MainActivity : ComponentActivity() {

    private val airportManager: AirportManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stopKoin()
        startKoin {
            androidLogger()
            androidContext(this@MainActivity)
            modules(appModule)
        }

        lifecycleScope.launch(Dispatchers.IO) {
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

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `airline_colors` (`carrier` TEXT NOT NULL, `seedColor` INTEGER NOT NULL, PRIMARY KEY(`carrier`))")
    }
}

val appModule = module {
    single { AirlineManager(androidContext()).also { it.init(androidContext()) } }
    single { AirportManager(androidContext()) }
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "boarding_passes.db")
            .addMigrations(MIGRATION_1_2)
            .build()
    }
    single { get<AppDatabase>().boardingPassDao() }
    single { get<AppDatabase>().airlineColorDao() }
    single { AirlineColorCache(get()) }
}

@Composable
fun BoardingPassApp() {

    val backStack = rememberNavBackStack(HomeRoute)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { if (backStack.size > 1) backStack.removeLast() },
            transitionSpec = {
                slideInHorizontally(tween(350)) { it } + fadeIn(tween(350)) togetherWith
                        slideOutHorizontally(tween(350)) { -it / 3 } + fadeOut(tween(150))
            },
            popTransitionSpec = {
                slideInHorizontally(tween(350)) { -it / 3 } + fadeIn(tween(350)) togetherWith
                        slideOutHorizontally(tween(350)) { it } + fadeOut(tween(150))
            },
            predictivePopTransitionSpec = {
                scaleIn(
                    initialScale = 0.9f,
                    transformOrigin = TransformOrigin.Center,
                    animationSpec = tween(
                        durationMillis = 350,
                        easing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f)
                    )
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 350),
                    initialAlpha = 0f
                ) togetherWith
                        scaleOut(
                            targetScale = 0.9f,
                            transformOrigin = TransformOrigin.Center,
                            animationSpec = tween(
                                durationMillis = 350,
                                easing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f)
                            )
                        ) + fadeOut(
                    animationSpec = tween(durationMillis = 350),
                    targetAlpha = 0f
                )
            },
            entryDecorators = listOf(
                rememberViewModelStoreNavEntryDecorator(),
                rememberSaveableStateHolderNavEntryDecorator()
            ),
            entryProvider = { key ->
                when (key) {
                    is HomeRoute -> NavEntry(key) {
                        HomeScreen(
                            innerPadding,
                            onScanClick = {
                                backStack.add(TestScanRoute(scannerId = "default"))

                            },
                            onPassClick = { rawBarcode, year ->
                                backStack.add(
                                    BoardingPassDetailRoute(rawBarcode, year)
                                )
                            }

                        )
                    }

                    is BoardingPassDetailRoute -> NavEntry(key) {
                        BoardingDetailScreen(key.rawBarcode, key.year, onBack = {
                            backStack.removeLast()
                        })
                    }

                    is TestScanRoute -> NavEntry(key) {
                        TestScanner()
                    }

                    else -> error("Unknown nav key: $key")
                }
            }
        )
    }
}