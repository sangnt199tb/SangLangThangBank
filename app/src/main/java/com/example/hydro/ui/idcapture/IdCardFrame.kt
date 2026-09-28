package com.example.hydro.ui.idcapture

/** Tỉ lệ rộng/cao của thẻ CCCD (khổ ID-1: 85,6 × 53,98 mm). */
const val ID_CARD_RATIO = 85.6f / 53.98f

/** Hình chữ nhật tính bằng pixel. Không dùng lớp Rect của Android để viết unit test được. */
data class PixelBox(val left: Float, val top: Float, val width: Float, val height: Float) {
    val right: Float get() = left + width
    val bottom: Float get() = top + height
}

/**
 * Vị trí khung căn thẻ trên màn camera có kích thước [containerWidth] × [containerHeight].
 * Khung rộng 88% màn (không cao quá 60% màn khi xoay ngang), căn giữa, tâm khung ở 42% chiều cao
 * (hơi lệch lên để chừa chỗ cho nút chụp phía dưới).
 */
fun cardFrameBox(containerWidth: Float, containerHeight: Float): PixelBox {
    val width = minOf(containerWidth * 0.88f, containerHeight * 0.6f * ID_CARD_RATIO)
    val height = width / ID_CARD_RATIO
    return PixelBox(
        left = (containerWidth - width) / 2,
        top = containerHeight * 0.42f - height / 2,
        width = width,
        height = height,
    )
}

/**
 * Tính vùng cần cắt trên ảnh chụp ([imageWidth] × [imageHeight], đã xoay đứng) để lấy phần nằm trong [frame].
 *
 * Ô xem trước camera có kích thước [viewWidth] × [viewHeight]. Nó phóng ảnh cho phủ kín ô rồi cắt bớt
 * phần thừa ở hai bên (kiểu FILL_CENTER), nên ở đây làm phép tính ngược lại.
 * [margin] là phần nới thêm mỗi cạnh (theo tỉ lệ kích thước khung) để không cắt mất mép thẻ.
 */
fun cropBoxInImage(
    imageWidth: Int,
    imageHeight: Int,
    viewWidth: Float,
    viewHeight: Float,
    frame: PixelBox,
    margin: Float = 0.04f,
): PixelBox {
    val scale = maxOf(viewWidth / imageWidth, viewHeight / imageHeight)
    // Phần ảnh bị tràn ra ngoài ô xem trước (số âm hoặc 0)
    val offsetX = (viewWidth - imageWidth * scale) / 2
    val offsetY = (viewHeight - imageHeight * scale) / 2
    val padX = frame.width * margin
    val padY = frame.height * margin

    fun toImageX(x: Float) = ((x - offsetX) / scale).coerceIn(0f, imageWidth.toFloat())
    fun toImageY(y: Float) = ((y - offsetY) / scale).coerceIn(0f, imageHeight.toFloat())

    val left = toImageX(frame.left - padX)
    val top = toImageY(frame.top - padY)
    val right = toImageX(frame.right + padX)
    val bottom = toImageY(frame.bottom + padY)
    return PixelBox(left, top, right - left, bottom - top)
}
