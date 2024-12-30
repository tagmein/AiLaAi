package app.nav

import LocalConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import api
import app.ailaai.api.platformMe
import app.ailaai.api.profile
import app.ailaai.api.updateMe
import app.ailaai.api.updateProfile
import app.components.EditField
import app.dialog.dialog
import app.dialog.inputDialog
import appString
import appText
import application
import com.queatz.db.Person
import com.queatz.db.PersonProfile
import com.queatz.db.Profile
import components.IconButton
import components.QrImg
import components.Wbr
import kotlinx.browser.window
import kotlinx.coroutines.launch
import notBlank
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.Text
import r
import webBaseUrl

@Composable
fun ProfileNavPage(
    onProfileClick: () -> Unit,
    onPlatformClick: () -> Unit,
    onScriptsClick: () -> Unit,
) {
    val me by application.me.collectAsState()
    val scope = rememberCoroutineScope()

    var profile by remember {
        mutableStateOf<PersonProfile?>(null)
    }

    var isPlatformHost by remember { mutableStateOf(false) }

    suspend fun reload() {
        api.profile(me!!.id!!) {
            profile = it
        }
    }

    LaunchedEffect(me) {
        if (me != null) {
            reload()
        }
    }

    LaunchedEffect(Unit) {
        api.platformMe {
            isPlatformHost = it.host
        }
    }

    suspend fun saveAbout(value: String): Boolean {
        var success = false
        api.updateProfile(Profile(about = value)) {
            success = true
            reload()
        }

        return success
    }

    NavTopBar(me, appString { this.profile }, onProfileClick = onProfileClick) {
        IconButton("qr_code", appString { qrCode }) {
            scope.launch {
                dialog("", cancelButton = null) {
                    QrImg("$webBaseUrl/profile/${me!!.id!!}")
                }
            }
        }

        IconButton("open_in_new", appString { viewProfile }, styles = {
            marginRight(.5.r)
        }) {
            window.open("/profile/${me!!.id!!}", "_blank")
        }
    }

    NavMenu {
        val yourName = appString { yourName }
        val update = appString { update }

        NavMenuItem("account_circle", me?.name?.notBlank ?: yourName) {
            scope.launch {
                val name = inputDialog(
                    yourName,
                    confirmButton = update,
                    defaultValue = me?.name ?: ""
                )

                api.updateMe(Person(name = name)) {
                    application.setMe(it)
                }
            }
        }

        if (profile != null) {
            EditField(
                profile?.profile?.about ?: "",
                placeholder = appString { introduceYourself },
                styles = {
                    margin(.5.r)
                    textAlign("center")
                }
            ) {
                saveAbout(it)
            }
        }

        val configuration = LocalConfiguration.current

        NavMenuItem(
            when (configuration.language) {
                "vi" -> "\uD83C\uDDFB\uD83C\uDDF3"
                "ru" -> "\uD83C\uDDF7\uD83C\uDDFA"
                else -> "\uD83C\uDDEC\uD83C\uDDE7"
            },
            when (configuration.language) {
                "vi" -> "Language"
                "ru" -> "Язык"
                else -> "Ngôn ngữ"
            },
            textIcon = true
        ) {
            configuration.set(
                when (configuration.language) {
                    "en" -> "vi"
                    //"vi" -> "ru"
                    else -> "en"
                }
            )
        }

        val signOut = appString { signOut }
        val signOutQuestion = appString { signOutQuestion }

        NavMenuItem("history_edu", appString { scripts }) {
            onScriptsClick()
        }

        if (isPlatformHost) {
            NavMenuItem("guardian", appString { platform }) {
                onPlatformClick()
            }
        }

        NavMenuItem("logout", signOut) {
            scope.launch {
                val result = dialog(signOutQuestion, signOut) {
                    appText { signOutQuestionLine1 }
                    Wbr()
                    Text(" ")
                    appText { signOutQuestionLine2 }
                }

                if (result == true) {
                    application.signOut()
                    window.location.pathname = "/"
                    window.location.reload()
                }
            }
        }
    }
}
