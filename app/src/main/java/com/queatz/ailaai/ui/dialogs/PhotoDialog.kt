package com.queatz.ailaai.ui.dialogs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.queatz.ailaai.api
import com.queatz.ailaai.ui.theme.PaddingDefault

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoDialog(onDismissRequest: () -> Unit, initialPhoto: String, photos: List<String>) {
    Dialog(
        {
            onDismissRequest()
        }, properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null
                ) {
                    onDismissRequest()
                }
        ) {
            val photosListState = rememberLazyListState(photos.indexOf(initialPhoto))
            LazyRow(
                state = photosListState,
                flingBehavior = rememberSnapFlingBehavior(photosListState),
                reverseLayout = true,
                horizontalArrangement = Arrangement.spacedBy(PaddingDefault * 2),
                modifier = Modifier.fillMaxSize()
            ) {
                items(photos, key = { it }) { photo ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(api.url(photo))
                                .crossfade(true)
                                .build(),
                            contentDescription = "",
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.Center,
                            modifier = Modifier
                                .fillParentMaxSize()
                        )
                    }
                }
            }
        }
    }
}
