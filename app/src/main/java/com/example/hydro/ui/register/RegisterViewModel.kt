package com.example.hydro.ui.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydro.data.ekyc.EkycSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// TODO: bản thật thì server kiểm tra tên đăng nhập đã có người dùng chưa; đây là danh sách giả để demo
private val DEMO_TAKEN_USERNAMES = setOf("admin", "sanglangthang", "nguyenvanan")

data class RegisterUiState(
    /** Số điện thoại đã xác thực, cũng là tên đăng nhập mặc định. */
    val phone: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isSubmitting: Boolean = false,
    val isRegistered: Boolean = false,
) {
    /** Khách đã đổi tên đăng nhập khác số điện thoại: hiện nút "Dùng số điện thoại" để quay lại. */
    val canResetToPhone: Boolean get() = phone.isNotEmpty() && username != phone
}

/** ViewModel màn tạo tên đăng nhập và mật khẩu. */
class RegisterViewModel : ViewModel() {

    var uiState by mutableStateOf(
        EkycSession.phone.orEmpty().let { phone -> RegisterUiState(phone = phone, username = phone) },
    )
        private set

    fun onUsernameChange(value: String) {
        uiState = uiState.copy(username = CredentialValidator.cleanUsername(value), usernameError = null)
    }

    fun useUsernameFromPhone() {
        uiState = uiState.copy(username = uiState.phone, usernameError = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = CredentialValidator.cleanPassword(value), passwordError = null)
    }

    fun onConfirmPasswordChange(value: String) {
        uiState = uiState.copy(
            confirmPassword = CredentialValidator.cleanPassword(value),
            confirmPasswordError = null,
        )
    }

    fun register() {
        val state = uiState
        if (state.isSubmitting) return

        val usernameError = CredentialValidator.validateUsername(state.username)
        val passwordError = CredentialValidator.validatePassword(state.password, state.username)
        val confirmPasswordError = CredentialValidator.validateConfirmPassword(state.password, state.confirmPassword)
        if (listOf(usernameError, passwordError, confirmPasswordError).any { it != null }) {
            uiState = state.copy(
                usernameError = usernameError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
            )
            return
        }

        uiState = state.copy(isSubmitting = true)
        viewModelScope.launch {
            // TODO: thay bằng lời gọi API đăng ký (gửi kèm thông tin CCCD đã xác nhận trong EkycSession)
            delay(1500)
            if (uiState.username in DEMO_TAKEN_USERNAMES) {
                uiState = uiState.copy(
                    isSubmitting = false,
                    usernameError = "Tên đăng nhập đã có người dùng. Vui lòng chọn tên khác.",
                )
            } else {
                // Đăng ký xong thì xoá dữ liệu mở tài khoản khỏi bộ nhớ
                EkycSession.clear()
                uiState = uiState.copy(isSubmitting = false, isRegistered = true)
            }
        }
    }

    /** Gọi sau khi đã chuyển màn, để khi quay lại không tự chuyển đi lần nữa. */
    fun onRegisteredHandled() {
        uiState = uiState.copy(isRegistered = false)
    }
}
