package dev.veeso.biangbianghanzi.services

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.tasks.await

data class OcrBox(
    val text: String,
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int
)

object OcrService {

    private val recognizer =
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    suspend fun recognizeText(
        bitmap: Bitmap,
        transformText: (String) -> String?
    ): List<OcrBox> {
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(image).await()

        return result.textBlocks
            .flatMap { it.lines }
            .flatMap { it.elements }
            .mapNotNull { element ->
                val transformedText = transformText(element.text) ?: return@mapNotNull null
                element.boundingBox?.let { box ->
                    OcrBox(
                        text = transformedText,
                        left = box.left,
                        top = box.top,
                        width = box.width(),
                        height = box.height()
                    )
                }
            }

    }

}


class LiveOcrAnalyzer(
    private val onResult: (List<OcrBox>) -> Unit,
    private val transformText: (String) -> String?
) : ImageAnalysis.Analyzer {


    private val recognizer = TextRecognition.getClient(
        ChineseTextRecognizerOptions.Builder().build()
    )

    private var lastProcessedTime = 0L

    override fun analyze(imageProxy: ImageProxy) {
        val now = System.currentTimeMillis()
        if (now - lastProcessedTime < 1000) {
            imageProxy.close()
            return // skip not elapsed
        }
        lastProcessedTime = now

        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        recognizer.process(image)
            .addOnSuccessListener { result ->
                val boxes = result.textBlocks
                    .flatMap { it.lines }
                    .flatMap { it.elements }
                    .mapNotNull { element ->
                        val transformedText = transformText(element.text) ?: return@mapNotNull null
                        element.boundingBox?.let { box ->
                            OcrBox(
                                text = transformedText,
                                left = box.left,
                                top = box.top,
                                width = box.width(),
                                height = box.height()
                            )
                        }
                    }
                onResult(boxes)
            }
            .addOnFailureListener { /* ignore for now */ }
            .addOnCompleteListener { imageProxy.close() }
    }
}
