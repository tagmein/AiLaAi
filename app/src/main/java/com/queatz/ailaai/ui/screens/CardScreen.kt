package com.queatz.ailaai.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.queatz.ailaai.*
import com.queatz.ailaai.R
import com.queatz.ailaai.ui.components.BasicCard
import com.queatz.ailaai.ui.theme.ElevationDefault
import com.queatz.ailaai.ui.theme.PaddingDefault
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardScreen(navBackStackEntry: NavBackStackEntry, navController: NavController, me: () -> Person?) {
    val cardId = navBackStackEntry.arguments!!.getString("id")!!
    var isLoading by remember { mutableStateOf(false) }
    var card by remember { mutableStateOf<Card?>(null) }
    var cards by remember { mutableStateOf(emptyList<Card>()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(true) {
        isLoading = true

        try {
            card = api.card(cardId)
            cards = listOf(card!!)
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxSize()
    ) {
        SmallTopAppBar(
            {
                Column {
                    Text(card?.name ?: "")

                    card?.location?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton({
                    navController.popBackStack()
                }) {
                    Icon(Icons.Outlined.ArrowBack, Icons.Outlined.ArrowBack.name)
                }
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.shadow(ElevationDefault / 2).zIndex(1f)
        )

        LazyColumn(
            contentPadding = PaddingValues(PaddingDefault),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PaddingDefault, Alignment.Top),
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLoading) {
                item {
                    LinearProgressIndicator(
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PaddingDefault)
                    )
                }
            } else {
                if (cards.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.no_cards_to_show),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(PaddingDefault * 2)
                        )
                    }
                } else {
                    items(cards, { it.id!! }) {
                        BasicCard(
                            {
                                navController.navigate("card/${it.id!!}")
                            },
                            {
                                coroutineScope.launch {
                                    try {
                                        navController.navigate("group/${api.cardGroup(it.id!!).id!!}")
                                    } catch (ex: Exception) {
                                        ex.printStackTrace()
                                    }
                                }
                            },
                            activity = navController.context as Activity,
                            card = it
                        )
                    }
                }
            }
        }
    }
}
