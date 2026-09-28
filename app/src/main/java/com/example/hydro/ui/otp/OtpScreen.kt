package com.example.hydro.ui.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hydro.ui.components.BrandHeader
import com.example.hydro.ui.components.BrandHeaderOverlap
import com.example.hydro.ui.theme.HydroTheme

private val ButtonShape = RoundedCornerShape(12.dp)

/**
 * Màn hình xác thực OTP gửi về số điện thoại.
 * [onVerified] được gọi khi nhập đúng mã, [onBack] khi bấm nút quay lại.
 */
@Composable
fun OtpScreen(
    onVerified: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OtpViewModel = viewModel(),
) {
    val state = viewModel.uiState

    LaunchedEffect(state.isVerified) {
        if (state.isVerified) onVerified()
    }

    OtpContent(
        state = state,
        onCodeChange = viewModel::onCodeChange,
        onVerify = viewModel::verify,
        onResend = viewModel::resend,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun OtpContent(
    state: OtpUiState,
    onCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState()),
    ) {
        BrandHeader(
            title = "Xác thực số điện thoại",
            subtitle = "Nhập mã OTP gồm $OTP_LENGTH chữ số chúng tôi vừa gửi qua tin nhắn SMS.",
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .offset(y = -BrandHeaderOverlap)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Mã OTP đã được gửi đến số",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = maskPhone(state.phone),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    Spacer(Modifier.height(24.dp))

                    OtpInput(
                        code = state.code,
                        onCodeChange = onCodeChange,
                        isError = state.error != null,
                        enabled = !state.isVerifying && !state.isLocked,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Dòng thông báo: lỗi (đỏ) hoặc thông tin (màu chính). Luôn giữ chỗ để giao diện không bị giật.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .height(40.dp),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        val message = state.error ?: state.info
                        if (message != null) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (state.error != null) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    ResendRow(state = state, onResend = onResend)

                    Button(
                        onClick = onVerify,
                        enabled = state.canVerify,
                        shape = ButtonShape,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        if (state.isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Xác nhận", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Text(
                text = "Không chia sẻ mã OTP cho bất kỳ ai, kể cả nhân viên ngân hàng.\n(Bản demo: dùng mã $DEMO_OTP)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

/** "Gửi lại mã sau 00:45" khi đang đếm ngược, hết giờ thì thành nút "Gửi lại mã". */
@Composable
private fun ResendRow(state: OtpUiState, onResend: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            state.isResending -> CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )

            state.resendCountdown > 0 -> Text(
                text = "Chưa nhận được mã? Gửi lại sau ${formatCountdown(state.resendCountdown)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            else -> TextButton(onClick = onResend, enabled = state.canResend) {
                Text("Gửi lại mã OTP", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun OtpPreview() {
    HydroTheme {
        OtpContent(
            state = OtpUiState(
                phone = "0912345678",
                code = "123",
                resendCountdown = 45,
            ),
            onCodeChange = {},
            onVerify = {},
            onResend = {},
            onBack = {},
        )
    }
}
