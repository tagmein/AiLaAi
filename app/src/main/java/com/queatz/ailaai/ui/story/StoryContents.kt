package com.queatz.ailaai.ui.story

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.ailaai.api.card
import app.ailaai.api.group
import coil.compose.AsyncImage
import com.queatz.ailaai.R
import com.queatz.ailaai.data.api
import com.queatz.ailaai.extensions.*
import com.queatz.ailaai.ui.components.*
import com.queatz.ailaai.ui.screens.exploreInitialCategory
import com.queatz.ailaai.ui.theme.PaddingDefault
import com.queatz.db.Card
import com.queatz.db.GroupExtended
import com.queatz.db.Person

@Composable
fun StoryContents(
    source: StorySource?,
    content: List<StoryContent>,
    state: LazyGridState,
    navController: NavController,
    me: () -> Person?,
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = 0.dp,
    fade: Boolean = false,
    actions: (@Composable (storyId: String) -> Unit)? = null
) {
    var viewHeight by rememberStateOf(Float.MAX_VALUE)
    var showOpenWidgetDialog by rememberStateOf(false)
    var size by rememberStateOf(Size.Zero)

    if (showOpenWidgetDialog) {
        val context = LocalContext.current
        AlertDialog(
            {
                showOpenWidgetDialog = false
            },
            text = {
               Text("Widgets are currently only interactable on web.")
            },
            dismissButton = {
                TextButton(
                    {
                        showOpenWidgetDialog = false
                    }
                ) {
                    Text(stringResource(R.string.close))
                }
            },
            confirmButton = {
                TextButton(
                    {
                        showOpenWidgetDialog = false
                        Card().apply { id = (source as StorySource.Card).id }.url.launchUrl(context)
                    }
                ) {
                    Text(stringResource(R.string.open_card))
                }
            }
        )
    }

    SelectionContainer(modifier = modifier) {
        LazyVerticalGrid(
            state = state,
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                PaddingDefault * 2,
                0.dp,
                PaddingDefault * 2,
                PaddingDefault * 2 + bottomContentPadding
            ),
            horizontalArrangement = Arrangement.spacedBy(PaddingDefault, Alignment.Start),
            verticalArrangement = Arrangement.spacedBy(PaddingDefault, Alignment.Top),
            modifier = Modifier
                .onPlaced {
                    size = it.boundsInParent().size
                    viewHeight = it.boundsInParent().height
                }
                .let {
                    if (fade) {
                        it.fadingEdge(size, state, 12f)
                    } else {
                        it
                    }
                }
        ) {
            content.forEach { content ->
                when (content) {
                    is StoryContent.Divider -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            DisableSelection {
                                Icon(
                                    Icons.Outlined.Flare,
                                    null,
                                    modifier = Modifier.padding(PaddingDefault * 2)
                                )
                            }
                        }
                    }

                    is StoryContent.Title -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(PaddingDefault),
                            ) {
                                Text(
                                    content.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .let {
                                            if (actions == null) {
                                                it
                                            } else {
                                                it.padding(top = PaddingDefault)
                                                    .clickable(
                                                        remember { MutableInteractionSource() },
                                                        indication = null
                                                    ) {
                                                        navController.navigate("story/${content.id}")
                                                    }
                                            }
                                        }
                                )
                                // https://issuetracker.google.com/issues/300781578
                                DisableSelection {
                                    actions?.invoke(content.id)
                                }
                            }
                        }
                    }

                    is StoryContent.Section -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                content.section,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }

                    is StoryContent.Text -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LinkifyText(
                                content.text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }

                    is StoryContent.Authors -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            StoryAuthors(
                                navController,
                                content.publishDate,
                                content.authors
                            )
                        }
                    }

                    is StoryContent.Groups -> {
                        items(content.groups, span = { GridItemSpan(maxLineSpan) }) { groupId ->
                            DisableSelection {
                                var group by remember { mutableStateOf<GroupExtended?>(null) }

                                LaunchedEffect(groupId) {
                                    api.group(groupId) { group = it }
                                }

                                LoadingText(group != null, stringResource(R.string.loading_group)) {
                                    ContactItem(
                                        navController,
                                        SearchResult.Group(group!!),
                                        me(),
                                        onChange = {},
                                        info = GroupInfo.LatestMessage
                                    )
                                }
                            }
                        }
                    }

                    is StoryContent.Cards -> {
                        items(content.cards) {
                            DisableSelection {
                                var card by remember { mutableStateOf<Card?>(null) }
                                LaunchedEffect(Unit) {
                                    api.card(it) { card = it }
                                }
                                CardItem(
                                    {
                                        navController.navigate("card/$it")
                                    },
                                    onCategoryClick = {
                                        exploreInitialCategory = it
                                        navController.navigate("explore")
                                    },
                                    navController = navController,
                                    card = card,
                                    isChoosing = true,
                                    modifier = Modifier
                                        .fillMaxWidth(.75f)
                                        .heightIn(max = viewHeight.inDp())
                                )
                            }
                        }
                    }

                    is StoryContent.Photos -> {
                        itemsIndexed(
                            content.photos,
                            span = { index, item ->
                                GridItemSpan(if (index == 0) maxLineSpan else if (index % 3 == 1) 1 else maxCurrentLineSpan)
                            }
                        ) { index, it ->
                            DisableSelection {
                                AsyncImage(
                                    model = api.url(it),
                                    contentDescription = "",
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.large)
                                        .fillMaxWidth()
                                        .aspectRatio(content.aspect)
                                        .heightIn(min = 240.dp.coerceAtMost(viewHeight.inDp()), max = viewHeight.inDp())
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                )
                            }
                        }
                    }

                    is StoryContent.Audio -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            DisableSelection {
                                Card(
                                    shape = MaterialTheme.shapes.large,
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.large)
                                ) {
                                    Audio(
                                        api.url(content.audio),
                                        modifier = Modifier
                                            .fillMaxSize()
                                    )
                                }
                            }
                        }
                    }

                    is StoryContent.Widget -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            DisableSelection {
                                Stub(content.widget.stringResource) {
                                    if (source is StorySource.Card) {
                                        showOpenWidgetDialog = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
