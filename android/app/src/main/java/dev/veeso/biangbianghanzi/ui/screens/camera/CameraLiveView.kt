package dev.veeso.biangbianghanzi.ui.screens.camera

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ZoomState
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
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
import dev.veeso.biangbianghanzi.models.HistoryVariant
import dev.veeso.biangbianghanzi.services.AppSettingsRepository
import dev.veeso.biangbianghanzi.services.CANTONESE
import dev.veeso.biangbianghanzi.services.LiveOcrAnalyzer
import dev.veeso.biangbianghanzi.services.OcrBox
import dev.veeso.biangbianghanzi.services.OcrService
import dev.veeso.biangbianghanzi.services.SIMPLIFIED_CHINESE
import dev.veeso.biangbianghanzi.services.availablePresets
import dev.veeso.biangbianghanzi.services.clampZoom
import dev.veeso.biangbianghanzi.ui.AppDesign
import dev.veeso.biangbianghanzi.ui.components.CopyToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun CameraLiveView() {
    var frameWidth by remember { mutableIntStateOf(1) }
    var frameHeight by remember { mutableIntStateOf(1) }
    var convertToPinyin by remember { mutableStateOf(true) }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    val ocrBoxes = remember { mutableStateListOf<OcrBox>() }
    val liveOcrBoxes = remember { mutableStateListOf<OcrBox>() }

    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var maxZoom by remember { mutableFloatStateOf(1f) }
    var presets by remember { mutableStateOf(listOf(1f)) }
    var showCopyToast by remember { mutableStateOf(false) }
    var showSavedToast by remember { mutableStateOf(false) }

    LaunchedEffect(showCopyToast) {
        if (showCopyToast) {
            delay(1500)
            showCopyToast = false
        }
    }

    LaunchedEffect(showSavedToast) {
        if (showSavedToast) {
            delay(1500)
            showSavedToast = false
        }
    }

    val context = LocalContext.current
    val settingsRepo = remember { AppSettingsRepository(context) }
    val chineseType by settingsRepo.chineseType.collectAsState(initial = SIMPLIFIED_CHINESE)
    val isCantonese = chineseType == CANTONESE
    val scope = rememberCoroutineScope()

    val saveBox: (OcrBox) -> Unit = { box ->
        scope.launch {
            settingsRepo.addHistory(
                original = box.hanzi,
                transliteration = box.pinyin,
                variant = if (isCantonese) HistoryVariant.CANTONESE
                else HistoryVariant.MANDARIN,
            )
        }
        showSavedToast = true
    }

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

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                LifecycleCameraController.IMAGE_CAPTURE or
                    LifecycleCameraController.IMAGE_ANALYSIS,
            )
            imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
        }
    }

    LaunchedEffect(cameraController) {
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(context),
            analyzer,
        )
        cameraController.bindToLifecycle(lifecycleOwner)
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

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
            controller = cameraController
        }
    }

    LaunchedEffect(capturedImage) {
        ocrBoxes.clear()
        capturedImage?.let { bitmap ->
            ocrBoxes.addAll(OcrService.recognizeHanzi(bitmap))
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let {
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
                .padding(innerPadding),
        ) {
            if (capturedImage == null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { previewView },
                )
                OcrOverlay(
                    boxes = liveOcrBoxes,
                    imageWidth = frameWidth,
                    imageHeight = frameHeight,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            // Pinch-to-zoom. Coexists with OcrOverlay's tap detector
                            // because detectTransformGestures waits for 2+ pointers.
                            detectTransformGestures { _, _, gestureZoom, _ ->
                                if (gestureZoom != 1f) {
                                    applyZoom(zoomRatio * gestureZoom)
                                }
                            }
                        },
                    isLive = true,
                    showPinyin = convertToPinyin,
                    onTextCopied = { showCopyToast = true },
                    onSaveBox = saveBox,
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = AppDesign.sectionSpacing),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (presets.isNotEmpty()) {
                        ZoomPresetBar(
                            presets = presets,
                            zoomRatio = zoomRatio,
                            onSelect = ::applyZoom,
                        )
                    }
                    CaptureControlBar(
                        convertToPinyin = convertToPinyin,
                        onTogglePinyin = { convertToPinyin = !convertToPinyin },
                        onCapture = {
                            capturePhoto(context, cameraController) { bitmap ->
                                capturedImage = bitmap
                            }
                        },
                        onPickGallery = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly,
                                ),
                            )
                        },
                    )
                }
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
                    onTextCopied = { showCopyToast = true },
                    onSaveBox = saveBox,
                )

                FilledTonalIconButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(AppDesign.stackSpacing),
                    onClick = { capturedImage = null },
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close preview")
                }
            }

            val anyBoxes =
                if (capturedImage == null) liveOcrBoxes.isNotEmpty()
                else ocrBoxes.isNotEmpty()
            if (anyBoxes) {
                Text(
                    text = "Tap to copy · long-press to save",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = AppDesign.stackSpacing),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = AppDesign.bottomToolbarPadding),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CopyToast(visible = showSavedToast, message = "Saved to History")
                    CopyToast(visible = showCopyToast)
                }
            }
        }
    }
}

@Composable
private fun ZoomPresetBar(
    presets: List<Float>,
    zoomRatio: Float,
    onSelect: (Float) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppDesign.stackSpacing),
        horizontalArrangement = Arrangement.Center,
    ) {
        presets.forEach { preset ->
            val isActive = abs(zoomRatio - preset) < 0.05f
            FilledTonalIconButton(
                modifier = Modifier
                    .size(AppDesign.tapTarget)
                    .padding(horizontal = 4.dp),
                onClick = { onSelect(preset) },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isActive) AppDesign.accentRed else Color.Unspecified,
                    contentColor = if (isActive) Color.White else Color.Unspecified,
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

@Composable
private fun CaptureControlBar(
    convertToPinyin: Boolean,
    onTogglePinyin: () -> Unit,
    onCapture: () -> Unit,
    onPickGallery: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalIconButton(
            modifier = Modifier.size(48.dp),
            onClick = onTogglePinyin,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (convertToPinyin) AppDesign.accentRed else Color.Unspecified,
                contentColor = if (convertToPinyin) Color.White else Color.Unspecified,
            ),
        ) {
            Icon(
                painter = painterResource(R.drawable.logo_button_ico),
                contentDescription = "Toggle Hanzi conversion",
                modifier = Modifier.padding(8.dp),
            )
        }

        FilledIconButton(
            modifier = Modifier.size(AppDesign.shutterSize),
            onClick = onCapture,
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
            onClick = onPickGallery,
        ) {
            Icon(Icons.Default.Image, contentDescription = "Pick from gallery")
        }
    }
}

