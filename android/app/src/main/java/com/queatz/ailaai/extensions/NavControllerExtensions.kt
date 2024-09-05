package com.queatz.ailaai.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.queatz.ailaai.AppNav

fun NavController.appNavigate(route: AppNav, options: NavOptions? = null) = navigate(route.route, options)

fun NavController.popBackStackOrFinish() {
    if (!popBackStack()) {
        (context as? Activity)?.finish()
    }
}

fun Context.goToSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.parse("package:$packageName")
    )
    (this as Activity).startActivity(intent)
}

fun NavController.goToSettings() {
    context.goToSettings()
}
