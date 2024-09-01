package com.queatz.ailaai.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.window.DialogProperties
import com.queatz.ailaai.extensions.fadingEdge
import com.queatz.ailaai.extensions.rememberStateOf

@Composable
fun Alert(
    onDismissRequest: () -> Unit,
    title: String?,
    text: String?,
    dismissButton: String?,
    confirmButton: String,
    confirmColor: Color? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    properties: DialogProperties = DialogProperties(),
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest,
        properties = properties,
        title = if (title != null) {
            {

                Text(title)
            }
        } else null,
        text = if (text != null) {
            {
                val state = rememberScrollState()
                var viewport by rememberStateOf(Size.Zero)
                Column(
                    modifier = Modifier
                        .verticalScroll(state)
                        .onPlaced { viewport = it.boundsInParent().size }
                        .fadingEdge(viewport, state, factor = 10f)
                ) {
                    SelectionContainer {
                        Text(text, style = textStyle)
                    }
                }
            }
        } else null,
        dismissButton = dismissButton?.let {
            {
                TextButton(
                    {
                        onDismissRequest()
                    }
                ) {
                    Text(it)
                }
            }
        },
        confirmButton = {
            TextButton(
                {
                    onConfirm()
                }
            ) {
                Text(confirmButton, color = confirmColor ?: Color.Unspecified)
            }
        }
    )
}
