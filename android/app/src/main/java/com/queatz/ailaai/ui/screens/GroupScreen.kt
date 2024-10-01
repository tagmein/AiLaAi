package com.queatz.ailaai.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NotificationsPaused
import androidx.compose.material.icons.outlined.Reply
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.ailaai.api.createGroup
import app.ailaai.api.createMember
import app.ailaai.api.group
import app.ailaai.api.message
import app.ailaai.api.messages
import app.ailaai.api.messagesBefore
import app.ailaai.api.newReminder
import app.ailaai.api.removeMember
import app.ailaai.api.sendMessage
import app.ailaai.api.updateGroup
import app.ailaai.api.updateMember
import at.bluesource.choicesdk.maps.common.LatLng
import com.queatz.ailaai.AppNav
import com.queatz.ailaai.R
import com.queatz.ailaai.api.sendAudioFromUri
import com.queatz.ailaai.api.sendMediaFromUri
import com.queatz.ailaai.api.sendVideosFromUri
import com.queatz.ailaai.api.uploadPhotosFromUris
import com.queatz.ailaai.background
import com.queatz.ailaai.data.api
import com.queatz.ailaai.data.getAttachment
import com.queatz.ailaai.data.json
import com.queatz.ailaai.extensions.appNavigate
import com.queatz.ailaai.extensions.attachmentText
import com.queatz.ailaai.extensions.copyToClipboard
import com.queatz.ailaai.extensions.fadingEdge
import com.queatz.ailaai.extensions.formatFuture
import com.queatz.ailaai.extensions.formatTime
import com.queatz.ailaai.extensions.groupUrl
import com.queatz.ailaai.extensions.inDp
import com.queatz.ailaai.extensions.inList
import com.queatz.ailaai.extensions.name
import com.queatz.ailaai.extensions.notBlank
import com.queatz.ailaai.extensions.nullIfBlank
import com.queatz.ailaai.extensions.profileUrl
import com.queatz.ailaai.extensions.rememberStateOf
import com.queatz.ailaai.extensions.scrollToTop
import com.queatz.ailaai.extensions.shareAsUrl
import com.queatz.ailaai.extensions.showDidntWork
import com.queatz.ailaai.extensions.startOfMinute
import com.queatz.ailaai.extensions.timeAgo
import com.queatz.ailaai.extensions.toLatLng
import com.queatz.ailaai.extensions.toList
import com.queatz.ailaai.extensions.toast
import com.queatz.ailaai.group.GroupCards
import com.queatz.ailaai.group.GroupJoinRequest
import com.queatz.ailaai.group.SendGroupDialog
import com.queatz.ailaai.helpers.StartEffect
import com.queatz.ailaai.helpers.audioRecorder
import com.queatz.ailaai.me
import com.queatz.ailaai.nav
import com.queatz.ailaai.schedule.ScheduleReminderDialog
import com.queatz.ailaai.services.calls
import com.queatz.ailaai.services.joins
import com.queatz.ailaai.services.push
import com.queatz.ailaai.services.say
import com.queatz.ailaai.services.stickers
import com.queatz.ailaai.services.ui
import com.queatz.ailaai.trade.TradeDialog
import com.queatz.ailaai.ui.components.AppBar
import com.queatz.ailaai.ui.components.BackButton
import com.queatz.ailaai.ui.components.Dropdown
import com.queatz.ailaai.ui.components.IconAndCount
import com.queatz.ailaai.ui.components.LinkifyText
import com.queatz.ailaai.ui.components.Loading
import com.queatz.ailaai.ui.components.MessageItem
import com.queatz.ailaai.ui.dialogs.ChooseCategoryDialog
import com.queatz.ailaai.ui.dialogs.ChoosePeopleDialog
import com.queatz.ailaai.ui.dialogs.ChoosePhotoDialog
import com.queatz.ailaai.ui.dialogs.ChoosePhotoDialogState
import com.queatz.ailaai.ui.dialogs.EditCardDialog
import com.queatz.ailaai.ui.dialogs.GroupDescriptionDialog
import com.queatz.ailaai.ui.dialogs.GroupSettingsDialog
import com.queatz.ailaai.ui.dialogs.Media
import com.queatz.ailaai.ui.dialogs.Menu
import com.queatz.ailaai.ui.dialogs.PeopleDialog
import com.queatz.ailaai.ui.dialogs.PhotoDialog
import com.queatz.ailaai.ui.dialogs.QrCodeDialog
import com.queatz.ailaai.ui.dialogs.RationaleDialog
import com.queatz.ailaai.ui.dialogs.RenameGroupDialog
import com.queatz.ailaai.ui.dialogs.ReportDialog
import com.queatz.ailaai.ui.dialogs.SetLocationDialog
import com.queatz.ailaai.ui.dialogs.TextFieldDialog
import com.queatz.ailaai.ui.dialogs.defaultConfirmFormatter
import com.queatz.ailaai.ui.dialogs.menuItem
import com.queatz.ailaai.ui.stickers.StickerPacks
import com.queatz.ailaai.ui.theme.pad
import com.queatz.ailaai.ui.theme.theme_call
import com.queatz.db.Card
import com.queatz.db.Group
import com.queatz.db.GroupAttachment
import com.queatz.db.GroupEditsConfig
import com.queatz.db.GroupExtended
import com.queatz.db.GroupMessagesConfig
import com.queatz.db.JoinRequestAndPerson
import com.queatz.db.Member
import com.queatz.db.Message
import com.queatz.db.Person
import com.queatz.db.PhotosAttachment
import com.queatz.db.Reminder
import com.queatz.db.ReplyAttachment
import com.queatz.db.Sticker
import com.queatz.db.StickerAttachment
import com.queatz.db.Trade
import com.queatz.db.TradeAttachment
import createTrade
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.Instant
import kotlinx.datetime.Instant.Companion.fromEpochMilliseconds
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.serialization.encodeToString
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours


