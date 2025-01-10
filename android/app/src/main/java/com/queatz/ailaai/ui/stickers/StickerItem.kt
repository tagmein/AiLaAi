package com.queatz.ailaai.ui.stickers

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.queatz.ailaai.data.api
import com.queatz.ailaai.ui.theme.pad
import com.queatz.db.Sticker

@Composable
fun StickerItem(
    sticker: Sticker,
    modifier: Modifier = Modifier,
    showName: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    if (showName && !sticker.name.isNullOrBlank()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            StickerPhoto(sticker.photo, onLongClick = onLongClick, onClick = onClick)
            Text(
                sticker.name ?: "",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(1.pad)
            )
        }
    } else {
        StickerPhoto(sticker.photo, modifier, onLongClick, onClick)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StickerPhoto(photo: String?, modifier: Modifier = Modifier, onLongClick: (() -> Unit)? = null, onClick: () -> Unit) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(photo?.let(api::url))
            .crossfade(true)
            .build(),
        contentDescription = "",
        contentScale = ContentScale.Fit,
        alignment = Alignment.Center,
        modifier = modifier
            .height(96.dp)
            .widthIn(min = 96.dp)
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    )
}
