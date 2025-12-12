package xyz.chrismiller.crochetti.ui.screen.qrscanner

import android.Manifest
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import xyz.chrismiller.crochetti.data.transfer.QrError
import xyz.chrismiller.crochetti.domain.model.Pattern
import xyz.chrismiller.crochetti.domain.model.transfer.PatternTransfer
import xyz.chrismiller.crochetti.domain.usecase.DecodePatternQrUseCase
import xyz.chrismiller.crochetti.ui.components.ImportPreviewBottomSheet
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    decodePatternQrUseCase: DecodePatternQrUseCase,
    onPatternImported: (Pattern) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var hasCameraPermission by remember { mutableStateOf(false) }
    var scannedPattern by remember { mutableStateOf<PatternTransfer?>(null) }
    var isScanning by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val importSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    // Request permission on first composition
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Show error in snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            errorMessage = null
            // Re-enable scanning after showing error
            isScanning = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Pattern QR") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!hasCameraPermission) {
                PermissionDeniedContent(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                CameraPreviewWithScanner(
                    isScanning = isScanning,
                    onQrCodeDetected = { rawValue ->
                        if (isScanning) {
                            isScanning = false
                            decodePatternQrUseCase(rawValue)
                                .onSuccess { transfer ->
                                    scannedPattern = transfer
                                }
                                .onFailure { e ->
                                    errorMessage = when (e) {
                                        is QrError.InvalidFormat -> "This doesn't look like a Crochetti pattern QR code."
                                        is QrError.UnsupportedVersion -> "This QR code was made with a newer app version. Please update Crochetti."
                                        is QrError.DecompressionFailed -> "The QR code appears damaged. Try scanning again."
                                        is QrError.ValidationFailed -> e.message
                                        else -> "Failed to read pattern: ${e.message}"
                                    }
                                }
                        }
                    }
                )

                // Scanning overlay
                ScannerOverlay()
            }
        }
    }

    // Import preview bottom sheet
    if (scannedPattern != null) {
        ImportPreviewBottomSheet(
            patternTransfer = scannedPattern!!,
            onImport = { pattern ->
                scannedPattern = null
                onPatternImported(pattern)
            },
            onDismiss = {
                scannedPattern = null
                isScanning = true
            },
            sheetState = importSheetState
        )
    }
}

@Composable
private fun PermissionDeniedContent(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Camera access is needed to scan QR codes containing crochet patterns.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}

@Composable
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun CameraPreviewWithScanner(
    isScanning: Boolean,
    onQrCodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            if (isScanning) {
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )

                                    barcodeScanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            for (barcode in barcodes) {
                                                if (barcode.format == Barcode.FORMAT_QR_CODE) {
                                                    barcode.rawValue?.let { value ->
                                                        onQrCodeDetected(value)
                                                    }
                                                }
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    // Handle camera binding errors
                }

            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun ScannerOverlay() {
    val scanBoxSize = 250.dp
    val cornerRadius = 16.dp
    val overlayColor = Color.Black.copy(alpha = 0.6f)
    val borderColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val boxSizePx = scanBoxSize.toPx()
        val cornerRadiusPx = cornerRadius.toPx()

        // Calculate box position (centered)
        val left = (canvasWidth - boxSizePx) / 2
        val top = (canvasHeight - boxSizePx) / 2
        val right = left + boxSizePx
        val bottom = top + boxSizePx

        // Create path for the scan box cutout
        val cutoutPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(left, top, right, bottom),
                    cornerRadius = CornerRadius(cornerRadiusPx)
                )
            )
        }

        // Draw semi-transparent overlay with cutout
        clipPath(cutoutPath, ClipOp.Difference) {
            drawRect(overlayColor)
        }

        // Draw border around scan box
        drawRoundRect(
            color = borderColor,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(boxSizePx, boxSizePx),
            cornerRadius = CornerRadius(cornerRadiusPx),
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw corner accents
        val accentLength = 30.dp.toPx()
        val accentWidth = 4.dp.toPx()

        // Top-left corner
        drawLine(borderColor, Offset(left, top + cornerRadiusPx), Offset(left, top + cornerRadiusPx + accentLength), strokeWidth = accentWidth)
        drawLine(borderColor, Offset(left + cornerRadiusPx, top), Offset(left + cornerRadiusPx + accentLength, top), strokeWidth = accentWidth)

        // Top-right corner
        drawLine(borderColor, Offset(right, top + cornerRadiusPx), Offset(right, top + cornerRadiusPx + accentLength), strokeWidth = accentWidth)
        drawLine(borderColor, Offset(right - cornerRadiusPx, top), Offset(right - cornerRadiusPx - accentLength, top), strokeWidth = accentWidth)

        // Bottom-left corner
        drawLine(borderColor, Offset(left, bottom - cornerRadiusPx), Offset(left, bottom - cornerRadiusPx - accentLength), strokeWidth = accentWidth)
        drawLine(borderColor, Offset(left + cornerRadiusPx, bottom), Offset(left + cornerRadiusPx + accentLength, bottom), strokeWidth = accentWidth)

        // Bottom-right corner
        drawLine(borderColor, Offset(right, bottom - cornerRadiusPx), Offset(right, bottom - cornerRadiusPx - accentLength), strokeWidth = accentWidth)
        drawLine(borderColor, Offset(right - cornerRadiusPx, bottom), Offset(right - cornerRadiusPx - accentLength, bottom), strokeWidth = accentWidth)
    }

    // Instruction text below the scan box
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.size(scanBoxSize))
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Point camera at a Crochetti QR code",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
