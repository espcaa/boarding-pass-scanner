package eu.espcaa.boardingpassscanner.screens

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import eu.espcaa.boardingpassscanner.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScanScreen() {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var isFlashlightOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {

        val cameraProvider = cameraProviderFuture.get()

        val preview = androidx.camera.core.Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }



            val barcodeScanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient()
            val imageAnalysis = androidx.camera.core.ImageAnalysis.Builder()
                .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = com.google.mlkit.vision.common.InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )

                    barcodeScanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                Log.d("MLKit", "Scanned: ${barcode.rawValue}")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("MLKit", "Scan failed", e)
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            Log.e("CameraX", "Binding failed", e)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding( vertical = 64.dp, horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {


            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize().weight(1f).clip(
                    RoundedCornerShape(16.dp)
                ),

            )
            Row(
                modifier = Modifier.padding(top = 32.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        isFlashlightOn = !isFlashlightOn
                        camera?.cameraControl?.enableTorch(isFlashlightOn)
                    },
                    modifier = Modifier.size(96.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isFlashlightOn) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.secondary,
                        contentColor = if (isFlashlightOn) androidx.compose.material3.MaterialTheme.colorScheme.onPrimary else androidx.compose.material3.MaterialTheme.colorScheme.onSecondary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isFlashlightOn) R.drawable.ic_torch_off else R.drawable.ic_torch_on
                        ),
                        contentDescription = "Flashlight On",
                        modifier = Modifier.size(32.dp),
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
        }
    }
}