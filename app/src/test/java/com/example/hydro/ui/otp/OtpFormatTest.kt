package com.example.hydro.ui.otp

import org.junit.Assert.assertEquals
import org.junit.Test

class OtpFormatTest {

    @Test
    fun maskPhone_hidesMiddleDigits() {
        assertEquals("091****678", maskPhone("0912345678"))
        assertEquals("123", maskPhone("123")) // quá ngắn thì giữ nguyên
    }

    @Test
    fun formatCountdown_showsMinutesAndSeconds() {
        assertEquals("01:00", formatCountdown(60))
        assertEquals("00:05", formatCountdown(5))
        assertEquals("00:00", formatCountdown(0))
    }
}