@Composable
fun GroupScreen(groupId: String) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var sendMessage by remember { mutableStateOf("") }
    var groupExtended by remember { mutableStateOf<GroupExtended?>(null) }
    var messages by remember { mutableStateOf<List<Message>>(listOf()) }
    var isLoading by rememberStateOf(false)
    var showGroupNotFound by rememberStateOf(false)
    var showLeaveGroup by rememberStateOf(false)
    var showQrCodeDialog by rememberStateOf(false)
    var showManageDialog by rememberStateOf(false)
    var showChangeGroupStatus by rememberStateOf(false)
    var showSettingsDialog by rememberStateOf(false)
    var showReportDialog by rememberStateOf(false)
    var showDescriptionDialog by rememberStateOf(false)
    var showSetPhotoDialog by rememberStateOf(false)
    var showSetBackgroundDialog by rememberStateOf(false)
    var showCategoryDialog by rememberStateOf(false)
    var showLocationDialog by rememberStateOf(false)
    var showRenameGroup by rememberStateOf(false)
    var showGroupMembers by rememberStateOf(false)
    var showManageGroupMembersMenu by rememberStateOf(false)
    var showRemoveGroupMembers by rememberStateOf(false)
    var showPromoteGroupMembers by rememberStateOf(false)
    var showInviteMembers by rememberStateOf(false)
    var showPhotoDialog by rememberStateOf(false)
    var isGeneratingPhoto by rememberStateOf(false)
    var isGeneratingGroupBackground by rememberStateOf(false)
    var isGeneratingGroupPhoto by rememberStateOf(false)
    var showJoinDialog by rememberStateOf(false)
    var showAudioRationale by rememberStateOf(false)
    var showSnoozeDialog by rememberStateOf(false)
    var showPhoto by remember { mutableStateOf<String?>(null) }
    var stageReply by remember { mutableStateOf<Message?>(null) }
    var showReplyInNewGroupDialog by remember { mutableStateOf<Message?>(null) }
    var showDescription by remember { mutableStateOf(ui.getShowDescription(groupId)) }
    val focusRequester = remember { FocusRequester() }
    var hasOlderMessages by rememberStateOf(true)
    var isRecordingAudio by rememberStateOf(false)
    var showMore by rememberStateOf(false)
    var recordingAudioDuration by rememberStateOf(0L)
    var maxInputAreaHeight by rememberStateOf(0f)
    val stickerPacks by stickers.rememberStickerPacks()
    var selectedMessages by rememberStateOf(emptySet<Message>())
    var showCards by rememberStateOf(false)
    var showTradeWithDialog by rememberStateOf(false)
    var showNewReminderWithDialog by rememberStateOf(false)
    var showScheduleNewReminderDialog by rememberStateOf<List<Person>?>(null)
    var showTradeDialog by rememberStateOf<Trade?>(null)
    var showSendDialog by rememberStateOf(false)
    var searchMessages by rememberStateOf<String?>(null)
    val inCallCount by calls.inCallCount(groupId).collectAsState(0)
    val nav = nav
    val me = me

    val generatePhotoState = remember {
        ChoosePhotoDialogState(mutableStateOf(""))
    }
    val setPhotoDialogState = remember(groupExtended == null) {
        ChoosePhotoDialogState(mutableStateOf(groupExtended?.group?.name ?: ""))
    }
    val setBackgroundDialogState = remember(groupExtended == null) {
        ChoosePhotoDialogState(mutableStateOf(groupExtended?.group?.name ?: ""))
    }

    val allJoinRequests by joins.joins.collectAsState()
    val myJoinRequests by joins.myJoins.collectAsState()
    var joinRequests by remember {
        mutableStateOf(emptyList<JoinRequestAndPerson>())
    }

    background(groupExtended?.group?.background?.let(api::url))

    LaunchedEffect(allJoinRequests) {
        joinRequests = allJoinRequests.filter { it.joinRequest?.group == groupId }
    }

    LaunchedEffect(Unit) {
        push.clear(groupId)
    }

    LaunchedEffect(Unit) {
        if (stickerPacks.isNullOrEmpty()) {
            stickers.reload()
        }
    }

    suspend fun reloadMessages() {
        api.messages(group = groupId, search = searchMessages) {
            hasOlderMessages = true
            messages = it
        }
    }

    fun trade(people: List<String>) {
        scope.launch {
            api.createTrade(
                Trade().apply {
                    this.people = people + me!!.id!!
                }
            ) {
                showTradeDialog = it
                api.sendMessage(
                    groupId,
                    Message(attachment = json.encodeToString(TradeAttachment(it.id!!)))
                )
            }
        }
    }

    fun newReminder(people: List<String>, reminder: Reminder) {
        scope.launch {
            api.newReminder(
                Reminder(
                    people = people,
                    title = reminder.title?.trim(),
                    start = reminder.start ?: now().startOfMinute(),
                    end = reminder.end,
                    schedule = reminder.schedule,
                    timezone = TimeZone.currentSystemDefault().id,
                    utcOffset = TimeZone.currentSystemDefault().offsetAt(now()).totalSeconds / (60.0 * 60.0),
                )
            ) {
                context.toast(R.string.reminder_created)
            }
        }
    }

    val audioRecorder = audioRecorder(
        onIsRecordingAudio = { isRecordingAudio = it },
        onRecordingAudioDuration = { recordingAudioDuration = it },
        onPermissionDenied = {
            showAudioRationale = true
        }
    ) { file ->
        api.sendAudioFromUri(groupId, file, stageReply?.id?.let {
            Message(attachments = listOf(json.encodeToString(ReplyAttachment(it))))
        }) {
            stageReply = null
            reloadMessages()
        }
    }

    suspend fun loadMore() {
        if (!hasOlderMessages || messages.isEmpty()) {
            return
        }

        val oldest = messages.lastOrNull()?.createdAt ?: return
        api.messagesBefore(group = groupId, before = oldest, search = searchMessages) { older ->
            val newMessages = (messages + older).distinctBy { it.id }

            if (messages.size == newMessages.size) {
                hasOlderMessages = false
            } else {
                messages = newMessages
            }
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        api.group(
            groupId,
            onError = { ex ->
                if (ex is CancellationException || ex is InterruptedException) {
                    // Ignore
                } else {
                    showGroupNotFound = true
                }
            }) {
            groupExtended = it
        }
        isLoading = false
    }

    LaunchedEffect(searchMessages) {
        reloadMessages()
    }

    LaunchedEffect(stageReply) {
        searchMessages = null
    }

    suspend fun reload() {
        api.group(groupId) {
            groupExtended = it
        }
    }

    val someone = stringResource(R.string.someone)
    val emptyGroup = stringResource(R.string.empty_group_name)

    suspend fun replyInNewGroup(title: String, message: Message, people: List<Person>) {
        val groupName = groupExtended?.name(
            someone = someone,
            emptyGroup = emptyGroup,
            omit = me?.id?.inList().orEmpty()
        ).orEmpty()

        api.createGroup(people.map { it.id!! }) {
            api.updateGroup(it.id!!, Group(name = "⮡ $title ($groupName)")) { newGroup ->
                api.sendMessage(
                    newGroup.id!!, message = Message(
                        attachment = json.encodeToString(ReplyAttachment(message = message.id!!)),
                        attachments = json.encodeToString(GroupAttachment(group = groupExtended!!.group!!.id!!)).inList()
                    )
                )
                nav.appNavigate(AppNav.Group(newGroup.id!!))
            }
        }
    }

    var newCard by rememberStateOf<Card?>(null)

    if (newCard != null) {
        EditCardDialog(
            newCard!!,
            {
                newCard = null
            },
            create = true
        ) {
            scope.launch {
                reload()
                nav.appNavigate(AppNav.Page(it.id!!))
            }
        }
    }

    StartEffect {
        reloadMessages()
    }

    LaunchedEffect(Unit) {
        push.latestMessage
            .filter { it != null }
            .conflate()
            .catch { it.printStackTrace() }
            .onEach {
                reloadMessages()
            }
            .launchIn(scope)
    }

    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxSize()
    ) {
        if (groupExtended == null) {
            if (isLoading) {
                Loading()
            }
        } else {
            val allMembers = groupExtended!!.members
                ?.sortedByDescending { it.person?.seen ?: fromEpochMilliseconds(0) }
                ?: emptyList()
            val myMember = groupExtended!!.members?.find { it.person?.id == me?.id }
            val otherMembers = groupExtended!!.members?.filter { it.person?.id != me?.id } ?: emptyList()
            val state = rememberLazyListState()

            var latestMessage by remember { mutableStateOf<Instant?>(null) }

            val someone = stringResource(R.string.someone)
            val emptyGroup = stringResource(R.string.empty_group_name)

            LaunchedEffect(messages) {
                val latest = messages.firstOrNull()?.createdAt
                if (latestMessage == null || (latest != null && latestMessage!! < latest)) {
                    state.animateScrollToItem(0)
                }
                latestMessage = latest
            }

            LaunchedEffect(sendMessage) {
                if (showMore && sendMessage.isNotBlank()) {
                    showMore = false
                }
            }

            fun snooze(snoozed: Boolean) {
                scope.launch {
                    api.updateMember(myMember!!.member!!.id!!, Member(snoozed = snoozed)) {
                        context.toast(if (snoozed) R.string.group_snoozed else R.string.group_unsnoozed)
                        reload()
                    }
                }
            }

            fun snooze(snoozedUntil: Instant) {
                scope.launch {
                    api.updateMember(myMember!!.member!!.id!!, Member(snoozedUntil = snoozedUntil)) {
                        context.toast(R.string.group_snoozed)
                        reload()
                    }
                }
            }

            AppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (state.firstVisibleItemIndex > 2) {
                                    scope.launch {
                                        state.scrollToTop()
                                    }
                                } else {
                                    if (otherMembers.size == 1) {
                                        nav.appNavigate(AppNav.Profile(otherMembers.first().person!!.id!!))
                                    } else {
                                        showGroupMembers = true
                                    }
                                }
                            }
                    ) {
                        val someone = stringResource(R.string.someone)
                        val emptyGroup = stringResource(R.string.empty_group_name)
                        Text(
                            groupExtended!!.name(
                                someone,
                                emptyGroup,
                                me?.id?.let(::listOf) ?: emptyList()
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val details = listOfNotNull(
                            if (groupExtended?.group?.open == true) stringResource(R.string.open_group) else null,
                            groupExtended?.seenText(stringResource(R.string.active), me)
                        )

                        if (details.isNotEmpty()) {
                            Text(
                                details.joinToString(" • "),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    BackButton()
                },
                actions = {
                    var showMenu by rememberStateOf(false)

                    val isSnoozed =
                        myMember?.member?.snoozed == true || myMember?.member?.snoozedUntil?.takeIf { it > now() } != null

                    if (!showDescription && groupExtended?.group?.description?.isBlank() == false && !showCards) {
                        IconButton({
                            showDescription = !showDescription
                            ui.setShowDescription(groupId, showDescription)
                        }) {
                            Icon(Icons.Outlined.Info, stringResource(R.string.introduction))
                        }
                    }

                    if (showCards && myMember != null) {
                        IconButton({
                            newCard = Card(group = groupId)
                        }) {
                            Icon(Icons.Outlined.Add, stringResource(R.string.create_page))
                        }
                    }

                    fun startCall() {
                        calls.start(
                            groupId = groupId,
                            groupName = groupExtended!!.name(
                                someone,
                                emptyGroup,
                                me?.id?.let(::listOf) ?: emptyList()
                            )
                        )
                    }

                    if (!showCards) {
                        if (myMember != null) {
                            if (inCallCount > 0) {
                                Button(
                                    onClick = {
                                        startCall()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = theme_call
                                    )
                                ) {
                                    Icon(
                                        Icons.Outlined.Call,
                                        stringResource(R.string.call)
                                    )
                                    Text(
                                        stringResource(R.string.x_in_call, inCallCount),
                                        modifier = Modifier
                                            .padding(start = 1.pad)
                                    )
                                }
                            } else {
                                IconButton(
                                    {
                                        startCall()
                                    }
                                ) {
                                    Icon(
                                        Icons.Outlined.Call,
                                        stringResource(R.string.call)
                                    )
                                }
                            }

                            if (isSnoozed) {
                                IconButton({
                                    snooze(false)
                                }) {
                                    Icon(
                                        Icons.Outlined.NotificationsPaused,
                                        stringResource(R.string.unsnooze),
                                        tint = MaterialTheme.colorScheme.tertiary.copy(alpha = .5f)
                                    )
                                }
                            }
                        }
                    }

                    if ((groupExtended?.cardCount ?: 0) > 0 || showCards) {
                        if (showCards) {
                            IconButton({
                                showCards = !showCards
                            }) {
                                Icon(Icons.Outlined.Forum, stringResource(R.string.go_back))
                            }
                        } else {
                            val count = groupExtended?.cardCount ?: 0
                            if (count > 0) {
                                IconAndCount(
                                    {
                                        Icon(Icons.Outlined.Map, stringResource(R.string.cards))
                                    },
                                    count
                                ) {
                                    showCards = !showCards
                                }
                            }
                        }
                    }

                    IconButton({
                        showMenu = !showMenu
                    }) {
                        if (isGeneratingGroupBackground || isGeneratingGroupPhoto) {
                            CircularProgressIndicator(
                                strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth / 2,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(Icons.Outlined.MoreVert, stringResource(R.string.more))
                        }
                    }

                    Dropdown(showMenu, { showMenu = false }) {
                        DropdownMenuItem({
                            Text(stringResource(R.string.members))
                        }, {
                            showMenu = false
                            showGroupMembers = true
                        })
                        if (myMember != null) {
                            if (groupExtended?.members?.any { it.person?.id != me?.id } == true) {
                                DropdownMenuItem({
                                    Text(stringResource(R.string.create_reminder))
                                }, {
                                    showMenu = false
                                    showNewReminderWithDialog = true
                                })
                            }
                            if (groupExtended?.members?.any { it.person?.id != me?.id } == true) {
                                DropdownMenuItem({
                                    Text(stringResource(R.string.trade))
                                }, {
                                    showMenu = false
                                    showTradeWithDialog = true
                                })
                            }
                        }
                        DropdownMenuItem({
                            Text(stringResource(R.string.cards))
                        }, {
                            showMenu = false
                            showCards = !showCards
                        })
                        DropdownMenuItem({
                            Text(stringResource(R.string.send))
                        }, {
                            showMenu = false
                            showSendDialog = true
                        })
                        if (myMember != null) {
                            if (myMember.member?.host == true || groupExtended?.group?.config?.edits != GroupEditsConfig.Hosts) {
                                DropdownMenuItem({
                                    Text(stringResource(R.string.manage))
                                }, {
                                    showMenu = false
                                    showManageDialog = true
                                })
                            }
                        }
                        if (groupExtended?.group?.open == true) {
                            DropdownMenuItem({
                                Text(stringResource(R.string.share))
                            }, {
                                showMenu = false
                                groupUrl(groupId).shareAsUrl(
                                    context = context,
                                    name = groupExtended?.name(
                                        context.getString(R.string.someone),
                                        context.getString(R.string.empty_group_name),
                                        me?.id?.inList().orEmpty()
                                    )
                                )
                            })
                        }
                        DropdownMenuItem({
                            Text(stringResource(R.string.qr_code))
                        }, {
                            showMenu = false
                            showQrCodeDialog = true
                        })
                        if (myMember != null) {
                            val hidden = myMember.member?.hide == true
                            DropdownMenuItem({
                                Text(
                                    if (hidden) stringResource(R.string.show) else stringResource(
                                        R.string.hide
                                    )
                                )
                            }, {
                                val hide = !hidden
                                scope.launch {
                                    api.updateMember(myMember.member!!.id!!, Member(hide = hide)) {
                                        if (hide) {
                                            context.toast(R.string.group_hidden)
                                            nav.popBackStack()
                                        }
                                    }
                                }
                                showMenu = false
                            })
                            if (isSnoozed) {
                                DropdownMenuItem({
                                    Column {
                                        Text(stringResource(R.string.unsnooze))
                                        Text(
                                            if (myMember.member?.snoozed == true) {
                                                stringResource(R.string.indefinitely)
                                            } else {
                                                stringResource(
                                                    R.string.until_x,
                                                    myMember.member?.snoozedUntil?.formatFuture() ?: ""
                                                )
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }, {
                                    showMenu = false
                                    snooze(false)
                                })
                            } else {
                                DropdownMenuItem({ Text(stringResource(R.string.snooze)) }, {
                                    showMenu = false
                                    showSnoozeDialog = true
                                })
                            }
                            DropdownMenuItem({
                                Text(stringResource(R.string.search))
                            }, {
                                showMenu = false
                                searchMessages = ""
                            })
                            DropdownMenuItem({
                                Text(stringResource(R.string.leave))
                            }, {
                                showMenu = false
                                showLeaveGroup = true
                            })
                            DropdownMenuItem({ Text(stringResource(R.string.report)) }, {
                                showMenu = false
                                showReportDialog = true
                            })
                        }
                    }
                },
                modifier = Modifier.zIndex(1f)
            )

            if (showCards && groupExtended != null) {
                GroupCards(groupExtended!!)
            } else {
                // todo: extract else block new component
                if (joinRequests.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 1.pad)
                            .shadow(1.dp, MaterialTheme.shapes.large)
                            .clip(MaterialTheme.shapes.large)
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .heightIn(max = 160.dp),
                        ) {
                            items(joinRequests) {
                                GroupJoinRequest(it) {
                                    scope.launch {
                                        reload()
                                    }
                                }
                            }
                        }
                    }
                } else {
                    AnimatedVisibility(showDescription && groupExtended?.group?.description?.isBlank() == false) {
                        OutlinedCard(
                            onClick = {
                                showDescription = false
                                ui.setShowDescription(groupId, showDescription)
                            },
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                    1.dp
                                )
                            ),
                            modifier = Modifier
                                .padding(1.pad)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(1.pad, Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                var viewport by remember { mutableStateOf(Size(0f, 0f)) }
                                val textScrollState = rememberScrollState()
                                LinkifyText(
                                    groupExtended?.group?.description ?: "",
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(max = 128.dp)
                                        .verticalScroll(textScrollState)
                                        .onPlaced { viewport = it.boundsInParent().size }
                                        .fadingEdge(viewport, textScrollState)
                                        .padding(1.5f.pad)
                                )
                                Icon(
                                    Icons.Outlined.Close,
                                    null,
                                    modifier = Modifier
                                        .padding(end = 1.5f.pad)
                                )
                            }
                        }
                    }
                }
                LazyColumn(reverseLayout = true, state = state, modifier = Modifier.weight(1f)) {
                    itemsIndexed(messages, key = { _, it -> it.id!! }) { index, it ->
                        MessageItem(
                            message = it,
                            previousMessage = index.takeIf { it < messages.lastIndex }?.let { it + 1 }?.let { messages[it] },
                            selectedMessages = selectedMessages,
                            onSelectedChange = { message, selected ->
                                if (searchMessages != null) {
                                    searchMessages = null
                                }

                                selectedMessages = if (selected) {
                                    selectedMessages + message
                                } else {
                                    selectedMessages - message
                                }
                            },
                            getPerson = {
                                groupExtended?.members?.find { member -> member.member?.id == it }?.person
                            },
                            getBot = {
                                groupExtended?.bots?.find { bot -> bot.id == it }
                            },
                            getMessage = { messageId ->
                                var message: Message? = messages.find { it.id == messageId }
                                api.message(messageId) {
                                    message = it
                                }
                                message
                            },
                            member = myMember?.member,
                            onUpdated = {
                                scope.launch {
                                    reloadMessages()
                                }
                            },
                            onReply = { stageReply = it },
                            onReplyInNewGroup = {
                                showReplyInNewGroupDialog = it
                            },
                            onShowPhoto = { showPhoto = it }
                        )
                    }
                    item {
                        AnimatedVisibility(hasOlderMessages && messages.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.pad)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                )
                                LaunchedEffect(Unit) {
                                    loadMore()
                                }
                            }
                        }
                    }
                }

                fun send() {
                    if (sendMessage.isNotBlank()) {
                        val text = sendMessage.trim()
                        val stagedReply = stageReply
                        scope.launch {
                            api.sendMessage(groupId, Message(text = text).also {
                                it.attachments =
                                    stagedReply?.id?.let { listOf(json.encodeToString(ReplyAttachment(it))) }
                            }, onError = {
                                if (sendMessage.isBlank()) {
                                    sendMessage = text
                                }
                                if (stageReply == null) {
                                    stageReply = stagedReply
                                }
                                context.showDidntWork()
                            }) {
                                reloadMessages()
                            }
                        }
                    }

                    stageReply = null
                    sendMessage = ""
                    focusRequester.requestFocus()
                }

                fun sendSticker(sticker: Sticker) {
                    val stagedReply = stageReply
                    scope.launch {
                        api.sendMessage(
                            groupId,
                            Message(
                                attachment = json.encodeToString(
                                    StickerAttachment(
                                        photo = sticker.photo,
                                        sticker = sticker.id,
                                        message = sticker.message
                                    )
                                )
                            ).also {
                                it.attachments =
                                    stagedReply?.id?.let { listOf(json.encodeToString(ReplyAttachment(it))) }
                            }
                        ) {
                            reloadMessages()
                        }

                        stageReply = null
                    }
                }

                if (myMember == null && groupExtended?.group?.open == true) {
                    val joinRequestId = myJoinRequests.find { it.joinRequest?.group == groupId }?.joinRequest?.id

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(1.pad)
                    ) {
                        if (joinRequestId == null) {
                            Button(
                                onClick = {
                                    showJoinDialog = true
                                }
                            ) {
                                Text(stringResource(R.string.join_group))
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        joins.delete(joinRequestId)
                                    }
                                }
                            ) {
                                Text(stringResource(R.string.cancel_join_request))
                            }
                        }
                    }
                } else if (groupExtended?.group?.config?.messages == GroupMessagesConfig.Hosts && myMember?.member?.host != true) {

                } else {
                    AnimatedVisibility(stageReply != null) {
                        var stagedReply by remember { mutableStateOf(stageReply) }
                        stagedReply = stageReply ?: stagedReply
                        stagedReply?.let { message ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(
                                        top = 1.pad,
                                        start = 1.pad,
                                        end = 1.pad
                                    )
                                    .clip(MaterialTheme.shapes.large)
                                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                            ) {
                                Icon(
                                    Icons.Outlined.Reply,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .5f),
                                    modifier = Modifier
                                        .padding(start = 2.pad, end = 1.pad)
                                        .requiredSize(16.dp)
                                )
                                Text(
                                    message.text?.notBlank ?: message.attachmentText(context) ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(.5f.pad)
                                        .heightIn(max = 64.dp)
                                )
                                IconButton(
                                    onClick = {
                                        stageReply = null
                                    }
                                ) {
                                    Icon(
                                        Icons.Outlined.Close,
                                        null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    val showTools = isRecordingAudio || selectedMessages.isNotEmpty() || searchMessages != null

                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onPlaced {
                                maxInputAreaHeight = maxInputAreaHeight.coerceAtLeast(it.boundsInParent().size.height)
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Crossfade(showTools, label = "") { show ->
                                when (show) {
                                    true -> {
                                        when {
                                            searchMessages != null -> {
                                                val searchFocusRequester = remember { FocusRequester() }

                                                LaunchedEffect(Unit) {
                                                    searchFocusRequester.requestFocus()
                                                }

                                                OutlinedTextField(
                                                    value = searchMessages.orEmpty(),
                                                    onValueChange = {
                                                        searchMessages = it
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            Icons.Outlined.Search,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    trailingIcon = {
                                                        IconButton({ searchMessages = null }) {
                                                            Icon(
                                                                Icons.Default.Clear,
                                                                Icons.Default.Clear.name,
                                                                tint = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                    },
                                                    placeholder = {
                                                        Text(
                                                            stringResource(R.string.search),
                                                            modifier = Modifier.alpha(.5f)
                                                        )
                                                    },
                                                    keyboardOptions = KeyboardOptions(
                                                        capitalization = KeyboardCapitalization.Sentences,
                                                        imeAction = ImeAction.Default
                                                    ),
                                                    shape = MaterialTheme.shapes.large,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(max = 128.dp)
                                                        .padding(1.pad)
                                                        .focusRequester(searchFocusRequester)
                                                        .onKeyEvent { keyEvent ->
                                                            if (keyEvent.key == Key.Backspace && searchMessages.isNullOrEmpty()) {
                                                                searchMessages = null
                                                                true
                                                            } else {
                                                                false
                                                            }
                                                        }
                                                )
                                            }

                                            selectedMessages.isNotEmpty() -> {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .heightIn(min = maxInputAreaHeight.inDp())
                                                ) {
                                                    Text(
                                                        pluralStringResource(
                                                            R.plurals.x_selected,
                                                            selectedMessages.size,
                                                            selectedMessages.size
                                                        ),
                                                        overflow = TextOverflow.Ellipsis,
                                                        textAlign = TextAlign.Center,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier
                                                            .padding(1.pad)
                                                            .fillMaxWidth()
                                                    )
                                                }
                                            }

                                            isRecordingAudio -> {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .heightIn(min = maxInputAreaHeight.inDp())
                                                ) {
                                                    IconButton(
                                                        {
                                                            audioRecorder.cancelRecording()
                                                        }
                                                    ) {
                                                        Icon(
                                                            Icons.Outlined.Delete,
                                                            stringResource(R.string.discard_recording),
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                    Text(
                                                        stringResource(
                                                            R.string.recording_audio,
                                                            recordingAudioDuration.formatTime()
                                                        ),
                                                        overflow = TextOverflow.Ellipsis,
                                                        textAlign = TextAlign.Center,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier
                                                            .padding(1.pad)
                                                            .fillMaxWidth()
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    else -> {
                                        OutlinedTextField(
                                            value = sendMessage,
                                            onValueChange = {
                                                sendMessage = it
                                            },
                                            trailingIcon = {
                                                Crossfade(targetState = sendMessage.isNotBlank()) { show ->
                                                    when (show) {
                                                        true -> IconButton({ send() }) {
                                                            Icon(
                                                                Icons.Default.Send,
                                                                Icons.Default.Send.name,
                                                                tint = MaterialTheme.colorScheme.primary
                                                            )
                                                        }

                                                        false -> {}
                                                    }
                                                }
                                            },
                                            placeholder = {
                                                Text(
                                                    stringResource(R.string.message),
                                                    modifier = Modifier.alpha(.5f)
                                                )
                                            },
                                            keyboardOptions = KeyboardOptions(
                                                capitalization = KeyboardCapitalization.Sentences,
                                                imeAction = ImeAction.Default
                                            ),
                                            keyboardActions = KeyboardActions(
                                                onSend = {
                                                    send()
                                                }
                                            ),
                                            shape = MaterialTheme.shapes.large,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 128.dp)
                                                .padding(1.pad)
                                                .focusRequester(focusRequester)
                                        )
                                    }
                                }
                            }
                        }
                        AnimatedVisibility(sendMessage.isBlank() && searchMessages == null) {
                            Row {
                                if (selectedMessages.isNotEmpty()) {
                                    IconButton(
                                        {
                                            selectedMessages.sortedBy {
                                                it.createdAt
                                            }.joinToString("\n") { it.text ?: "" }
                                                .copyToClipboard(context)
                                            context.toast(R.string.copied)
                                            selectedMessages = emptySet()
                                        }
                                    ) {
                                        Icon(
                                            Icons.Outlined.ContentCopy,
                                            null
                                        )
                                    }
                                    IconButton(
                                        {
                                            selectedMessages = emptySet()
                                        }
                                    ) {
                                        Icon(
                                            Icons.Outlined.Close,
                                            null
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            if (isRecordingAudio) {
                                                audioRecorder.sendActiveRecording()
                                            } else {
                                                audioRecorder.recordAudio()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            if (isRecordingAudio) Icons.Default.Send else Icons.Outlined.Mic,
                                            stringResource(R.string.record_audio),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                AnimatedVisibility(!showTools) {
                                    Row {
                                        IconButton(
                                            onClick = {
                                                showPhotoDialog = true
                                            }
                                        ) {
                                            if (isGeneratingPhoto) {
                                                CircularProgressIndicator(
                                                    strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth / 2,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Outlined.CameraAlt,
                                                    stringResource(R.string.photo),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                showMore = !showMore
                                            },
                                            modifier = Modifier
                                                .padding(end = 1.pad)
                                        ) {
                                            Icon(
                                                if (showMore) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                                null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    AnimatedVisibility(showMore) {
                        Box(
                            modifier = Modifier.height(240.dp)
                                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                                .shadow(0.25.dp)
                        ) {
                            StickerPacks(
                                stickerPacks ?: emptyList(),
                                onStickerLongClick = {
                                    scope.launch {
                                        say.say(it.message)
                                    }
                                },
                                onStickerPack = {
                                    nav.appNavigate(AppNav.StickerPack(it.id!!))
                                },
                                modifier = Modifier.fillMaxSize()
                            ) { sticker ->
                                showMore = false
                                focusRequester.requestFocus()
                                sendSticker(sticker)
                            }
                            FloatingActionButton(
                                onClick = {
                                    nav.appNavigate(AppNav.StickerPacks)
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 2.pad, end = 2.pad)
                                    .size(32.dp + 1.pad)
                            ) {
                                Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            if (showPhoto != null) {
                PhotoDialog(
                    onDismissRequest = {
                        showPhoto = null
                    },
                    initialMedia = Media.Photo(showPhoto!!),
                    medias = if (showPhoto !in messages.photos()) {
                        listOf(Media.Photo(showPhoto!!))
                    } else {
                        messages.photos().map { Media.Photo(it) }
                    }
                )
            }

            if (showPhotoDialog) {
                ChoosePhotoDialog(
                    scope = scope,
                    state = generatePhotoState,
                    onDismissRequest = { showPhotoDialog = false },
                    onIsGeneratingPhoto = { isGeneratingPhoto = it },
                    onPhotos = { photos ->
                        val localStageReply = stageReply
                        scope.launch {
                            api.sendMediaFromUri(
                                context,
                                groupId,
                                photos,
                                localStageReply?.id?.let {
                                    Message(attachments = listOf(json.encodeToString(ReplyAttachment(it))))
                                }
                            ) {
                                reloadMessages()
                            }
                        }

                        stageReply = null
                    },
                    onVideos = { videos ->
                        scope.launch {
                            api.sendVideosFromUri(
                                context = context,
                                group = groupId,
                                videos = videos,
                                onError = {
                                    context.showDidntWork()
                                },
                                message = stageReply?.id?.let {
                                    Message(attachments = listOf(json.encodeToString(ReplyAttachment(it))))
                                },
                                processingCallback = {
                                    // todo
                                },
                                uploadCallback = {
                                    // todo
                                }
                            )

                            stageReply = null
                        }
                    },
                    onGeneratedPhoto = { photo ->
                        scope.launch {
                            api.sendMessage(
                                groupId,
                                Message(
                                    // todo let the user choose to include the prompt
                                    // text = prompt,
                                    attachment = json.encodeToString(PhotosAttachment(photos = listOf(photo))),
                                    attachments = stageReply?.id?.let {
                                        listOf(json.encodeToString(ReplyAttachment(it)))
                                    }
                                )
                            ) {
                                stageReply = null
                                reloadMessages()
                            }
                        }
                    }
                )
            }

            if (showLocationDialog) {
                SetLocationDialog(
                    {
                        showLocationDialog = false
                    },
                    initialLocation = groupExtended?.group?.geo?.toLatLng() ?: LatLng(0.0, 0.0),
                    initialZoom = groupExtended?.group?.geo?.let { 14f } ?: 5f,
                ) {
                    scope.launch {
                        api.updateGroup(groupId, Group(geo = it.toList())) {
                            reload()
                        }
                    }
                }
            }

            if (showCategoryDialog) {
                ChooseCategoryDialog(
                    {
                        showCategoryDialog = false
                    },
                    preselect = groupExtended?.group?.categories?.firstOrNull(),
                    { category ->
                        scope.launch {
                            api.updateGroup(
                                groupId,
                                Group().apply {
                                    categories = if (category == null) emptyList() else listOf(category)
                                }
                            ) {
                                reload()
                            }
                        }
                    }
                )
            }

            if (showGroupMembers) {
                PeopleDialog(
                    stringResource(R.string.members),
                    {
                        showGroupMembers = false
                    },
                    people = allMembers.map { it.person!! },
                    infoFormatter = { person ->
                        val member = allMembers.firstOrNull { it.person?.id == person.id }
                        "${if (member?.member?.host == true) "${context.getString(R.string.host)} • " else ""}${
                            person.seenText(
                                context.getString(R.string.active)
                            )
                        }"
                    },
                    actions = {
                        IconButton(
                            {
                                showInviteMembers = true
                            }
                        ) {
                            Icon(Icons.Outlined.Add, stringResource(R.string.invite_someone))
                        }
                    },
                    extraButtons = {
                        if (myMember?.member?.host == true) {
                            TextButton(
                                {
                                    showManageGroupMembersMenu = true
                                }
                            ) {
                                Text(stringResource(R.string.manage))
                            }
                        }
                    }
                ) {
                    showGroupMembers = false
                    nav.appNavigate(AppNav.Profile(it.id!!))
                }
            }

            if (showManageGroupMembersMenu) {
                Menu({
                    showManageGroupMembersMenu = false
                }) {
                    menuItem(stringResource(R.string.add_hosts)) {
                        showManageGroupMembersMenu = false
                        showPromoteGroupMembers = true
                    }
                    menuItem(stringResource(R.string.remove_members)) {
                        showManageGroupMembersMenu = false
                        showRemoveGroupMembers = true
                    }
                }
            }

            if (showRemoveGroupMembers) {
                val members = groupExtended!!.members!!
                    .mapNotNull { it.person?.id }
                    .filter { it != me?.id }
                ChoosePeopleDialog(
                    {
                        showRemoveGroupMembers = false
                    },
                    title = stringResource(R.string.manage),
                    confirmFormatter = defaultConfirmFormatter(
                        R.string.remove,
                        R.string.remove_person,
                        R.string.remove_people,
                        R.string.remove_x_people
                    ) { it.name ?: someone },
                    onPeopleSelected = { people ->
                        var anySucceeded = false
                        var anyFailed = false
                        people.forEach { person ->
                            api.removeMember(
                                otherMembers.find { member -> member.person?.id == person.id }?.member?.id
                                    ?: return@forEach,
                                onError = {
                                    anyFailed = true
                                }
                            ) {
                                context.toast(
                                    context.getString(
                                        R.string.x_removed,
                                        person.name?.nullIfBlank ?: someone
                                    )
                                )
                                anySucceeded = true
                            }
                        }
                        if (anySucceeded) {
                            reload()
                        }
                        if (anyFailed) {
                            context.showDidntWork()
                        }
                    },
                    omit = { it.id!! !in members }
                )
            }

            if (showPromoteGroupMembers) {
                val members = groupExtended!!.members!!
                    .mapNotNull { it.person?.id }
                    .filter { it != me?.id }
                ChoosePeopleDialog(
                    {
                        showPromoteGroupMembers = false
                    },
                    title = stringResource(R.string.manage),
                    confirmFormatter = defaultConfirmFormatter(
                        R.string.promote,
                        R.string.promote_person,
                        R.string.promote_people,
                        R.string.promote_x_people
                    ) { it.name ?: someone },
                    onPeopleSelected = { people ->
                        var anySucceeded = false
                        var anyFailed = false
                        people.forEach { person ->
                            api.updateMember(
                                otherMembers.find { member -> member.person?.id == person.id }?.member?.id
                                    ?: return@forEach,
                                Member(host = true),
                                onError = {
                                    anyFailed = true
                                }
                            ) {
                                context.toast(
                                    context.getString(
                                        R.string.x_promoted,
                                        person.name?.nullIfBlank ?: someone
                                    )
                                )
                                anySucceeded = true
                            }
                        }
                        if (anySucceeded) {
                            reload()
                        }
                        if (anyFailed) {
                            context.showDidntWork()
                        }
                    },
                    omit = { it.id!! !in members }
                )
            }

            if (showTradeWithDialog) {
                val people = groupExtended!!.members!!
                    .mapNotNull { it.person }
                    .filter { it.id != me?.id }
                ChoosePeopleDialog(
                    {
                        showTradeWithDialog = false
                    },
                    title = stringResource(R.string.trade),
                    people = people,
                    confirmFormatter = defaultConfirmFormatter(
                        R.string.trade,
                        R.string.trade_with_x,
                        R.string.trade_with_x_and_x,
                        R.string.trade_with_x_people
                    ) { it.name ?: someone },
                    onPeopleSelected = { people ->
                        trade(people.map { it.id!! })
                    }
                )
            }

            if (showNewReminderWithDialog) {
                val people = groupExtended!!.members!!
                    .mapNotNull { it.person }
                    .filter { it.id != me?.id }
                ChoosePeopleDialog(
                    {
                        showNewReminderWithDialog = false
                    },
                    title = stringResource(R.string.create_reminder),
                    people = people,
                    confirmFormatter = defaultConfirmFormatter(
                        R.string.create_reminder,
                        R.string.create_reminder_with_x,
                        R.string.create_reminder_with_x_and_x,
                        R.string.create_reminder_with_x_people
                    ) { it.name ?: someone },
                    onPeopleSelected = { people ->
                        showScheduleNewReminderDialog = people
                    }
                )
            }

            if (showScheduleNewReminderDialog != null) {
                ScheduleReminderDialog(
                    {
                        showScheduleNewReminderDialog = null
                    },
                    initialReminder = Reminder(
                        start = now().startOfMinute()
                    ),
                    showTitle = true,
                    confirmText = stringResource(R.string.add_reminder)
                ) {
                    newReminder(showScheduleNewReminderDialog!!.mapNotNull { it.id }, it)
                    showScheduleNewReminderDialog = null
                }
            }

            if (showReportDialog) {
                ReportDialog("group/$groupId") {
                    showReportDialog = false
                }
            }

            if (showSnoozeDialog) {
                Menu({
                    showSnoozeDialog = false
                }) {
                    menuItem(stringResource(R.string.for_an_hour)) {
                        showSnoozeDialog = false
                        snooze(now() + 1.hours)
                    }
                    menuItem(stringResource(R.string.for_3_hours)) {
                        showSnoozeDialog = false
                        snooze(now() + 3.hours)
                    }
                    menuItem(stringResource(R.string.for_6_hours)) {
                        showSnoozeDialog = false
                        snooze(now() + 6.hours)
                    }
                    menuItem(stringResource(R.string.for_12_hours)) {
                        showSnoozeDialog = false
                        snooze(now() + 12.hours)
                    }
                    menuItem(stringResource(R.string.for_a_day)) {
                        showSnoozeDialog = false
                        snooze(now() + 1.days)
                    }
                    menuItem(stringResource(R.string.for_a_week)) {
                        showSnoozeDialog = false
                        snooze(now() + 7.days)
                    }
                    menuItem(stringResource(R.string.for_a_month)) {
                        showSnoozeDialog = false
                        snooze(now() + 30.days)
                    }
                    menuItem(stringResource(R.string.indefinitely)) {
                        showSnoozeDialog = false
                        snooze(true)
                    }
                }
            }

            if (showInviteMembers) {
                val someone = stringResource(R.string.someone)
                val omit = groupExtended!!.members!!.mapNotNull { it.person?.id }
                ChoosePeopleDialog(
                    {
                        showInviteMembers = false
                    },
                    title = stringResource(R.string.invite_someone),
                    confirmFormatter = defaultConfirmFormatter(
                        R.string.invite_someone,
                        R.string.invite_person,
                        R.string.invite_x_and_y,
                        R.string.invite_x_people
                    ) { it.name ?: someone },
                    onPeopleSelected = { people ->
                        var anySucceeded = false
                        var anyFailed = false
                        people.forEach { person ->
                            api.createMember(Member().apply {
                                from = person.id!!
                                to = groupId
                            }, onError = { anyFailed = true }) {
                                context.toast(
                                    context.getString(
                                        R.string.person_invited,
                                        person.name?.nullIfBlank ?: someone
                                    )
                                )
                                anySucceeded = true
                            }
                        }
                        if (anySucceeded) {
                            reload()
                        }
                        if (anyFailed) {
                            context.showDidntWork()
                        }
                    },
                    omit = { it.id!! in omit }
                )
            }

            val recomposeScope = currentRecomposeScope

            if (showSendDialog) {
                SendGroupDialog(
                    {
                        showSendDialog = false
                    },
                    groupId
                )
            }

            if (showQrCodeDialog) {
                QrCodeDialog(
                    {
                        showQrCodeDialog = false
                    },
                    groupUrl(groupId),
                    groupExtended!!.name(
                        someone,
                        emptyGroup,
                        me?.id?.let(::listOf) ?: emptyList()
                    )
                )
            }

            if (showRenameGroup) {
                RenameGroupDialog({
                    showRenameGroup = false
                }, groupExtended!!.group!!, {
                    groupExtended!!.group = it
                    recomposeScope.invalidate()
                })
            }

            if (showDescriptionDialog) {
                GroupDescriptionDialog({
                    showDescriptionDialog = false
                }, groupExtended!!.group!!, {
                    groupExtended!!.group = it
                    recomposeScope.invalidate()
                })
            }

            if (showLeaveGroup) {
                AlertDialog(
                    {
                        showLeaveGroup = false
                    },
                    title = {
                        Text(stringResource(R.string.leave_group))
                    },
                    text = {
                    },
                    confirmButton = {
                        TextButton({
                            scope.launch {
                                api.removeMember(myMember!!.member!!.id!!) {
                                    showLeaveGroup = false
                                    nav.popBackStack()
                                }
                            }
                        }) {
                            Text(stringResource(R.string.leave))
                        }
                    },
                    dismissButton = {
                        TextButton({
                            showLeaveGroup = false
                        }) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                )
            }

            if (showGroupNotFound) {
                AlertDialog(
                    {
                        showGroupNotFound = false
                    },
                    title = {
                        Text(stringResource(R.string.group_not_found))
                    },
                    text = {
                    },
                    confirmButton = {
                        TextButton({
                            showGroupNotFound = false
                            nav.popBackStack()
                        }) {
                            Text(stringResource(R.string.leave))
                        }
                    }
                )
            }

            if (showJoinDialog) {
                TextFieldDialog(
                    {
                        showJoinDialog = false
                    },
                    title = stringResource(R.string.join_group),
                    button = stringResource(R.string.send_request),
                    placeholder = stringResource(R.string.message),
                    requireModification = false,
                    showDismiss = true
                ) {
                    joins.join(groupId, it)
                    showJoinDialog = false
                }
            }

            if (showManageDialog) {
                Menu(
                    { showManageDialog = false }
                ) {
                    DropdownMenuItem({
                        Text(stringResource(R.string.rename))
                    }, {
                        showManageDialog = false
                        showRenameGroup = true
                    })
                    DropdownMenuItem({
                        Text(stringResource(R.string.introduction))
                    }, {
                        showManageDialog = false
                        showDescriptionDialog = true
                    })
                    DropdownMenuItem({
                        Text(stringResource(R.string.photo))
                    }, {
                        showManageDialog = false
                        showSetPhotoDialog = true
                    })
                    DropdownMenuItem({
                        Text(stringResource(R.string.background))
                    }, {
                        showManageDialog = false
                        showSetBackgroundDialog = true
                    })
                    DropdownMenuItem({
                        Text(stringResource(R.string.set_category))
                    }, {
                        showManageDialog = false
                        showCategoryDialog = true
                    })
                    DropdownMenuItem({
                        Text(stringResource(R.string.move))
                    }, {
                        showManageDialog = false
                        showLocationDialog = true
                    })
                    if (myMember?.member?.host == true) {
                        if (groupExtended?.group?.open == true) {
                            menuItem(stringResource(R.string.make_group_closed)) {
                                showManageDialog = false
                                showChangeGroupStatus = true
                            }
                        } else {
                            menuItem(stringResource(R.string.make_group_open)) {
                                showManageDialog = false
                                showChangeGroupStatus = true
                            }
                        }
                        menuItem(stringResource(R.string.settings)) {
                            showManageDialog = false
                            showSettingsDialog = true
                        }
                    }
                }
            }

            if (showSettingsDialog && groupExtended?.group != null) {
                GroupSettingsDialog(
                    {
                        showSettingsDialog = false
                    },
                    groupExtended!!.group!!
                )
            }

            if (showChangeGroupStatus) {
                val open = groupExtended?.group?.open == true
                AlertDialog(
                    {
                        showChangeGroupStatus = false
                    },
                    title = {
                        if (!open) Text(stringResource(R.string.open_group_action))
                        else Text(stringResource(R.string.close_group_action))
                    },
                    text = {
                        if (!open) Text(stringResource(R.string.make_group_open_description))
                        else Text(stringResource(R.string.make_group_closed_description))
                    },
                    confirmButton = {
                        Button({
                            scope.launch {
                                api.updateGroup(groupExtended!!.group!!.id!!, Group(open = !open)) {
                                    reload()
                                }
                                showChangeGroupStatus = false
                            }
                        }) {
                            if (!open) Text(stringResource(R.string.make_group_open))
                            else Text(stringResource(R.string.make_group_closed))
                        }
                    },
                    dismissButton = {
                        TextButton({
                            showChangeGroupStatus = false
                        }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            if (showSetPhotoDialog) {
                ChoosePhotoDialog(
                    scope = scope,
                    state = setPhotoDialogState,
                    onDismissRequest = { showSetPhotoDialog = false },
                    multiple = false,
                    imagesOnly = true,
                    onPhotos = { photos ->
                        scope.launch {
                            isGeneratingGroupPhoto = true
                            api.uploadPhotosFromUris(context, photos) {
                                val photo = it.urls.first()
                                api.updateGroup(groupId, Group(photo = photo)) {
                                    reload()
                                }
                            }
                            isGeneratingGroupPhoto = false
                        }
                    },
                    onGeneratedPhoto = { photo ->
                        scope.launch {
                            api.updateGroup(groupId, Group(photo = photo)) {
                                context.toast(R.string.photo_updated)
                                reload()
                            }
                        }
                    },
                    onIsGeneratingPhoto = {
                        isGeneratingGroupPhoto = it
                    }
                )
            }

            if (showSetBackgroundDialog) {
                ChoosePhotoDialog(
                    scope = scope,
                    state = setBackgroundDialogState,
                    onDismissRequest = { showSetBackgroundDialog = false },
                    multiple = false,
                    imagesOnly = true,
                    onPhotos = { photos ->
                        scope.launch {
                            isGeneratingGroupBackground = true
                            api.uploadPhotosFromUris(context, photos) {
                                val photo = it.urls.first()
                                api.updateGroup(groupId, Group(background = photo)) {
                                    reload()
                                }
                            }
                            isGeneratingGroupBackground = false
                        }
                    },
                    onGeneratedPhoto = { photo ->
                        scope.launch {
                            api.updateGroup(groupId, Group(background = photo)) {
                                reload()
                            }
                        }
                    },
                    onIsGeneratingPhoto = {
                        isGeneratingGroupBackground = it
                    }
                )
            }

            if (showAudioRationale) {
                RationaleDialog(
                    {
                        showAudioRationale = false
                    },
                    stringResource(R.string.permission_request)
                )
            }

            showReplyInNewGroupDialog?.let { message ->
                val title = message.text?.notBlank ?: message.attachmentText(context)?.let { "\"$it\"" } ?: stringResource(R.string.reply)
                ChoosePeopleDialog(
                    onDismissRequest = { showReplyInNewGroupDialog = null },
                    title = title,
                    confirmFormatter = { stringResource(R.string.reply) },
                    people = groupExtended?.members?.mapNotNull { it.person }.orEmpty(),
                    allowNone = true,
                    multiple = true,
                    onPeopleSelected = {
                        replyInNewGroup(title, message, it)
                    },
                    omit = { it.id == me?.id },
                )
            }

            showTradeDialog?.let {
                TradeDialog(
                    {
                        showTradeDialog = null
                    },
                    it.id!!
                )
            }
        }
    }
}

// Todo should support videos
private fun List<Message>.photos() =
    flatMap { message -> (message.getAttachment() as? PhotosAttachment)?.photos?.asReversed() ?: emptyList() }

fun Person.seenText(active: String) = seen?.timeAgo()?.let { timeAgo ->
    "$active ${timeAgo.lowercase()}"
}

fun Person.seenText() = seen?.timeAgo()?.lowercase()
