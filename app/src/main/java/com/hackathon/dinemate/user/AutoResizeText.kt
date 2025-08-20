package com.hackathon.dinemate.user

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.isUnspecified


@Composable
fun AutoResizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE
) {
    var resizedTextStyle by remember { mutableStateOf(style) }
    var shouldDraw by remember { mutableStateOf(false) }

    val defaultFontSize = style.fontSize

    Text(
        text = text,
        color = color,
        modifier = modifier.drawWithContent {
            if (shouldDraw) {
                drawContent()
            }
        },
        softWrap = false, // This is important to prevent wrapping
        style = resizedTextStyle,
        textAlign = textAlign,
        maxLines = maxLines,
        onTextLayout = { result ->
            // This is called when the text layout is calculated
            if (result.didOverflowWidth) {
                if (resizedTextStyle.fontSize.isUnspecified) {
                    resizedTextStyle = resizedTextStyle.copy(
                        fontSize = defaultFontSize
                    )
                }
                resizedTextStyle = resizedTextStyle.copy(
                    fontSize = resizedTextStyle.fontSize * 0.95 // Reduce font size
                )
            } else {
                shouldDraw = true
            }
        }
    )
}