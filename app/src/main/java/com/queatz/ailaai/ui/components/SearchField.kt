package com.queatz.ailaai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.queatz.ailaai.R
import com.queatz.ailaai.ui.theme.ElevationDefault

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchField(value: String, onValueChange: (value: String) -> Unit, modifier: Modifier = Modifier) {
    val keyboardController = LocalSoftwareKeyboardController.current!!
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(),
        elevation = CardDefaults.elevatedCardElevation(ElevationDefault / 2),
        modifier = modifier
            .widthIn(max = 480.dp)
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value,
            onValueChange = onValueChange,
            placeholder = { Text(stringResource(R.string.search)) },
            shape = MaterialTheme.shapes.large,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = {
                keyboardController.hide()
            }),
            trailingIcon = {
                if (value.isNotEmpty()) {
                    Icon(Icons.Outlined.Close, stringResource(R.string.clear), modifier = Modifier.clickable {
                        onValueChange("")
                    })
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}
