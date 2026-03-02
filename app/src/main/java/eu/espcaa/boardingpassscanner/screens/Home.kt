package eu.espcaa.boardingpassscanner.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import eu.espcaa.boardingpassscanner.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat

@SuppressLint("UnrememberedMutableState")
@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
fun HomeScreen(onScanClick: () -> Unit) {

    val context = LocalContext.current
    val activity = context as Activity
    val cameraPermission = Manifest.permission.CAMERA

    var permissionTrigger by remember { mutableStateOf(0) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                permissionTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val hasPermission = remember(permissionTrigger) {
        ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED
    }

    var shouldShowRationale by remember(permissionTrigger) {
        mutableStateOf(activity.shouldShowRequestPermissionRationale(cameraPermission))
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onScanClick()
        } else {
            shouldShowRationale = activity.shouldShowRequestPermissionRationale(cameraPermission)
        }
    }

    val buttonText = if (hasPermission || shouldShowRationale) "Scan boarding pass" else "Grant camera permission"

    val infiniteTransition = rememberInfiniteTransition(label = "blob rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    Box {
        if (!hasPermission && !shouldShowRationale) {
            CameraPermissionCard()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                // image container (bg rotating in a clipped img + plane)
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
                Text(
                    text = "Boarding pass scanner",
                    style = MaterialTheme.typography.displayLargeEmphasized,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = {
                        // ask for camera permission
                        if (!hasPermission) {
                            launcher.launch(cameraPermission)
                        } else {
                            onScanClick()
                        }
                    },
                    modifier = Modifier.padding(top = 24.dp),
                    shape = RoundedCornerShape(30),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    enabled = hasPermission || shouldShowRationale
                ) {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.bodyMediumEmphasized,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                // settings
                IconButton(
                    onClick = { /* TODO: Open settings */ },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(64.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Settings icon",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CameraPermissionCard() {

    val context = LocalContext.current

    Card(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    text = "Camera permission is required to scan boarding passes.",
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(1f),
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(
                    onClick = { openAppSettings(context) },
                    modifier = Modifier
                        .padding(10.dp)
                        .size(48.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_open),
                        contentDescription = "Open settings icon button",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

    }
}

fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}