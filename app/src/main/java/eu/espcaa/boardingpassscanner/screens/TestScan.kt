package eu.espcaa.boardingpassscanner.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirlineStops
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import eu.espcaa.boardingpassscanner.R
import eu.espcaa.boardingpassscanner.parser.JulianBoardingPass
import eu.espcaa.boardingpassscanner.parser.JulianLeg
import eu.espcaa.boardingpassscanner.utils.AirlineManager
import eu.espcaa.boardingpassscanner.utils.AirportManager
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScanner(
    airlineManager: AirlineManager = koinInject(),
    airportManager: AirportManager = koinInject(),
) {

    var scannedPass by remember { mutableStateOf<JulianBoardingPass?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    BoardingPassScanner(
        onSuccess = {
            scannedPass = it
            showSheet = true
        },
        overlayContent = {},
        canScan = !showSheet,
    )

    if (showSheet && scannedPass != null) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            ResultSheetContent(
                boardingPass = scannedPass!!,
                airlineManager = airlineManager,
                airportManager = airportManager
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultSheetContent(
    boardingPass: JulianBoardingPass,
    airlineManager: AirlineManager,
    airportManager: AirportManager
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
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
                            .size(64.dp)
                            .padding(16.dp),
                        contentDescription = "Airline Logo"
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Top
            ) {
                // save button
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        bottomStart = 16.dp,
                        topEnd = 4.dp,
                        bottomEnd = 4.dp
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_download),
                        contentDescription = "Save",
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(text = "Save")


                }
                // open in other view
                IconButton(
                    shape = RoundedCornerShape(
                        topEnd = 16.dp,
                        bottomEnd = 16.dp,
                        topStart = 4.dp,
                        bottomStart = 4.dp
                    ),
                    onClick = {},
                    modifier = Modifier.padding(horizontal = 0.5.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_open),
                        contentDescription = "Open in other view",
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }


        val outlineColor = colorScheme.outlineVariant
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {
                val wavePath = Path()
                val waveHeight = 15f
                val waveWidth = 30f
                var x = 0f
                var goingUp = true

                wavePath.moveTo(0f, size.height / 2)

                while (x < size.width) {
                    val controlY =
                        if (goingUp) size.height / 2 - waveHeight else size.height / 2 + waveHeight
                    wavePath.quadraticTo(
                        x + waveWidth / 2, controlY,
                        x + waveWidth, size.height / 2
                    )
                    x += waveWidth
                    goingUp = !goingUp
                }

                drawPath(
                    path = wavePath,
                    color = outlineColor,
                    style = Stroke(
                        width = 8f,
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            boardingPass.legs.forEachIndexed { index, leg ->
                FlightLegSegment(
                    leg = leg,
                    isLastLeg = index == boardingPass.legs.size - 1,
                    isFirstLeg = index == 0,
                    airlineManager = airlineManager,
                    airportManager = airportManager
                )
            }
        }
    }
}

fun getAirlineLogoURL(airlineCode: String, airlineManager: AirlineManager): String {
    return "https://www.flightaware.com/images/airline_logos/180px/${
        airlineManager.getIcao(
            airlineCode
        )
    }.png"
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FlightLegSegment(
    leg: JulianLeg,
    isLastLeg: Boolean,
    isFirstLeg: Boolean = false,
    airlineManager: AirlineManager,
    airportManager: AirportManager
) {
    Column {
        if (isFirstLeg) {
            AirportNode(
                airportName = airportManager.getCity(leg.from),
                icon = Icons.Default.FlightTakeoff,
                label = "Departure"
            )
        } else {
            AirportNode(
                airportName = airportManager.getCity(leg.from),
                icon = Icons.Default.AirlineStops,
                label = "Layover"
            )
        }
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(colorScheme.primaryContainer)
            )

            Surface(
                modifier = Modifier
                    .padding(start = 20.dp, top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colorScheme.surfaceBright,
                border = BorderStroke(1.dp, colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${leg.carrier} ${leg.flightNumber.trimStart('0')}",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = colorScheme.primary
                        )
                        Text(
                            text = "JULIAN ${leg.flightDateJulian}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.outline
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DetailItem(label = "SEAT", value = leg.seat.trim())
                        DetailItem(label = "SEQ", value = leg.sequenceNumber.trimStart('0'))
                    }
                }
            }
        }

        if (isLastLeg) {
            AirportNode(
                airportName = airportManager.getCity(leg.to),
                icon = Icons.Default.FlightLand,
                label = "Arrival"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AirportNode(airportName: String, icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = colorScheme.primaryContainer,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(6.dp),
                tint = colorScheme.onPrimaryContainer
            )
        }
        Column {
            Text(text = airportName, style = MaterialTheme.typography.titleLargeEmphasized)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.outline
            )
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = colorScheme.outline)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}