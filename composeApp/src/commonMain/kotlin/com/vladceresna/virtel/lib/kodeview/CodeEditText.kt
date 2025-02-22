package com.vladceresna.virtel.lib.kodeview

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation

import dev.snipme.highlights.DefaultHighlightsResultListener
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.CodeHighlight

@Composable
fun CodeEditText(
    highlights: Highlights,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    handleIndentations: Boolean = true,
    textStyle: TextStyle = LocalTextStyle.current,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: TextFieldColors = TextFieldDefaults.colors()
) {
    val currentText = remember {
        mutableStateOf(
            TextFieldValue(
                AnnotatedString(highlights.getCode())
            )
        )
    }

    LaunchedEffect(highlights) {
        try {
            highlights.getHighlightsAsync(object : DefaultHighlightsResultListener() {
                override fun onSuccess(result: List<CodeHighlight>) {
                    try {
                        currentText.value = currentText.value.copy(
                            annotatedString = result.generateAnnotatedString(currentText.value.text),
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateNewValue(change: TextFieldValue) {

        var currentTextValue = currentText.value

        val updated = change.updateIndentations(handleIndentations)
        if (updated.text != currentTextValue.text) {
            onValueChange(updated.text)
        }

        currentText.value = updated.copySpanStyles(
            currentTextValue
        )

    }

    TextField(
        modifier = modifier.fillMaxWidth(),
        onValueChange = ::updateNewValue,
        value = currentText.value,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        colors = colors,
    )
}