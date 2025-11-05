package dev.veeso.biangbianghanzi.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lens
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.veeso.biangbianghanzi.R
import dev.veeso.biangbianghanzi.services.HanziExtractor
import dev.veeso.biangbianghanzi.services.LiveOcrAnalyzer
import dev.veeso.biangbianghanzi.services.OcrBox
import dev.veeso.biangbianghanzi.services.OcrService
import dev.veeso.biangbianghanzi.services.PinyinConverter
import dev.veeso.biangbianghanzi.ui.screens.camera.OcrOverlay


@Composable
fun CameraModeView() {

    // states
    val extractor = HanziExtractor()
    val pinyinConverter = PinyinConverter()
    var convertToPinyin by remember { mutableStateOf(true) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    val ocrBoxes = remember { mutableStateListOf<OcrBox>() }
    val liveOcrBoxes = remember { mutableStateListOf<OcrBox>() }

    val transformOcr = let@{ text: String ->
        val hanzi = extractor.extract(text) ?: return@let null
        if (convertToPinyin) {
            // convert to pinyin
            pinyinConverter.hanziToPinyin(hanzi)
        } else {
            hanzi
        }
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current


    val analyzer = remember {
        LiveOcrAnalyzer(
            onResult = { newBoxes ->
                liveOcrBoxes.clear()
                liveOcrBoxes.addAll(newBoxes)
            },
            transformText = transformOcr
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

    // --- CameraX controller bound to lifecycle ---
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                LifecycleCameraController.IMAGE_CAPTURE or
                        LifecycleCameraController.VIDEO_CAPTURE or
                        LifecycleCameraController.IMAGE_ANALYSIS
            )
            imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
        }
    }

    LaunchedEffect(cameraController) {
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(context),
            analyzer
        )
    }

    LaunchedEffect(lifecycleOwner) { cameraController.bindToLifecycle(lifecycleOwner) }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
            controller = cameraController
        }
    }

    // add effect on captured image to do OCR
    LaunchedEffect(capturedImage) {
        // always clear
        ocrBoxes.clear()
        capturedImage?.let { bitmap ->
            ocrBoxes.addAll(OcrService.recognizeText(bitmap, transformText = transformOcr));
        }
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) return@LaunchedEffect

        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(ContextCompat.getMainExecutor(context), analyzer)
            }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture,
                analysis
            )
        } catch (exc: Exception) {
            exc.printStackTrace()
        }
    }


    // --- Gallery picker (Photo Picker) ---
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            // decode URI -> Bitmap
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }
            capturedImage = bitmap
        }
    }

    Scaffold() { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (capturedImage == null) {
                // Camera preview layer
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { previewView }
                )
                OcrOverlay(
                    boxes = liveOcrBoxes,
                    imageWidth = previewView.width,
                    imageHeight = previewView.height,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // show captured image
                Image(
                    bitmap = capturedImage!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                OcrOverlay(
                    boxes = ocrBoxes,
                    imageWidth = capturedImage!!.width,
                    imageHeight = capturedImage!!.height,
                    modifier = Modifier.fillMaxSize()
                )

                // reset button
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

            // menu
            if (capturedImage == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Toggle Hanzi conversion (quick)
                        FilledTonalIconButton(
                            modifier = Modifier.size(48.dp),
                            onClick = { convertToPinyin = !convertToPinyin },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (convertToPinyin) Color(0xFFDE2910) else Color.Unspecified,
                                contentColor = if (convertToPinyin) Color.White else Color.Unspecified
                            ),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.logo_button_ico),
                                contentDescription = "Toggle Hanzi conversion",
                                modifier = Modifier.padding(8.dp)

                            )
                        }

                        // Shutter (bigger)
                        FilledIconButton(
                            modifier = Modifier.size(72.dp),
                            onClick = {
                                if (hasCameraPermission) capturePhoto(
                                    context,
                                    imageCapture
                                ) { bitmap ->
                                    capturedImage = bitmap
                                }
                                else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            shape = CircleShape
                        ) {
                            Icon(
                                Icons.Default.Lens,
                                contentDescription = "Shutter",
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        // Pick from gallery
                        FilledTonalIconButton(
                            modifier = Modifier.size(48.dp),
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "Pick from gallery")
                        }
                    }
                }
            }

        }
    }


}

fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onPhotoCaptured: (Bitmap?) -> Unit
) {
    // create file info
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "biangbianghanzi_capture.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/BiangBiangHanzi")
        }
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    // take picture async
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Toast.makeText(context, "Capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                onPhotoCaptured(null)
            }

            override fun onImageSaved(res: ImageCapture.OutputFileResults) {
                val uri = res.savedUri ?: return
                try {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                    onPhotoCaptured(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onPhotoCaptured(null)
                }
            }
        }
    )
}
