package com.queatz.ailaai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import at.bluesource.choicesdk.maps.common.LatLng
import com.queatz.ailaai.AppNav
import com.queatz.ailaai.R
import com.queatz.ailaai.extensions.appNavigate
import com.queatz.ailaai.extensions.inDp
import com.queatz.ailaai.extensions.rememberAutoplayIndex
import com.queatz.ailaai.nav
import com.queatz.ailaai.ui.theme.pad
import com.queatz.db.Card
import kotlinx.coroutines.launch

@Composable
fun CardList(
    cards: List<Card>,
    isMine: (Card) -> Boolean,
    geo: LatLng?,
    isLoading: Boolean,
    isError: Boolean,
    value: String,
    valueChange: (String) -> Unit,
    onChanged: () -> Unit = {},
    state: LazyGridState = rememberLazyGridState(),
    placeholder: String = stringResource(R.string.search),
    modifier: Modifier = Modifier,
    hasMore: Boolean = false,
    onLoadMore: (suspend () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    onAction: (() -> Unit)? = null,
    aboveSearchFieldContent: @Composable () -> Unit = {}
) {
    var viewport by remember { mutableStateOf(Size(0f, 0f)) }
    var playingVideo by remember { mutableStateOf<Card?>(null) }
    val scope = rememberCoroutineScope()
    val nav = nav

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier.fillMaxSize()
    ) {
        if (isLoading) {
            Loading()
        } else if (isError || cards.isEmpty()) {
            Text(
                stringResource(if (isError) R.string.didnt_work else R.string.no_cards_to_show),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(2.pad)
            )
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
                    3.5f.pad + viewport.height.inDp()
                ),
                horizontalArrangement = Arrangement.spacedBy(1.pad, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(1.pad, Alignment.Top),
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(240.dp)
            ) {
                @Composable
                fun basicCard(it: Card) {
                    CardLayout(
                        card = it,
                        showTitle = true,
                        showDistance = geo,
                        onClick = {
                            nav.appNavigate(AppNav.Page(it.id!!))
                        },
                        scope = scope,
                        playVideo = playingVideo == it,
                    )
                }

                val nearbyCards = if (geo != null) cards.takeWhile {
                    it.geo != null
                } else emptyList()

                val remainingCards = cards.drop(nearbyCards.size)

                items(items = nearbyCards, key = { it.id!! }) {
                    basicCard(it)
                }
                if (remainingCards.isNotEmpty()) {
                    items(items = remainingCards, key = { it.id!! }) {
                        basicCard(it)
                    }
                }
                if (onLoadMore != null && cards.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LoadMore(
                            hasMore && cards.isNotEmpty()
                        ) {
                            scope.launch {
                                onLoadMore()
                            }
                        }
                    }
                }
            }
        }
        PageInput(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onPlaced { viewport = it.boundsInParent().size }
        ) {
            aboveSearchFieldContent()
            SearchFieldAndAction(value,
                valueChange = valueChange,
                placeholder = placeholder,
                showClear = true,
                action = action,
                onAction = onAction
            )
        }
    }
}
