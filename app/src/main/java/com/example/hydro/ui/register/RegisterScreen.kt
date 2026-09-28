package com.example.hydro.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hydro.R
import com.example.hydro.ui.components.BrandHeader
import com.example.hydro.ui.components.BrandHeaderOverlap
import com.example.hydro.ui.theme.HydroTheme

private val FieldShape = RoundedCornerShape(12.dp)

/**
 * Màn tạo tên đăng nhập (mặc định là số điện thoại, khách tự đổi được) và mật khẩu.
 * [onRegistered] được gọi kèm tên đăng nhập khi đăng ký thành công.
 */
@Composable
fun RegisterScreen(
    onRegistered: (username: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = viewModel(),
) {
    val state = viewModel.uiState

    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered) {
            onRegistered(state.username)
            viewModel.onRegisteredHandled()
        }
    }

    RegisterContent(
        state = state,
        onUsernameChange = viewModel::onUsernameChange,
        onUseUsernameFromPhone = viewModel::useUsernameFromPhone,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onRegister = viewModel::register,
        modifier = modifier,
    )
}

@Composable
private fun RegisterContent(
    state: RegisterUiState,
    onUsernameChange: (String) -> Unit,
    onUseUsernameFromPhone: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = !state.isSubmitting

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding() // chừa chỗ cho bàn phím, tránh che ô nhập
            .verticalScroll(rememberScrollState()),
    ) {
        BrandHeader(
            title = "Tạo tài khoản đăng nhập",
            subtitle = "Bước cuối cùng: đặt tên đăng nhập và mật khẩu để dùng ứng dụng.",
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
                    Column {
                        OutlinedTextField(
                            value = state.username,
                            onValueChange = onUsernameChange,
                            label = { Text("Tên đăng nhập") },
                            leadingIcon = { Icon(painterResource(R.drawable.ic_person), contentDescription = null) },
                            isError = state.usernameError != null,
                            supportingText = {
                                Text(
                                    state.usernameError
                                        ?: "Mặc định là số điện thoại. Bạn có thể đổi thành tên dễ nhớ hơn (6-20 ký tự).",
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                autoCorrectEnabled = false,
                                imeAction = ImeAction.Next,
                            ),
                            singleLine = true,
                            enabled = enabled,
                            shape = FieldShape,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (state.canResetToPhone) {
                            TextButton(
                                onClick = onUseUsernameFromPhone,
                                enabled = enabled,
                                modifier = Modifier.align(Alignment.End),
                            ) {
                                Text("Dùng số điện thoại ${state.phone}")
                            }
                        }
                    }

                    PasswordField(
                        value = state.password,
                        onValueChange = onPasswordChange,
                        label = "Mật khẩu",
                        error = state.passwordError,
                        imeAction = ImeAction.Next,
                        enabled = enabled,
                    )

                    PasswordRules(password = state.password, username = state.username)

                    PasswordField(
                        value = state.confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = "Nhập lại mật khẩu",
                        error = state.confirmPasswordError,
                        imeAction = ImeAction.Done,
                        keyboardActions = KeyboardActions(onDone = { onRegister() }),
                        enabled = enabled,
                    )

                    Button(
                        onClick = onRegister,
                        enabled = enabled,
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
                            Text("Đăng ký", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Text(
                text = "Không chia sẻ mật khẩu cho bất kỳ ai, kể cả nhân viên ngân hàng.",
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

/** Ô nhập mật khẩu có nút con mắt để hiện/ẩn. */
@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    imeAction: ImeAction,
    enabled: Boolean,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(painterResource(R.drawable.ic_lock), contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    painter = painterResource(if (visible) R.drawable.ic_visibility_off else R.drawable.ic_visibility),
                    contentDescription = if (visible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = keyboardActions,
        singleLine = true,
        enabled = enabled,
        shape = FieldShape,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Danh sách điều kiện mật khẩu, điều kiện nào đạt thì hiện dấu tích màu chính. */
@Composable
private fun PasswordRules(password: String, username: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background, FieldShape)
            .padding(12.dp),
    ) {
        PasswordRule.entries.forEach { rule ->
            RuleRow(label = rule.label, met = CredentialValidator.isRuleMet(rule, password, username))
        }
    }
}

@Composable
private fun RuleRow(label: String, met: Boolean) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (met) {
            Icon(
                painter = painterResource(R.drawable.ic_check_circle),
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(16.dp),
            )
        } else {
            // Vòng tròn rỗng: điều kiện chưa đạt
            Box(
                Modifier
                    .size(16.dp)
                    .border(1.5.dp, colors.outline, CircleShape),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (met) colors.onSurface else colors.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun RegisterPreview() {
    HydroTheme {
        RegisterContent(
            state = RegisterUiState(
                phone = "0912345678",
                username = "nguyenvan.an",
                password = "Sang2026",
            ),
            onUsernameChange = {},
            onUseUsernameFromPhone = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onRegister = {},
        )
    }
}
