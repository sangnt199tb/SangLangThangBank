package com.example.hydro.ui.nfc

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hydro.R
import com.example.hydro.ui.components.BrandHeader
import com.example.hydro.ui.components.BrandHeaderOverlap
import com.example.hydro.ui.theme.HydroTheme

private val ButtonShape = RoundedCornerShape(12.dp)

/** Tình trạng NFC của điện thoại. */
enum class NfcStatus { UNSUPPORTED, DISABLED, ENABLED }

/**
 * Màn quét chip CCCD bằng NFC.
 * [onCompleted] được gọi khi đọc chip xong, [onBack] khi bấm nút quay lại.
 */
@Composable
fun NfcScanScreen(
    onCompleted: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NfcScanViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state = viewModel.uiState

    // Kiểm tra lại mỗi lần quay về app, vì khách có thể vừa vào Cài đặt bật NFC
    var nfcStatus by remember { mutableStateOf(context.nfcStatus()) }
    LifecycleResumeEffect(Unit) {
        nfcStatus = context.nfcStatus()
        onPauseOrDispose { }
    }

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            onCompleted()
            viewModel.onCompletedHandled()
        }
    }

    NfcScanContent(
        state = state,
        nfcStatus = nfcStatus,
        onStartScan = viewModel::startScan,
        onCancelScan = viewModel::cancelScan,
        onContinue = viewModel::continueNext,
        onOpenNfcSettings = { context.openNfcSettings() },
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun NfcScanContent(
    state: NfcScanUiState,
    nfcStatus: NfcStatus,
    onStartScan: () -> Unit,
    onCancelScan: () -> Unit,
    onContinue: () -> Unit,
    onOpenNfcSettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        BrandHeader(
            title = "Quét chip CCCD",
            subtitle = "Đọc thông tin từ chip trên căn cước công dân bằng NFC để xác minh danh tính.",
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
                    if (nfcStatus != NfcStatus.ENABLED) {
                        NfcStatusBanner(status = nfcStatus, onOpenNfcSettings = onOpenNfcSettings)
                        Spacer(Modifier.height(16.dp))
                    }

                    NfcIllustration(phase = state.phase)

                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = phaseTitle(state.phase),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = when (state.phase) {
                            NfcPhase.READING -> readingStepLabel(state.progress)
                            NfcPhase.ERROR -> state.error.orEmpty()
                            else -> phaseDescription(state.phase)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (state.phase == NfcPhase.ERROR) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center,
                    )

                    // Thanh tiến độ luôn giữ chỗ để giao diện không bị giật khi chuyển bước
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .height(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (state.phase == NfcPhase.READING) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { state.progress / 100f },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp),
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = "${state.progress}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    when (state.phase) {
                        NfcPhase.IDLE, NfcPhase.ERROR -> Button(
                            onClick = onStartScan,
                            // NFC đang tắt thì phải bật trước. Máy không có NFC vẫn cho quét vì đang giả lập.
                            enabled = nfcStatus != NfcStatus.DISABLED,
                            shape = ButtonShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                        ) {
                            Text(
                                text = if (state.phase == NfcPhase.ERROR) "Thử lại" else "Bắt đầu quét",
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        NfcPhase.WAITING, NfcPhase.READING -> OutlinedButton(
                            onClick = onCancelScan,
                            shape = ButtonShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                        ) {
                            Text("Huỷ", fontWeight = FontWeight.SemiBold)
                        }

                        NfcPhase.SUCCESS -> Button(
                            onClick = onContinue,
                            shape = ButtonShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                        ) {
                            Text("Tiếp tục", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            ScanGuide()

            Text(
                text = "(Bản demo: quét giả lập, chưa dùng SDK đọc chip)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

private fun phaseTitle(phase: NfcPhase): String = when (phase) {
    NfcPhase.IDLE -> "Sẵn sàng quét chip"
    NfcPhase.WAITING -> "Áp CCCD vào điện thoại"
    NfcPhase.READING -> "Đang đọc chip"
    NfcPhase.SUCCESS -> "Đọc chip thành công"
    NfcPhase.ERROR -> "Không đọc được chip"
}

private fun phaseDescription(phase: NfcPhase): String = when (phase) {
    NfcPhase.IDLE -> "Bấm \"Bắt đầu quét\" rồi áp mặt sau CCCD vào lưng điện thoại."
    NfcPhase.WAITING -> "Đặt mặt sau CCCD (mặt có chip) sát lưng điện thoại, gần camera."
    NfcPhase.SUCCESS -> "Đang chuyển sang bước xác nhận thông tin..."
    NfcPhase.READING, NfcPhase.ERROR -> ""
}

/** Vòng tròn biểu tượng NFC ở giữa. Khi đang quét có các vòng sóng lan ra. */
@Composable
private fun NfcIllustration(phase: NfcPhase) {
    val colors = MaterialTheme.colorScheme
    val (circleColor, iconRes) = when (phase) {
        NfcPhase.SUCCESS -> colors.primary to R.drawable.ic_check
        NfcPhase.ERROR -> colors.error to R.drawable.ic_close
        else -> colors.primary to R.drawable.ic_nfc
    }
    val isScanning = phase == NfcPhase.WAITING || phase == NfcPhase.READING

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(168.dp)) {
        if (isScanning) {
            PulseWaves(color = colors.primary)
        } else {
            Box(
                Modifier
                    .size(128.dp)
                    .background(circleColor.copy(alpha = 0.12f), CircleShape),
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(88.dp)
                .background(circleColor, CircleShape),
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp),
            )
        }
    }
}

/** Hai vòng sóng to dần rồi mờ đi, lặp lại liên tục. */
@Composable
private fun PulseWaves(color: Color) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
        label = "pulseProgress",
    )
    Canvas(Modifier.fillMaxSize()) {
        val minRadius = 44.dp.toPx()
        val maxRadius = size.minDimension / 2
        // Vòng thứ hai chậm hơn nửa nhịp để sóng nối tiếp nhau
        listOf(progress, (progress + 0.5f) % 1f).forEach { p ->
            drawCircle(
                color = color.copy(alpha = 0.35f * (1f - p)),
                radius = minRadius + (maxRadius - minRadius) * p,
            )
        }
    }
}

@Composable
private fun NfcStatusBanner(status: NfcStatus, onOpenNfcSettings: () -> Unit) {
    val message = when (status) {
        NfcStatus.DISABLED -> "NFC đang tắt. Vui lòng bật NFC để quét chip."
        // TODO: khi dùng SDK thật thì máy không có NFC phải dừng ở đây, không cho quét
        else -> "Điện thoại không hỗ trợ NFC. Bản demo vẫn cho quét giả lập."
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
            .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
        )
        if (status == NfcStatus.DISABLED) {
            TextButton(onClick = onOpenNfcSettings) {
                Text("Bật NFC", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ScanGuide() {
    val steps = listOf(
        "Tháo ốp lưng điện thoại nếu ốp dày hoặc có kim loại.",
        "Đặt mặt sau CCCD (mặt có chip) sát lưng điện thoại, gần vị trí camera.",
        "Giữ yên thẻ khoảng 5–10 giây đến khi đọc xong. Nếu không nhận thẻ, thử dịch thẻ lên xuống từ từ.",
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        Text(
            text = "Hướng dẫn quét chip",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        steps.forEachIndexed { index, step ->
            Row {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(22.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun Context.nfcStatus(): NfcStatus {
    val adapter = NfcAdapter.getDefaultAdapter(this) ?: return NfcStatus.UNSUPPORTED
    return if (adapter.isEnabled) NfcStatus.ENABLED else NfcStatus.DISABLED
}

/** Mở trang bật NFC. Vài dòng máy không có trang riêng thì mở trang cài đặt kết nối chung. */
private fun Context.openNfcSettings() {
    try {
        startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
    } catch (e: ActivityNotFoundException) {
        startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun NfcScanReadingPreview() {
    HydroTheme {
        NfcScanContent(
            state = NfcScanUiState(phase = NfcPhase.READING, progress = 45),
            nfcStatus = NfcStatus.ENABLED,
            onStartScan = {},
            onCancelScan = {},
            onContinue = {},
            onOpenNfcSettings = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun NfcScanDisabledPreview() {
    HydroTheme {
        NfcScanContent(
            state = NfcScanUiState(),
            nfcStatus = NfcStatus.DISABLED,
            onStartScan = {},
            onCancelScan = {},
            onContinue = {},
            onOpenNfcSettings = {},
            onBack = {},
        )
    }
}
