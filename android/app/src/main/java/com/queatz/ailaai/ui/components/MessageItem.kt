package com.queatz.ailaai.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.queatz.ailaai.AppNav
import com.queatz.ailaai.extensions.appNavigate
import com.queatz.ailaai.extensions.rememberStateOf
import com.queatz.ailaai.nav
import com.queatz.ailaai.ui.bot.BotProfileDialog
import com.queatz.ailaai.ui.theme.pad
import com.queatz.db.Bot
import com.queatz.db.Member
import com.queatz.db.Message
import com.queatz.db.Person

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    message: Message,
    previousMessage: Message?,
    initiallyShowTime: Boolean = false,
    selectedMessages: Set<Message>,
    onSelectedChange: (Message, Boolean) -> Unit,
    getPerson: (String) -> Person?,
    getBot: (String) -> Bot?,
    getMessage: suspend (String) -> Message?,
    member: Member?,
    onUpdated: () -> Unit,
    canReply: Boolean,
    onReply: (Message) -> Unit,
    onReplyInNewGroup: (Message) -> Unit,
    onShowPhoto: (String) -> Unit,
) {
    var showTime by remember(initiallyShowTime, message) {
        mutableStateOf(initiallyShowTime)
    }
    var showMessageDialog by rememberStateOf(false)
    var botDialog by rememberStateOf<String?>(null)
    val isMe = member?.id == message.member
    val nav = nav

    botDialog?.let { id ->
        BotProfileDialog({ botDialog = null }, id)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(1.pad)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    showTime = !showTime
                },
                onLongClick = {
                    showMessageDialog = true
                }
            )) {
        if (!isMe) {
            if (previousMessage?.member != message.member) {
                when {
                    message.member != null -> {
                        val person = message.member!!.let(getPerson)
                        ProfileImage(
                            person?.photo,
                            person?.name,
                            PaddingValues(1.pad, 1.pad, 0.dp, 1.pad),
                        ) {
                            if (message.member != null) {
                                nav.appNavigate(
                                    AppNav.Profile(
                                        message.member?.let(getPerson)?.id ?: return@ProfileImage
                                    )
                                )
                            }
                        }
                    }

                    message.bot != null -> {
                        val bot = message.bot!!.let(getBot)
                        ProfileImage(
                            bot?.photo,
                            bot?.name,
                            PaddingValues(1.pad, 1.pad, 0.dp, 1.pad),
                        ) {
                            botDialog = message.bot!!
                        }
                    }
                }
            } else {
                Box(Modifier.requiredSize(32.dp + 1.pad))
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            MessageContent(
                message = message,
                isMe = isMe,
                isHost = member?.host == true,
                me = member?.id,
                showTime = showTime,
                onShowTime = {
                    if (selectedMessages.isEmpty()) {
                        showTime = it
                    } else {
                        onSelectedChange(message, message !in selectedMessages)
                    }
                },
                showMessageDialog = showMessageDialog,
                onShowMessageDialog = { showMessageDialog = it },
                getPerson = getPerson,
                getBot = getBot,
                getMessage = getMessage,
                canReply = canReply,
                canReact = member != null,
                onReply = onReply,
                onReplyInNewGroup = onReplyInNewGroup,
                onUpdated = onUpdated,
                onShowPhoto = onShowPhoto,
                selected = message in selectedMessages,
                onSelectedChange = {
                    onSelectedChange(message, it)
                }
            )
        }
    }
}
