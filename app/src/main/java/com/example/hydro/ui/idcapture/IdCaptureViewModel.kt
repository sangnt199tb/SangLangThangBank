package com.example.hydro.ui.idcapture

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Hai mặt của thẻ CCCD, chụp theo đúng thứ tự này. */
enum class IdSide(val label: String) {
    FRONT("Mặt trước"),
    BACK("Mặt sau"),
}

data class IdCaptureUiState(
    val front: ImageBitmap? = null,
    val back: ImageBitmap? = null,
    /** Đang mở camera để chụp mặt nào. null là đang ở màn tổng quan. */
    val cameraSide: IdSide? = null,
    /** Ảnh vừa chụp, chờ khách xem lại rồi bấm "Dùng ảnh này" hoặc "Chụp lại". */
    val pendingPhoto: ImageBitmap? = null,
    val cameraError: String? = null,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
) {
    fun photoOf(side: IdSide): ImageBitmap? = when (side) {
        IdSide.FRONT -> front
        IdSide.BACK -> back
    }

    val canSubmit: Boolean get() = front != null && back != null && !isSubmitting
}

/**
 * ViewModel màn chụp ảnh CCCD hai mặt.
 * Ảnh chỉ giữ trong bộ nhớ (không ghi ra file) vì là dữ liệu cá nhân nhạy cảm.
 */
class IdCaptureViewModel : ViewModel() {

    var uiState by mutableStateOf(IdCaptureUiState())
        private set

    fun openCamera(side: IdSide) {
        if (uiState.isSubmitting) return
        uiState = uiState.copy(cameraSide = side, pendingPhoto = null, cameraError = null)
    }

    fun closeCamera() {
        uiState = uiState.copy(cameraSide = null, pendingPhoto = null, cameraError = null)
    }

    fun onPhotoTaken(photo: ImageBitmap) {
        if (uiState.cameraSide == null) return
        uiState = uiState.copy(pendingPhoto = photo, cameraError = null)
    }

    fun onCaptureError(message: String) {
        uiState = uiState.copy(cameraError = message)
    }

    fun retake() {
        uiState = uiState.copy(pendingPhoto = null, cameraError = null)
    }

    fun confirmPhoto() {
        val state = uiState
        val side = state.cameraSide ?: return
        val photo = state.pendingPhoto ?: return

        val updated = when (side) {
            IdSide.FRONT -> state.copy(front = photo)
            IdSide.BACK -> state.copy(back = photo)
        }
        // Còn mặt nào chưa chụp thì chuyển luôn sang chụp mặt đó, đủ rồi thì đóng camera
        val nextSide = IdSide.entries.firstOrNull { updated.photoOf(it) == null }
        uiState = updated.copy(cameraSide = nextSide, pendingPhoto = null)
    }

    fun submit() {
        if (!uiState.canSubmit) return

        uiState = uiState.copy(isSubmitting = true)
        viewModelScope.launch {
            // TODO: thay bằng lời gọi API tải ảnh 2 mặt lên, server đọc thông tin trên thẻ (OCR)
            delay(1500)
            uiState = uiState.copy(isSubmitting = false, isSubmitted = true)
        }
    }

    /** Gọi sau khi đã chuyển màn, để khi quay lại không tự chuyển đi lần nữa. */
    fun onSubmittedHandled() {
        uiState = uiState.copy(isSubmitted = false)
    }
}
