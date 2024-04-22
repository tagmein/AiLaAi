package com.queatz.ailaai.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import app.ailaai.api.*
import com.queatz.ailaai.AppNav
import com.queatz.ailaai.R
import com.queatz.ailaai.api.uploadCardPhotoFromUri
import com.queatz.ailaai.api.uploadCardVideoFromUri
import com.queatz.ailaai.data.api
import com.queatz.ailaai.data.json
import com.queatz.ailaai.dataStore
import com.queatz.ailaai.extensions.*
import com.queatz.ailaai.helpers.ResumeEffect
import com.queatz.ailaai.me
import com.queatz.ailaai.nav
import com.queatz.ailaai.services.SavedIcon
import com.queatz.ailaai.services.ToggleSaveResult
import com.queatz.ailaai.services.saves
import com.queatz.ailaai.ui.components.*
import com.queatz.ailaai.ui.dialogs.*
import com.queatz.ailaai.ui.state.jsonSaver
import com.queatz.ailaai.ui.theme.pad
import com.queatz.db.Card
import com.queatz.db.CardAttachment
import com.queatz.db.Message
import com.queatz.db.Person
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant.Companion.fromEpochMilliseconds
import kotlinx.serialization.encodeToString
import kotlin.time.Duration.Companion.seconds

private val showGeneratingMessage = booleanPreferencesKey("ui.showGeneratingMessage")

