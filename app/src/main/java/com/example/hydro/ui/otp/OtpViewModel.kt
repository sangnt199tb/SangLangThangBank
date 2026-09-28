package com.example.hydro.ui.otp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydro.data.ekyc.EkycSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val OTP_LENGTH = 6
const val RESEND_SECONDS = 60
const val MAX_ATTEMPTS = 5

// TODO: bản thật thì server gửi OTP qua SMS và tự kiểm tra; đây là mã cố định để demo
const val DEMO_OTP = "123456"

/** Tên tham số số điện thoại trên đường dẫn điều hướng, ví dụ "otp/0912345678". */
const val PHONE_ARG = "phone"

data class OtpUiState(
    val phone: String = "",
    val code: String = "",
    val error: String? = null,
    val info: String? = null,
    val remainingAttempts: Int = MAX_ATTEMPTS,
    val resendCountdown: Int = RESEND_SECONDS,
    val isVerifying: Boolean = false,
    val isResending: Boolean = false,
    val isVerified: Boolean = false,
) {
    /** Nhập sai quá số lần cho phép: khoá ô nhập, phải gửi lại mã mới. */
    val isLocked: Boolean get() = remainingAttempts <= 0
    val canVerify: Boolean get() = code.length == OTP_LENGTH && !isVerifying && !isLocked
    val canResend: Boolean get() = resendCountdown == 0 && !isResending && !isVerifying
}

/**
 * ViewModel màn xác thực OTP: nhận mã, đếm ngược gửi lại, giới hạn số lần nhập sai.
 * Số điện thoại được lấy từ đường dẫn điều hướng qua [SavedStateHandle].
 */
class OtpViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    var uiState by mutableStateOf(OtpUiState(phone = savedStateHandle.get<String>(PHONE_ARG).orEmpty()))
        private set

    private var countdownJob: Job? = null

    init {
        startCountdown()
    }

    fun onCodeChange(value: String) {
        if (uiState.isVerifying || uiState.isLocked) return
        val digits = value.filter { it in '0'..'9' }.take(OTP_LENGTH)
        uiState = uiState.copy(code = digits, error = null)
        // Nhập đủ 6 số thì tự xác nhận, khách không cần bấm nút
        if (digits.length == OTP_LENGTH) verify()
    }

    fun verify() {
        val state = uiState
        if (!state.canVerify) return

        uiState = state.copy(isVerifying = true, error = null, info = null)
        viewModelScope.launch {
            // TODO: thay bằng lời gọi API xác thực OTP
            delay(1000)
            if (uiState.code == DEMO_OTP) {
                countdownJob?.cancel()
                EkycSession.phone = uiState.phone
                uiState = uiState.copy(isVerifying = false, isVerified = true)
            } else {
                val remaining = uiState.remainingAttempts - 1
                uiState = uiState.copy(
                    isVerifying = false,
                    code = "",
                    remainingAttempts = remaining,
                    error = if (remaining > 0) {
                        "Mã OTP không đúng. Bạn còn $remaining lần thử."
                    } else {
                        "Bạn đã nhập sai quá $MAX_ATTEMPTS lần. Vui lòng gửi lại mã mới."
                    },
                )
            }
        }
    }

    fun resend() {
        if (!uiState.canResend) return

        uiState = uiState.copy(isResending = true, error = null, info = null)
        viewModelScope.launch {
            // TODO: thay bằng lời gọi API gửi lại OTP
            delay(800)
            uiState = uiState.copy(
                isResending = false,
                code = "",
                remainingAttempts = MAX_ATTEMPTS,
                info = "Đã gửi mã OTP mới đến số điện thoại của bạn.",
            )
            startCountdown()
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        uiState = uiState.copy(resendCountdown = RESEND_SECONDS)
        countdownJob = viewModelScope.launch {
            while (uiState.resendCountdown > 0) {
                delay(1000)
                uiState = uiState.copy(resendCountdown = uiState.resendCountdown - 1)
            }
        }
    }
}

/** Che bớt số điện thoại khi hiển thị: "0912345678" thành "091****678". */
fun maskPhone(phone: String): String =
    if (phone.length < 7) phone else phone.take(3) + "*".repeat(phone.length - 6) + phone.takeLast(3)

/** Đổi số giây thành dạng "mm:ss", ví dụ 65 thành "01:05". */
fun formatCountdown(seconds: Int): String = "%02d:%02d".format(seconds / 60, seconds % 60)
