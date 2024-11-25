package com.queatz.ailaai.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ailaai.api.createGroup
import app.ailaai.api.exploreGroups
import app.ailaai.api.friendStatuses
import app.ailaai.api.groups
import app.ailaai.api.groupsWith
import app.ailaai.api.hiddenGroups
import app.ailaai.api.myGeo
import app.ailaai.api.people
import app.ailaai.api.recentStatuses
import app.ailaai.api.removeMember
import app.ailaai.api.updateGroup
import app.ailaai.api.updateMember
import at.bluesource.choicesdk.maps.common.LatLng
import com.queatz.ailaai.AppNav
import com.queatz.ailaai.R
import com.queatz.ailaai.data.api
import com.queatz.ailaai.extensions.SwipeResult
import com.queatz.ailaai.extensions.appNavigate
import com.queatz.ailaai.extensions.inDp
import com.queatz.ailaai.extensions.notEmpty
import com.queatz.ailaai.extensions.px
import com.queatz.ailaai.extensions.rememberSavableStateOf
import com.queatz.ailaai.extensions.rememberStateOf
import com.queatz.ailaai.extensions.scrollToTop
import com.queatz.ailaai.extensions.showDidntWork
import com.queatz.ailaai.extensions.sortedDistinct
import com.queatz.ailaai.extensions.swipe
import com.queatz.ailaai.extensions.timeAgo
import com.queatz.ailaai.extensions.toGeo
import com.queatz.ailaai.helpers.ResumeEffect
import com.queatz.ailaai.helpers.locationSelector
import com.queatz.ailaai.me
import com.queatz.ailaai.nav
import com.queatz.ailaai.services.joins
import com.queatz.ailaai.services.messages
import com.queatz.ailaai.services.push
import com.queatz.ailaai.ui.components.AppHeader
import com.queatz.ailaai.ui.components.ContactItem
import com.queatz.ailaai.ui.components.DisplayText
import com.queatz.ailaai.ui.components.Dropdown
import com.queatz.ailaai.ui.components.Friends
import com.queatz.ailaai.ui.components.GroupInfo
import com.queatz.ailaai.ui.components.Loading
import com.queatz.ailaai.ui.components.LocationScaffold
import com.queatz.ailaai.ui.components.MainTab
import com.queatz.ailaai.ui.components.MainTabs
import com.queatz.ailaai.ui.components.NotificationsDisabledBanner
import com.queatz.ailaai.ui.components.PageInput
import com.queatz.ailaai.ui.components.ScanQrCodeButton
import com.queatz.ailaai.ui.components.SearchFieldAndAction
import com.queatz.ailaai.ui.components.SearchResult
import com.queatz.ailaai.ui.components.swipeMainTabs
import com.queatz.ailaai.ui.dialogs.ChooseGroupDialog
import com.queatz.ailaai.ui.dialogs.ChoosePeopleDialog
import com.queatz.ailaai.ui.dialogs.EditStatusDialog
import com.queatz.ailaai.ui.dialogs.PersonStatusDialog
import com.queatz.ailaai.ui.dialogs.TextFieldDialog
import com.queatz.ailaai.ui.dialogs.defaultConfirmFormatter
import com.queatz.ailaai.ui.theme.pad
import com.queatz.db.Group
import com.queatz.db.GroupExtended
import com.queatz.db.Member
import com.queatz.db.Person
import com.queatz.db.PersonStatus
import com.queatz.db.Status
import com.queatz.db.people
import com.queatz.push.GroupPushData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

private var cache = mutableMapOf<MainTab, List<GroupExtended>>()

