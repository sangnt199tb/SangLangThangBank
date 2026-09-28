package com.example.hydro.ui.confirminfo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hydro.R
import com.example.hydro.data.ekyc.CitizenInfo
import com.example.hydro.data.ekyc.MockCccdChipReader
import com.example.hydro.ui.components.BrandHeader
import com.example.hydro.ui.components.BrandHeaderOverlap
import com.example.hydro.ui.theme.HydroTheme

private val ButtonShape = RoundedCornerShape(12.dp)
private val CardShape = RoundedCornerShape(20.dp)

/**
 * Màn xác nhận thông tin đọc từ chip CCCD.
 * [onSubmitted] được gọi khi khách xác nhận xong, [onBack] khi bấm quay lại (cũng dùng để quét lại chip).
 */
@Composable
fun ConfirmInfoScreen(
    onSubmitted: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfirmInfoViewModel = viewModel(),
) {
    val state = viewModel.uiState

    LaunchedEffect(state.isSubmitted) {
        if (state.isSubmitted) {
            onSubmitted()
            viewModel.onSubmittedHandled()
        }
    }

    ConfirmInfoContent(
        state = state,
        onConfirmedChange = viewModel::onConfirmedChange,
        onSubmit = viewModel::submit,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun ConfirmInfoContent(
    state: ConfirmInfoUiState,
    onConfirmedChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        BrandHeader(
            title = "Xác nhận thông tin",
            subtitle = "Thông tin được đọc từ chip CCCD. Vui lòng kiểm tra kỹ trước khi tiếp tục.",
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .offset(y = -BrandHeaderOverlap)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val info = state.info
            if (info == null) {
                MissingInfoCard(onBack = onBack)
            } else {
                ProfileCard(info)

                InfoSection(
                    title = "Thông tin cá nhân",
                    rows = listOf(
                        "Ngày sinh" to info.dateOfBirth,
                        "Giới tính" to info.gender,
                        "Quốc tịch" to info.nationality,
                        "Dân tộc" to info.ethnicity,
                        "Quê quán" to info.placeOfOrigin,
                        "Nơi thường trú" to info.placeOfResidence,
                    ),
                )

                InfoSection(
                    title = "Thông tin giấy tờ",
                    rows = listOfNotNull(
                        "Số CCCD" to info.idNumber,
                        info.oldIdNumber?.let { "Số CMND cũ" to it },
                        "Ngày cấp" to info.issueDate,
                        "Có giá trị đến" to info.expiryDate,
                    ),
                )

                Text(
                    text = "Thông tin đọc từ chip không chỉnh sửa được. Nếu có sai sót, vui lòng liên hệ cơ quan cấp CCCD.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )

                ConfirmCheckbox(
                    checked = state.isConfirmed,
                    onCheckedChange = onConfirmedChange,
                    enabled = !state.isSubmitting,
                )

                Button(
                    onClick = onSubmit,
                    enabled = state.canSubmit,
                    shape = ButtonShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Xác nhận", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

/** Thẻ đầu: ảnh chân dung, họ tên và số CCCD. */
@Composable
private fun ProfileCard(info: CitizenInfo) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp),
        ) {
            // Ảnh chân dung theo tỉ lệ 3×4 như trên thẻ
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 72.dp, height = 96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
            ) {
                val portrait = info.portrait
                if (portrait != null) {
                    Image(
                        bitmap = portrait,
                        contentDescription = "Ảnh chân dung trên chip",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_person),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.fullName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Số CCCD: ${info.idNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_shield),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Đã xác thực chip",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

/** Một nhóm thông tin: tiêu đề và các dòng "nhãn — giá trị", ngăn cách bằng đường kẻ mảnh. */
@Composable
private fun InfoSection(title: String, rows: List<Pair<String, String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, CardShape)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        rows.forEachIndexed { index, (label, value) ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Row(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.4f),
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(0.6f),
                )
            }
        }
    }
}

@Composable
private fun ConfirmCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
) {
    // Bấm vào cả dòng chữ cũng tích/bỏ tích được, không chỉ ô vuông
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            ),
    ) {
        Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)
        Text(
            text = "Tôi xác nhận các thông tin trên là chính xác và là thông tin của chính tôi.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Hiện khi không còn dữ liệu chip trong bộ nhớ. */
@Composable
private fun MissingInfoCard(onBack: () -> Unit) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = "Phiên xác thực đã hết. Vui lòng quét lại chip CCCD.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onBack,
                shape = ButtonShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text("Quét lại chip", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun ConfirmInfoPreview() {
    HydroTheme {
        ConfirmInfoContent(
            state = ConfirmInfoUiState(info = MockCccdChipReader.SAMPLE_CITIZEN, isConfirmed = true),
            onConfirmedChange = {},
            onSubmit = {},
            onBack = {},
        )
    }
}
