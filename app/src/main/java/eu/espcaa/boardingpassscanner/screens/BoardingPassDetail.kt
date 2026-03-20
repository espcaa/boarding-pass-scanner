package eu.espcaa.boardingpassscanner.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import coil3.compose.AsyncImage
import com.google.zxing.aztec.AztecWriter
import eu.espcaa.boardingpassscanner.parser.ConvertToBoardingPass
import eu.espcaa.boardingpassscanner.parser.Leg
import eu.espcaa.boardingpassscanner.parser.ParseBCBP
import eu.espcaa.boardingpassscanner.utils.AirlineColorCache
import eu.espcaa.boardingpassscanner.utils.AirlineManager
import eu.espcaa.boardingpassscanner.utils.AirportManager
import org.koin.compose.koinInject
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalLayoutApi::class
)
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

    val infiniteTransition = rememberInfiniteTransition()
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    MaterialTheme(colorScheme = airlineColorScheme ?: MaterialTheme.colorScheme) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            "${airportManager.getCity(boardingPass.legs.first().from)} → ${
                                airportManager.getCity(
                                    boardingPass.legs.first().to
                                )
                            }",
                            style = MaterialTheme.typography.titleLargeEmphasized
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        navigationIconContentColor = Color.Unspecified,
                        titleContentColor = Color.Unspecified,
                        actionIconContentColor = Color.Unspecified
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hero card
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Airline logo + flight number header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(72.dp)
                                    .rotate(angle),
                                shape = MaterialShapes.SoftBurst.toShape(),
                                color = Color.White,
                                tonalElevation = 4.dp
                            ) {
                                AsyncImage(
                                    model = getAirlineLogoURL(carrier, airlineManager),
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(16.dp)
                                        .rotate(-angle),
                                    contentDescription = "Airline Logo"
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${carrier} ${
                                        boardingPass.legs.first().flightNumber.trimStart(
                                            '0'
                                        )
                                    }",
                                    style = MaterialTheme.typography.titleLargeEmphasized,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                                if (boardingPass.legs.first().isEticket) {
                                    Text(
                                        text = "E-TICKET",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                            alpha = 0.6f
                                        ),
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Route display
                        RouteHeader(
                            leg = boardingPass.legs.first(),
                            airportManager = airportManager
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Separator()
                        Spacer(modifier = Modifier.height(16.dp))

                        // Passenger info
                        InfoField(
                            label = "PASSENGER",
                            value = boardingPass.passengerName.replace("/", " "),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Flight details grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoField(label = "PNR", value = boardingPass.pnrCode)
                            InfoField(
                                label = "DATE",
                                value = boardingPass.legs.first().flightDate?.format(
                                    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
                                ) ?: "Day ${boardingPass.legs.first().flightJulian.trimStart('0')}"
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoField(
                                label = "SEAT",
                                value = boardingPass.legs.first().seat.trimStart('0')
                                    .ifEmpty { "—" }
                            )
                            InfoField(
                                label = "CLASS",
                                value = compartmentCodeToName(boardingPass.legs.first().compartmentCode)
                            )
                            InfoField(
                                label = "SEQ",
                                value = boardingPass.legs.first().sequenceNumber.trimStart('0')
                                    .ifEmpty { "—" }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Extra legs
                        if (boardingPass.legs.size > 1) {
                            boardingPass.legs.drop(1).forEachIndexed { index, leg ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Separator()
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "CONNECTION ${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                )

                                RouteHeader(leg = leg, airportManager = airportManager)
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    InfoField(
                                        label = "FLIGHT",
                                        value = "${leg.carrier} ${leg.flightNumber.trimStart('0')}"
                                    )
                                    InfoField(
                                        label = "DATE",
                                        value = leg.flightDate?.format(
                                            DateTimeFormatter.ofPattern(
                                                "dd MMM yyyy",
                                                Locale.ENGLISH
                                            )
                                        ) ?: "Day ${leg.flightJulian.trimStart('0')}"
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    InfoField(
                                        label = "SEAT",
                                        value = leg.seat.trimStart('0').ifEmpty { "—" }
                                    )
                                    InfoField(
                                        label = "CLASS",
                                        value = compartmentCodeToName(leg.compartmentCode)
                                    )
                                    InfoField(
                                        label = "SEQ",
                                        value = leg.sequenceNumber.trimStart('0').ifEmpty { "—" }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Separator()
                        Spacer(modifier = Modifier.height(24.dp))

                        // Aztec code
                        AztecCode(
                            data = rawBarcode,
                            modifier = Modifier.fillMaxWidth(),
                            colorScheme = airlineColorScheme ?: MaterialTheme.colorScheme
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RouteHeader(
    leg: Leg,
    airportManager: AirportManager
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
            Text(
                text = leg.from,
                style = MaterialTheme.typography.displayMediumEmphasized,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = airportManager.getCity(leg.from),
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
        }
        Icon(
            Icons.Filled.AirplanemodeActive,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.size(28.dp)
        )
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
            Text(
                text = leg.to,
                style = MaterialTheme.typography.displayMediumEmphasized,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.End,
            )
            Text(
                text = airportManager.getCity(leg.to),
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.End,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InfoField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

fun compartmentCodeToName(code: String): String {
    return when (code.uppercase()) {
        "F" -> "First"
        "A" -> "First"
        "J" -> "Business"
        "C" -> "Business"
        "D" -> "Business"
        "I" -> "Business"
        "W" -> "Premium Eco"
        "P" -> "Premium Eco"
        "Y" -> "Economy"
        "B", "H", "K", "L", "M", "N", "Q", "T", "V", "X", "G", "S", "E", "O", "R", "U" -> "Economy"
        else -> code
    }
}

@Composable
fun AztecCode(
    data: String,
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    val aztecBgColor = Color.White
    val aztecFgColor = Color.Black

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

@Composable
fun Separator(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp),
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
        shape = RoundedCornerShape(1.dp)
    ) {}
}
