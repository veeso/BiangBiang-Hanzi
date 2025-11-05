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
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
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


@Composable
fun CameraModeView() {

    // states
    var convertToPinyin by remember { mutableStateOf(true) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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
                        LifecycleCameraController.VIDEO_CAPTURE
            )
            imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
        }
    }
    LaunchedEffect(lifecycleOwner) { cameraController.bindToLifecycle(lifecycleOwner) }

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
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            if (capturedImage == null) {
                // Camera preview layer
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            controller = cameraController
                        }
                    }
                )
            } else {
                // show captured image
                Image(
                    bitmap = capturedImage!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
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
                                    cameraController
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
    controller: LifecycleCameraController,
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
    controller.takePicture(
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
