package com.queatz.ailaai.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.queatz.ailaai.R
import com.queatz.ailaai.ui.components.DialogBase
import com.queatz.ailaai.ui.components.PersonMember
import com.queatz.ailaai.ui.theme.pad
import com.queatz.db.Person

@Composable
fun <T> ItemsPeopleDialog(
    title: String,
    onDismissRequest: () -> Unit,
    items: List<T>,
    people: (T) -> Person,
    key: (T) -> Any,
    showCountInTitle: Boolean = true,
    infoFormatter: (T) -> String? = { null },
    itemAction: (@Composable RowScope.(T) -> Unit)? = null,
    extraButtons: @Composable RowScope.() -> Unit = {},
    onClick: (T) -> Unit,
) {
    DialogBase(onDismissRequest) {
        Column(
            modifier = Modifier
                .padding(3.pad)
        ) {
            Text(
                if (showCountInTitle) "$title (${items.size})" else title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 1.pad)
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
            ) {
                items(items, key = key) {
                    PersonMember(
                        people(it),
                        infoFormatter = { _ -> infoFormatter(it) },
                        action = if (itemAction == null) {
                            null
                        } else {
                            @Composable {
                                itemAction(it)
                            }
                        }
                    ) { onClick(it) }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.pad, Alignment.End),
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                extraButtons()
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}
