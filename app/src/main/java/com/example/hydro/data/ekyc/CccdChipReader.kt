package com.example.hydro.data.ekyc

import kotlinx.coroutines.delay

/**
 * Đọc chip CCCD qua NFC. Màn quét chip chỉ biết đến interface này,
 * nên khi có SDK GTEL chỉ cần viết thêm một lớp dùng SDK rồi truyền vào thay cho [MockCccdChipReader].
 */
interface CccdChipReader {
    /**
     * Chờ khách áp thẻ rồi đọc chip. Gọi [onCardDetected] khi điện thoại nhận được thẻ,
     * [onProgress] (0..100) trong lúc đọc. Đọc lỗi thì ném [ChipReadException].
     */
    suspend fun read(
        onCardDetected: () -> Unit,
        onProgress: (Int) -> Unit,
    ): CitizenInfo
}

/** Lỗi khi đọc chip, [message] là câu hiển thị thẳng cho khách. */
class ChipReadException(message: String) : Exception(message)

/**
 * Bản giả lập: không cần NFC, không cần thẻ thật, lúc nào cũng đọc thành công.
 * TODO: thay bằng lớp dùng SDK GTEL khi đối tác gửi SDK.
 */
class MockCccdChipReader : CccdChipReader {

    override suspend fun read(
        onCardDetected: () -> Unit,
        onProgress: (Int) -> Unit,
    ): CitizenInfo {
        delay(2000) // giả vờ chờ khách áp thẻ
        onCardDetected()
        for (percent in 0..100 step 5) {
            onProgress(percent)
            delay(120)
        }
        return SAMPLE_CITIZEN
    }

    companion object {
        /** Người mẫu, thông tin bịa ra để demo. */
        val SAMPLE_CITIZEN = CitizenInfo(
            idNumber = "001095012345",
            oldIdNumber = "013456789",
            fullName = "NGUYỄN VĂN AN",
            dateOfBirth = "15/05/1995",
            gender = "Nam",
            nationality = "Việt Nam",
            ethnicity = "Kinh",
            placeOfOrigin = "Xã Tân Hòa, Huyện Quốc Oai, Hà Nội",
            placeOfResidence = "Số 12 ngõ 34 Trần Thái Tông, Phường Dịch Vọng, Quận Cầu Giấy, Hà Nội",
            issueDate = "10/08/2021",
            expiryDate = "15/05/2035",
        )
    }
}
