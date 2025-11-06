package dev.veeso.biangbianghanzi.ui.screens.camera

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import dev.veeso.biangbianghanzi.services.OcrBox
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun OcrOverlay(
    boxes: List<OcrBox>,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier,
    isLive: Boolean,
    showPinyin: Boolean,
) {
    val textMeasurer = rememberTextMeasurer()
    val context = LocalContext.current
    val fontFamily = typography.bodySmall.fontFamily

    // highlight selected box
    var highlightedBox by remember { mutableStateOf<OcrBox?>(null) }
    val highlightAlpha by animateFloatAsState(
        targetValue = if (highlightedBox != null) 0.4f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300),
        label = "highlightAlpha"
    )
    // reset highlight after delay
    val resetHighlight =
        LaunchedEffect(highlightedBox) {
            delay(300)
            highlightedBox = null
        }

    val renderedBoxes = remember { mutableListOf<Pair<OcrBox, android.graphics.RectF>>() }
    renderedBoxes.clear()

    Box(
        modifier = modifier
            .pointerInput(boxes, showPinyin, imageWidth, imageHeight) {
                detectTapGestures { offset ->
                    val hit = renderedBoxes.firstOrNull { (_, rect) ->
                        rect.contains(offset.x, offset.y)
                    }?.first
                    hit?.let { box ->
                        val text = if (showPinyin) box.pinyin else box.hanzi
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("OCR text", text))
                        Toast.makeText(context, "Text copied", Toast.LENGTH_SHORT).show()
                        // trigger highlight
                        highlightedBox = box
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Scale factors to match actual image vs displayed size
            val scaleX = size.width / imageWidth
            val scaleY = size.height / imageHeight

            val imageAspect = imageWidth.toFloat() / imageHeight
            val viewAspect = size.width / size.height
            var verticalOffset: Float = 0f
            var horizontalOffset: Float = 0f

            if (isLive) {
                if (imageAspect > viewAspect) {
                    val scaledHeight = size.width / imageAspect
                    verticalOffset = (size.height - scaledHeight) / 4f
                    horizontalOffset = 0f
                } else {
                    val scaledWidth = size.height * imageAspect
                    horizontalOffset = (size.width - scaledWidth) / 2f
                    verticalOffset = 0f
                }
            }

            boxes.forEach { box ->
                val textToDisplay = if (showPinyin) box.pinyin else box.hanzi
                val scaleRatio =
                    if (showPinyin) box.hanzi.length.toFloat() / box.pinyin.length.toFloat() else 1f
                val scaleFactor = scaleRatio.coerceIn(0.6f, 1.0f)
                val boxHeightScaled = if (isLive) {
                    (box.height * scaleY) / imageAspect
                } else {
                    box.height * scaleY
                }
                val dynamicFontSize = (boxHeightScaled * 0.5f * scaleFactor).sp

                val textLayout = textMeasurer.measure(
                    text = textToDisplay,
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = dynamicFontSize,
                        fontFamily = fontFamily
                    )
                )
                val measuredWidth = textLayout.size.width.toFloat()
                val measuredHeight = textLayout.size.height.toFloat()

                val width = max(box.width * scaleX, measuredWidth + 12f)
                val height = max(box.height * scaleY, measuredHeight + 12f)
                val left = box.left * scaleX + horizontalOffset
                val top = box.top * scaleY - verticalOffset

                renderedBoxes.add(
                    box to android.graphics.RectF(left, top, left + width, top + height)
                )

                drawRoundRect(
                    color = Color.White.copy(alpha = 0.9f),
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                if (highlightedBox == box && highlightAlpha > 0f) {
                    drawRoundRect(
                        color = Color(0xFFB0C4DE).copy(alpha = highlightAlpha),
                        topLeft = Offset(left, top),
                        size = Size(width, height),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                }
                drawText(
                    textLayout,
                    topLeft = Offset(left + 6f, top + 6f)
                )
            }
        }
    }

}
