package com.example.hydro.ui.idcapture

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.hydro.R
import com.example.hydro.ui.components.StatusBarIcons
import com.example.hydro.ui.theme.HydroTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Ảnh sau khi cắt được thu nhỏ về chiều rộng này: đủ nét để đọc chữ mà không tốn bộ nhớ. */
private const val MAX_PHOTO_WIDTH = 1600

private val FrameCorner = 16.dp
private val ButtonShape = RoundedCornerShape(12.dp)

/**
 * Màn camera chụp một mặt CCCD: khung căn thẻ, nút chụp, rồi xem lại ảnh vừa chụp.
 * Tự xin quyền camera khi mở. Ảnh trả về qua [onPhotoTaken] đã được cắt đúng phần trong khung.
 */
@Composable
fun IdCameraScreen(
    side: IdSide,
    pendingPhoto: ImageBitmap?,
    error: String?,
    onPhotoTaken: (ImageBitmap) -> Unit,
    onCaptureError: (String) -> Unit,
    onRetake: () -> Unit,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasPermission by remember { mutableStateOf(context.hasCameraPermission()) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasPermission = granted }

    // Mở màn là hỏi quyền ngay. Mỗi lần quay lại app (ví dụ từ Cài đặt) thì kiểm tra lại quyền.
    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    LifecycleResumeEffect(Unit) {
        hasPermission = context.hasCameraPermission()
        onPauseOrDispose { }
    }

    // CameraX: bộ điều khiển camera tự bật/tắt theo vòng đời màn hình
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }
    DisposableEffect(lifecycleOwner, hasPermission) {
        if (hasPermission) controller.bindToLifecycle(lifecycleOwner)
        onDispose { controller.unbind() }
    }

    var isCapturing by remember { mutableStateOf(false) }

    IdCameraContent(
        side = side,
        pendingPhoto = pendingPhoto,
        error = error,
        hasPermission = hasPermission,
        isCapturing = isCapturing,
        cameraPreview = { previewModifier ->
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        this.controller = controller
                    }
                },
                modifier = previewModifier,
            )
        },
        onShutter = { viewWidth, viewHeight ->
            isCapturing = true
            controller.takePicture(
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        scope.launch {
                            // Xoay, cắt, thu nhỏ ảnh là việc nặng nên làm ở luồng nền
                            val photo = withContext(Dispatchers.Default) {
                                try {
                                    image.toCardPhoto(viewWidth, viewHeight)
                                } finally {
                                    image.close()
                                }
                            }
                            isCapturing = false
                            onPhotoTaken(photo)
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        isCapturing = false
                        onCaptureError("Không chụp được ảnh. Vui lòng thử lại.")
                    }
                },
            )
        },
        onRetake = onRetake,
        onConfirm = onConfirm,
        onClose = onClose,
        onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
        onOpenSettings = { context.openAppSettings() },
        modifier = modifier,
    )
}

@Composable
private fun IdCameraContent(
    side: IdSide,
    pendingPhoto: ImageBitmap?,
    error: String?,
    hasPermission: Boolean,
    isCapturing: Boolean,
    cameraPreview: @Composable (Modifier) -> Unit,
    onShutter: (viewWidth: Float, viewHeight: Float) -> Unit,
    onRetake: () -> Unit,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Nền camera tối nên icon thanh trạng thái màu trắng
    StatusBarIcons(darkIcons = false)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        val density = LocalDensity.current
        val viewWidth = constraints.maxWidth.toFloat()
        val viewHeight = constraints.maxHeight.toFloat()
        val frame = cardFrameBox(viewWidth, viewHeight)
        val frameTop = with(density) { frame.top.toDp() }
        val frameBottom = with(density) { frame.bottom.toDp() }
        val frameLeft = with(density) { frame.left.toDp() }
        val frameWidth = with(density) { frame.width.toDp() }
        val frameHeight = with(density) { frame.height.toDp() }
        val isReviewing = pendingPhoto != null

        if (hasPermission) {
            cameraPreview(Modifier.fillMaxSize())
        }

        if (pendingPhoto != null) {
            Image(
                bitmap = pendingPhoto,
                contentDescription = "Ảnh ${side.label.lowercase()} CCCD vừa chụp",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .offset(x = frameLeft, y = frameTop)
                    .size(frameWidth, frameHeight)
                    .clip(RoundedCornerShape(FrameCorner)),
            )
        }

        // Lớp phủ vẽ sau ảnh để viền trắng của khung nằm đè lên ảnh
        FrameOverlay(frame = frame, dimAlpha = if (isReviewing) 0.9f else 0.6f)

        // Thanh trên cùng: nút đóng và tiêu đề
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "Đóng camera",
                    tint = Color.White,
                )
            }
            Text(
                text = "Chụp ${side.label.lowercase()} CCCD",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            // Ô trống cùng cỡ nút đóng để tiêu đề nằm chính giữa
            Spacer(Modifier.size(48.dp))
        }

        // Hướng dẫn phía trên khung
        Text(
            text = when {
                !hasPermission -> ""
                isReviewing -> "Kiểm tra ảnh: rõ nét, đủ sáng, không bị lóa hay mất góc"
                else -> "Đặt ${side.label.lowercase()} CCCD vừa khít trong khung"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = frameTop - 64.dp)
                .padding(horizontal = 32.dp),
        )

        // Nhãn "Mặt trước · 1/2" phía dưới khung
        Text(
            text = "${side.label} · ${side.ordinal + 1}/${IdSide.entries.size}",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = frameBottom + 16.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                .padding(horizontal = 14.dp, vertical = 6.dp),
        )

        if (!hasPermission) {
            PermissionMessage(
                onRequestPermission = onRequestPermission,
                onOpenSettings = onOpenSettings,
                modifier = Modifier
                    .offset(x = frameLeft, y = frameTop)
                    .size(frameWidth, frameHeight)
                    .padding(16.dp),
            )
        }

        // Phần dưới: thông báo lỗi và các nút
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
        ) {
            if (error != null) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.error, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }

            if (isReviewing) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onRetake,
                        shape = ButtonShape,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                    ) {
                        Text("Chụp lại", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onConfirm,
                        shape = ButtonShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                    ) {
                        Text("Dùng ảnh này", fontWeight = FontWeight.SemiBold)
                    }
                }
            } else if (hasPermission) {
                ShutterButton(
                    isCapturing = isCapturing,
                    onClick = { onShutter(viewWidth, viewHeight) },
                )
            }
        }
    }
}

