package app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import api
import app.ailaai.api.crashes
import com.queatz.db.Crash
import components.IconButton
import components.LazyColumn
import components.Loading
import format
import json
import kotlinx.browser.window
import kotlinx.datetime.toJSDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.overflowX
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.whiteSpace
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import r

@Composable
fun CrashesPlatformPage() {
    var crashes by remember {
        mutableStateOf(emptyList<Crash>())
    }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        api.crashes {
            crashes = it
        }
        isLoading = false
    }

    if (isLoading) {
        Loading()
    } else {
        LazyColumn {
            items(crashes) { crash ->
                val details = remember {
                    json.decodeFromString<JsonObject>(crash.details.orEmpty())
                }

                Div({
                    style {
                        fontSize(18.px)
                        marginTop(1.r)
                        marginBottom(1.r)
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                    }
                }) {
                    Text(crash.createdAt!!.toJSDate().format())

                    if (crash.person != null) {
                        IconButton(
                            "person",
                            // todo: translate
                            "Go to profile",
                            onClick = {
                                window.open("/profile/${crash.person!!}", target = "_blank")
                            }
                        )
                    }
                }

                Div({
                    style { marginBottom(1.r) }
                }) {
                    Div {
                        Span({ style { color(Color.gray) } }) { Text("App version ") }
                        Text(details["APP_VERSION_NAME"]?.jsonPrimitive?.content.orEmpty())
                    }
                    Div {
                        Span({ style { color(Color.gray) } }) { Text("Android version ") }
                        Text(details["ANDROID_VERSION"]?.jsonPrimitive?.content.orEmpty())
                    }
                    Div {
                        Span({ style { color(Color.gray) } }) { Text("Device ") }
                        Text(details["PHONE_MODEL"]?.jsonPrimitive?.content.orEmpty())
                    }
                }
                Div({
                    style {
                        whiteSpace("pre-wrap")
                        padding(1.r)
                        overflowX("auto")
                        borderRadius(1.r)
                        border(1.px, LineStyle.Solid, Color.gray)
                    }
                }) {
                    Text(details["STACK_TRACE"]?.jsonPrimitive?.content.orEmpty())
                }
            }
        }
    }
}
