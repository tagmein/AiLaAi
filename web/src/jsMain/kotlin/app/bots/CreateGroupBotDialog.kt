package app.bots

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import api
import app.ailaai.api.bot
import app.ailaai.api.createGroupBot
import app.dialog.dialog
import app.menu.Menu
import com.queatz.db.Bot
import com.queatz.db.BotConfigValue
import com.queatz.db.GroupBot
import components.IconButton
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import notBlank
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.DOMRect
import org.w3c.dom.HTMLElement
import r

suspend fun createGroupBotDialog(
    reload: SharedFlow<Unit>,
    group: String,
    bot: Bot,
    onOpenBot: (Bot) -> Unit,
    onGroupBotCreated: (GroupBot) -> Unit
) {
    val values = MutableStateFlow(emptyList<BotConfigValue>())

    val result = dialog(
        title = bot.name!!,
        // todo: translate
        confirmButton = "Add to group",
        actions = { resolve ->
            var menuTarget by remember { mutableStateOf<DOMRect?>(null) }

            var bot by remember { mutableStateOf(bot) }

            LaunchedEffect(Unit) {
                reload.collectLatest {
                    api.bot(bot.id!!, onError = {
                        if ((it as? ResponseException)?.response?.status == HttpStatusCode.NotFound) {
                            resolve(null)
                        }
                    }) {
                        bot = it
                    }
                }
            }

            menuTarget?.let {
                Menu({ menuTarget = null }, it) {
                    // todo: translate
                    item("Go to bot") {
                        onOpenBot(bot)
                    }
                }
            }

            // todo: translate
            IconButton("more_vert", "Menu") {
                menuTarget = if (menuTarget == null) (it.target as HTMLElement).getBoundingClientRect() else null
            }
        }
    ) {
        Div({
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(1.r)
            }
        }) {
            bot.description?.notBlank?.let {
                Text(it)
            }

            bot.config?.let {
                BotConfigValues(it) {
                    values.value = it
                }
            }
        }
    }

    if (result == true) {
        api.createGroupBot(
            group = group,
            groupBot = GroupBot(
                group = group,
                bot = bot.id!!,
                config = values.value,
                active = true,
            )
        ) {
            onGroupBotCreated(it)
        }
    }
}
