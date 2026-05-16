package dev.veeso.biangbianghanzi.services

import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.tasks.await

data class OcrBox(
    val hanzi: String,
    val pinyin: String,
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int
)

object OcrService {

    private val recognizer =
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    private val textProcessor = TextProcessor()

    suspend fun recognizeHanzi(
        bitmap: Bitmap,
    ): List<OcrBox> {
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(image).await()

        return result.textBlocks
            .flatMap { it.lines }
            .flatMap { it.elements }
            .mapNotNull { element ->
                val pinyin = textProcessor.process(element.text) ?: return@mapNotNull null
                element.boundingBox?.let { box ->
                    OcrBox(
                        hanzi = element.text,
                        pinyin = pinyin,
                        left = box.left,
                        top = box.top,
                        width = box.width(),
                        height = box.height()
                    )
                }
            }
            // Drop spurious giant detections (see LiveOcrAnalyzer).
            .filter { b ->
                b.height < bitmap.height * 0.5f && b.width < bitmap.width * 0.9f
            }

    }

}


class LiveOcrAnalyzer(
    private val onResult: (List<OcrBox>, Int, Int) -> Unit
) : ImageAnalysis.Analyzer {


    private val recognizer = TextRecognition.getClient(
        ChineseTextRecognizerOptions.Builder().build()
    )

    private val textProcessor = TextProcessor()

    private var lastProcessedTime = 0L

    @OptIn(ExperimentalGetImage::class)
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
                        val pinyin =
                            textProcessor.process(element.text) ?: return@mapNotNull null
                        element.boundingBox?.let { box ->
                            OcrBox(
                                hanzi = element.text,
                                pinyin = pinyin,
                                left = box.left,
                                top = box.top,
                                width = box.width(),
                                height = box.height()
                            )
                        }
                    }
                // ML Kit returns bounding boxes in the upright/display
                // coordinate space (rotation already applied), but
                // image.width/height are the unrotated buffer dims. Swap
                // for 90/270 so the overlay scales against the same space
                // the boxes are in.
                val rotated = rotationDegrees == 90 || rotationDegrees == 270
                val uprightWidth = if (rotated) image.height else image.width
                val uprightHeight = if (rotated) image.width else image.height
                // Drop spurious detections: a real text element never spans
                // half the frame height (or nearly its full width). These
                // garbage boxes are what render as random HUGE text.
                val sane = boxes.filter { b ->
                    b.height < uprightHeight * 0.5f && b.width < uprightWidth * 0.9f
                }
                onResult(sane, uprightWidth, uprightHeight)
            }
            .addOnFailureListener { /* ignore for now */ }
            .addOnCompleteListener { imageProxy.close() }
    }
}
