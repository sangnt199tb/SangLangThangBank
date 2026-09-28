package com.example.hydro.ui.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.hydro.R
import com.example.hydro.ui.theme.BrandRed
import com.example.hydro.ui.theme.BrandRedDark

/** Thẻ nội dung bên dưới đè lên phần đầu màu đỏ một đoạn bằng chừng này. */
val BrandHeaderOverlap = 40.dp

/**
 * Phần đầu màu đỏ: logo, tiêu đề và mô tả. Nền kéo dài lên cả thanh trạng thái.
 * Truyền [onBack] để hiện nút quay lại bên trái logo.
 */
@Composable
fun BrandHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    // Nền đỏ nên icon giờ, pin... trên thanh trạng thái phải màu trắng
    StatusBarIcons(darkIcons = false)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Brush.linearGradient(listOf(BrandRed, BrandRedDark)))
            .drawBehind {
                // Hai vòng tròn mờ trang trí ở góc phải
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.width * 0.45f,
                    center = Offset(size.width * 0.95f, size.height * 0.1f),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f),
                    radius = size.width * 0.25f,
                    center = Offset(size.width * 0.7f, size.height * 0.75f),
                )
            }
            .statusBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 28.dp + BrandHeaderOverlap),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = "Quay lại",
                        tint = Color.White,
                    )
                }
            }
            BrandLogo()
        }
        Spacer(Modifier.height(28.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f),
        )
    }
}

/**
 * Đặt màu icon trên thanh trạng thái: [darkIcons] = true cho nền sáng, false cho nền tối/đỏ.
 * Mỗi màn tự gọi hàm này nên khi chuyển màn, màn mới luôn quyết định màu.
 */
@Composable
fun StatusBarIcons(darkIcons: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkIcons
    }
}
