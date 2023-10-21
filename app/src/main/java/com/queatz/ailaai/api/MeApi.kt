package com.queatz.ailaai.api

import android.content.Context
import android.net.Uri
import app.ailaai.api.*
import com.queatz.ailaai.extensions.asInputProvider
import com.queatz.ailaai.extensions.asScaledJpeg
import com.queatz.ailaai.extensions.asScaledVideo
import io.ktor.http.*


suspend fun Api.updateProfilePhoto(
    context: Context,
    photo: Uri,
    onError: ErrorBlock = null,
    onSuccess: SuccessBlock<HttpStatusCode> = {},
) {
    return updateProfilePhoto(
        photo.asScaledJpeg(context),
        onError,
        onSuccess
    )
}

suspend fun Api.updateProfileVideo(
    context: Context,
    video: Uri,
    contentType: String,
    filename: String,
    processingCallback: (Float) -> Unit,
    uploadCallback: (Float) -> Unit,
    onError: ErrorBlock = null,
    onSuccess: SuccessBlock<HttpStatusCode> = {},
) {
    val scaledVideo = try {
        video.asScaledVideo(context, progressCallback = processingCallback)
    } catch (e: Exception) {
        e.printStackTrace()
        onError?.invoke(e)
        return
    }
    return updateProfileVideo(
        scaledVideo.asInputProvider(),
        contentType,
        filename,
        uploadCallback,
        onError,
        onSuccess
    )
}

suspend fun Api.updateMyPhoto(
    context: Context,
    photo: Uri,
    onError: ErrorBlock = null,
    onSuccess: SuccessBlock<HttpStatusCode> = {},
) {
    return updateMyPhoto(
        photo.asScaledJpeg(context),
        onError,
        onSuccess
    )
}

