package dev.veeso.biangbianghanzi.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ZoomState
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lens
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.veeso.biangbianghanzi.R
import dev.veeso.biangbianghanzi.capturePhoto
import dev.veeso.biangbianghanzi.services.LiveOcrAnalyzer
import dev.veeso.biangbianghanzi.services.OcrBox
import dev.veeso.biangbianghanzi.services.OcrService
import dev.veeso.biangbianghanzi.services.availablePresets
import dev.veeso.biangbianghanzi.services.clampZoom
import dev.veeso.biangbianghanzi.ui.screens.camera.OcrOverlay
import kotlin.math.abs

@Composable
fun CameraModeView() {
    var frameWidth by remember { mutableIntStateOf(1) }
    var frameHeight by remember { mutableIntStateOf(1) }
    var convertToPinyin by remember { mutableStateOf(true) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    val ocrBoxes = remember { mutableStateListOf<OcrBox>() }
    val liveOcrBoxes = remember { mutableStateListOf<OcrBox>() }

    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var maxZoom by remember { mutableFloatStateOf(1f) }
    var presets by remember { mutableStateOf(listOf(1f)) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val analyzer = remember {
        LiveOcrAnalyzer(
            onResult = { newBoxes, w, h ->
                liveOcrBoxes.clear()
                liveOcrBoxes.addAll(newBoxes)
                frameWidth = w
                frameHeight = h
            },
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                LifecycleCameraController.IMAGE_CAPTURE or
                        LifecycleCameraController.IMAGE_ANALYSIS
            )
            imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
        }
    }

    LaunchedEffect(cameraController) {
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(context),
            analyzer,
        )
    }

    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            cameraController.bindToLifecycle(lifecycleOwner)
        }
    }

    DisposableEffect(cameraController, lifecycleOwner) {
        val observer = Observer<ZoomState> { state ->
            maxZoom = state.maxZoomRatio
            presets = availablePresets(maxZoom = maxZoom)
            zoomRatio = state.zoomRatio
        }
        cameraController.zoomState.observe(lifecycleOwner, observer)
        onDispose { cameraController.zoomState.removeObserver(observer) }
    }

    fun applyZoom(newZoom: Float) {
        val clamped = clampZoom(newZoom, 1f, maxZoom)
        cameraController.setZoomRatio(clamped)
        zoomRatio = clamped
    }

    val scaleGestureDetector = remember {
        ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    applyZoom(zoomRatio * detector.scaleFactor)
                    return true
                }
            },
        )
    }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
            controller = cameraController
        }
    }

    // Wrap PreviewView in a FrameLayout that intercepts multi-touch so the
    // ScaleGestureDetector receives pinch events. PreviewView's internal
    // SurfaceView consumes touches otherwise.
    val previewContainer = remember {
        object : FrameLayout(context) {
            override fun onInterceptTouchEvent(ev: MotionEvent): Boolean =
                ev.pointerCount > 1
        }.apply {
            addView(
                previewView,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ),
            )
            setOnTouchListener { _, event ->
                scaleGestureDetector.onTouchEvent(event)
                true
            }
        }
    }

    LaunchedEffect(capturedImage) {
        ocrBoxes.clear()
        capturedImage?.let { bitmap ->
            ocrBoxes.addAll(OcrService.recognizeHanzi(bitmap))
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { it ->
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
            }.onSuccess { capturedImage = it }
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (capturedImage == null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { previewContainer },
                )
                OcrOverlay(
                    boxes = liveOcrBoxes,
                    imageWidth = frameWidth,
                    imageHeight = frameHeight,
                    modifier = Modifier.fillMaxSize(),
                    isLive = true,
                    showPinyin = convertToPinyin,
                )
            } else {
                Image(
                    bitmap = capturedImage!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )

                OcrOverlay(
                    boxes = ocrBoxes,
                    imageWidth = capturedImage!!.width,
                    imageHeight = capturedImage!!.height,
                    modifier = Modifier.fillMaxSize(),
                    isLive = false,
                    showPinyin = convertToPinyin,
                )

                FilledTonalIconButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    onClick = { capturedImage = null },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(),
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            if (capturedImage == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (presets.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            presets.forEach { preset ->
                                val isActive = abs(zoomRatio - preset) < 0.05f
                                FilledTonalIconButton(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .padding(horizontal = 4.dp),
                                    onClick = { applyZoom(preset) },
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor =
                                            if (isActive) Color(0xFFDE2910) else Color.Unspecified,
                                        contentColor =
                                            if (isActive) Color.White else Color.Unspecified,
                                    ),
                                ) {
                                    Text(
                                        text = "${preset.toInt()}x",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilledTonalIconButton(
                            modifier = Modifier.size(48.dp),
                            onClick = { convertToPinyin = !convertToPinyin },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor =
                                    if (convertToPinyin) Color(0xFFDE2910) else Color.Unspecified,
                                contentColor =
                                    if (convertToPinyin) Color.White else Color.Unspecified,
                            ),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.logo_button_ico),
                                contentDescription = "Toggle Hanzi conversion",
                                modifier = Modifier.padding(8.dp),
                            )
                        }

                        FilledIconButton(
                            modifier = Modifier.size(72.dp),
                            onClick = {
                                if (hasCameraPermission) {
                                    capturePhoto(context, cameraController) { bitmap ->
                                        capturedImage = bitmap
                                    }
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            shape = CircleShape,
                        ) {
                            Icon(
                                Icons.Default.Lens,
                                contentDescription = "Shutter",
                                modifier = Modifier.size(40.dp),
                            )
                        }

                        FilledTonalIconButton(
                            modifier = Modifier.size(48.dp),
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly,
                                    ),
                                )
                            },
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "Pick from gallery")
                        }
                    }
                }
            }
        }
    }
}
