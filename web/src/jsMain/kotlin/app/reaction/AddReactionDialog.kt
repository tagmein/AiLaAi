package app.reaction

import Styles
import androidx.compose.runtime.remember
import app.dialog.inputDialog
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexShrink
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.overflow
import org.jetbrains.compose.web.css.paddingBottom
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.whiteSpace
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import r

suspend fun addReactionDialog(
    quickReactions: List<String> = emptyList(),
    // todo: translate
    confirmButton: String = "React"
) = inputDialog(
    title = null,
    // todo: translate
    placeholder = "Custom",
    confirmButton = confirmButton,
    maxLength = 64,
    inputStyles = {
        width(100.percent)
    },
    topContent = { resolve, value, onValue ->
        val common = remember(quickReactions) {
            // todo include from GroupExtended.topReactions and from my top reactions
            (quickReactions + listOf(
                "\uD83D\uDE02",
                "\uD83D\uDE0E",
                "\uD83D\uDE32",
                "\uD83E\uDD73",
                "\uD83E\uDD17",
                "\uD83E\uDD14",
                "\uD83D\uDE18",
                "\uD83D\uDE2C",
            ).shuffled()).distinct()
        }

        Div({
            style {
                display(DisplayStyle.Flex)
                gap(.5.r)
                fontSize(18.px)
                paddingBottom(1.r)
                maxWidth(32.r)
                overflow("auto")
            }
        }) {
            common.forEach { reaction ->
                Button(
                    {
                        classes(Styles.outlineButton)

                        style {
                            flexShrink(0)
                            whiteSpace("nowrap")
                        }

                        onClick {
                            onValue(reaction)
                            resolve(true)
                        }
                    }
                ) {
                    Text(reaction)
                }
            }
        }
    }
)