/** Lớp phủ tối, khoét một ô trong suốt đúng vị trí [frame] và viền trắng quanh ô. */
@Composable
private fun FrameOverlay(frame: PixelBox, dimAlpha: Float) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            // Vẽ ra lớp riêng thì BlendMode.Clear mới khoét thủng được lớp phủ
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
    ) {
        val corner = CornerRadius(FrameCorner.toPx())
        val topLeft = Offset(frame.left, frame.top)
        val frameSize = Size(frame.width, frame.height)

        drawRect(Color.Black.copy(alpha = dimAlpha))
        drawRoundRect(
            color = Color.Transparent,
            topLeft = topLeft,
            size = frameSize,
            cornerRadius = corner,
            blendMode = BlendMode.Clear,
        )
        drawRoundRect(
            color = Color.White,
            topLeft = topLeft,
            size = frameSize,
            cornerRadius = corner,
            style = Stroke(width = 3.dp.toPx()),
        )
    }
}

/** Nút chụp tròn kiểu camera điện thoại. */
@Composable
private fun ShutterButton(isCapturing: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(76.dp)
            .border(4.dp, Color.White, CircleShape)
            .padding(8.dp)
            .clip(CircleShape)
            .background(if (isCapturing) Color.White.copy(alpha = 0.5f) else Color.White)
            .clickable(enabled = !isCapturing, onClickLabel = "Chụp ảnh", onClick = onClick),
    ) {
        if (isCapturing) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

/** Hiện trong khung khi chưa có quyền camera. */
@Composable
private fun PermissionMessage(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        Text(
            text = "Ứng dụng cần quyền truy cập camera để chụp ảnh CCCD.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRequestPermission, shape = ButtonShape) {
            Text("Cấp quyền camera")
        }
        // Nếu khách đã chọn "Không hỏi lại", hệ thống sẽ không hiện hộp thoại nữa: phải vào Cài đặt
        TextButton(onClick = onOpenSettings) {
            Text("Mở Cài đặt", color = Color.White)
        }
    }
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

private fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}

/**
 * Ảnh camera trả về → ảnh thẻ: xoay cho đứng, cắt phần nằm trong khung, thu nhỏ bớt.
 * [viewWidth], [viewHeight] là kích thước ô xem trước lúc bấm chụp.
 */
private fun ImageProxy.toCardPhoto(viewWidth: Float, viewHeight: Float): ImageBitmap {
    val source = toBitmap()
    val rotation = imageInfo.rotationDegrees
    val upright = if (rotation == 0) {
        source
    } else {
        Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            Matrix().apply { postRotate(rotation.toFloat()) }, true,
        )
    }

    val crop = cropBoxInImage(
        imageWidth = upright.width,
        imageHeight = upright.height,
        viewWidth = viewWidth,
        viewHeight = viewHeight,
        frame = cardFrameBox(viewWidth, viewHeight),
    )
    val cropped = if (crop.width < 1f || crop.height < 1f) {
        upright // Trường hợp hiếm: khung nằm ngoài ảnh thì giữ nguyên ảnh
    } else {
        Bitmap.createBitmap(upright, crop.left.toInt(), crop.top.toInt(), crop.width.toInt(), crop.height.toInt())
    }

    val result = if (cropped.width > MAX_PHOTO_WIDTH) {
        val height = cropped.height * MAX_PHOTO_WIDTH / cropped.width
        Bitmap.createScaledBitmap(cropped, MAX_PHOTO_WIDTH, height, true)
    } else {
        cropped
    }
    return result.asImageBitmap()
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun IdCameraPreview() {
    HydroTheme {
        IdCameraContent(
            side = IdSide.FRONT,
            pendingPhoto = null,
            error = null,
            hasPermission = true,
            isCapturing = false,
            // Preview trong Android Studio không có camera thật: dùng nền xám thay thế
            cameraPreview = { Box(it.background(Color(0xFF5A5A5A))) },
            onShutter = { _, _ -> },
            onRetake = {},
            onConfirm = {},
            onClose = {},
            onRequestPermission = {},
            onOpenSettings = {},
        )
    }
}
