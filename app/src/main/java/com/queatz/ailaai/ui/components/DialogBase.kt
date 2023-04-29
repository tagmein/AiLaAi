package com.queatz.ailaai.ui.components

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.queatz.ailaai.ui.theme.PaddingDefault

@Composable
fun DialogBase(onDismissRequest: () -> Unit, dismissable: Boolean = true, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Dialog(
        onDismissRequest,
        properties = DialogProperties(
            decorFitsSystemWindows = false,
            usePlatformDefaultWidth = false,
            dismissOnBackPress = dismissable,
            dismissOnClickOutside = dismissable
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .padding(PaddingDefault * 2)
                .imePadding()
                .then(modifier)
        ) {
            content()
        }
    }
}
