package com.queatz.ailaai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.queatz.ailaai.R
import com.queatz.ailaai.api.uploadPhotosFromUris
import com.queatz.ailaai.data.api
import com.queatz.ailaai.extensions.rememberStateOf
import com.queatz.ailaai.ui.dialogs.ChoosePhotoDialog
import com.queatz.ailaai.ui.dialogs.ChoosePhotoDialogState
import com.queatz.ailaai.ui.dialogs.Media
import com.queatz.ailaai.ui.dialogs.PhotoDialog
import com.queatz.ailaai.ui.theme.pad
import kotlinx.coroutines.launch

@Composable
fun SetPhotoButton(
    photoText: String,
    photo: String,
    transparentBackground: Boolean = false,
    onPhoto: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var choosePhotoDialog by rememberStateOf(false)
    var showPhotoDialog by rememberStateOf(false)
    var isGeneratingPhoto by rememberStateOf(false)
    val setPhotoState = remember(photoText) {
        ChoosePhotoDialogState(mutableStateOf(photoText))
    }

    if (showPhotoDialog) {
        PhotoDialog(
            {
                showPhotoDialog = false
            },
            initialMedia = Media.Photo(photo),
            medias = listOf(Media.Photo(photo))
        )
    }

    if (choosePhotoDialog) {
        ChoosePhotoDialog(
            scope = scope,
            state = setPhotoState,
            onDismissRequest = { choosePhotoDialog = false },
            multiple = false,
            imagesOnly = true,
            transparentBackground = transparentBackground,
            onPhotos = { photos ->
                scope.launch {
                    isGeneratingPhoto = true
                    api.uploadPhotosFromUris(context, photos, removeBackground = transparentBackground) {
                        onPhoto(it.urls.first())
                    }
                    isGeneratingPhoto = false
                }
            },
            onGeneratedPhoto = {
                onPhoto(it)
            },
            onIsGeneratingPhoto = {
                isGeneratingPhoto = it
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if (photo.isNotBlank() && !isGeneratingPhoto) {
            Column(
                verticalArrangement = Arrangement.spacedBy(1.pad),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                AsyncImage(
                    model = photo.let { api.url(it) },
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .requiredSize(64.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable {
                            showPhotoDialog = true
                        }
                )
                OutlinedButton(
                    {
                        choosePhotoDialog = true
                    }
                ) {
                    Text(stringResource(R.string.change_photo))
                }
            }
        } else {
            TextButton(
                {
                    choosePhotoDialog = true
                },
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(1.pad),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isGeneratingPhoto) {
                        CircularProgressIndicator(
                            strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth / 2,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            Icons.Outlined.CameraAlt, ""
                        )
                    }
                    Text(stringResource(R.string.photo))
                }
            }
        }
    }
}
