package components

import Styles
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import api
import app.ailaai.api.wildReply
import app.dialog.dialog
import appString
import application
import com.queatz.db.Card
import com.queatz.db.CardOptions
import com.queatz.db.ConversationAction
import com.queatz.db.ConversationItem
import com.queatz.db.WildReplyBody
import kotlinx.coroutines.launch
import notBlank
import notEmpty
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.autoFocus
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextArea
import r

@Composable
fun CardReply(
    card: Card,
    cardOptions: CardOptions?,
    cardConversation: ConversationItem?,
    stack: MutableList<ConversationItem>,
    replyMessage: String,
    replyMessageContact: String,
    isReplying: List<ConversationItem>?,
    onCardConversation: (ConversationItem?) -> Unit,
    onMessageSent: () -> Unit,
    onIsReplying: (List<ConversationItem>?) -> Unit,
    onReplyMessage: (String) -> Unit,
    onReplyMessageContact: (String) -> Unit,
    isLastElement: Boolean
) {
    val me by application.me.collectAsState()
    val scope = rememberCoroutineScope()
    var isSendingReply by remember { mutableStateOf(false) }

    val sentString = appString { messageWasSent }
    val didntWorkString = appString { didntWork }

    suspend fun sendMessage() {
        isSendingReply = true
        val body = WildReplyBody(
            message = listOfNotNull(
                replyMessage,
                replyMessageContact.notBlank
            ).joinToString("\n\n"),
            conversation = isReplying!!.map { it.title }.filter { it.isNotBlank() }
                .notEmpty?.joinToString(" → "),
            card = card.id!!,
            device = api.device
        )
        api.wildReply(
            reply = body,
            onError = {
                scope.launch {
                    dialog(didntWorkString, cancelButton = null)
                }
            }
        ) {
            onMessageSent()
            scope.launch {
                dialog(sentString, cancelButton = null)
            }
        }
        isSendingReply = false
    }

    if (isReplying != null) {
        TextArea(replyMessage) {
            classes(Styles.textarea)
            style {
                width(100.percent)
                height(8.r)
                marginBottom(1.r)
            }

            if (isSendingReply) {
                disabled()
            }

            // todo: translate
            placeholder("Enter your message")

            onInput {
                onReplyMessage(it.value)
            }

            autoFocus()

            ref {
                it.focus()
                onDispose {}
            }
        }
        if (me == null) {
            Div({
                style {
                    fontWeight("bold")
                }
            }) {
                // todo: translate
                Text("How would you like to be contacted?")
            }
            Input(InputType.Text) {
                classes(Styles.textarea)
                style {
                    width(100.percent)
                    marginBottom(1.r)
                }

                // todo: translate
                placeholder("Your phone number or email")

                onInput {
                    onReplyMessageContact(it.value)
                }

                if (isSendingReply) {
                    disabled()
                }
            }
        }
        Div({
            style {
                display(DisplayStyle.Flex)

                if (!isLastElement) {
                    marginBottom(1.r)
                }
            }
        }) {
            Button({
                classes(Styles.button)
                style {
                    marginRight(1.r)
                }
                onClick {
                    scope.launch {
                        sendMessage()
                    }
                }
                if (isSendingReply || replyMessage.isBlank() || (me == null && replyMessageContact.isBlank())) {
                    disabled()
                }
            }) {
                Text(appString { sendMessage })
            }
            Button({
                classes(Styles.outlineButton)
                onClick {
                    onIsReplying(null)
                }
                if (isSendingReply) {
                    disabled()
                }
            }) {
                Text(appString { cancel })
            }
        }
    } else {
        cardConversation?.items?.forEach { item ->
            when (item.action) {
                ConversationAction.Message -> {
                    Button({
                        classes(Styles.button)
                        onClick {
                            onIsReplying(stack + cardConversation.let(::listOf) + item.let(::listOf))
                        }
                    }) {
                        Span({
                            classes("material-symbols-outlined")
                        }) {
                            Text("message")
                        }
                        Text(" ${item.title}")
                    }
                }

                else -> {
                    Button({
                        classes(Styles.outlineButton)
                        onClick {
                            stack.add(cardConversation)
                            onCardConversation(item)
                        }
                    }) {
                        Text(item.title)
                    }
                }
            }
        }
        if (
            cardConversation?.items.isNullOrEmpty() &&
            ((me != null && cardOptions?.enableReplies != false) || cardOptions?.enableAnonymousReplies != false)
        ) {
            Button({
                classes(Styles.button)
                onClick {
                    onIsReplying(stack + (cardConversation?.let(::listOf) ?: emptyList()))
                }
            }) {
                Span({
                    classes("material-symbols-outlined")
                }) {
                    Text("message")
                }
                Text(" ${appString { message }}")
            }
        }
        if (stack.isNotEmpty()) {
            Button({
                classes(Styles.outlineButton)
                onClick {
                    onCardConversation(stack.removeLast())
                }
            }) {
                Text(appString { goBack })
            }
        }
    }
}
