package eu.espcaa.boardingpassscanner.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import eu.espcaa.boardingpassscanner.R
import eu.espcaa.boardingpassscanner.parser.JulianBoardingPass
import eu.espcaa.boardingpassscanner.utils.AirlineManager
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScanner(
    airlineManager: AirlineManager = koinInject(),
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
            ResultSheetContent(boardingPass = scannedPass!!, airlineManager = airlineManager)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ResultSheetContent(boardingPass: JulianBoardingPass, airlineManager: AirlineManager) {

    Column {
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
                        bottomStart = 16.dp
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
                        bottomEnd = 16.dp
                    ),
                    onClick = {},
                    modifier = Modifier.padding(horizontal = 1.dp),
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
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${boardingPass.legs.first().from} → ${boardingPass.legs.last().to}",
                style = MaterialTheme.typography.titleLargeEmphasized
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // cards for each leg
            boardingPass.legs.forEach { leg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = when {
                        boardingPass.legs.size == 1 -> {
                            RoundedCornerShape(16.dp)
                        }

                        leg == boardingPass.legs.first() -> {
                            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        }

                        leg == boardingPass.legs.last() -> {
                            RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        }

                        else -> {
                            RoundedCornerShape(0.dp)
                        }
                    },
                    color = colorScheme.surfaceBright,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "${leg.carrier} ${leg.flightNumber}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "From ${leg.from} to ${leg.to}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Seat: ${leg.seat}, Date: ${leg.flightDateJulian}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
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