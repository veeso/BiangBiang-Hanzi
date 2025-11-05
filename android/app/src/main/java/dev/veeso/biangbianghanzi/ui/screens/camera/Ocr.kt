package dev.veeso.biangbianghanzi.ui.screens.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import dev.veeso.biangbianghanzi.services.OcrBox
import kotlin.math.max

@Composable
fun OcrOverlay(
    boxes: List<OcrBox>,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier,
    isLive: Boolean
) {
    val textMeasurer = rememberTextMeasurer()
    val fontFamily = typography.bodySmall.fontFamily

    Box(modifier = modifier) {
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
                val boxHeightScaled = if (isLive) {
                    (box.height * scaleY) / imageAspect
                } else {
                    box.height * scaleY
                }
                val dynamicFontSize = (boxHeightScaled * 0.5f).sp

                val textLayout = textMeasurer.measure(
                    text = box.text,
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

                drawRoundRect(
                    color = Color.White.copy(alpha = 0.9f),
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                drawText(
                    textLayout,
                    topLeft = Offset(left + 6f, top + 6f)
                )
            }
        }
    }

}
