package com.example.hydro.data.ekyc

/**
 * Nơi giữ tạm dữ liệu mở tài khoản để chuyển giữa các màn (OTP → quét chip → xác nhận → tạo tài khoản).
 * Chỉ nằm trong bộ nhớ: tắt app là mất, đúng ý đồ vì đây là dữ liệu nhạy cảm.
 * Không truyền qua đường dẫn điều hướng vì Android có thể lưu đường dẫn đó lại.
 */
object EkycSession {
    /** Số điện thoại đã xác thực OTP, dùng làm tên đăng nhập mặc định. */
    var phone: String? = null
    var citizenInfo: CitizenInfo? = null

    /** Gọi khi đăng ký xong để xoá dữ liệu khỏi bộ nhớ. */
    fun clear() {
        phone = null
        citizenInfo = null
    }
}
