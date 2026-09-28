package com.example.hydro.data.ekyc

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Thông tin công dân đọc được từ chip CCCD.
 * Đây là dữ liệu cá nhân nhạy cảm (Nghị định 13/2023): không ghi ra log, không lưu xuống máy.
 * Ngày tháng giữ dạng chuỗi "dd/MM/yyyy" như trên thẻ.
 */
data class CitizenInfo(
    val idNumber: String,
    val oldIdNumber: String?,
    val fullName: String,
    val dateOfBirth: String,
    val gender: String,
    val nationality: String,
    val ethnicity: String,
    val placeOfOrigin: String,
    val placeOfResidence: String,
    val issueDate: String,
    val expiryDate: String,
    /** Ảnh chân dung lưu trong chip. Bản giả lập chưa có ảnh. */
    val portrait: ImageBitmap? = null,
)
