package app.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.components.TextBox
import app.nav.NavSearchInput
import application
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.Position.Companion.Relative
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.P
import r

suspend fun inputDialog(
    title: String?,
    placeholder: String = "",
    confirmButton: String = application.appString { okay },
    cancelButton: String? = application.appString { cancel },
    defaultValue: String = "",
    singleLine: Boolean = true,
    maxLength: Int? = null,
    type: InputType<*>? = null,
    inputStyles: StyleScope.() -> Unit = {},
    extraButtons: (@Composable (resolve: (Boolean?) -> Unit) -> Unit)? = null,
    inputAction: (@Composable (resolve: (Boolean?) -> Unit, value: String, onValue: (String) -> Unit) -> Unit)? = null,
    actions: (@Composable (resolve: (Boolean?) -> Unit) -> Unit)? = null,
    topContent: @Composable (resolve: (Boolean?) -> Unit, value: String, onValue: (String) -> Unit) -> Unit = { _, _, _ -> },
    content: @Composable (resolve: (Boolean?) -> Unit, value: String, onValue: (String) -> Unit) -> Unit = { _, _, _ -> },
): String? {
    var text: String = defaultValue
    val result = dialog(
        title = title,
        confirmButton = confirmButton,
        cancelButton = cancelButton,
        extraButtons = extraButtons,
        actions = actions
    ) { resolve ->
        var value by remember {
            mutableStateOf(defaultValue)
        }

        topContent(resolve, value) {
            value = it.take(maxLength ?: Int.MAX_VALUE)
            text = it.take(maxLength ?: Int.MAX_VALUE)
        }

        Div(
            attrs = {
                style {
                    position(Relative)
                }
            }
        ) {
            if (singleLine) {
                NavSearchInput(
                    value = value,
                    onChange = {
                        value = it.take(maxLength ?: Int.MAX_VALUE)
                        text = it.take(maxLength ?: Int.MAX_VALUE)
                    },
                    placeholder = placeholder,
                    selectAll = true,
                    type = type,
                    styles = {
                        margin(0.r)
                        maxWidth(100.percent)
                        width(100.percent)
                        inputStyles()
                    },
                    onDismissRequest = {
                        resolve(false)
                    }
                ) {
                    resolve(true)
                }
            } else {
                TextBox(
                    value = value,
                    onValue = {
                        value = it.take(maxLength ?: Int.MAX_VALUE)
                        text = it.take(maxLength ?: Int.MAX_VALUE)
                    },
                    placeholder = placeholder,
                    selectAll = false,
                    styles = {
                        margin(0.r)
                        width(32.r)
                        maxWidth(100.percent)
                        inputStyles()
                    },
                ) {
                    resolve(true)
                }
            }

            inputAction?.invoke(
                resolve,
                value,
                {
                    value = it.take(maxLength ?: Int.MAX_VALUE)
                    text = it.take(maxLength ?: Int.MAX_VALUE)
                }
            )
        }
        content(resolve, value) {
            value = it.take(maxLength ?: Int.MAX_VALUE)
            text = it.take(maxLength ?: Int.MAX_VALUE)
        }
    }

    return if (result == true) text else null
}
