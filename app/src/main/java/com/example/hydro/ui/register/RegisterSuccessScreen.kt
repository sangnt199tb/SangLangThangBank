package com.example.hydro.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hydro.R
import com.example.hydro.ui.components.StatusBarIcons
import com.example.hydro.ui.theme.HydroTheme

/** Tên tham số tên đăng nhập trên đường dẫn điều hướng, ví dụ "register_success/nguyenvan.an". */
const val USERNAME_ARG = "username"

/**
 * Màn báo đăng ký thành công, hiện tên đăng nhập vừa tạo.
 * Chỉ hiển thị, không có logic nên không cần ViewModel. [onStart] được gọi khi bấm "Bắt đầu sử dụng".
 */
@Composable
fun RegisterSuccessScreen(
    username: String,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Nền sáng thì icon thanh trạng thái màu tối (và ngược lại khi dùng giao diện tối)
    StatusBarIcons(darkIcons = !isSystemInDarkTheme())

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(128.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(88.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp),
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = "Đăng ký thành công!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Chúc mừng bạn đã mở tài khoản SangLangThangBank. " +
                "Hãy dùng tên đăng nhập và mật khẩu vừa tạo để đăng nhập ứng dụng.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = "Tên đăng nhập",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = username,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onStart,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("Bắt đầu sử dụng", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun RegisterSuccessPreview() {
    HydroTheme {
        RegisterSuccessScreen(username = "nguyenvan.an", onStart = {})
    }
}
