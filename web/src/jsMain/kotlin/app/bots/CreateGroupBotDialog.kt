package app.bots

import app.dialog.dialog
import com.queatz.db.Bot
import notBlank
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import r

suspend fun createGroupBotDialog(bot: Bot) {
    val result = dialog(
        title = bot.name!!,
        // todo: translate
        confirmButton = "Add to group",
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
                BotConfigValues(it)
            }
        }
    }
}
