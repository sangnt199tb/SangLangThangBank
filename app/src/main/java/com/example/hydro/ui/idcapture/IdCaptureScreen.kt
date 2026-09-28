package com.example.hydro.ui.idcapture

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hydro.R
import com.example.hydro.ui.components.BrandHeader
import com.example.hydro.ui.components.BrandHeaderOverlap
import com.example.hydro.ui.theme.HydroTheme

private val ButtonShape = RoundedCornerShape(12.dp)
private val SlotShape = RoundedCornerShape(12.dp)

/**
 * Màn hình chụp ảnh CCCD hai mặt. Bấm vào từng ô thì mở camera trong app.
 * [onSubmitted] được gọi khi đã gửi ảnh thành công.
 */
@Composable
fun IdCaptureScreen(
    onSubmitted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IdCaptureViewModel = viewModel(),
) {
    val state = viewModel.uiState

    LaunchedEffect(state.isSubmitted) {
        if (state.isSubmitted) {
            onSubmitted()
            viewModel.onSubmittedHandled()
        }
    }

    val cameraSide = state.cameraSide
    if (cameraSide != null) {
        // Đang mở camera: nút Back của điện thoại chỉ đóng camera, không thoát màn
        BackHandler { viewModel.closeCamera() }
        IdCameraScreen(
            side = cameraSide,
            pendingPhoto = state.pendingPhoto,
            error = state.cameraError,
            onPhotoTaken = viewModel::onPhotoTaken,
            onCaptureError = viewModel::onCaptureError,
            onRetake = viewModel::retake,
            onConfirm = viewModel::confirmPhoto,
            onClose = viewModel::closeCamera,
            modifier = modifier,
        )
    } else {
        IdCaptureContent(
            state = state,
            onOpenCamera = viewModel::openCamera,
            onSubmit = viewModel::submit,
            modifier = modifier,
        )
    }
}

@Composable
private fun IdCaptureContent(
    state: IdCaptureUiState,
    onOpenCamera: (IdSide) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        BrandHeader(
            title = "Chụp ảnh CCCD",
            subtitle = "Chụp hai mặt căn cước công dân gắn chip để xác minh danh tính của bạn.",
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
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    IdSide.entries.forEach { side ->
                        IdPhotoSlot(
                            side = side,
                            photo = state.photoOf(side),
                            enabled = !state.isSubmitting,
                            onClick = { onOpenCamera(side) },
                        )
                    }
                }
            }

            CaptureTips()

            Button(
                onClick = onSubmit,
                enabled = state.canSubmit,
                shape = ButtonShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Tiếp tục", fontWeight = FontWeight.SemiBold)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_shield),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Ảnh CCCD chỉ dùng để xác minh danh tính khi mở tài khoản.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

/** Một ô ảnh: chưa chụp thì hiện hình minh hoạ thẻ, chụp rồi thì hiện ảnh kèm nút "Chụp lại". */
@Composable
private fun IdPhotoSlot(
    side: IdSide,
    photo: ImageBitmap?,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = side.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            if (photo != null) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_circle),
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Đã chụp",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.primary,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ID_CARD_RATIO)
                .clip(SlotShape)
                .clickable(enabled = enabled, onClickLabel = "Chụp ${side.label.lowercase()}", onClick = onClick),
        ) {
            if (photo != null) {
                Image(
                    bitmap = photo,
                    contentDescription = "Ảnh ${side.label.lowercase()} CCCD",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camera),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Chụp lại", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            } else {
                IdCardIllustration(side = side, modifier = Modifier.fillMaxSize())
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .background(colors.primary, CircleShape),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_camera),
                            contentDescription = null,
                            tint = colors.onPrimary,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Chạm để chụp ${side.label.lowercase()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primary,
                    )
                }
            }
        }
    }
}

/** Hình minh hoạ thẻ CCCD vẽ bằng Canvas (các khối xám thay cho ảnh, chữ, mã QR...). */
@Composable
private fun IdCardIllustration(side: IdSide, modifier: Modifier = Modifier) {
    val background = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    val border = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    val shapeColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)

    Canvas(modifier = modifier.background(background)) {
        val w = size.width
        val h = size.height
        val radius = CornerRadius(4.dp.toPx())

        fun block(x: Float, y: Float, width: Float, height: Float) = drawRoundRect(
            color = shapeColor,
            topLeft = Offset(w * x, h * y),
            size = Size(w * width, h * height),
            cornerRadius = radius,
        )

        when (side) {
            IdSide.FRONT -> {
                drawCircle(shapeColor, radius = h * 0.08f, center = Offset(w * 0.1f, h * 0.15f)) // quốc huy
                block(0.3f, 0.08f, 0.4f, 0.05f) // dòng tiêu đề
                block(0.35f, 0.16f, 0.3f, 0.04f)
                block(0.82f, 0.06f, 0.12f, 0.19f) // mã QR
                block(0.05f, 0.34f, 0.22f, 0.5f) // ảnh chân dung
                block(0.33f, 0.36f, 0.55f, 0.05f) // các dòng thông tin
                block(0.33f, 0.48f, 0.45f, 0.05f)
                block(0.33f, 0.6f, 0.5f, 0.05f)
                block(0.33f, 0.72f, 0.35f, 0.05f)
            }

            IdSide.BACK -> {
                block(0.06f, 0.1f, 0.13f, 0.19f) // chip
                block(0.06f, 0.38f, 0.5f, 0.05f) // các dòng thông tin
                block(0.06f, 0.5f, 0.4f, 0.05f)
                block(0.64f, 0.08f, 0.14f, 0.36f) // hai ô vân tay
                block(0.81f, 0.08f, 0.14f, 0.36f)
                block(0.06f, 0.68f, 0.88f, 0.05f) // 3 dòng mã MRZ
                block(0.06f, 0.77f, 0.88f, 0.05f)
                block(0.06f, 0.86f, 0.88f, 0.05f)
            }
        }

        drawDashedBorder(border)
    }
}

private fun DrawScope.drawDashedBorder(color: Color) {
    val strokeWidth = 1.5.dp.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
        size = Size(size.width - strokeWidth, size.height - strokeWidth),
        cornerRadius = CornerRadius(12.dp.toPx()),
        style = Stroke(
            width = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())),
        ),
    )
}

@Composable
private fun CaptureTips() {
    val tips = listOf(
        "Dùng CCCD gắn chip bản gốc, còn hạn. Không dùng bản photo hay ảnh chụp màn hình.",
        "Chụp ở nơi đủ sáng. Ảnh rõ nét, không bị lóa sáng hay mờ.",
        "Đặt thẻ nằm trọn trong khung, không để tay che mất góc hay thông tin trên thẻ.",
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(20.dp),
    ) {
        Text(
            text = "Lưu ý khi chụp",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        tips.forEach { tip ->
            Row {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun IdCapturePreview() {
    HydroTheme {
        IdCaptureContent(
            state = IdCaptureUiState(),
            onOpenCamera = {},
            onSubmit = {},
        )
    }
}
