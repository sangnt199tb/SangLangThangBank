package com.example.hydro.ui.idcapture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IdCardFrameTest {

    private val delta = 0.01f

    @Test
    fun cardFrameBox_keepsCardRatioAndCentered() {
        val frame = cardFrameBox(1000f, 2000f)
        assertEquals(880f, frame.width, delta)
        assertEquals(ID_CARD_RATIO, frame.width / frame.height, delta)
        assertEquals(60f, frame.left, delta)
        assertEquals(1000f - frame.right, frame.left, delta)
    }

    @Test
    fun cardFrameBox_landscapeLimitsHeight() {
        val frame = cardFrameBox(2000f, 1000f)
        assertEquals(600f, frame.height, delta)
        assertTrue(frame.width < 2000f * 0.88f)
    }

    @Test
    fun cropBoxInImage_sameSizeReturnsFrame() {
        val frame = PixelBox(100f, 200f, 300f, 150f)
        val crop = cropBoxInImage(1000, 2000, 1000f, 2000f, frame, margin = 0f)
        assertEquals(frame, crop)
    }

    @Test
    fun cropBoxInImage_scalesWithImage() {
        val frame = PixelBox(100f, 200f, 300f, 150f)
        val crop = cropBoxInImage(2000, 4000, 1000f, 2000f, frame, margin = 0f)
        assertEquals(PixelBox(200f, 400f, 600f, 300f), crop)
    }

    @Test
    fun cropBoxInImage_subtractsHiddenSides() {
        // Ảnh vuông 1000×1000 phủ kín ô 500×1000: giữ nguyên cỡ, mỗi bên tràn ra 250px
        val frame = PixelBox(50f, 100f, 400f, 250f)
        val crop = cropBoxInImage(1000, 1000, 500f, 1000f, frame, margin = 0f)
        assertEquals(PixelBox(300f, 100f, 400f, 250f), crop)
    }

    @Test
    fun cropBoxInImage_addsMarginInsideImage() {
        val frame = PixelBox(0f, 0f, 1000f, 500f)
        val crop = cropBoxInImage(1000, 2000, 1000f, 2000f, frame, margin = 0.1f)
        assertEquals(0f, crop.left, delta)
        assertEquals(0f, crop.top, delta)
        assertEquals(1000f, crop.right, delta)
        assertEquals(550f, crop.bottom, delta)
    }
}