@Composable
fun CardScreen(cardId: String) {
    var isLoading by rememberStateOf(false)
    var notFound by rememberStateOf(false)
    var showMenu by rememberStateOf(false)
    var showManageMenu by rememberStateOf(false)
    var openDeleteCard by rememberStateOf(false)
    var openLocationDialog by rememberStateOf(false)
    var showReportDialog by rememberStateOf(false)
    var openEditDialog by rememberStateOf(false)
    var openChangeOwner by rememberStateOf(false)
    var showQrCode by rememberSavableStateOf(false)
    var showSendDialog by rememberSavableStateOf(false)
    var openAddCollaboratorDialog by rememberSavableStateOf(false)
    var openRemoveCollaboratorsDialog by rememberSavableStateOf(false)
    var openCollaboratorsDialog by rememberSavableStateOf(false)
    var openLeaveCollaboratorsDialog by rememberSavableStateOf(false)
    var card by rememberSaveable(stateSaver = jsonSaver<Card?>()) { mutableStateOf(null) }
    var cards by rememberSaveable(stateSaver = jsonSaver<List<Card>>(emptyList())) { mutableStateOf(emptyList()) }
    val scope = rememberCoroutineScope()
    val state = rememberLazyGridState()
    val stateLandscape = rememberLazyGridState()
    val context = LocalContext.current
    var uploadJob by remember { mutableStateOf<Job?>(null) }
    var isUploadingVideo by rememberStateOf(false)
    var videoUploadStage by remember { mutableStateOf(ProcessingVideoStage.Processing) }
    var videoUploadProgress by remember { mutableStateOf(0f) }
    var showSetCategory by rememberStateOf(false)
    var showPay by rememberStateOf(false)
    var showRegeneratePhotoDialog by rememberStateOf(false)
    var showGeneratingPhotoDialog by rememberStateOf(false)
    var isGeneratingPhoto by rememberStateOf(false)
    var showPhotoDialog by rememberStateOf(false)
    var oldPhoto by rememberStateOf<String?>(null)
    val me = me
    val nav = nav
    val setPhotoState = remember(card?.name == null) {
        ChoosePhotoDialogState(mutableStateOf(card?.name ?: ""))
    }

    if (showPhotoDialog) {
        ChoosePhotoDialog(
            scope = scope,
            state = setPhotoState,
            onDismissRequest = { showPhotoDialog = false },
            multiple = false,
            onPhotos = { photos ->
                scope.launch {
                    api.uploadCardPhotoFromUri(context, card!!.id!!, photos.firstOrNull() ?: return@launch) {
                        api.card(cardId) { card = it }
                    }
                }
            },
            onVideos = { videos ->
                val it = videos.firstOrNull() ?: return@ChoosePhotoDialog
                uploadJob = scope.launch {
                    videoUploadProgress = 0f
                    isUploadingVideo = true
                    api.uploadCardVideoFromUri(
                        context,
                        card!!.id!!,
                        it,
                        context.contentResolver.getType(it) ?: "video/*",
                        it.lastPathSegment ?: "video.${
                            context.contentResolver.getType(it)?.split("/")?.lastOrNull() ?: ""
                        }",
                        processingCallback = {
                            videoUploadStage = ProcessingVideoStage.Processing
                            videoUploadProgress = it
                        },
                        uploadCallback = {
                            videoUploadStage = ProcessingVideoStage.Uploading
                            videoUploadProgress = it
                        }
                    )
                    api.card(cardId) { card = it }
                    uploadJob = null
                    isUploadingVideo = false
                }
            },
            onGeneratedPhoto = { photo ->
                scope.launch {
                    api.updateCard(card!!.id!!, Card(photo = photo)) {
                        api.card(cardId) { card = it }
                    }
                }
            },
            onIsGeneratingPhoto = {
                isGeneratingPhoto = it
            }
        )
    }

    if (isUploadingVideo) {
        ProcessingVideoDialog(
            onDismissRequest = { isUploadingVideo = false },
            onCancelRequest = { uploadJob?.cancel() },
            stage = videoUploadStage,
            progress = videoUploadProgress
        )
    }

    LaunchedEffect(Unit) {
        if (card != null) {
            return@LaunchedEffect
        }
        isLoading = true
        notFound = false

        api.card(cardId, onError = {
            if (it.status == HttpStatusCode.NotFound) {
                notFound = true
            }
        }) { card = it }
        api.cardsCards(cardId) { cards = it }
        isLoading = false
    }

    val isMine = me?.id == card?.person
    val isMineOrIAmACollaborator = isMine || card?.collaborators?.contains(me?.id) == true
    val recomposeScope = currentRecomposeScope

    fun reload() {
        scope.launch {
            api.card(cardId) { card = it }
        }
    }

    fun reloadCards() {
        scope.launch {
            api.cardsCards(cardId) { cards = it }
        }
    }

    fun generatePhoto() {
        scope.launch {
            api.generateCardPhoto(cardId) {
                if (
                    context.dataStore.data.first().let {
                        it[showGeneratingMessage] != false
                    }
                ) {
                    showGeneratingPhotoDialog = true
                }
                oldPhoto = card?.photo ?: ""
            }
        }
    }

    fun regeneratePhoto() {
        card ?: return

        if (card!!.photo.isNullOrBlank()) {
            generatePhoto()
        } else {
            showRegeneratePhotoDialog = true
        }
    }

    ResumeEffect {
        reload()
        reloadCards()
    }

    LaunchedEffect(oldPhoto) {
        var tries = 0
        while (tries++ < 5 && oldPhoto != null) {
            delay(3.seconds)
            api.card(cardId) {
                if (if (oldPhoto.isNullOrBlank()) !it.photo.isNullOrBlank() else it.photo != oldPhoto) {
                    reload()
                    oldPhoto = null
                }
            }
        }
    }

    if (showRegeneratePhotoDialog) {
        AlertDialog(
            onDismissRequest = {
                showRegeneratePhotoDialog = false
            },
            title = {
                Text(stringResource(R.string.generate_a_new_photo))
            },
            text = {
                Text(stringResource(R.string.this_will_replace_the_current_photo))
            },
            confirmButton = {
                TextButton({
                    showRegeneratePhotoDialog = false
                    generatePhoto()
                }) {
                    Text(stringResource(R.string.yes))
                }
            }
        )
    }

    if (showGeneratingPhotoDialog) {
        AlertDialog(
            onDismissRequest = {
                showGeneratingPhotoDialog = false
            },
            title = {
                Text(stringResource(R.string.generating))
            },
            text = {
                Text(stringResource(R.string.generating_description))
            },
            dismissButton = {
                TextButton({
                    showGeneratingPhotoDialog = false
                }) {
                    Text(stringResource(R.string.close))
                }
            },
            confirmButton = {
                TextButton({
                    showGeneratingPhotoDialog = false
                    scope.launch {
                        context.dataStore.edit {
                            it[showGeneratingMessage] = false
                        }
                    }
                }) {
                    Text(stringResource(R.string.dont_show))
                }
            },
        )
    }

    if (showSetCategory) {
        ChooseCategoryDialog(
            {
                showSetCategory = false
            },
            preselect = card?.categories?.firstOrNull(),
            { category ->
                scope.launch {
                    api.updateCard(
                        card!!.id!!,
                        Card().apply {
                            categories = if (category == null) emptyList() else listOf(category)
                        }
                    ) {
                        reload()
                    }
                }
            }
        )
    }

    if (showPay) {
        PayDialog(
            {
                showPay = false
            },
            defaultPay = card?.pay?.pay,
            defaultFrequency = card?.pay?.frequency
        ) { pay ->
            api.updateCard(
                cardId,
                Card(pay = pay)
            ) {
                reload()
            }
        }
    }

    if (showReportDialog) {
        ReportDialog("card/$cardId") {
            showReportDialog = false
        }
    }

    if (openLocationDialog) {
        EditCardLocationDialog(card!!, nav.context as Activity, {
            openLocationDialog = false
        }, {
            recomposeScope.invalidate()
        })
    }

    if (openEditDialog) {
        EditCardDialog(card!!, {
            openEditDialog = false
        }) {
            recomposeScope.invalidate()
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
            reloadCards()
            nav.navigate(AppNav.Page(it.id!!))
        }
    }

    card?.let { card ->
        if (openDeleteCard) {
            DeleteCardDialog(card, {
                openDeleteCard = false
            }) {
                nav.popBackStackOrFinish()
            }
        }
    }

    if (openChangeOwner) {
        val someone = stringResource(R.string.someone)
        ChoosePeopleDialog(
            {
                openChangeOwner = false
            },
            title = stringResource(R.string.change_owner),
            confirmFormatter = defaultConfirmFormatter(
                R.string.give,
                R.string.give_to_person,
                R.string.give_to_people,
                R.string.give_to_x_people
            ) { it.name ?: someone },
            omit = { it.id == me?.id },
            multiple = false,
            onPeopleSelected = {
                if (it.size == 1) {
                    val newOwner = it.first().id
                    scope.launch {
                        card!!.person = newOwner
                        api.updateCard(card!!.id!!, Card().apply {
                            person = card!!.person
                        }) {
                            card = it
                        }
                    }
                }
            }
        )
    }

    if (showManageMenu) {
        Menu(
            {
                showManageMenu = false
            }
        ) {
            menuItem(stringResource(R.string.change_owner)) {
                openChangeOwner = true
                showManageMenu = false
            }
            menuItem(stringResource(R.string.delete_card)) {
                openDeleteCard = true
                showManageMenu = false
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxSize()
    ) {
        AppBar(
            title = {
                Column {
                    Text(card?.name ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis)

                    card?.hint?.notBlank?.let {
                        Text(
                            it,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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
                card?.let { card ->
                    IconButton({
                        scope.launch {
                            when (saves.toggleSave(card)) {
                                ToggleSaveResult.Saved -> {
                                    context.toast(R.string.card_saved)
                                }

                                ToggleSaveResult.Unsaved -> {
                                    context.toast(R.string.card_unsaved)
                                }

                                else -> {
                                    context.showDidntWork()
                                }
                            }
                        }
                    }) {
                        SavedIcon(card)
                    }
                }

                IconButton({
                    showMenu = !showMenu
                }) {
                    Icon(Icons.Outlined.MoreVert, stringResource(R.string.more))
                }

                val cardString = stringResource(R.string.card)

                Dropdown(showMenu, { showMenu = false }) {
                    if (isMine) {
                        DropdownMenuItem({
                            Text(stringResource(R.string.manage))
                        }, {
                            showManageMenu = true
                            showMenu = false
                        })
                        if (card?.collaborators?.isNotEmpty() != true) {
                            DropdownMenuItem({
                                Text(stringResource(R.string.add_collaborators))
                            }, {
                                openAddCollaboratorDialog = true
                                showMenu = false
                            })
                        }
                    }
                    if (isMineOrIAmACollaborator && (card?.collaborators?.isNotEmpty() == true)) {
                        DropdownMenuItem({
                            Text(stringResource(R.string.collaborators))
                        }, {
                            if (isMine) {
                                openRemoveCollaboratorsDialog = true
                            } else {
                                openCollaboratorsDialog = true
                            }
                            showMenu = false
                        })
                    }
                    card?.let { card ->
                        DropdownMenuItem({
                            Text(stringResource(R.string.view_profile))
                        }, {
                            nav.navigate(AppNav.Profile(card.person!!))
                            showMenu = false
                        })
                        if (card.parent != null) {
                            DropdownMenuItem({
                                Text(stringResource(R.string.open_enclosing_card))
                            }, {
                                nav.navigate(AppNav.Page(card.parent!!))
                                showMenu = false
                            })
                        }
                    }
                    DropdownMenuItem({
                        Text(stringResource(R.string.send_card))
                    }, {
                        showSendDialog = true
                        showMenu = false
                    })
                    DropdownMenuItem({
                        Text(stringResource(R.string.qr_code))
                    }, {
                        showQrCode = true
                        showMenu = false
                    })
                    if (card?.geo?.size == 2) {
                        DropdownMenuItem({
                            Text(stringResource(R.string.show_on_map))
                        }, {
                            card?.let { card ->
                                val uri = Uri.parse(
                                    "geo:${card.geo!![0]},${card.geo!![1]}?q=${card.geo!![0]},${card.geo!![1]}(${
                                        Uri.encode(card.name ?: cardString)
                                    })"
                                )
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                nav.context.startActivity(Intent.createChooser(intent, null))
                            }
                            showMenu = false
                        })
                    }
                    val textCopied = stringResource(R.string.copied)
                    DropdownMenuItem({
                        Text(stringResource(R.string.share))
                    }, {
                        cardUrl(cardId).shareAsUrl(context, card?.name ?: cardString)
                        showMenu = false
                    })
                    DropdownMenuItem({
                        Text(stringResource(R.string.copy_link))
                    }, {
                        cardUrl(cardId).copyToClipboard(context, card?.name ?: cardString)
                        context.toast(textCopied)
                        showMenu = false
                    })
                    DropdownMenuItem({
                        Text(stringResource(R.string.report))
                    }, {
                        showReportDialog = true
                        showMenu = false
                    })
                }
            },
            modifier = Modifier
                .zIndex(1f)
        )

        fun LazyGridScope.cardHeaderItem(
            card: Card?,
            isMine: Boolean,
            aspect: Float,
            scope: CoroutineScope,
            elevation: Int = 1,
            playVideo: Boolean = false
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (card != null && isMine) {
                        var active by remember { mutableStateOf(card.active ?: false) }
                        var activeCommitted by remember { mutableStateOf(active) }

                        CardToolbar(
                            modifier = Modifier.padding(bottom = 1.pad)
                        ) {
                            item(
                                if (active) Icons.Outlined.ToggleOn else Icons.Outlined.ToggleOff,
                                if (activeCommitted) stringResource(R.string.posted) else stringResource(R.string.not_posted),
                                selected = active
                            ) {
                                active = !active
                                scope.launch {
                                    api.updateCard(card.id!!, Card(active = active)) {
                                        card.active = it.active
                                        activeCommitted = it.active ?: false
                                        active = activeCommitted
                                    }
                                }
                            }

                            item(
                                Icons.Outlined.Place,
                                when {
                                    card.parent != null -> stringResource(R.string.inside_another_card)
                                    card.group != null -> stringResource(R.string.in_a_group)
                                    card.equipped == true -> stringResource(R.string.on_profile)
                                    card.offline != true -> stringResource(R.string.at_a_location)
                                    else -> stringResource(R.string.none)
                                },
                                selected = when {
                                    card.parent != null -> true
                                    card.group != null -> true
                                    card.equipped == true -> true
                                    card.offline != true -> true
                                    else -> false
                                }
                            ) {
                                openLocationDialog = true
                            }

                            item(
                                Icons.Outlined.Edit,
                                stringResource(R.string.edit)
                            ) {
                                openEditDialog = true
                            }

                            item(
                                Icons.Outlined.CameraAlt,
                                stringResource(R.string.set_photo),
                                isLoading = isGeneratingPhoto
                            ) {
                                showPhotoDialog = true
                            }

                            item(
                                Icons.Outlined.AutoAwesome,
                                stringResource(R.string.generate_photo),
                                isLoading = oldPhoto != null
                            ) {
                                regeneratePhoto()
                                showMenu = false
                            }

                            val category = card.categories?.firstOrNull()
                            item(
                                Icons.Outlined.Category,
                                category ?: stringResource(R.string.set_category),
                                selected = category != null
                            ) {
                                showSetCategory = true
                                showMenu = false
                            }

                            item(
                                Icons.Outlined.Payments,
                                stringResource(if (card.pay == null) R.string.add_pay else R.string.change_pay),
                                selected = card.pay != null
                            ) {
                                showPay = true
                                showMenu = false
                            }

                            item(
                                Icons.Outlined.AddBox,
                                stringResource(if (card.content?.notBlank == null) R.string.add_content else R.string.content),
                                selected = card.content?.notBlank != null
                            ) {
                                nav.navigate(AppNav.EditCard(card.id!!))
                            }
                        }
                    }
                    CardLayout(
                        card = card,
                        showTitle = false,
                        aspect = aspect,
                        scope = scope,
                        elevation = elevation,
                        playVideo = playVideo
                    )
                }
            }
        }

        if (isLoading) {
            Loading()
        } else if (notFound) {
            Text(
                stringResource(R.string.card_not_found),
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.pad)
            )
        } else {
            val isLandscape = LocalConfiguration.current.screenWidthDp > LocalConfiguration.current.screenHeightDp
            var playingVideo by remember { mutableStateOf<Card?>(null) }
            val isAtTop by state.isAtTop()
            val autoplayIndex by state.rememberAutoplayIndex()
            LaunchedEffect(autoplayIndex, isLandscape) {
                playingVideo = cards.getOrNull(
                    (autoplayIndex - (if (isLandscape) 0 else 1)).coerceAtLeast(0)
                )
            }
            Box {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isLandscape) {
                        LazyVerticalGrid(
                            state = stateLandscape,
                            contentPadding = PaddingValues(1.pad),
                            horizontalArrangement = Arrangement.spacedBy(1.pad, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(1.pad, Alignment.Top),
                            columns = GridCells.Fixed(1),
                            modifier = Modifier
                                .width(240.dp)
                                .fillMaxHeight()
                        ) {
                            cardHeaderItem(
                                card,
                                isMine,
                                1.5f,
                                scope,
                                elevation = 2,
                                playVideo = isAtTop
                            )
                        }
                    }

                    LazyVerticalGrid(
                        state = state,
                        contentPadding = PaddingValues(1.pad),
                        horizontalArrangement = Arrangement.spacedBy(1.pad, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(1.pad, Alignment.Top),
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Adaptive(240.dp)
                    ) {
                        if (!isLandscape) {
                            cardHeaderItem(
                                card,
                                isMine,
                                1.5f,
                                scope,
                                playVideo = isAtTop
                            )
                        }
                        if (cards.isEmpty()) {
                            if (isLandscape && !isMine) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Text(
                                        stringResource(R.string.no_cards),
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(2.pad)
                                    )
                                }
                            }
                        } else {
                            items(cards, { it.id!! }) {
                                CardLayout(
                                    card = it,
                                    showTitle = true,
                                    hideCreators = card?.person?.inList()?.let { it + (card?.collaborators ?: emptyList()) },
                                    onClick = {
                                        nav.navigate(AppNav.Page(it.id!!))
                                    },
                                    scope = scope,
                                    playVideo = playingVideo == it && !isAtTop,
                                )
                            }
                        }
                    }
                }
                if (isMineOrIAmACollaborator) {
                    FloatingActionButton(
                        onClick = {
                            newCard = Card(parent = cardId)
                        },
                        modifier = Modifier
                            .padding(2.pad)
                            .align(Alignment.BottomEnd)
                    ) {
                        Icon(Icons.Outlined.Add, stringResource(R.string.add_a_card))
                    }
                }
            }
        }
    }

    val someone = stringResource(R.string.someone)
    val emptyGroup = stringResource(R.string.empty_group_name)

    if (openLeaveCollaboratorsDialog) {
        AlertDialog(
            onDismissRequest = {
                openLeaveCollaboratorsDialog = false
            },
            title = {
                Text(stringResource(R.string.leave_card))
            },
            confirmButton = {
                TextButton({
                    scope.launch {
                        api.leaveCollaboration(cardId)
                        api.card(cardId) { card = it }
                        api.cardsCards(cardId) { cards = it }
                        openLeaveCollaboratorsDialog = false
                    }
                }) {
                    Text(stringResource(R.string.leave))
                }
            },
            dismissButton = {
                TextButton({
                    openLeaveCollaboratorsDialog = false
                }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (openAddCollaboratorDialog) {
        ChoosePeopleDialog(
            {
                openAddCollaboratorDialog = false
            },
            title = stringResource(R.string.add_collaborators),
            confirmFormatter = defaultConfirmFormatter(
                R.string.add,
                R.string.add_person,
                R.string.add_people,
                R.string.add_x_people
            ) { it.name ?: someone },
            onPeopleSelected = { people ->
                card!!.collaborators = (card?.collaborators ?: emptyList()) + people.map { it.id!! }
                api.updateCard(card!!.id!!, Card().apply {
                    collaborators = card!!.collaborators
                }) {
                    card = it
                }
            },
            omit = { it.id == me?.id || card!!.collaborators?.contains(it.id) == true }
        )
    }

    if (openRemoveCollaboratorsDialog) {
        ChoosePeopleDialog(
            {
                openRemoveCollaboratorsDialog = false
            },
            title = stringResource(R.string.collaborators),
            confirmFormatter = defaultConfirmFormatter(
                R.string.remove,
                R.string.remove_person,
                R.string.remove_people,
                R.string.remove_x_people
            ) { it.name ?: someone },
            extraButtons = {
                TextButton(
                    {
                        openRemoveCollaboratorsDialog = false
                        openAddCollaboratorDialog = true
                    }
                ) {
                    Text(stringResource(R.string.add))
                }
            },
            onPeopleSelected = { people ->
                card!!.collaborators = (card?.collaborators ?: emptyList()) - people.map { it.id!! }.toSet()
                api.updateCard(card!!.id!!, Card().apply {
                    collaborators = card!!.collaborators
                }) {
                    card = it
                }
            },
            omit = { it.id !in (card!!.collaborators ?: emptyList()) }
        )
    }

    var collaborators by remember { mutableStateOf(emptyList<Person>()) }

    LaunchedEffect(openCollaboratorsDialog) {
        if (openCollaboratorsDialog) {
            api.cardPeople(cardId) {
                collaborators = it.sortedByDescending { it.seen ?: fromEpochMilliseconds(0) }
            }
        }
    }

    if (openCollaboratorsDialog && collaborators.isNotEmpty()) {
        PeopleDialog(
            stringResource(R.string.collaborators),
            {
                openCollaboratorsDialog = false
            }, collaborators, infoFormatter = { person ->
                if (person.id == me?.id) {
                    context.getString(R.string.leave)
                } else {
                    person.seen?.timeAgo()?.let { timeAgo ->
                        "${context.getString(R.string.active)} ${timeAgo.lowercase()}"
                    }
                }
            }) { person ->
            if (person.id == me?.id) {
                openLeaveCollaboratorsDialog = true
                openCollaboratorsDialog = false
            } else {
                scope.launch {
                    // todo open conversations dialog
                    api.createGroup(listOf(me!!.id!!, person.id!!), reuse = true) {
                        nav.navigate(AppNav.Group(it.id!!))
                        openCollaboratorsDialog = false
                    }
                }
            }
        }
    }

    if (showSendDialog) {
        val sent = stringResource(R.string.sent)
        ChooseGroupDialog(
            {
                showSendDialog = false
            },
            title = stringResource(R.string.send_card),
            confirmFormatter = defaultConfirmFormatter(
                R.string.send_card,
                R.string.send_card_to_group,
                R.string.send_card_to_groups,
                R.string.send_card_to_x_groups
            ) { it.name(someone, emptyGroup, me?.id?.let(::listOf) ?: emptyList()) }
        ) { groups ->
            coroutineScope {
                var sendSuccess = false
                groups.map { group ->
                    async {
                        api.sendMessage(
                            group.id!!,
                            Message(attachment = json.encodeToString(CardAttachment(cardId)))
                        ) {
                            sendSuccess = true
                        }
                    }
                }.awaitAll()
                if (sendSuccess) {
                    context.toast(sent)
                }
            }
        }
    }

    if (showQrCode) {
        QrCodeDialog(
            {
                showQrCode = false
            },
            cardUrl(cardId),
            card?.name
        )
    }
}
