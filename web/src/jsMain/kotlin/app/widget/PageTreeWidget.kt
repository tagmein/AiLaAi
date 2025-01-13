package app.widget

import Styles
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import api
import app.ailaai.api.card
import app.ailaai.api.cardsCards
import app.ailaai.api.newCard
import app.ailaai.api.updateCard
import app.cards.NewCardInput
import app.components.Empty
import app.dialog.inputDialog
import app.dialog.inputSelectDialog
import app.nav.NavSearchInput
import app.softwork.routingcompose.Router
import appString
import application
import com.queatz.db.Card
import com.queatz.db.Widget
import com.queatz.widgets.widgets.PageTreeData
import components.Icon
import components.getConversation
import isMine
import json
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import lib.toLocaleString
import notBlank
import notEmpty
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.CSSSizeValue
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.FlexWrap
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.flexGrow
import org.jetbrains.compose.web.css.flexWrap
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.opacity
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import r
import updateWidget
import widget
import kotlin.random.Random

sealed class TagFilter {
    data class Tag(val tag: String) : TagFilter()
    data object Untagged : TagFilter()
}

internal fun tagColor(tag: String): CSSColorValue {
    val hue = Random(tag.hashCode()).nextInt(360)
    return Color("hsl($hue, 60%, 40%)")
}

