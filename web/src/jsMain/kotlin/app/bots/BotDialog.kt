package app.bots

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import api
import app.ailaai.api.botData
import app.ailaai.api.deleteBot
import app.ailaai.api.reloadBot
import app.ailaai.api.updateBot
import app.ailaai.api.updateBotData
import app.dialog.dialog
import app.dialog.photoDialog
import app.dialog.rememberChoosePhotoDialog
import app.menu.Menu
import application
import baseUrl
import com.queatz.db.Bot
import com.queatz.db.BotData
import components.GroupPhoto
import components.GroupPhotoItem
import components.IconButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import notBlank
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Pre
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.DOMRect
import org.w3c.dom.HTMLElement
import r

suspend fun botDialog(
    scope: CoroutineScope,
    bot: Bot,
    onBotUpdated: () -> Unit,
    onBotDeleted: () -> Unit
) {
    val photo = MutableStateFlow(bot.photo ?: "")

    val result = dialog(
        title = bot.name!!,
        confirmButton = application.appString { close },
        cancelButton = null,
        actions = {
            val me by application.me.collectAsState()

            if (me?.id == bot.creator) {
                val choosePhoto = rememberChoosePhotoDialog(showUpload = true)

                var menuTarget by remember { mutableStateOf<DOMRect?>(null) }

                menuTarget?.let {
                    Menu({ menuTarget = null }, it) {
                        item(application.appString { this.choosePhoto }) {
                            scope.launch {
                                choosePhoto.launch {
                                    api.updateBot(bot.id!!, Bot(photo = it)) {
                                        photo.value = it.photo ?: ""
                                    }
                                }
                            }
                        }

                        // todo: translate
                        item("Secret") {
                            scope.launch {
                                val secret = MutableStateFlow("")

                                api.botData(bot.id!!) {
                                    secret.value = it.secret.orEmpty()
                                }

                                val result = botSecretDialog(secret.value)

                                if (result != null) {
                                    secret.value = result

                                    api.updateBotData(bot.id!!, BotData(secret = secret.value)) {
                                        onBotUpdated()
                                    }
                                }
                            }
                        }

                        item("Reload") {
                            scope.launch {
                                api.reloadBot(bot.id!!) {
                                    onBotUpdated()
                                }
                            }
                        }

                        item(application.appString { delete }) {
                            scope.launch {
                                val result = dialog(
                                    // todo: translate
                                    "Delete this bot?",
                                    // todo: translate
                                    confirmButton = "Yes, delete"
                                ) {
                                    // todo: translate
                                    Text("This will remove the bot from any groups it is currently added to.")
                                }

                                if (result == true) {
                                    api.deleteBot(bot.id!!) {
                                        onBotDeleted()
                                    }
                                }
                            }
                        }
                    }
                }

                val isGenerating = choosePhoto.isGenerating.collectAsState().value

                // todo: translate
                IconButton("more_vert", "More options", isLoading = isGenerating) {
                    menuTarget = if (menuTarget == null) (it.target as HTMLElement).getBoundingClientRect() else null
                }
            }
        }
    ) {
        val photo = photo.collectAsState().value

        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(1.r)
            }
        }) {
            if (photo.isNotBlank()) {
                Div({
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Column)
                        alignItems(AlignItems.Center)
                    }
                }) {
                    GroupPhoto(
                        listOf(GroupPhotoItem(photo, null)),
                        size = 84.px
                    ) {
                        scope.launch {
                            photoDialog("$baseUrl/$photo")
                        }
                    }
                }
            }
            bot.description?.notBlank?.let {
                Text(it)
            }
            Pre({
                codeBlock()
            }) {
                Text(bot.url ?: "")
            }
        }
    }
}
