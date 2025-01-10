package com.queatz.ailaai.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.queatz.ailaai.R
import com.queatz.ailaai.data.api
import com.queatz.ailaai.extensions.*
import com.queatz.ailaai.services.authors
import com.queatz.ailaai.ui.components.PersonItem
import com.queatz.ailaai.ui.dialogs.Media
import com.queatz.ailaai.ui.dialogs.PhotoDialog
import com.queatz.ailaai.ui.theme.pad
import com.queatz.db.InventoryItemExtended
import com.queatz.db.Person

@Composable
fun InventoryItemDetails(inventoryItem: InventoryItemExtended) {
    var showPhotoDialog by rememberStateOf(false)
    var creator by rememberStateOf<Person?>(null)
    val expired = inventoryItem.inventoryItem!!.isExpired

    LaunchedEffect(Unit) {
        creator = authors.person(inventoryItem.item!!.creator!!)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(1.pad),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 1.pad)
    ) {
        AsyncImage(
            model = inventoryItem.item?.photo?.let { api.url(it) },
            contentDescription = "",
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier
                .requiredSize(64.dp)
                .clip(MaterialTheme.shapes.large)
                .clickable {
                    showPhotoDialog = true
                }
        )
        Text(
            listOfNotNull(
                inventoryItem.item?.name ?: "",
                (inventoryItem.inventoryItem!!.quantity?.format() ?: "0").let { "x$it" }
            ).joinToString(" "),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Text(
            inventoryItem.item?.description ?: "",
            textAlign = TextAlign.Center
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(.5f.pad),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.creator),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline),
                textAlign = TextAlign.Center
            )

            if (creator != null) {
                PersonItem(creator!!)
            }
        }

        inventoryItem.inventoryItem?.expiresAt?.let { expiration ->
            Text(
                if (expired) {
                    stringResource(R.string.expired)
                } else {
                    stringResource(
                        R.string.expires_x,
                        "${expiration.timeUntil()} ${stringResource(R.string.inline_on)} ${expiration.formatDateAndTime()}"
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(top = 1.pad)
            )
        } ?: let {
            Text(
                stringResource(R.string.never_expires),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(top = 1.pad)
            )
        }
    }


    if (showPhotoDialog) {
        PhotoDialog(
            {
                showPhotoDialog = false
            },
            initialMedia = Media.Photo(inventoryItem.item!!.photo!!),
            medias = listOf(Media.Photo(inventoryItem.item!!.photo!!))
        )
    }
}
