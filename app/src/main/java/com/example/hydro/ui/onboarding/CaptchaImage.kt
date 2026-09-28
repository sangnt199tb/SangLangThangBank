package com.example.hydro.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

private val CaptchaBackground = Color(0xFFF1F1F4)
private val CaptchaPalette = listOf(
    Color(0xFFB71C1C),
    Color(0xFF0D47A1),
    Color(0xFF1B5E20),
    Color(0xFF4A148C),
    Color(0xFFE65100),
)

/** Các giá trị ngẫu nhiên (góc xoay, màu, nét nhiễu) dùng để vẽ một mã captcha. */
private class CaptchaStyle(
    val rotations: List<Float>,
    val offsetsY: List<Float>,
    val colors: List<Color>,
    val lines: List<Pair<Offset, Offset>>,
    val dots: List<Offset>,
)

private fun randomCaptchaStyle(length: Int) = CaptchaStyle(
    rotations = List(length) { Random.nextInt(-25, 26).toFloat() },
    offsetsY = List(length) { Random.nextFloat() * 2 - 1 },
    colors = List(length) { CaptchaPalette.random() },
    // Toạ độ tính theo tỉ lệ 0..1 của khung vẽ
    lines = List(4) { Offset(Random.nextFloat(), Random.nextFloat()) to Offset(Random.nextFloat(), Random.nextFloat()) },
    dots = List(40) { Offset(Random.nextFloat(), Random.nextFloat()) },
)

/**
 * Vẽ mã captcha thành hình: mỗi ký tự lệch và xoay ngẫu nhiên, có thêm nét và chấm nhiễu.
 */
@Composable
fun CaptchaImage(text: String, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    // Chỉ tạo kiểu vẽ mới khi mã đổi, để hình không "nhảy" mỗi lần giao diện vẽ lại
    val captchaStyle = remember(text) { randomCaptchaStyle(text.length) }

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CaptchaBackground)
            .semantics { contentDescription = "Ảnh mã captcha" }
    ) {
        if (text.isEmpty()) return@Canvas

        captchaStyle.lines.forEach { (start, end) ->
            drawLine(
                color = Color.Gray.copy(alpha = 0.6f),
                start = Offset(start.x * size.width, start.y * size.height),
                end = Offset(end.x * size.width, end.y * size.height),
                strokeWidth = 1.5.dp.toPx(),
            )
        }
        captchaStyle.dots.forEach { dot ->
            drawCircle(
                color = Color.DarkGray.copy(alpha = 0.4f),
                radius = 1.dp.toPx(),
                center = Offset(dot.x * size.width, dot.y * size.height),
            )
        }

        val slotWidth = size.width / text.length
        text.forEachIndexed { index, char ->
            val layout = textMeasurer.measure(
                text = char.toString(),
                style = TextStyle(
                    color = captchaStyle.colors[index],
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                ),
            )
            val x = slotWidth * index + (slotWidth - layout.size.width) / 2
            val y = (size.height - layout.size.height) / 2 + captchaStyle.offsetsY[index] * size.height * 0.12f
            val center = Offset(x + layout.size.width / 2f, y + layout.size.height / 2f)
            rotate(degrees = captchaStyle.rotations[index], pivot = center) {
                drawText(layout, topLeft = Offset(x, y))
            }
        }
    }
}
