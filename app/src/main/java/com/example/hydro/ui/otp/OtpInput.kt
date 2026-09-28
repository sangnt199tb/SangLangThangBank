package com.example.hydro.ui.otp

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

private val CellShape = RoundedCornerShape(12.dp)

/**
 * Ô nhập OTP dạng 6 ô vuông. Thực chất là một ô nhập ẩn phía sau,
 * nên dán (paste) cả mã hay xoá lùi đều hoạt động bình thường.
 */
@Composable
fun OtpInput(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = OTP_LENGTH,
    isError: Boolean = false,
    enabled: Boolean = true,
) {
    val focusRequester = remember { FocusRequester() }
    var hasFocus by remember { mutableStateOf(false) }

    // Vào màn là mở bàn phím số ngay
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BasicTextField(
        value = code,
        onValueChange = onCodeChange,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done,
        ),
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { hasFocus = it.isFocused }
            .semantics { contentDescription = "Ô nhập mã OTP gồm $length chữ số" },
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(length) { index ->
                    OtpCell(
                        char = code.getOrNull(index),
                        isActive = hasFocus && enabled && index == code.length,
                        isError = isError,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                    )
                }
            }
        },
    )
}

@Composable
private fun OtpCell(
    char: Char?,
    isActive: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val borderColor = when {
        isError -> colors.error
        isActive -> colors.primary
        char != null -> colors.onSurfaceVariant
        else -> colors.outline
    }
    val background = if (char != null && !isError) colors.primaryContainer.copy(alpha = 0.35f) else colors.surface

    Box(
        modifier = modifier
            .background(background, CellShape)
            .border(if (isActive || isError) 2.dp else 1.dp, borderColor, CellShape),
        contentAlignment = Alignment.Center,
    ) {
        if (char != null) {
            Text(
                text = char.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
        } else if (isActive) {
            BlinkingCaret()
        }
    }
}

/** Vạch nhấp nháy báo ô đang chờ nhập. */
@Composable
private fun BlinkingCaret() {
    val transition = rememberInfiniteTransition(label = "caret")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "caretAlpha",
    )
    Box(
        modifier = Modifier
            .size(width = 2.dp, height = 24.dp)
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.primary),
    )
}