@Composable
fun PageTreeWidget(widgetId: String) {
    val me by application.me.collectAsState()
    val scope = rememberCoroutineScope()
    val router = Router.current
    var widget by remember(widgetId) {
        mutableStateOf<Widget?>(null)
    }
    var isMine by remember(widgetId) {
        mutableStateOf(false)
    }
    var cards by remember(widgetId) {
        mutableStateOf<List<Card>>(emptyList())
    }
    var search by remember(widgetId) {
        mutableStateOf("")
    }
    var tagFilters by remember(widgetId) {
        mutableStateOf(emptySet<TagFilter>())
    }
    var stageFilters by remember(widgetId) {
        mutableStateOf(emptySet<TagFilter>())
    }
    var data by remember(widgetId) {
        mutableStateOf<PageTreeData?>(null)
    }
    val searchedCards = remember(cards, search) {
        if (search.isNotBlank()) {
            cards.filter {
                it.name?.contains(search, ignoreCase = true) == true
            }
        } else {
            cards
        }
    }
    val stageCount = remember(data, searchedCards) {
        data?.stages?.filterKeys { id ->
            searchedCards.any { it.id == id }
        }?.values?.groupingBy { it }?.eachCount()
    }
    val noStageCount = remember(searchedCards, data) {
        val stagedCards = data?.stages?.entries?.filter { it.value.isNotEmpty() }?.map { it.key } ?: emptyList()

        cards.count { card ->
            card.id !in stagedCards && searchedCards.any { card.id == it.id }
        }
    }
    val allStages = remember(data, searchedCards) {
        data?.stages?.filterKeys { id ->
            searchedCards.any { it.id == id }
        }?.values?.distinct()?.sorted()?.sortedDescending()
    }
    val stagedCards = remember(searchedCards, stageFilters) {
        if (search.isNotBlank()) {
            cards.filter {
                it.name?.contains(search, ignoreCase = true) == true
            }
        } else {
            cards
        }.let {
            val stages = stageFilters.map {
                when (it) {
                    is TagFilter.Tag -> it.tag
                    is TagFilter.Untagged -> null
                }
            }
            when {
                stages.isNotEmpty() -> {
                    it.filter {
                        data?.stages?.get(it.id!!) in stages
                    }
                }

                else -> {
                    it
                }
            }
        }
    }
    val tagCount = remember(data, stagedCards) {
        data?.tags?.filterKeys { id ->
            stagedCards.any { it.id == id }
        }?.values?.flatten()?.groupingBy { it }?.eachCount()
    }
    val noTagCount = remember(stagedCards, data) {
        val taggedCards = data?.tags?.entries?.filter { it.value.isNotEmpty() }?.map { it.key } ?: emptyList()

        stagedCards.count { card ->
            card.id !in taggedCards && stagedCards.any { card.id == it.id }
        }
    }
    val allTags = remember(data, stagedCards) {
        data?.tags?.filterKeys { id ->
            stagedCards.any { it.id == id }
        }?.values?.flatten()?.distinct()?.sorted()?.sortedByDescending { tagCount?.get(it) ?: 0 }
    }
    val shownCards = remember(stagedCards, tagFilters) {
        stagedCards.let {
            val tags = tagFilters.map {
                when (it) {
                    is TagFilter.Tag -> it.tag
                    is TagFilter.Untagged -> null
                }
            }
            when {
                tags.isNotEmpty() -> {
                    it.filter {
                        data?.tags?.get(it.id!!).orEmpty().let {
                            it.any { it in tags } || (it.isEmpty() && null in tags)
                        }
                    }
                }

                else -> {
                    it
                }
            }
        }
    }

    suspend fun reload() {
        api.cardsCards(data?.card ?: return) {
            cards = it
        }
    }

    suspend fun saveCard(cardId: String, card: Card) {
        api.updateCard(cardId, card) {
            reload()
        }
    }

    suspend fun saveConversation(card: Card, value: String) {
        val conversation = card.getConversation()
        conversation.message = value
        saveCard(card.id!!, Card(conversation = json.encodeToString(conversation)))
    }

    fun newSubCard(inCardId: String, name: String, active: Boolean) {
        scope.launch {
            api.newCard(Card(name = name, parent = inCardId, active = active)) {
                reload()
            }
        }
    }

    LaunchedEffect(widgetId, me, data) {
        api.card(data?.card ?: return@LaunchedEffect) {
            isMine = it.isMine(me?.id)
        }
    }

    LaunchedEffect(widgetId) {
        // todo loading
        api.widget(widgetId) {
            it.data ?: return@widget

            widget = it
            data = json.decodeFromString<PageTreeData>(it.data!!)

            reload()
        }
    }

    suspend fun save(widgetData: PageTreeData) {
        api.updateWidget(widgetId, Widget(data = json.encodeToString(widgetData))) {
            widget = it
            data = json.decodeFromString<PageTreeData>(it.data!!)
        }
    }

    fun removeTag(card: Card, tag: String) {
        scope.launch {
            api.updateWidget(
                widgetId,
                Widget(data = json.encodeToString(data!!.copy(tags = data!!.tags.toMutableMap().apply {
                    put(card.id!!, getOrElse(card.id!!) { emptyList() } - tag)
                })))
            ) {
                widget = it
                data = json.decodeFromString<PageTreeData>(it.data!!)
            }
        }
    }

    fun setStage(card: Card) {
        scope.launch {
            val stage = inputSelectDialog(
                // todo: translate
                confirmButton = "Update",
                items = allStages
            )

            if (stage != null) {
                api.updateWidget(
                    widgetId,
                    Widget(data = json.encodeToString(data!!.copy(stages = data!!.stages.toMutableMap().apply {
                        if (stage.isNotBlank()) {
                            put(card.id!!, stage.trim())
                        } else {
                            remove(card.id!!)
                        }
                    })))
                ) {
                    widget = it
                    data = json.decodeFromString<PageTreeData>(it.data!!)
                }
            }
        }
    }

    fun addTag(card: Card) {
        scope.launch {
            val tag = inputSelectDialog(
                // todo: translate
                confirmButton = "Add tag",
                items = allTags,
                itemStyle = { tag ->
                    backgroundColor(tagColor(tag))
                }
            )

            if (!tag.isNullOrBlank()) {
                api.updateWidget(
                    widgetId,
                    Widget(data = json.encodeToString(data!!.copy(tags = data!!.tags.toMutableMap().apply {
                        put(card.id!!, (getOrElse(card.id!!) { emptyList() } + tag.trim()).distinct())
                    })))
                ) {
                    widget = it
                    data = json.decodeFromString<PageTreeData>(it.data!!)
                }
            }
        }
    }

    Div(
        {
            classes(WidgetStyles.pageTree)
        }
    ) {
        if (isMine) {
            NewCardInput(defaultMargins = false) { name, active ->
                newSubCard(data?.card ?: return@NewCardInput, name, active)
            }
        }

        if (cards.size > 5) {
            NavSearchInput(
                search,
                { search = it },
                defaultMargins = false,
                autoFocus = false,
                styles = {
                    width(100.percent)
                    marginBottom(1.r)
                }
            )

            allStages?.notEmpty?.let { stages ->
                Div({
                    style {
                        marginTop(1.r)
                        display(DisplayStyle.Flex)
                        flexWrap(FlexWrap.Wrap)
                        gap(.5.r)
                    }
                }) {
                    stages.forEach { stage ->
                        val tags = stageFilters.filterIsInstance<TagFilter.Tag>().map { it.tag }

                        TagButton(
                            tag = stage,
                            // todo: translate
                            title = "Tap to filter",
                            selected = stage in tags,
                            outline = true,
                            count = stageCount?.get(stage)?.toString() ?: "",
                            onClick = { multiselect ->
                                if (!multiselect) {
                                    stageFilters = if (TagFilter.Tag(stage) in stageFilters) {
                                        emptySet()
                                    } else {
                                        setOf(TagFilter.Tag(stage))
                                    }
                                } else {
                                    if (stage in tags) {
                                        stageFilters -= TagFilter.Tag(stage)
                                    } else {
                                        stageFilters += TagFilter.Tag(stage)
                                    }
                                }
                            }
                        )
                    }
                    if (stages.isNotEmpty()) {
                        TagButton(
                            // todo: Translate
                            tag = "New",
                            count = noStageCount.toString(),
                            // todo: Translate
                            title = "Tap to filter",
                            selected = TagFilter.Untagged in stageFilters,
                            outline = true,
                            onClick = { multiselect ->
                                if (!multiselect) {
                                    stageFilters = if (TagFilter.Untagged in stageFilters) {
                                        emptySet()
                                    } else {
                                        setOf(TagFilter.Untagged)
                                    }
                                } else {
                                    stageFilters = if (TagFilter.Untagged in stageFilters) {
                                        stageFilters - TagFilter.Untagged
                                    } else {
                                        stageFilters + TagFilter.Untagged
                                    }
                                }
                            }
                        )
                    }
                }
            }

            allTags?.notEmpty?.let { tags ->
                Tags(
                    tags = tags,
                    selected = tagFilters,
                    marginTop = 0.r,
                    // todo: translate
                    title = "Tap to filter",
                    formatCount = { tag ->
                        if (tag == null) {
                            noTagCount.toString()
                        } else {
                            tagCount?.get(tag)?.toString() ?: ""
                        }
                    },
                    showNoTag = true,
                    onClick = { tag, multiselect ->
                        if (!multiselect) {
                            tagFilters = if (tag in tagFilters) {
                                emptySet()
                            } else {
                                setOf(tag)
                            }
                        } else {
                            if (tag in tagFilters) {
                                tagFilters -= tag
                            } else {
                                tagFilters += tag
                            }
                        }
                    }
                )
            }
        }

        if (search.isNotBlank() && shownCards.isEmpty()) {
            Empty {
                Text(appString { noCards })
            }
        }

        shownCards.sortedByDescending {
            data?.votes?.get(it.id!!) ?: 0
        }.forEach { card ->
            key(card.id!!) {
                val votes = data?.votes?.get(card.id!!) ?: 0
                Div({
                    classes(WidgetStyles.pageTreeItem)
                }) {
                    Div({
                        style {
                            textAlign("center")
                            marginRight(1.r)
                        }
                    }) {
                        if (me != null) {
                            Button({
                                classes(Styles.outlineButton)

                                title("+1 vote")

                                onClick {
                                    it.stopPropagation()

                                    scope.launch {
                                        save(
                                            data!!.copy(
                                                votes = data!!.votes.toMutableMap().apply {
                                                    put(card.id!!, (data!!.votes[card.id!!] ?: 0) + 1)
                                                }
                                            )
                                        )
                                    }
                                }
                            }) {
                                // todo: translate
                                Text("Vote")
                            }
                        }

                        Div({
                            style {
                                if (me != null) {
                                    cursor("pointer")
                                    marginTop(.5.r)
                                }
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Column)
                                alignItems(AlignItems.Center)
                            }

                            if (me != null) {
                                // todo: translate
                                title("Edit votes")

                                onClick {
                                    it.stopPropagation()

                                    scope.launch {
                                        val result = inputDialog(
                                            // todo: translate
                                            "Votes",
                                            confirmButton = application.appString { update },
                                            defaultValue = data!!.votes[card.id!!]?.toString() ?: "0"
                                        )

                                        result ?: return@launch

                                        save(
                                            data!!.copy(
                                                votes = data!!.votes.toMutableMap().apply {
                                                    put(card.id!!, result.toIntOrNull() ?: 0)
                                                }
                                            )
                                        )
                                    }
                                }
                            } else {
                                // todo: translate
                                title("Sign in to vote")
                            }
                        }) {
                            if (me != null) {
                                // todo: translate
                                Text("${votes.toLocaleString()} ${if (votes == 1) "vote" else "votes"}")
                            } else {
                                Div({
                                    style {
                                        fontSize(24.px)
                                        fontWeight("bold")
                                    }
                                }) {
                                    Text(votes.toLocaleString())
                                }
                                Text(if (votes == 1) "vote" else "votes")
                            }
                        }
                    }
                    Div({
                        style {
                            display(DisplayStyle.Flex)
                            marginRight(1.r)
                            textAlign("center")
                            justifyContent(JustifyContent.Center)
                            alignItems(AlignItems.Center)
                            if (me != null) {
                                cursor("pointer")
                            }
                        }

                        // todo: translate
                        title("Stage")

                        onClick {
                            if (me != null) {
                                setStage(card)
                            }
                        }
                    }) {
                        val stage = data?.stages?.get(card.id!!)
                        if (stage == null) {
                            // todo: translate
                            Span({
                                style {
                                    opacity(.5f)
                                }
                            }) { Text("New") }
                        } else {
                            Span {
                                Text(stage)
                            }
                        }
                    }
                    Div({
                        style {
                            cursor("pointer")
                            flexGrow(1)
                        }

                        if (card.person == me?.id) {
                            // todo: translate
                            title("Edit")
                        } else {
                            // todo: translate
                            title("Open page")
                        }

                        onClick { event ->
                            event.stopPropagation()

                            if (event.ctrlKey) {
                                window.open("/page/${card.id!!}", target = "_blank")
                            } else {
                                if (card.person == me?.id) {
                                    scope.launch {
                                        val result = inputDialog(
                                            title = application.appString { details },
                                            singleLine = false,
                                            confirmButton = application.appString { update },
                                            defaultValue = card.getConversation().message
                                        )

                                        if (result != null) {
                                            saveConversation(card, result)
                                        }
                                    }
                                } else {
                                    router.navigate("/page/${card.id!!}")
                                }
                            }
                        }
                    }) {
                        Div({
                            style {
                                fontWeight("bold")
                                fontSize(18.px)
                            }

                            onClick { event ->
                                event.stopPropagation()

                                if (card.person == me?.id) {
                                    scope.launch {
                                        val result = inputDialog(
                                            title = application.appString { title },
                                            singleLine = false,
                                            confirmButton = application.appString { update },
                                            defaultValue = card.name.orEmpty()
                                        )

                                        if (result != null) {
                                            saveCard(card.id!!, Card(name = result))
                                        }
                                    }
                                }
                            }
                        }) {
                            Text(card.name ?: "")
                        }

                        card.getConversation().message.notBlank?.let {
                            Div({
                                style {
                                    fontSize(16.px)
                                }
                            }) {
                                Text(it)
                            }
                        }

                        val tags = data?.tags?.get(card.id!!) ?: emptyList()

                        Tags(
                            tags = tags,
                            // todo: translate
                            title = if (me != null) "Tap to remove" else "",
                            onClick = { tag, _ ->
                                if (me != null) {
                                    removeTag(card, (tag as? TagFilter.Tag)?.tag ?: "")
                                }
                            }
                        ) {
                            if (me != null) {
                                Button(
                                    {
                                        classes(Styles.outlineButton)

                                        style {
                                            padding(0.r, 1.5.r)
                                            height(2.5.r)
                                        }

                                        // todo: translate
                                        title("Add tag")

                                        onClick {
                                            it.stopPropagation()
                                            addTag(card)
                                        }
                                    }
                                ) {
                                    Icon("new_label") {
                                        marginRight(0.r)
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

@Composable
fun Tags(
    tags: List<String>,
    selected: Set<TagFilter> = emptySet(),
    marginTop: CSSSizeValue<*> = 1.r,
    title: String,
    onClick: (tag: TagFilter, multiselect: Boolean) -> Unit,
    formatCount: ((tag: String?) -> String?)? = null,
    showNoTag: Boolean = false,
    content: @Composable () -> Unit = {},
) {
    Div({
        style {
            marginTop(marginTop)
            display(DisplayStyle.Flex)
            flexWrap(FlexWrap.Wrap)
            gap(.5.r)
        }
    }) {
        tags.forEach { tag ->
            TagButton(
                tag = tag,
                title = title,
                selected = tag in selected.filterIsInstance<TagFilter.Tag>().map { it.tag },
                count = formatCount?.invoke(tag),
                onClick = { multiselect ->
                    onClick(TagFilter.Tag(tag), multiselect)
                }
            )
        }

        if (tags.isNotEmpty() && showNoTag) {
            TagButton(
                // todo: Translate
                tag = "No tag",
                count = formatCount?.invoke(null),
                title = title,
                selected = TagFilter.Untagged in selected,
                outline = true,
                onClick = {
                    onClick(TagFilter.Untagged, it)
                }
            )
        }

        content()
    }
}

@Composable
fun TagButton(
    tag: String,
    title: String,
    selected: Boolean,
    count: String? = null,
    outline: Boolean = false,
    onClick: (multiselect: Boolean) -> Unit,
) {
    Button(
        {
            classes(if (outline) Styles.outlineButton else Styles.button)

            if (selected) {
                classes(Styles.buttonSelected)
            }

            style {
                height(2.5.r)
                padding(0.r, 1.5.r)

                if (!outline) {
                    color(Color.white)
                    backgroundColor(tagColor(tag))
                }
            }

            title(title)

            onClick {
                it.stopPropagation()
                onClick(it.ctrlKey)
            }
        }
    ) {
        Text(tag)

        count?.let {
            Span({
                style {
                    fontWeight("normal")
                    paddingLeft(.25.r)
                }
            }) { Text(it) }
        }
    }
}
