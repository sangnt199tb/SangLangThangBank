package com.example.hydro.ui.onboarding

/**
 * Các quy tắc kiểm tra dữ liệu nhập ở màn hình onboarding.
 * Mỗi hàm trả về câu báo lỗi, hoặc null nếu dữ liệu hợp lệ.
 */
object OnboardingValidator {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // Số di động Việt Nam: 10 chữ số, bắt đầu bằng 03, 05, 07, 08 hoặc 09
    private val PHONE_REGEX = Regex("^0[35789][0-9]{8}$")

    // Mã MIS (mã nhân viên/đơn vị giới thiệu): 4-10 ký tự chữ hoặc số
    private val MIS_CODE_REGEX = Regex("^[A-Z0-9]{4,10}$")

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Vui lòng nhập email"
        !EMAIL_REGEX.matches(email) -> "Email không hợp lệ"
        else -> null
    }

    fun validatePhone(phone: String): String? = when {
        phone.isBlank() -> "Vui lòng nhập số điện thoại"
        !PHONE_REGEX.matches(phone) -> "Số điện thoại không hợp lệ (VD: 0912345678)"
        else -> null
    }

    /** Mã MIS không bắt buộc: để trống là hợp lệ. */
    fun validateMisCode(misCode: String): String? = when {
        misCode.isEmpty() -> null
        !MIS_CODE_REGEX.matches(misCode) -> "Mã MIS gồm 4-10 ký tự chữ hoặc số"
        else -> null
    }

    fun validateCaptcha(input: String, expected: String): String? = when {
        input.isBlank() -> "Vui lòng nhập mã captcha"
        !input.equals(expected, ignoreCase = true) -> "Mã captcha không đúng, vui lòng nhập lại"
        else -> null
    }
}
