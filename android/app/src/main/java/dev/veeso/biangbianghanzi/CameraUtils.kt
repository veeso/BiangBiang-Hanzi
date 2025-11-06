package dev.veeso.biangbianghanzi

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import java.io.File


fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onPhotoCaptured: (Bitmap?) -> Unit
) {

    val photoFile = File(context.cacheDir, "capture.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

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
