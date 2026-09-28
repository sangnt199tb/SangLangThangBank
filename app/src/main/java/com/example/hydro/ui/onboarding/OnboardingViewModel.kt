package com.example.hydro.ui.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val CAPTCHA_LENGTH = 5

// Bỏ các ký tự dễ nhầm lẫn: 0/O, 1/I/L
private const val CAPTCHA_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"

/** Toàn bộ dữ liệu mà màn hình cần để hiển thị. */
data class OnboardingUiState(
    val email: String = "",
    val phone: String = "",
    val misCode: String = "",
    val captchaInput: String = "",
    val captchaText: String = "",
    val acceptedTerms: Boolean = false,
    val emailError: String? = null,
    val phoneError: String? = null,
    val misCodeError: String? = null,
    val captchaError: String? = null,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
)

/**
 * ViewModel giữ trạng thái màn hình onboarding. Dữ liệu không bị mất khi xoay màn hình.
 */
class OnboardingViewModel : ViewModel() {

    var uiState by mutableStateOf(OnboardingUiState(captchaText = generateCaptcha()))
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value.filterNot { it.isWhitespace() }, emailError = null)
    }

    fun onPhoneChange(value: String) {
        uiState = uiState.copy(phone = value.filter { it in '0'..'9' }.take(10), phoneError = null)
    }

    fun onMisCodeChange(value: String) {
        val cleaned = value.uppercase().filter { it in 'A'..'Z' || it in '0'..'9' }.take(10)
        uiState = uiState.copy(misCode = cleaned, misCodeError = null)
    }

    fun onCaptchaInputChange(value: String) {
        uiState = uiState.copy(captchaInput = value.trim().take(CAPTCHA_LENGTH), captchaError = null)
    }

    /** Gọi sau khi đã chuyển sang màn tiếp theo, để sự kiện "gửi thành công" chỉ xử lý một lần. */
    fun onSubmittedHandled() {
        uiState = uiState.copy(isSubmitted = false)
    }

    fun onAcceptedTermsChange(accepted: Boolean) {
        uiState = uiState.copy(acceptedTerms = accepted)
    }

    fun refreshCaptcha() {
        uiState = uiState.copy(captchaText = generateCaptcha(), captchaInput = "", captchaError = null)
    }

    fun submit() {
        val state = uiState
        // Nút đã bị khoá khi chưa đồng ý điều khoản, kiểm tra lại cho chắc
        if (state.isSubmitting || !state.acceptedTerms) return

        val emailError = OnboardingValidator.validateEmail(state.email)
        val phoneError = OnboardingValidator.validatePhone(state.phone)
        val misCodeError = OnboardingValidator.validateMisCode(state.misCode)
        val captchaError = OnboardingValidator.validateCaptcha(state.captchaInput, state.captchaText)

        if (listOf(emailError, phoneError, misCodeError, captchaError).any { it != null }) {
            val wrongCaptcha = captchaError != null && state.captchaInput.isNotBlank()
            uiState = state.copy(
                emailError = emailError,
                phoneError = phoneError,
                misCodeError = misCodeError,
                captchaError = captchaError,
                // Nhập sai captcha thì đổi mã mới để tránh đoán thử nhiều lần
                captchaText = if (wrongCaptcha) generateCaptcha() else state.captchaText,
                captchaInput = if (wrongCaptcha) "" else state.captchaInput,
            )
            return
        }

        uiState = state.copy(isSubmitting = true)
        viewModelScope.launch {
            // TODO: thay bằng lời gọi API thật của backend
            delay(1500)
            uiState = uiState.copy(isSubmitting = false, isSubmitted = true)
        }
    }

    private fun generateCaptcha(): String =
        (1..CAPTCHA_LENGTH).map { CAPTCHA_CHARS.random() }.joinToString("")
}
