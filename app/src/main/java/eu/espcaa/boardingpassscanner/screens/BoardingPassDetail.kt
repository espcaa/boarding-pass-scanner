package eu.espcaa.boardingpassscanner.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import coil3.compose.AsyncImage
import com.google.zxing.aztec.AztecWriter
import eu.espcaa.boardingpassscanner.parser.ConvertToBoardingPass
import eu.espcaa.boardingpassscanner.parser.ParseBCBP
import eu.espcaa.boardingpassscanner.utils.AirlineColorCache
import eu.espcaa.boardingpassscanner.utils.AirlineManager
import eu.espcaa.boardingpassscanner.utils.AirportManager
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoardingDetailScreen(
    rawBarcode: String,
    year: Int,
    airlineManager: AirlineManager = koinInject(),
    airportManager: AirportManager = koinInject(),
    colorCache: AirlineColorCache = koinInject(),
    onBack: () -> Unit,
) {
    val parsed = remember(rawBarcode) { ParseBCBP(rawBarcode) }
    val boardingPass = remember(parsed, year) {
        parsed.boardingPass?.let { ConvertToBoardingPass(it, year) }
    }

    if (boardingPass == null) return

    val carrier = boardingPass.legs.first().carrier
    val airlineColors by colorCache.colors.collectAsState()
    val airlineColorScheme = airlineColors[carrier]

    MaterialTheme(colorScheme = airlineColorScheme ?: MaterialTheme.colorScheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Boarding Pass") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Surface(
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                modifier = Modifier.size(128.dp),
                                shape = MaterialShapes.SoftBurst.toShape(),
                                color = Color(255, 255, 255, 255),
                                tonalElevation = 4.dp
                            ) {
                                AsyncImage(
                                    model = getAirlineLogoURL(
                                        boardingPass.legs.first().carrier,
                                        airlineManager
                                    ),
                                    modifier = Modifier
                                        .padding(24.dp),
                                    contentDescription = "Airline Logo"
                                )
                            }

                            Text(
                                text = "${boardingPass.legs.first().from}\n${boardingPass.legs.first().to}",
                                style = MaterialTheme.typography.titleLargeEmphasized,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        AztecCode(
                            data = rawBarcode,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            colorScheme = airlineColorScheme ?: MaterialTheme.colorScheme
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun AztecCode(
    data: String,
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    val aztecBgColor = lerp(colorScheme.primary, Color.White, 0.5f)
    val aztecFgColor = lerp(colorScheme.onPrimary, Color.Black, 0.5f)

    val colorSurface = aztecBgColor.toArgb()
    val colorOnSurface = aztecFgColor.toArgb()

    val bitmap = remember(data, colorOnSurface, colorSurface) {
        val matrix = AztecWriter().encode(data, com.google.zxing.BarcodeFormat.AZTEC, 512, 512)
        val bmp = createBitmap(512, 512)

        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bmp[x, y] = if (matrix[x, y]) colorOnSurface else colorSurface
            }
        }
        bmp.asImageBitmap()
    }

    Surface(
        modifier = modifier
            .aspectRatio(1f),
        color = aztecBgColor,
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = "Boarding Pass Aztec Code",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None
            )
        }
    }
}
