package components

import app.dialog.dialog
import application
import kotlinx.browser.window
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import r

suspend fun getAppDialog() {
    dialog(
        title = null,
        confirmButton = application.appString { close },
        cancelButton = null
    ) {
        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(1.r)
            }
        }) {
            Button({
                classes(Styles.outlineButton, Styles.outlineButtonAlt)

                style {
                    borderRadius(1.r)
                }

                onClick {
                    window.open("https://play.google.com/store/apps/details?id=com.ailaai.app", target = "_blank")
                }
            }) {
                Icon("android")
                // todo: translate
                Text("Google Play")
            }
            Button({
                classes(Styles.outlineButton, Styles.outlineButtonAlt)

                style {
                    borderRadius(1.r)
                }

                onClick {
                    window.open("/ailaai.apk", target = "_blank")
                }
            }) {
                Icon("download")
                // todo: translate
                Text("Android APK")
            }
            Button({
                classes(Styles.outlineButton, Styles.outlineButtonAlt)

                style {
                    borderRadius(1.r)
                }

                onClick {
                    window.open("https://apps.apple.com/us/app/ai-l%C3%A0-ai/id6475618663", target = "_blank")
                }
            }) {
                Icon("ios")
                // todo: translate
                Text("App Store")
            }
        }
    }
}