@Composable
fun FriendsScreen() {
    val context = LocalContext.current
    val state = rememberLazyListState()
    var searchText by rememberSaveable { mutableStateOf("") }
    var allHiddenGroups by remember { mutableStateOf(emptyList<GroupExtended>()) }
    var allPeople by remember { mutableStateOf(emptyList<Person>()) }
    var results by remember { mutableStateOf(emptyList<SearchResult>()) }
    var createGroupName by remember { mutableStateOf("") }
    var showCreateGroupName by rememberStateOf(false)
    var showCreateGroupMembers by rememberStateOf(false)
    var showHiddenGroupsDialog by rememberStateOf(false)
    var showSharedGroupsDialogPerson by rememberStateOf<Person?>(null)
    var showSharedGroupsDialog by rememberStateOf<List<GroupExtended>>(emptyList())
    val scope = rememberCoroutineScope()
    var selectedHiddenGroups by rememberStateOf(listOf<Group>())
    var tab by rememberSavableStateOf(MainTab.Friends)
    var allGroups by remember { mutableStateOf(
        cache[tab] ?: emptyList()
    ) }
    var isLoading by rememberStateOf(allGroups.isEmpty())
    var geo: LatLng? by remember { mutableStateOf(null) }
    val nav = nav
    val me = me
    val locationSelector = locationSelector(
        geo,
        { geo = it },
        nav.context as Activity
    )
    val reloadFlow = remember {
        MutableSharedFlow<Boolean>()
    }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var categories by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var myStatusDialog by rememberStateOf(false)
    var statuses by rememberStateOf(emptyMap<String, PersonStatus?>())
    var recentStatuses by rememberStateOf(emptyList<Status>())
    var showFriendStatusDialog by rememberStateOf<Pair<Person, PersonStatus?>?>(null)

    LaunchedEffect(geo) {
        geo?.let {
            api.myGeo(it.toGeo())
        }
    }

    LaunchedEffect(allGroups) {
        cache[tab] = allGroups
        categories = allGroups
            .flatMap { it.group?.categories ?: emptyList() }
            .sortedDistinct()
    }

    suspend fun reloadStatuses() {
        api.friendStatuses {
            statuses = it.groupBy { it.person!! }.mapValues { it.value.firstOrNull() }
        }
        api.recentStatuses {
            recentStatuses = it
        }
    }

    LaunchedEffect(allGroups) {
        reloadStatuses()
    }

    fun update() {
        results = allPeople
            .map { SearchResult.Connect(it) } +
                searchText.trim().let { text ->
                    (if (text.isBlank()) allGroups else allGroups.filter {
                        (it.group?.name?.contains(text, true) ?: false) ||
                                it.members?.any {
                                    it.person?.id != me?.id && it.person?.name?.contains(
                                        text,
                                        true
                                    ) ?: false
                                } ?: false
                    }).map { SearchResult.Group(it) }
                }.let {
                    if (selectedCategory == null) it else it.filter {
                        it.groupExtended.group?.categories?.contains(selectedCategory) == true
                    }
                }
    }

    suspend fun reload(passive: Boolean = false) {
        isLoading = results.isEmpty()
        when (tab) {
            MainTab.Friends -> {
                api.groups(
                    onError = {
                        if (!passive && it !is CancellationException) {
                            context.showDidntWork()
                        }
                    }
                ) {
                    allGroups = it.filter { it.group != null }
                    messages.refresh(me, allGroups)
                }
            }

            MainTab.Local -> {
                if (geo != null) {
                    api.exploreGroups(
                        geo = geo!!.toGeo(),
                        search = searchText,
                        public = true,
                        onError = {
                            if (!passive && it !is CancellationException) {
                                context.showDidntWork()
                            }
                        }
                    ) {
                        allGroups = it.filter { it.group != null }
                    }
                }
            }

            else -> {}
        }
        update()
        isLoading = false
        scope.launch {
            joins.reload()
        }
    }

    LaunchedEffect(Unit) {
        reloadFlow.collectLatest {
            reload(it)
        }
    }

    LaunchedEffect(Unit) {
        push.events
            .filterIsInstance<GroupPushData>()
            .collectLatest {
                reloadFlow.emit(true)
            }
    }

    ResumeEffect {
        reloadFlow.emit(true)
    }

// Todo: is there a nice way to ask upfront
//    LaunchedEffect(Unit) {
//        requestNotifications()
//    }

    LaunchedEffect(searchText) {
        if (searchText.isBlank()) {
            allPeople = emptyList()
        } else {
            api.people(searchText) {
                allPeople = it
            }
        }
        update()
        reloadFlow.emit(true)
    }

    LaunchedEffect(selectedCategory, searchText) {
        update()
    }

    var skipFirst by rememberStateOf(geo != null)

    LaunchedEffect(geo) {
        if (geo == null || tab != MainTab.Local) {
            return@LaunchedEffect
        }
        if (skipFirst) {
            skipFirst = false
            return@LaunchedEffect
        }
        // todo search server, set allGroups
        reloadFlow.emit(true)
    }

    fun setTab(it: MainTab) {
        tab = it
        allGroups = cache[tab] ?: emptyList()
        allPeople = emptyList()
        update()
        isLoading = allGroups.isEmpty()
        selectedCategory = null
        scope.launch {
            state.scrollToTop()
            reloadFlow.emit(false)
        }
    }

    fun onFriendClick(person: Person, sendMessage: Boolean = false) {
        val status = statuses[person.id!!]

        if (status != null && !sendMessage) {
            showFriendStatusDialog = person to status
        } else {
            me?.let { me ->
                scope.launch {
                    api.groupsWith(listOf(me.id!!, person.id!!)) {
                        showSharedGroupsDialogPerson = person
                        showSharedGroupsDialog = it
                    }
                }
            }
        }
    }

    val tabs = listOf(MainTab.Friends, MainTab.Local)

    showFriendStatusDialog?.let { (person, status) ->
        PersonStatusDialog(
            onDismissRequest = { showFriendStatusDialog = null },
            person = person,
            personStatus = status,
            onMessageClick = {
                onFriendClick(person, sendMessage = true)
                showFriendStatusDialog = null
            }
        ) {
            showFriendStatusDialog = null
            nav.appNavigate(AppNav.Profile(person.id!!))
        }
    }

    if (showSharedGroupsDialog.isNotEmpty()) {
        ChooseGroupDialog(
            {
                showSharedGroupsDialog = emptyList()
            },
            title = showSharedGroupsDialogPerson?.name ?: stringResource(R.string.someone),
            multiple = false,
            allowNone = true,
            actions = {
                IconButton(
                    {
                        showSharedGroupsDialog = emptyList()
                        nav.appNavigate(AppNav.Profile(showSharedGroupsDialogPerson!!.id!!))
                    }
                ) {
                    Icon(Icons.Outlined.Person, stringResource(R.string.view_profile))
                }
            },
            confirmFormatter = { stringResource(R.string.close) },
            infoFormatter = { "${it.members?.size ?: 0} ${context.resources.getQuantityString(R.plurals.inline_members, it.members?.size ?: 0)}" },
            groups = {
                showSharedGroupsDialog
            }
        ) { selected ->
            showSharedGroupsDialog = emptyList()
            nav.appNavigate(AppNav.Group(selected.first().id!!))
        }
    }

    if (selectedHiddenGroups.isNotEmpty()) {
        AlertDialog(
            {
                selectedHiddenGroups = emptyList()
            },
            title = {
                Text(pluralStringResource(R.plurals.x_groups, selectedHiddenGroups.size, selectedHiddenGroups.size))
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(1.pad)) {
                    TextButton(
                        {
                            selectedHiddenGroups = emptyList()
                        }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(
                        {
                            scope.launch {
                                coroutineScope {
                                    allHiddenGroups
                                        .filter { group -> selectedHiddenGroups.any { it.id == group.group?.id } }
                                        .mapNotNull { groupExtended ->
                                            val member =
                                                groupExtended.members?.firstOrNull { it.person?.id == me?.id }?.member
                                                    ?: return@mapNotNull null

                                            async {
                                                api.removeMember(member.id!!)
                                            }
                                        }.awaitAll()
                                    reloadFlow.emit(true)
                                    selectedHiddenGroups = emptyList()
                                }
                            }
                        }
                    ) {
                        Text(stringResource(R.string.leave), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    {
                        scope.launch {
                            coroutineScope {
                                allHiddenGroups
                                    .filter { group -> selectedHiddenGroups.any { it.id == group.group?.id } }
                                    .mapNotNull { groupExtended ->
                                        val member =
                                            groupExtended.members?.firstOrNull { it.person?.id == me?.id }?.member
                                                ?: return@mapNotNull null

                                        async {
                                            api.updateMember(
                                                member.id!!,
                                                Member(
                                                    hide = false
                                                )
                                            )
                                        }
                                    }.awaitAll()
                                reloadFlow.emit(true)
                                selectedHiddenGroups = emptyList()
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.show))
                }
            })
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppHeader(
            stringResource(R.string.groups),
            {
                scope.launch {
                    state.scrollToTop()
                }
            }
        ) {
            var showMenu by rememberStateOf(false)
            IconButton(
                {
                    showMenu = true
                }
            ) {
                Icon(Icons.Outlined.MoreVert, null)
                Dropdown(showMenu, { showMenu = false }) {
                    DropdownMenuItem({
                        Text(stringResource(R.string.hidden_groups))
                    }, {
                        showMenu = false
                        showHiddenGroupsDialog = true
                    })
                }
            }
            ScanQrCodeButton()
        }
        MainTabs(
            tab,
            {
                setTab(it)
            },
            tabs = tabs
        )
        LocationScaffold(
            geo,
            locationSelector,
            enabled = tab == MainTab.Local,
            rationale = {
                // todo: translate
                DisplayText("Join and host groups in your town.")
            }
        ) {
            var h by rememberStateOf(80.dp.px)

            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .swipeMainTabs {
                        when (val it = tabs.swipe(tab, it)) {
                            is SwipeResult.Previous -> {
                                nav.appNavigate(AppNav.Schedule)
                            }

                            is SwipeResult.Next -> {
                                nav.appNavigate(AppNav.Stories)
                            }

                            is SwipeResult.Select<*> -> {
                                setTab(it.item as MainTab)
                            }
                        }
                    }
            ) {
                LazyColumn(
                    state = state,
                    contentPadding = PaddingValues(
                        1.pad,
                        1.pad,
                        1.pad,
                        3.pad + h.inDp()
                    ),
                    verticalArrangement = Arrangement.spacedBy(1.pad),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.then(
                        if (tab == MainTab.Local) {
                            Modifier.widthIn(max = 640.dp)
                        } else {
                            Modifier
                        }
                    ).fillMaxSize()
                ) {
                    if (isLoading) {
                        item {
                            Loading()
                        }
                    } else if (results.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(
                                    if (searchText.isBlank()) when (tab) {
                                        MainTab.Friends -> R.string.you_have_no_groups
                                        else -> R.string.no_groups_nearby
                                    } else R.string.no_groups_to_show
                                ),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(2.pad)
                            )

                            if (tab == MainTab.Friends) {
                                var localCategories by mutableStateOf(emptyList<String>())
                                LaunchedEffect(geo) {
                                    if (geo != null) {
                                        api.exploreGroups(
                                            geo = geo!!.toGeo(),
                                            public = true,
                                            onError = {}
                                        ) {
                                            localCategories = it.mapNotNull { it.group?.categories }.flatten().distinct()
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        setTab(MainTab.Local)
                                    }
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(1.pad),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Outlined.Search, null)
                                        Column {
                                            Text(
                                                text = stringResource(R.string.explore_local_groups),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            localCategories.notEmpty?.let {
                                                Text(
                                                    text = it.joinToString(),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    NotificationsDisabledBanner(show = tab == MainTab.Friends)
                                    AnimatedVisibility(tab == MainTab.Friends && searchText.isBlank() && selectedCategory == null) {
                                        Friends(
                                            people = remember(allGroups) {
                                                allGroups.people().sortedByDescending { it.id == me?.id }
                                            },
                                            statuses = statuses,
                                            title = {
                                                if (it.id == me?.id) {
                                                    context.getString(R.string.set_status)
                                                } else {
                                                    null
                                                }
                                            },
                                            onLongClick = {
                                                nav.appNavigate(AppNav.Profile(it.id!!))
                                            }
                                        ) { person ->
                                            if (person.id == me?.id) {
                                                myStatusDialog = true
                                            } else {
                                                onFriendClick(person)
                                            }
                                        }
                                    }
                                }
                            }

                        items(
                            items = results,
                            key = {
                                when (it) {
                                    is SearchResult.Connect -> "connect:${it.person.id}"
                                    is SearchResult.Group -> "group:${it.groupExtended.group!!.id!!}"
                                }
                            }
                        ) {
                            ContactItem(
                                item = it,
                                onChange = {
                                    scope.launch {
                                        reloadFlow.emit(true)
                                    }
                                },
                                info = when (tab) {
                                    MainTab.Friends -> GroupInfo.LatestMessage
                                    else -> GroupInfo.Members
                                },
                                coverPhoto = tab == MainTab.Local
                            )
                        }
                    }
                }

                PageInput(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onPlaced {
                            h = it.size.height
                        }
                ) {
                    SearchContent(
                        locationSelector = locationSelector,
                        isLoading = isLoading,
                        categories = categories,
                        category = selectedCategory
                    ) {
                        selectedCategory = it
                    }
                    SearchFieldAndAction(
                        value = searchText,
                        valueChange = { searchText = it },
                        placeholder = stringResource(R.string.search_people_and_groups),
                        action = {
                            Icon(Icons.Outlined.Add, stringResource(R.string.create_group))
                        },
                        onAction = {
                            createGroupName = searchText
                            showCreateGroupName = true
                        },
                    )
                }
            }
        }
    }

    if (showHiddenGroupsDialog) {
        var groups by rememberStateOf(listOf<GroupExtended>())
        ChooseGroupDialog(
            {
                showHiddenGroupsDialog = false
            },
            title = stringResource(R.string.hidden_groups),
            confirmFormatter = { stringResource(R.string.next) },
            infoFormatter = { it.seenText(context.getString(R.string.active), me) },
            groups = {
                api.hiddenGroups {
                    groups = it
                }
                allHiddenGroups = groups
                groups
            }
        ) { selected ->
            selectedHiddenGroups = selected
        }
    }

    if (showCreateGroupName) {
        TextFieldDialog(
            onDismissRequest = { showCreateGroupName = false },
            title = stringResource(R.string.group_name),
            button = stringResource(R.string.next),
            singleLine = true,
            initialValue = createGroupName,
            placeholder = stringResource(R.string.empty_group_name),
            requireModification = false
        ) { value ->
            createGroupName = value
            showCreateGroupName = false
            showCreateGroupMembers = true
        }
    }

    if (showCreateGroupMembers) {
        val someone = stringResource(R.string.someone)
        ChoosePeopleDialog(
            {
                showCreateGroupMembers = false
            },
            title = stringResource(R.string.invite_people),
            confirmFormatter = defaultConfirmFormatter(
                R.string.skip,
                R.string.new_group_with_person,
                R.string.new_group_with_people,
                R.string.new_group_with_x_people
            ) { it.name ?: someone },
            allowNone = true,
            onPeopleSelected = { people ->
                api.createGroup(people.map { it.id!! }) { group ->
                    if (createGroupName.isNotBlank()) {
                        api.updateGroup(group.id!!, Group(name = createGroupName))
                    }
                    nav.appNavigate(AppNav.Group(group.id!!))
                }
            },
            omit = { it.id == me?.id }
        )
    }

    if (myStatusDialog) {
        EditStatusDialog(
            onDismissRequest = { myStatusDialog = false },
            initialStatus = statuses[me!!.id!!],
            recentStatuses = recentStatuses
        ) {
            myStatusDialog = false
            scope.launch {
                reloadStatuses()
            }
        }
    }
}

fun GroupExtended.seenText(active: String, me: Person?): String? {
    val otherMemberLastSeen = members?.filter { it.person?.id != me?.id }?.maxByOrNull {
        it.person?.seen ?: Instant.fromEpochMilliseconds(0)
    }?.person?.seen
    return otherMemberLastSeen?.timeAgo()?.lowercase()?.let { timeAgo ->
        "$active $timeAgo"
    }
}
