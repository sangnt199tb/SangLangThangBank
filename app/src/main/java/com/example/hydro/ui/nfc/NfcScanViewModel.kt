package com.example.hydro.ui.nfc

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydro.data.ekyc.CccdChipReader
import com.example.hydro.data.ekyc.ChipReadException
import com.example.hydro.data.ekyc.EkycSession
import com.example.hydro.data.ekyc.MockCccdChipReader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Các bước của màn quét chip. */
enum class NfcPhase {
    /** Chưa quét: hiện hướng dẫn và nút "Bắt đầu quét". */
    IDLE,

    /** Đang chờ khách áp thẻ vào lưng điện thoại. */
    WAITING,

    /** Đã nhận thẻ, đang đọc dữ liệu (có phần trăm). */
    READING,
    SUCCESS,
    ERROR,
}

data class NfcScanUiState(
    val phase: NfcPhase = NfcPhase.IDLE,
    val progress: Int = 0,
    val error: String? = null,
    /** Đọc xong, chuyển sang màn xác nhận thông tin. */
    val isCompleted: Boolean = false,
) {
    val isScanning: Boolean get() = phase == NfcPhase.WAITING || phase == NfcPhase.READING
}

/**
 * ViewModel màn quét chip CCCD. Việc đọc chip giao cho [reader].
 * Hiện dùng bản giả lập; có SDK GTEL thì đổi giá trị mặc định của [reader].
 */
class NfcScanViewModel(
    private val reader: CccdChipReader = MockCccdChipReader(),
) : ViewModel() {

    var uiState by mutableStateOf(NfcScanUiState())
        private set

    private var scanJob: Job? = null

    fun startScan() {
        if (uiState.isScanning) return

        uiState = NfcScanUiState(phase = NfcPhase.WAITING)
        scanJob = viewModelScope.launch {
            try {
                val info = reader.read(
                    onCardDetected = { uiState = uiState.copy(phase = NfcPhase.READING) },
                    onProgress = { uiState = uiState.copy(progress = it) },
                )
                EkycSession.citizenInfo = info
                uiState = uiState.copy(phase = NfcPhase.SUCCESS, progress = 100)
                delay(800) // cho khách kịp thấy dấu tích thành công
                uiState = uiState.copy(isCompleted = true)
            } catch (e: ChipReadException) {
                uiState = uiState.copy(phase = NfcPhase.ERROR, error = e.message)
            } catch (e: CancellationException) {
                throw e // bị huỷ (bấm "Huỷ" hoặc rời màn): để coroutine dừng bình thường
            } catch (e: Exception) {
                uiState = uiState.copy(
                    phase = NfcPhase.ERROR,
                    error = "Không đọc được chip. Vui lòng giữ yên thẻ và thử lại.",
                )
            }
        }
    }

    fun cancelScan() {
        scanJob?.cancel()
        uiState = NfcScanUiState()
    }

    /** Ở bước thành công, khách quay lại màn này rồi bấm "Tiếp tục". */
    fun continueNext() {
        if (uiState.phase == NfcPhase.SUCCESS) uiState = uiState.copy(isCompleted = true)
    }

    /** Gọi sau khi đã chuyển màn, để khi quay lại không tự chuyển đi lần nữa. */
    fun onCompletedHandled() {
        uiState = uiState.copy(isCompleted = false)
    }
}

/** Dòng mô tả đang đọc phần nào của chip, theo phần trăm tiến độ. */
fun readingStepLabel(progress: Int): String = when {
    progress < 30 -> "Đang kết nối với chip..."
    progress < 60 -> "Đang đọc thông tin cá nhân..."
    progress < 90 -> "Đang đọc ảnh chân dung..."
    else -> "Đang kiểm tra tính toàn vẹn dữ liệu..."
}
