package com.example.hydro.ui.nfc

import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingStepLabelTest {

    @Test
    fun readingStepLabel_followsProgress() {
        assertEquals("Đang kết nối với chip...", readingStepLabel(0))
        assertEquals("Đang đọc thông tin cá nhân...", readingStepLabel(30))
        assertEquals("Đang đọc ảnh chân dung...", readingStepLabel(89))
        assertEquals("Đang kiểm tra tính toàn vẹn dữ liệu...", readingStepLabel(100))
    }
}
