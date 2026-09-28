package com.example.hydro.ui.confirminfo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydro.data.ekyc.CitizenInfo
import com.example.hydro.data.ekyc.EkycSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ConfirmInfoUiState(
    /** null khi không còn dữ liệu (ví dụ Android đã tắt app chạy nền): phải quét chip lại. */
    val info: CitizenInfo? = null,
    val isConfirmed: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
) {
    val canSubmit: Boolean get() = info != null && isConfirmed && !isSubmitting
}

/** ViewModel màn xác nhận thông tin đọc từ chip CCCD. */
class ConfirmInfoViewModel : ViewModel() {

    var uiState by mutableStateOf(ConfirmInfoUiState(info = EkycSession.citizenInfo))
        private set

    fun onConfirmedChange(checked: Boolean) {
        if (uiState.isSubmitting) return
        uiState = uiState.copy(isConfirmed = checked)
    }

    fun submit() {
        if (!uiState.canSubmit) return

        uiState = uiState.copy(isSubmitting = true)
        viewModelScope.launch {
            // TODO: thay bằng lời gọi API gửi thông tin đã xác nhận để mở tài khoản
            delay(1500)
            uiState = uiState.copy(isSubmitting = false, isSubmitted = true)
        }
    }

    /** Gọi sau khi đã chuyển màn, để khi quay lại không tự chuyển đi lần nữa. */
    fun onSubmittedHandled() {
        uiState = uiState.copy(isSubmitted = false)
    }
}
