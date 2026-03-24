package com.pebble.feature.chat.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.pebble.core.design.theme.PebbleTheme

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message Pebble…") },
                shape = MaterialTheme.shapes.extraLarge,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = if (text.isNotBlank()) ImeAction.Send else ImeAction.Default,
                ),
                keyboardActions = KeyboardActions(
                    onSend = { if (!isGenerating && text.isNotBlank()) onSend() },
                ),
            )

            Spacer(Modifier.width(8.dp))

            // The send button pops in/out with a bouncy spring when text is present.
            // When generating, it transforms into a stop icon instead.
            AnimatedContent(
                targetState = when {
                    isGenerating       -> SendButtonState.Generating
                    text.isNotBlank() -> SendButtonState.Ready
                    else               -> SendButtonState.Hidden
                },
                transitionSpec = {
                    scaleIn(spring(Spring.DampingRatioMediumBouncy)) togetherWith scaleOut()
                },
                label = "send_button",
            ) { state ->
                when (state) {
                    SendButtonState.Hidden -> Spacer(Modifier.size(48.dp))
                    SendButtonState.Ready -> FilledIconButton(onClick = onSend) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send",
                        )
                    }
                    SendButtonState.Generating -> FilledIconButton(
                        onClick = { /* stop not implemented yet */ },
                        enabled = false,
                    ) {
                        Icon(Icons.Rounded.Stop, contentDescription = "Generating…")
                    }
                }
            }
        }
    }
}

private enum class SendButtonState { Hidden, Ready, Generating }

@PreviewLightDark
@Composable
private fun ChatInputBarPreview() {
    PebbleTheme {
        ChatInputBar(
            text = "What is on-device AI?",
            onTextChange = {},
            onSend = {},
            isGenerating = false,
        )
    }
}
