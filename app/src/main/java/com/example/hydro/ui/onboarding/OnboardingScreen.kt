package com.example.hydro.ui.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hydro.R
import com.example.hydro.ui.components.BrandHeader
import com.example.hydro.ui.components.BrandHeaderOverlap
import com.example.hydro.ui.theme.HydroTheme

private val FieldShape = RoundedCornerShape(12.dp)

/**
 * Màn hình onboarding: nhập email, số điện thoại, mã MIS và captcha.
 * [onSubmitted] được gọi kèm số điện thoại khi gửi thông tin thành công.
 */
@Composable
fun OnboardingScreen(
    onSubmitted: (phone: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = viewModel(),
) {
    val state = viewModel.uiState

    LaunchedEffect(state.isSubmitted) {
        if (state.isSubmitted) {
            onSubmitted(state.phone)
            // Đánh dấu đã xử lý, để khi quay lại màn này không tự chuyển đi lần nữa
            viewModel.onSubmittedHandled()
        }
    }

    OnboardingContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPhoneChange = viewModel::onPhoneChange,
        onMisCodeChange = viewModel::onMisCodeChange,
        onCaptchaInputChange = viewModel::onCaptchaInputChange,
        onAcceptedTermsChange = viewModel::onAcceptedTermsChange,
        onRefreshCaptcha = viewModel::refreshCaptcha,
        onSubmit = viewModel::submit,
        modifier = modifier,
    )
}

/** Phần giao diện chỉ hiển thị theo [state], không chứa logic: dễ xem trước (Preview). */
@Composable
private fun OnboardingContent(
    state: OnboardingUiState,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onMisCodeChange: (String) -> Unit,
    onCaptchaInputChange: (String) -> Unit,
    onAcceptedTermsChange: (Boolean) -> Unit,
    onRefreshCaptcha: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = !state.isSubmitting
    var showTerms by rememberSaveable { mutableStateOf(false) }

    if (showTerms) {
        TermsDialog(
            onAccept = {
                onAcceptedTermsChange(true)
                showTerms = false
            },
            onDismiss = { showTerms = false },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding() // chừa chỗ cho bàn phím, tránh che ô nhập
            .verticalScroll(rememberScrollState()),
    ) {
        BrandHeader(
            title = "Mở tài khoản trực tuyến",
            subtitle = "Chỉ vài bước đơn giản, không cần đến quầy giao dịch.",
        )

        Column(
            modifier = Modifier
                .offset(y = -BrandHeaderOverlap)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionTitle("Thông tin liên hệ")

                    FormField(
                        value = state.email,
                        onValueChange = onEmailChange,
                        label = "Email",
                        placeholder = "ten@example.com",
                        iconRes = R.drawable.ic_mail,
                        error = state.emailError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                        enabled = enabled,
                    )

                    FormField(
                        value = state.phone,
                        onValueChange = onPhoneChange,
                        label = "Số điện thoại",
                        placeholder = "0912345678",
                        iconRes = R.drawable.ic_smartphone,
                        error = state.phoneError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next,
                        ),
                        enabled = enabled,
                    )

                    FormField(
                        value = state.misCode,
                        onValueChange = onMisCodeChange,
                        label = "Mã MIS (không bắt buộc)",
                        hint = "Mã nhân viên giới thiệu, nếu có",
                        iconRes = R.drawable.ic_person,
                        error = state.misCodeError,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Next,
                        ),
                        enabled = enabled,
                    )

                    SectionTitle("Mã xác thực", Modifier.padding(top = 4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CaptchaImage(
                            text = state.captchaText,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                        )
                        FilledTonalIconButton(
                            onClick = onRefreshCaptcha,
                            enabled = enabled,
                            shape = FieldShape,
                            modifier = Modifier.size(56.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_refresh),
                                contentDescription = "Đổi mã captcha khác",
                            )
                        }
                    }

                    FormField(
                        value = state.captchaInput,
                        onValueChange = onCaptchaInputChange,
                        label = "Nhập mã captcha",
                        iconRes = R.drawable.ic_shield,
                        error = state.captchaError,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            // Chưa đồng ý điều khoản thì chỉ đóng bàn phím
                            if (state.acceptedTerms) onSubmit() else defaultKeyboardAction(ImeAction.Done)
                        }),
                        enabled = enabled,
                    )

                    TermsCheckbox(
                        checked = state.acceptedTerms,
                        onCheckedChange = onAcceptedTermsChange,
                        onOpenTerms = { showTerms = true },
                        enabled = enabled,
                    )

                    Button(
                        onClick = onSubmit,
                        enabled = enabled && state.acceptedTerms,
                        shape = FieldShape,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Tiếp tục", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Text(
                text = "Thông tin của bạn được mã hoá và chỉ dùng cho mục đích mở tài khoản.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier,
    )
}

/** Ô nhập dùng chung cho form: có icon bên trái, báo lỗi hoặc gợi ý bên dưới. */
@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    @DrawableRes iconRes: Int,
    error: String?,
    keyboardOptions: KeyboardOptions,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    hint: String? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = { Icon(painterResource(iconRes), contentDescription = null) },
        isError = error != null,
        supportingText = (error ?: hint)?.let { { Text(it) } },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        enabled = enabled,
        shape = FieldShape,
        modifier = modifier.fillMaxWidth(),
    )
}

/** Ô tích đồng ý điều khoản. Bấm vào chữ "Điều khoản và điều kiện" để mở nội dung. */
@Composable
private fun TermsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onOpenTerms: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val text = buildAnnotatedString {
        append("Tôi đã đọc và đồng ý với ")
        withLink(
            LinkAnnotation.Clickable(
                tag = "terms",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = linkColor,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline,
                    ),
                ),
                linkInteractionListener = { onOpenTerms() },
            ),
        ) {
            append("Điều khoản và điều kiện")
        }
        append(" mở tài khoản của SangLangThangBank.")
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Hộp thoại hiển thị điều khoản. Bấm "Tôi đồng ý" sẽ tự tích vào ô đồng ý. */
@Composable
private fun TermsDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Điều khoản và điều kiện") },
        text = {
            // TODO: thay bằng nội dung điều khoản thật (thường lấy từ server)
            Text(
                text = """
                    1. Khách hàng cam kết thông tin cung cấp là chính xác và thuộc sở hữu của mình.

                    2. Ngân hàng được phép sử dụng email và số điện thoại để liên hệ, gửi mã OTP và thông báo liên quan đến tài khoản.

                    3. Thông tin cá nhân được bảo mật theo quy định của pháp luật về bảo vệ dữ liệu cá nhân.

                    4. Tài khoản chỉ được kích hoạt sau khi khách hàng hoàn tất xác thực danh tính (eKYC).

                    5. Khách hàng tự bảo quản mật khẩu, mã OTP và không chia sẻ cho bất kỳ ai.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
            )
        },
        confirmButton = {
            TextButton(onClick = onAccept) { Text("Tôi đồng ý") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        },
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun OnboardingPreview() {
    HydroTheme {
        OnboardingContent(
            state = OnboardingUiState(
                email = "abc@",
                captchaText = "K7PX3",
                emailError = "Email không hợp lệ",
            ),
            onEmailChange = {},
            onPhoneChange = {},
            onMisCodeChange = {},
            onCaptchaInputChange = {},
            onAcceptedTermsChange = {},
            onRefreshCaptcha = {},
            onSubmit = {},
        )
    }
}
