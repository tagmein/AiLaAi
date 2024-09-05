package com.queatz.ailaai.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import app.ailaai.api.myCards
import com.queatz.ailaai.AppNav
import com.queatz.ailaai.R
import com.queatz.ailaai.data.api
import com.queatz.ailaai.dataStore
import com.queatz.ailaai.extensions.*
import com.queatz.ailaai.nav
import com.queatz.ailaai.ui.components.*
import com.queatz.ailaai.ui.dialogs.EditCardDialog
import com.queatz.ailaai.ui.theme.elevation
import com.queatz.ailaai.ui.theme.pad
import com.queatz.db.Card
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class Filter {
    Active,
    NotActive
}

private val meParentTypeKey = stringPreferencesKey("me.parentType")
private val meFiltersKey = stringSetPreferencesKey("me.filters")

@Composable
fun MeScreen() {
    var myCards by remember { mutableStateOf(emptyList<Card>()) }
    var isLoading by rememberStateOf(true)
    val scope = rememberCoroutineScope()
    val state = rememberLazyGridState()
    var cardParentType by rememberSaveable { mutableStateOf<CardParentType?>(null) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var filters by rememberSaveable { mutableStateOf(emptySet<Filter>()) }
    val context = LocalContext.current
    var playingVideo by remember { mutableStateOf<Card?>(null) }
    val nav = nav

    LaunchedEffect(Unit) {
        context.dataStore.data.first().let {
            it[meParentTypeKey]?.let { parentType ->
                cardParentType = CardParentType.valueOf(parentType)
            }
            it[meFiltersKey]?.let { previousFilters ->
                filters = previousFilters.map { Filter.valueOf(it) }.toSet()
            }
        }
    }

    fun setParentType(newCardParentType: CardParentType?) {
        cardParentType = newCardParentType
        scope.launch {
            context.dataStore.edit {
                if (newCardParentType == null) {
                    it.remove(meParentTypeKey)
                } else {
                    it[meParentTypeKey] = newCardParentType.name
                }
            }
        }
    }

    fun setFilters(newFilters: Set<Filter>) {
        filters = newFilters
        scope.launch {
            context.dataStore.edit {
                if (cardParentType == null) {
                    it.remove(meFiltersKey)
                } else {
                    it[meFiltersKey] = newFilters.map { it.name }.toSet()
                }
            }
        }
    }

    suspend fun reload() {
        api.myCards {
            myCards = it
        }
    }

    LaunchedEffect(Unit) {
        reload()
        isLoading = false
    }

    val cards = remember(myCards, cardParentType, filters, searchText) {
        when (cardParentType) {
            CardParentType.Person -> myCards.filter { it.parent == null && it.equipped.isTrue }
            CardParentType.Map -> myCards.filter { it.parent == null && it.equipped.isFalse && it.geo != null && it.offline.isFalse }
            CardParentType.Card -> myCards.filter { it.parent != null }
            CardParentType.Group -> myCards.filter { it.group != null }
            CardParentType.Offline -> myCards.filter { it.offline == true }
            else -> myCards
        }.filter {
            val searchTextTrimmed = searchText.trim()
            filters.all { filter ->
                when (filter) {
                    Filter.Active -> it.active == true
                    Filter.NotActive -> it.active != true
                }
            } &&
                    (searchTextTrimmed.isBlank() || (
                            it.conversation?.contains(searchTextTrimmed, true) == true ||
                                    it.name?.contains(searchTextTrimmed, true) == true ||
                                    it.location?.contains(searchTextTrimmed, true) == true
                            ))
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
            nav.appNavigate(AppNav.Page(it.id!!))
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AppHeader(
            stringResource(
                when (cardParentType) {
                    CardParentType.Map -> R.string.at_a_location
                    CardParentType.Card -> R.string.inside_another_card
                    CardParentType.Group -> R.string.in_a_group
                    CardParentType.Person -> R.string.on_profile
                    CardParentType.Offline -> R.string.none
                    else -> R.string.your_cards
                }
            ) + (if (isLoading) "" else " (${cards.size})"),
            {
                scope.launch {
                    state.scrollToTop()
                }
            }
        )
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (isLoading) {
                Loading()
            } else {
                val autoplayIndex by state.rememberAutoplayIndex()
                LaunchedEffect(autoplayIndex) {
                    playingVideo = cards.getOrNull(autoplayIndex)
                }
                LazyVerticalGrid(
                    state = state,
                    contentPadding = PaddingValues(
                        1.pad,
                        1.pad,
                        1.pad,
                        1.pad + 80.dp + 58.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(1.pad, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(1.pad, Alignment.Top),
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Adaptive(240.dp)
                ) {
                    items(cards, key = { it.id!! }) { card ->
                        CardLayout(
                            card = card,
                            showTitle = true,
                            onClick = {
                                nav.appNavigate(AppNav.Page(card.id!!))
                            },
                            scope = scope,
                            playVideo = playingVideo == card
                        )
                    }

                    if (cards.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                stringResource(if (cardParentType == null && filters.isEmpty() && searchText.isBlank()) R.string.you_have_no_cards else R.string.no_cards_to_show),
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(2.pad)
                            )
                        }
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.pad),
                modifier = Modifier
                    .padding(vertical = 2.pad)
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
            ) {
                var viewport by remember { mutableStateOf(Size(0f, 0f)) }
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .onPlaced { viewport = it.boundsInParent().size }
                        .padding(horizontal = 1.pad)
                ) {
                    CardParentSelector(
                        cardParentType,
                        modifier = Modifier
                            .wrapContentWidth(unbounded = true)
                            .padding(horizontal = 1.pad)
                            .padding(bottom = .5f.pad),
                        showOffline = true
                    ) {
                        setParentType(if (it == cardParentType) null else it)
                    }
                    Button(
                        {
                            setFilters(filters.minus(Filter.NotActive).toggle(Filter.Active))
                        },
                              elevation = ButtonDefaults.elevatedButtonElevation(.5f.elevation),
                        colors = if (!filters.contains(Filter.Active)) ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ) else ButtonDefaults.buttonColors(),
                        modifier = Modifier.padding(end = 1.pad)
                    ) {
                        Text(stringResource(R.string.posted))
                    }
                    Button(
                        {
                            setFilters(filters.minus(Filter.Active).toggle(Filter.NotActive))
                        },
                               elevation = ButtonDefaults.elevatedButtonElevation(.5f.elevation),
                        colors = if (!filters.contains(Filter.NotActive)) ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ) else ButtonDefaults.buttonColors(),
                        modifier = Modifier.padding(end = 1.pad)
                    ) {
                        Text(stringResource(R.string.not_posted))
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 2.pad)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentWidth()
                    ) {
                        SearchField(
                            searchText,
                            { searchText = it }
                        )
                    }
                    FloatingActionButton(
                        onClick = {
                            newCard = Card()
                        },
                        modifier = Modifier
                            .padding(start = 2.pad)
                    ) {
                        Icon(Icons.Outlined.Add, stringResource(R.string.add_a_card))
                    }
                }
            }
        }
    }
}
