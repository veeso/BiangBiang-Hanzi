package dev.veeso.biangbianghanzi.ui.screens.camera

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import dev.veeso.biangbianghanzi.services.OcrBox
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

@Composable
fun OcrOverlay(
    boxes: List<OcrBox>,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier,
    isLive: Boolean,
    showPinyin: Boolean,
    onTextCopied: (() -> Unit)? = null,
) {
    val textMeasurer = rememberTextMeasurer()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val fontFamily = typography.bodySmall.fontFamily

    var highlightedBox by remember { mutableStateOf<OcrBox?>(null) }
    val highlightAlpha by animateFloatAsState(
        targetValue = if (highlightedBox != null) 0.4f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "highlightAlpha",
    )
    LaunchedEffect(highlightedBox) {
        if (highlightedBox != null) {
            delay(300)
            highlightedBox = null
        }
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
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        highlightedBox = box
                        onTextCopied?.invoke()
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Map ML Kit image-pixel coords to view coords using the SAME
            // transform the preview uses, so boxes align with what's on screen:
            //  - live preview: PreviewView.ScaleType.FILL_CENTER -> aspect-fill
            //    (uniform scale = max, overflow cropped, centered)
            //  - captured image: ContentScale.Fit -> aspect-fit
            //    (uniform scale = min, letterboxed, centered)
            // A single uniform scale + centered offsets covers both: the offset
            // is negative when filling (cropped) and positive when fitting.
            val scale = if (isLive) {
                max(size.width / imageWidth, size.height / imageHeight)
            } else {
                min(size.width / imageWidth, size.height / imageHeight)
            }
            val offsetX = (size.width - imageWidth * scale) / 2f
            val offsetY = (size.height - imageHeight * scale) / 2f

            boxes.forEach { box ->
                val textToDisplay = if (showPinyin) box.pinyin else box.hanzi
                val scaleRatio =
                    if (showPinyin) box.hanzi.length.toFloat() / box.pinyin.length.toFloat() else 1f
                val scaleFactor = scaleRatio.coerceIn(0.6f, 1.0f)
                // Font tracks the on-screen box height, with a 12sp floor
                // (matches iOS RecognizedTextOverlay.minFontSize).
                val dynamicFontSize =
                    (box.height * scale * 0.4f * scaleFactor).coerceAtLeast(12f).sp

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

                val width = max(box.width * scale, measuredWidth + 12f)
                val height = max(box.height * scale, measuredHeight + 12f)
                val left = box.left * scale + offsetX
                val top = box.top * scale + offsetY

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
