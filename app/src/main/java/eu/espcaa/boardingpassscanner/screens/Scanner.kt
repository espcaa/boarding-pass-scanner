package eu.espcaa.boardingpassscanner.screens

import android.annotation.SuppressLint
import android.util.Log
import android.view.ScaleGestureDetector
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import eu.espcaa.boardingpassscanner.R
import eu.espcaa.boardingpassscanner.parser.BCBPParseResult
import eu.espcaa.boardingpassscanner.parser.JulianBoardingPass
import eu.espcaa.boardingpassscanner.parser.ParseBCBP
import kotlinx.coroutines.launch


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ZoomBubble(
    zoom: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        tonalElevation = 6.dp,
        color = colorScheme.tertiary
    ) {
        Text(
            text = String.format("%.1fx", zoom),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            style = MaterialTheme.typography.labelLargeEmphasized,
            color = colorScheme.onTertiary
        )
    }
}

@Composable
fun BarcodeTracker(
    rect: Rect,
    imageSize: android.util.Size?,
    modifier: Modifier = Modifier,
    rotationDegrees: Int,
) {
    if (imageSize == null || rect.isEmpty) return

    val primaryColor = colorScheme.tertiary

    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {

        val isRotated = rotationDegrees == 90 || rotationDegrees == 270
        val correctedWidth =
            if (isRotated) imageSize.height.toFloat() else imageSize.width.toFloat()
        val correctedHeight =
            if (isRotated) imageSize.width.toFloat() else imageSize.height.toFloat()

        val scaleX = size.width / correctedWidth
        val scaleY = size.height / correctedHeight

        val scale = maxOf(scaleX, scaleY)
        val offsetX = (size.width - correctedWidth * scale) / 2f
        val offsetY = (size.height - correctedHeight * scale) / 2f

        val left = (rect.left * scale) + offsetX
        val top = (rect.top * scale) + offsetY
        val right = (rect.right * scale) + offsetX
        val bottom = (rect.bottom * scale) + offsetY

        drawRoundRect(
            color = primaryColor,
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
        )

        drawRoundRect(
            color = primaryColor.copy(alpha = 0.3f),
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
            style = androidx.compose.ui.graphics.drawscope.Fill,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
        )
    }
}

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun BoardingPassScanner(
    onSuccess: (JulianBoardingPass) -> Unit,
    overlayContent: @Composable (BoxScope.() -> Unit) = {},
    canScan: Boolean = true,
) {

    var rotationDegrees by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var isFlashlightOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    var zoomLevel by remember { mutableFloatStateOf(1f) }

    // barcode overlay variables
    var barcodeRect by remember { mutableStateOf<android.graphics.Rect?>(null) }
    val animatedRect = remember {
        Animatable(
            Rect(0f, 0f, 0f, 0f),
            Rect.VectorConverter
        )
    }

    var imageSize by remember { mutableStateOf<android.util.Size?>(null) }
    var hideJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val scope = rememberCoroutineScope()

    val scaleGestureDetector = remember(camera) {
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val cameraControl = camera?.cameraControl ?: return false
                val zoomState = camera?.cameraInfo?.zoomState?.value ?: return false

                val currentZoomRatio = zoomState.zoomRatio
                val delta = detector.scaleFactor
                cameraControl.setZoomRatio(currentZoomRatio * delta)
                zoomLevel = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
                return true
            }
        })
    }


    val previewView = remember {
        PreviewView(context).apply {
            setOnTouchListener { view, event ->
                scaleGestureDetector.onTouchEvent(event)

                if (event.action == android.view.MotionEvent.ACTION_UP) {
                    val factory = this.meteringPointFactory

                    val point = factory.createPoint(event.x, event.y)

                    val action = androidx.camera.core.FocusMeteringAction.Builder(
                        point,
                        androidx.camera.core.FocusMeteringAction.FLAG_AF
                                or androidx.camera.core.FocusMeteringAction.FLAG_AE
                    )
                        .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                        .build()

                    camera?.cameraControl?.startFocusAndMetering(action)

                    view.performClick()
                }
                true
            }
        }
    }

    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {

        val cameraProvider = cameraProviderFuture.get()

        val preview = androidx.camera.core.Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val options = com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417,
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC,
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE
            )
            .build()
        val barcodeScanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient(options)
        val imageAnalysis = androidx.camera.core.ImageAnalysis.Builder()
            .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->

            @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)

            val mediaImage = imageProxy.image
            if (mediaImage != null) {

                rotationDegrees = imageProxy.imageInfo.rotationDegrees

                val image = com.google.mlkit.vision.common.InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )


                barcodeScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val firstBarcode = barcodes.firstOrNull()
                        if (firstBarcode != null) {
                            hideJob?.cancel()
                            barcodeRect = firstBarcode.boundingBox
                            imageSize = android.util.Size(image.width, image.height)
                            if (canScan) {
                                firstBarcode.rawValue?.let { rawData ->
                                    // don't do it if the bottom sheet is alr opened :pensive:
                                    handleSuccessfulScan(rawData, onSuccess = {
                                        onSuccess(it)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    })
                                }
                            }
                        } else {
                            if (hideJob?.isActive != true) {
                                hideJob = scope.launch {
                                    kotlinx.coroutines.delay(1000)
                                    barcodeRect = null
                                    // make it disappear
                                    animatedRect.snapTo(
                                        androidx.compose.ui.geometry.Rect(
                                            0f,
                                            0f,
                                            0f,
                                            0f
                                        )
                                    )
                                }
                            }
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
            zoomLevel = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
        } catch (e: Exception) {
            Log.e("CameraX", "Binding failed", e)
        }
    }

    LaunchedEffect(barcodeRect) {
        barcodeRect?.let { rect ->
            animatedRect.animateTo(
                targetValue = androidx.compose.ui.geometry.Rect(
                    left = rect.left.toFloat(),
                    top = rect.top.toFloat(),
                    right = rect.right.toFloat(),
                    bottom = rect.bottom.toFloat()
                ),
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Column() {
            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                AndroidView(
                    factory = { previewView },
                )

                BarcodeTracker(
                    rect = animatedRect.value,
                    imageSize = imageSize,
                    rotationDegrees = rotationDegrees
                )


                ZoomBubble(
                    zoom = zoomLevel,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }

            Row(
                modifier = Modifier
                    .padding(vertical = 32.dp)
                    .navigationBarsPadding()
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        isFlashlightOn = !isFlashlightOn
                        camera?.cameraControl?.enableTorch(isFlashlightOn)
                    },
                    modifier = Modifier.size(96.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isFlashlightOn) colorScheme.primary else colorScheme.secondary,
                        contentColor = if (isFlashlightOn) colorScheme.onPrimary else colorScheme.onSecondary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isFlashlightOn) R.drawable.ic_torch_off else R.drawable.ic_torch_on
                        ),
                        contentDescription = "Flashlight On",
                        modifier = Modifier.size(32.dp),
                        tint = colorScheme.onTertiary
                    )
                }
            }
        }

        overlayContent()
    }
}

fun handleSuccessfulScan(rawData: String, onSuccess: (JulianBoardingPass) -> Unit = {}) {
    val bcbpParseResult: BCBPParseResult = ParseBCBP(rawData)
    if (bcbpParseResult.errors.isEmpty() && bcbpParseResult.boardingPass != null) {
        onSuccess(bcbpParseResult.boardingPass)
    } else {
        Log.e("ScanScreen", "Failed to parse boarding pass: ${bcbpParseResult.errors}")
    }
}
