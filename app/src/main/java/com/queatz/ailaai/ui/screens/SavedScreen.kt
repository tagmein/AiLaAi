package com.queatz.ailaai.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.queatz.ailaai.Card
import com.queatz.ailaai.Person
import com.queatz.ailaai.R
import com.queatz.ailaai.api
import com.queatz.ailaai.saves
import com.queatz.ailaai.ui.components.AppHeader
import com.queatz.ailaai.ui.components.CardsList
import com.queatz.ailaai.ui.state.jsonSaver
import io.ktor.utils.io.*

@Composable
fun SavedScreen(navController: NavController, me: () -> Person?) {
    var value by rememberSaveable { mutableStateOf("") }
    var cards by rememberSaveable(stateSaver = jsonSaver<List<Card>>()) { mutableStateOf(listOf()) }
    var isLoading by remember { mutableStateOf(cards.isEmpty()) }
    var isError by remember { mutableStateOf(false) }
    var hasInitialCards by remember { mutableStateOf(cards.isNotEmpty()) }

    LaunchedEffect(Unit) {
        saves.reload()
    }

    LaunchedEffect(value) {
        if (hasInitialCards) {
            hasInitialCards = false

            if (cards.isNotEmpty()) {
                return@LaunchedEffect
            }
        }
        try {
            isLoading = true
            cards = api.savedCards(value.takeIf { it.isNotBlank() }).mapNotNull { it.card }
            isError = false
            isLoading = false
        } catch (ex: Exception) {
            if (ex is CancellationException || ex is InterruptedException) {
                // Ignore, probably geo or search value changed
            } else {
                isLoading = true
                isError = true
                ex.printStackTrace()
            }
        }
    }

    Column {
        AppHeader(
            navController,
            if (isLoading) {
                stringResource(R.string.saved)
            } else {
                "${stringResource(R.string.saved)} (${cards.size})"
            },
            {
                // todo scroll to top
            },
            me
        )
        CardsList(
            cards = cards,
            isMine = { it.person == me()?.id },
            geo = null,
            isLoading = isLoading,
            isError = isError,
            value = value,
            valueChange = { value = it },
            navController = navController
        )
    }
}
