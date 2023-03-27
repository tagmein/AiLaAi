package com.queatz.ailaai.ui.dialogs

import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.queatz.ailaai.*
import com.queatz.ailaai.R


@Composable
fun ChoosePeopleDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmFormatter: @Composable (List<Person>) -> String,
    onPeopleSelected: suspend (List<Person>) -> Unit,
    omit: (Person) -> Boolean = { false }
) {

    var isLoading by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var allGroups by remember { mutableStateOf(listOf<GroupExtended>()) }
    var people by remember { mutableStateOf(listOf<Person>()) }
    var selected by remember { mutableStateOf(listOf<Person>()) }

    LaunchedEffect(true) {
        isLoading = true
        try {
            allGroups = api.groups()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        isLoading = false
    }

    LaunchedEffect(allGroups, selected, searchText) {
        val allPeople = allGroups
            .flatMap { it.members!!.map { it.person!! } }
            .distinctBy { it.id!! }
            .filter { it.source != PersonSource.Web }
            .filter { !omit(it) }
        people = (if (searchText.isBlank()) allPeople else allPeople.filter {
            it.name?.contains(searchText, true) ?: false
        })
    }

    ChooseDialog(
        onDismissRequest = onDismissRequest,
        isLoading = isLoading,
        title = title,
        photoFormatter = { listOf(it.photo ?: "") },
        nameFormatter = { it.name ?: stringResource(R.string.someone) },
        confirmFormatter = confirmFormatter,
        textWhenEmpty = { stringResource(R.string.no_people_to_show) },
        searchText = searchText,
        searchTextChange = { searchText = it },
        items = people,
        key = { it.id!! },
        selected = selected,
        onSelectedChange = { selected = it },
        onConfirm = onPeopleSelected
    )
}
