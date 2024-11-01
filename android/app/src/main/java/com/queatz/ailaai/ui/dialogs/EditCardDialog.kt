package com.queatz.ailaai.ui.dialogs

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import app.ailaai.api.newCard
import app.ailaai.api.updateCard
import com.queatz.ailaai.R
import com.queatz.ailaai.data.api
import com.queatz.ailaai.data.json
import com.queatz.ailaai.extensions.notBlank
import com.queatz.ailaai.extensions.rememberStateOf
import com.queatz.ailaai.extensions.toast
import com.queatz.ailaai.ui.components.CardOptions
import com.queatz.ailaai.ui.components.ConversationAction
import com.queatz.ailaai.ui.components.ConversationItem
import com.queatz.ailaai.ui.components.DialogBase
import com.queatz.ailaai.ui.theme.pad
import com.queatz.db.Card
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditCardDialog(
    card: Card,
    onDismissRequest: () -> Unit,
    create: Boolean = false,
    onChange: (Card) -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current!!

    val conversation = remember {
        card.conversation?.let {
            json.decodeFromString<ConversationItem>(it)
        } ?: ConversationItem()
    }

    val options = remember {
        card.options?.let {
            json.decodeFromString<CardOptions>(it)
        } ?: CardOptions()
    }

    var cardName by remember { mutableStateOf(card.name ?: "") }
    var locationName by remember { mutableStateOf(card.location ?: "") }
    val backstack = remember { mutableListOf<ConversationItem>() }
    var cardConversation by remember { mutableStateOf(conversation) }
    var enableWebReplies by rememberStateOf(options.enableReplies != false)
    var enableReplies by rememberStateOf(options.enableAnonymousReplies != false)
    val scope = rememberCoroutineScope()

    LaunchedEffect(enableReplies, enableWebReplies) {
        options.enableReplies = enableReplies
        options.enableAnonymousReplies = enableReplies && enableWebReplies
    }

    DialogBase(onDismissRequest, dismissable = false, modifier = Modifier.wrapContentHeight()) {
        val scrollState = rememberScrollState()
        val currentRecomposeScope = currentRecomposeScope
        fun invalidate() {
            currentRecomposeScope.invalidate()
        }

        val titleFocusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            try {
                titleFocusRequester.requestFocus()
            } catch (e: IllegalStateException) {
                // Ignore
            }
        }

        Column(
            modifier = Modifier
                .padding(3.pad)
                .verticalScroll(scrollState)
        ) {
            if (backstack.isEmpty()) {
                Text(
                    if (create) stringResource(R.string.create_page) else stringResource(R.string.edit),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 1.pad)
                )
                OutlinedTextField(
                    cardName,
                    onValueChange = {
                        cardName = it
                    },
                    label = {
                        Text(stringResource(R.string.title))
                    },
                    shape = MaterialTheme.shapes.large,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            keyboardController.hide()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                        .focusRequester(titleFocusRequester)
                )
                OutlinedTextField(
                    locationName,
                    onValueChange = {
                        locationName = it
                    },
                    label = {
                        Text(stringResource(R.string.hint))
                    },
                    shape = MaterialTheme.shapes.large,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onSearch = {
                        keyboardController.hide()
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(1.pad),
                modifier = Modifier
            ) {
                if (backstack.isNotEmpty()) {
                    TextButton(
                        {
                            backstack.removeLastOrNull()?.let {
                                cardConversation = it
                            }
                            invalidate()
                        }
                    ) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            stringResource(R.string.go_back),
                            modifier = Modifier.padding(end = 1.pad)
                        )
                        Text(
                            backstack.last().message.notBlank
                                ?: stringResource(R.string.go_back),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                }

                var messageState by mutableStateOf(cardConversation.message)

                OutlinedTextField(
                    messageState,
                    {
                        messageState = it
                        cardConversation.message = it
                    },
                    shape = MaterialTheme.shapes.large,
                    label = {
                        Text(stringResource(R.string.details))
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            keyboardController.hide()
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (backstack.isNotEmpty()) {
                    Text(
                        stringResource(R.string.card_reply_description, cardConversation.title),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(PaddingValues(bottom = 1.pad))
                    )
                }

                cardConversation.items.forEach {
                    var titleState by mutableStateOf(it.title)

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            {
                                it.action = when (it.action) {
                                    ConversationAction.Message -> null
                                    else -> ConversationAction.Message
                                }
                                if (it.action == ConversationAction.Message) {
                                    context.toast(R.string.changed_to_message_button)
                                }
                                invalidate()
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = if (it.action == ConversationAction.Message) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    LocalContentColor.current.copy(alpha = .5f)
                                }
                            )
                        ) {
                            Icon(
                                Icons.Outlined.Message,
                                null
                            )
                        }
                        OutlinedTextField(
                            titleState,
                            { value ->
                                titleState = value
                                it.title = value
                            },
                            placeholder = {
                                Text(stringResource(R.string.message))
                            },
                            shape = MaterialTheme.shapes.large,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onSearch = {
                                keyboardController.hide()
                            }),
                            modifier = Modifier
                                .weight(1f)
                                .onKeyEvent { keyEvent ->
                                    if (it.title.isEmpty() && keyEvent.key == Key.Backspace) {
                                        cardConversation.items.remove(it)
                                        invalidate()
                                        true
                                    } else false
                                }
                        )
                        AnimatedVisibility(titleState.isNotBlank() && it.action == null) {
                            IconButton(
                                {
                                    backstack.add(cardConversation)
                                    cardConversation = it
                                    invalidate()
                                },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    Icons.Outlined.ArrowForward,
                                    stringResource(R.string.continue_conversation)
                                )
                            }
                        }
                    }
                }
                if (cardConversation.items.size < 4) {
                    TextButton(
                        {
                            cardConversation.items.add(ConversationItem())
                            invalidate()
                        }
                    ) {
                        Icon(
                            Icons.Outlined.Add,
                            stringResource(R.string.add_an_option),
                            modifier = Modifier.padding(end = 1.pad)
                        )
                        Text(stringResource(R.string.add_an_option))
                    }
                }
                if (backstack.isEmpty()) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(1.pad),
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.large)
                                .clickable {
                                    enableReplies = !enableReplies
                                }
                                .padding(end = 2.pad)) {
                            Checkbox(enableReplies, {
                                enableReplies = it
                            })
                            Text(
                                stringResource(R.string.enable_replies),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        AnimatedVisibility(enableReplies) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(1.pad),
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.large)
                                    .clickable {
                                        enableWebReplies = !enableWebReplies
                                    }
                                    .padding(end = 2.pad)) {
                                Checkbox(enableWebReplies, {
                                    enableWebReplies = it
                                })
                                Text(
                                    stringResource(R.string.enable_anonymous_replies),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.pad, Alignment.End),
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                var disableSaveButton by rememberStateOf(false)

                TextButton(
                    {
                        onDismissRequest()
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
                TextButton(
                    {
                        disableSaveButton = true

                        fun trim(it: ConversationItem) {
                            it.title = it.title.trim()
                            it.message = it.message.trim()
                            it.items = it.items.onEach { trim(it) }
                        }

                        trim(conversation)

                        scope.launch {
                            var cardToUpdate: Card? = null
                            if (create) {
                                api.newCard(card) {
                                    cardToUpdate = it
                                }
                            } else {
                                cardToUpdate = card
                            }

                            if (cardToUpdate == null) {
                                return@launch
                            }

                            api.updateCard(
                                cardToUpdate!!.id!!,
                                Card(
                                    name = cardName.trim(),
                                    location = locationName.trim(),
                                    conversation = json.encodeToString(conversation),
                                    options = json.encodeToString(options)
                                )
                            ) { update ->
                                card.name = update.name
                                card.location = update.location
                                card.conversation = update.conversation
                                card.options = update.options
                                onDismissRequest()
                                onChange(update)
                            }
                            disableSaveButton = false
                        }
                    },
                    enabled = !disableSaveButton
                ) {
                    Text(
                        stringResource(
                            if (create) R.string.create else R.string.update
                        )
                    )
                }
            }
        }
    }
}
