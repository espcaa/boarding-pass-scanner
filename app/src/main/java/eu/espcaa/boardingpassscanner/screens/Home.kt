package eu.espcaa.boardingpassscanner.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Text
import androidx.compose.material3.toPath
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import eu.espcaa.boardingpassscanner.R

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
fun HomeScreen(onScanClick: () -> Unit) {

    val infiniteTransition: rememberInfiniteTransition()

    Box(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            // image container (bg rotating in a clipped img + plane)
            Box(
                modifier = Modifier.size(300.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bg),
                    contentDescription = "Rotating background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(MaterialShapes.SoftBurst.toShape())
                )

                Image(
                    painter = painterResource(id = R.drawable.plane_without_bg),
                    contentDescription = "Plane icon",
                    modifier = Modifier.fillMaxSize().graphicsLayer(
                        scaleX = 1.2f,
                        scaleY = 1.2f
                    )
                )
            }
            Text(text = "Boarding pass scanner")
            Button(onClick = onScanClick) {
                Text(text = "Start Scanning")
            }
        }
    }
}
