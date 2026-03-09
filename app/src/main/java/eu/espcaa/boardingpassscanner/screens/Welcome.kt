package eu.espcaa.boardingpassscanner.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.espcaa.boardingpassscanner.R

@SuppressLint("UnrememberedMutableState")
@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
fun WelcomeScreen(onNextClick: () -> Unit, modifier: Modifier = Modifier) {


    val infiniteTransition = rememberInfiniteTransition(label = "blob rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.size(300.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .rotate(rotation),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.bg),
                            contentDescription = "Rotating background",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(200.dp)
                                .clip(MaterialShapes.SoftBurst.toShape())
                                .rotate(-rotation)

                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.plane_without_bg),
                        contentDescription = "Plane icon",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = 1.2f,
                                scaleY = 1.2f
                            )
                    )
                }
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Flightprint",
                        style = MaterialTheme.typography.displayLargeEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "All of your flights in one app!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Button(
                        onClick = onNextClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            "Start with a boarding pass",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    OutlinedButton(
                        onClick = { /* Action */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text("Create your profile", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}