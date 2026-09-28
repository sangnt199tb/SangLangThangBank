package com.example.hydro.ui.register

/** Các điều kiện của mật khẩu, hiện thành danh sách để khách thấy mình còn thiếu điều kiện nào. */
enum class PasswordRule(val label: String) {
    LENGTH("Từ 8 đến 20 ký tự"),
    UPPERCASE("Có chữ in hoa (A-Z)"),
    LOWERCASE("Có chữ thường (a-z)"),
    DIGIT("Có chữ số (0-9)"),
    SPECIAL("Có ký tự đặc biệt (!@#\$%...)"),
    NOT_USERNAME("Không chứa tên đăng nhập"),
}

/**
 * Các quy tắc kiểm tra tên đăng nhập và mật khẩu.
 * Hàm validate trả về câu báo lỗi, hoặc null nếu hợp lệ.
 */
object CredentialValidator {

    const val USERNAME_MAX_LENGTH = 20
    const val PASSWORD_MAX_LENGTH = 20

    // 6-20 ký tự: chữ thường không dấu, số, dấu chấm, gạch dưới. Đầu và cuối phải là chữ hoặc số.
    private val USERNAME_REGEX = Regex("^[a-z0-9][a-z0-9._]{4,18}[a-z0-9]$")

    fun validateUsername(username: String): String? = when {
        username.isBlank() -> "Vui lòng nhập tên đăng nhập"
        username.length !in 6..USERNAME_MAX_LENGTH -> "Tên đăng nhập gồm 6-20 ký tự"
        !USERNAME_REGEX.matches(username) ->
            "Chỉ dùng chữ không dấu, số, dấu chấm hoặc gạch dưới. Không bắt đầu hay kết thúc bằng dấu."
        else -> null
    }

    /** Kiểm tra mật khẩu đạt được điều kiện [rule] chưa. */
    fun isRuleMet(rule: PasswordRule, password: String, username: String): Boolean = when (rule) {
        PasswordRule.LENGTH -> password.length in 8..PASSWORD_MAX_LENGTH
        PasswordRule.UPPERCASE -> password.any { it in 'A'..'Z' }
        PasswordRule.LOWERCASE -> password.any { it in 'a'..'z' }
        PasswordRule.DIGIT -> password.any { it in '0'..'9' }
        PasswordRule.SPECIAL -> password.any { !it.isLetterOrDigit() }
        // Chưa nhập mật khẩu thì chưa tính là đạt
        PasswordRule.NOT_USERNAME ->
            password.isNotEmpty() && (username.isBlank() || !password.contains(username, ignoreCase = true))
    }

    fun validatePassword(password: String, username: String): String? = when {
        password.isEmpty() -> "Vui lòng nhập mật khẩu"
        PasswordRule.entries.any { !isRuleMet(it, password, username) } -> "Mật khẩu chưa đủ các điều kiện bên dưới"
        else -> null
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? = when {
        confirmPassword.isEmpty() -> "Vui lòng nhập lại mật khẩu"
        confirmPassword != password -> "Mật khẩu nhập lại không khớp"
        else -> null
    }

    /** Làm sạch tên đăng nhập khi gõ: đổi thành chữ thường, bỏ ký tự không cho phép. */
    fun cleanUsername(input: String): String =
        input.lowercase()
            .filter { it in 'a'..'z' || it in '0'..'9' || it == '.' || it == '_' }
            .take(USERNAME_MAX_LENGTH)

    /**
     * Làm sạch mật khẩu khi gõ: chỉ giữ ký tự ASCII in được (bỏ dấu cách, chữ có dấu tiếng Việt),
     * tránh trường hợp bộ gõ Telex tự thêm dấu mà khách không để ý.
     */
    fun cleanPassword(input: String): String =
        input.filter { it.code in 33..126 }.take(PASSWORD_MAX_LENGTH)
}
