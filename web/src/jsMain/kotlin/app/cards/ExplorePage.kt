package app.cards

import LocalConfiguration
import Styles
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import api
import app.AppStyles
import app.PageTopBar
import app.ailaai.api.card
import app.ailaai.api.cardsCards
import app.ailaai.api.deleteCard
import app.ailaai.api.generateCardPhoto
import app.ailaai.api.groups
import app.ailaai.api.myCollaborations
import app.ailaai.api.newCard
import app.ailaai.api.updateCard
import app.ailaai.api.uploadCardPhoto
import app.components.EditField
import app.dialog.dialog
import app.dialog.inputDialog
import app.dialog.searchDialog
import app.group.GroupInfo
import app.group.GroupItem
import app.menu.InlineMenu
import app.menu.Menu
import appString
import application
import com.queatz.db.Card
import com.queatz.db.GroupExtended
import com.queatz.db.Pay
import com.queatz.db.PayFrequency
import components.CardItem
import components.CardPhotoOrVideo
import components.IconButton
import components.Loading
import components.Switch
import components.getConversation
import focusable
import hint
import json
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import notBlank
import org.jetbrains.compose.web.attributes.autoFocus
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.fontWeight
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.marginBottom
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.maxHeight
import org.jetbrains.compose.web.css.overflowX
import org.jetbrains.compose.web.css.overflowY
import org.jetbrains.compose.web.css.paddingBottom
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.CheckboxInput
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Label
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.w3c.dom.DOMRect
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.set
import pickPhotos
import qr
import r
import saves
import toScaledBytes
import webBaseUrl
import kotlin.time.Duration.Companion.seconds

@Composable
fun ExplorePage(
    card: Card,
    onCard: (Card) -> Unit,
    onCardUpdated: (Card) -> Unit,
    onCardDeleted: (card: Card) -> Unit,
) {
    val me by application.me.collectAsState()
    val scope = rememberCoroutineScope()
    var cards by remember(card.id) {
        mutableStateOf(listOf<Card>())
    }

    var isLoading by remember(card.id) {
        mutableStateOf(true)
    }

    var oldPhoto by remember(card.id) {
        mutableStateOf<String?>(null)
    }

    var menuTarget by remember {
        mutableStateOf<DOMRect?>(null)
    }

    suspend fun reload() {
        if (me == null) return
        api.cardsCards(card.id!!) {
            cards = it
        }
        isLoading = false
    }

    LaunchedEffect(card.id) {
        reload()
    }

    LaunchedEffect(oldPhoto) {
        var tries = 0
        while (tries++ < 5 && oldPhoto != null) {
            delay(3.seconds)
            api.card(card.id!!) {
                if (it.photo != oldPhoto) {
                    onCardUpdated(it)
                    oldPhoto = null
                }
            }
        }
    }

    fun newSubCard(inCard: Card, name: String, active: Boolean) {
        scope.launch {
            api.newCard(Card(name = name, parent = inCard.id!!, active = active)) {
                reload()
                onCardUpdated(it)
            }
        }
    }

    fun generatePhoto() {
        scope.launch {
            api.generateCardPhoto(card.id!!) {
                oldPhoto = card.photo ?: ""
                if (localStorage["app.config.ai.disclaimer.show"] != json.encodeToString(false)) {
                    // todo: translate
                    dialog("Generating", cancelButton = null) {
                        Div {
                            Text("The page will be updated when the photo is generated.")
                            Br()
                            Br()
                            Text("Page title, hint, and details are shared with a 3rd party.")
                        }
                        Div({
                            style {
                                marginTop(1.r)
                            }
                        }) {
                            Label {
                                var dontShow by remember {
                                    mutableStateOf(false)
                                }
                                CheckboxInput(dontShow) {
                                    style {
                                        margin(0.r, .5.r, 0.r, 0.r)
                                    }

                                    onInput {
                                        if (!it.value) {
                                            localStorage["app.config.ai.disclaimer.show"] = json.encodeToString(false)
                                        } else {
                                            localStorage.removeItem("app.config.ai.disclaimer.skip")
                                        }
                                    }

                                    onChange {
                                        dontShow = it.value
                                    }
                                }
                                Span({
                                    style {
                                        fontWeight("bold")
                                        fontSize(14.px)
                                    }
                                }) {
                                    // todo: translate
                                    Text("Don't show this again")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun moveToPage(cardId: String) {
        scope.launch {
            api.updateCard(
                card.id!!,
                Card(offline = false, parent = cardId, group = null, equipped = false, geo = null)
            ) {
                onCardUpdated(it)
            }
        }
    }

    fun moveToGroup(groupId: String) {
        scope.launch {
            api.updateCard(
                card.id!!,
                Card(offline = false, parent = null, group = groupId, equipped = false, geo = null)
            ) {
                onCardUpdated(it)
            }
        }
    }

    val configuration = LocalConfiguration.current
    val inAPage = appString { inAPage }
    val cancel = appString { cancel }
    val titleString = appString { title }
    val rename = appString { rename }
    val hint = appString { hint }
    val update = appString { update }

    fun rename() {
        scope.launch {
            val name = inputDialog(
                titleString,
                "",
                rename,
                defaultValue = card.name ?: ""
            )

            if (name == null) {
                return@launch
            }

            api.updateCard(card.id!!, Card(name = name)) {
                onCardUpdated(it)
            }
        }
    }

    fun rehint() {
        scope.launch {
            val hint = inputDialog(
                hint,
                "",
                update,
                defaultValue = card.location ?: ""
            )

            if (hint == null) {
                return@launch
            }

            api.updateCard(card.id!!, Card(location = hint)) {
                onCardUpdated(it)
            }
        }
    }

    fun moveToGroup() {
        scope.launch {
            val result = searchDialog(
                configuration = configuration,
                title = application.appString { inAGroup },
                confirmButton = cancel,
                load = {
                    var groups = emptyList<GroupExtended>()
                    api.groups {
                        groups = it
                    }
                    groups
                },
                filter = { it, value ->
                    (it.group?.name?.contains(value, true)
                        ?: false) || (it.members?.any { it.person?.name?.contains(value, true) ?: false } ?: false)
                }
            ) { it, resolve ->
                GroupItem(
                    it,
                    selectable = true,
                    onSelected = {
                        moveToGroup(it.group!!.id!!)
                        resolve(false)
                    },
                    info = GroupInfo.LatestMessage
                )
            }
        }
    }

    fun moveToPage() {
        scope.launch {
            val result = searchDialog(
                configuration = configuration,
                title = inAPage,
                confirmButton = cancel,
                load = {
                    var cards = emptyList<Card>()
                    api.myCollaborations {
                        cards = it
                    }
                    cards.sortedByDescending {
                        saves.cards.value.any { save -> it.id == save.id }
                    }
                },
                filter = { it, value ->
                    (it.name?.contains(value, true) ?: false)
                }
            ) { card, resolve ->
                app.nav.CardItem(
                    card = card,
                    scroll = false,
                    selected = false,
                    saved = saves.cards.value.any { save -> save.id == card.id },
                    published = card.active == true
                ) {
                    if (!it) {
                        moveToPage(card.id!!)
                        resolve(false)
                    }
                }
            }
        }
    }

    if (menuTarget != null) {
        Menu({ menuTarget = null }, menuTarget!!) {
            val isSaved = saves.cards.value.any { it.id == card.id }
            item(appString { openInNewTab }, icon = "open_in_new") {
                window.open("/page/${card.id}", target = "_blank")
            }
            item(if (isSaved) appString { unsave } else appString { save }) {
                scope.launch {
                    if (isSaved) {
                        saves.unsave(card.id!!)
                    } else {
                        saves.save(card.id!!)
                    }
                }
            }

            item(rename) {
                rename()
            }

            item(hint) {
                rehint()
            }

            val location = appString { location }
            val close = appString { close }

            item(location) {
                scope.launch {
                    val name = dialog(
                        location,
                        close,
                        null
                    ) {
                        InlineMenu({
                            it(true)
                        }) {
                            item(appString { onProfile }, selected = card.equipped == true, "account_circle") {
                                scope.launch {
                                    api.updateCard(
                                        card.id!!,
                                        Card(offline = false, parent = null, equipped = true, geo = null)
                                    ) {
                                        onCardUpdated(it)
                                    }
                                }
                            }
                            item(
                                appString { inAGroup },
                                selected = card.parent == null && card.offline != true && card.equipped != true && card.group != null,
                                "group"
                            ) {
                                moveToGroup()
                            }
                            item(
                                appString { atALocation },
                                selected = card.parent == null && card.offline != true && card.equipped != true && card.geo != null,
                                "location_on"
                            ) {
                                // todo: needs map
                            }
                            item(inAPage, selected = card.parent != null, "description") {
                                moveToPage()
                            }
                            item(appString { none }, selected = card.offline == true) {
                                scope.launch {
                                    api.updateCard(
                                        card.id!!,
                                        Card(offline = true, parent = null, equipped = false, geo = null)
                                    ) {
                                        onCardUpdated(it)
                                    }
                                }
                            }
                        }
                    }

                    if (name == null) {
                        return@launch
                    }
                }
            }

            item(appString { if (card.pay == null) addPay else changePay }) {
                scope.launch {
                    var updatedPay = card.pay ?: Pay(pay = "")

                    val result = dialog(
                        application.appString { pay },
                        confirmButton = application.appString { this.update }
                    ) { resolve ->
                        var pay by remember {
                            mutableStateOf(updatedPay.pay ?: "")
                        }

                        var payFrequency by remember {
                            mutableStateOf(updatedPay.frequency)
                        }

                        LaunchedEffect(pay, payFrequency) {
                            updatedPay.pay = pay
                            updatedPay.frequency = payFrequency
                        }

                        TextInput(pay) {
                            classes(Styles.textarea)
                            style {
                                width(100.percent)
                                marginBottom(1.r)
                            }

                            onKeyDown {
                                if (it.key == "Enter") {
                                    it.preventDefault()
                                    it.stopPropagation()
                                    resolve(true)
                                }
                            }

                            onInput {
                                pay = it.value
                            }

                            autoFocus()

                            ref {
                                it.select()

                                onDispose {

                                }
                            }
                        }

                        PayFrequency.entries.forEach { frequency ->
                            Div({
                                classes(
                                    listOf(AppStyles.groupItem, AppStyles.groupItemOnSurface)
                                )

                                if (payFrequency == frequency) {
                                    classes(AppStyles.groupItemSelected)
                                }

                                onClick {
                                    payFrequency = if (payFrequency == frequency) null else frequency
                                }

                                focusable()
                            }) {
                                Div {
                                    Div({
                                        classes(AppStyles.groupItemName)
                                    }) {
                                        Text(frequency.appString)
                                    }
                                }
                            }
                        }
                    }

                    if (result == true) {
                        api.updateCard(card.id!!, Card(pay = updatedPay)) {
                            onCardUpdated(it)
                        }
                    }
                }
            }

            item(appString { choosePhoto }) {
                pickPhotos(multiple = false) {
                    it.singleOrNull()?.let {
                        scope.launch {
                            val photo = it.toScaledBytes()
                            api.uploadCardPhoto(
                                card.id!!,
                                photo
                            ) {
                                onCardUpdated(card)
                            }
                        }
                    }
                }
            }

            if (card.video == null) {
                item(if (card.photo == null) appString { this.generatePhoto } else appString { regeneratePhoto }) {
                    if (card.photo == null) {
                        generatePhoto()
                    } else {
                        scope.launch {
                            // todo: translate
                            val result = dialog("Generate a new photo?") {
                                // todo: translate
                                Text("This will replace the current photo.")
                            }

                            if (result == true) {
                                generatePhoto()
                            }
                        }
                    }
                }
            }

            if (card.parent != null) {
                item(appString { openEnclosingCard }) {
                    scope.launch {
                        api.card(card.parent!!) {
                            onCard(it)
                        }
                    }
                }
            }

            item(appString { qrCode }) {
                scope.launch {
                    dialog("", cancelButton = null) {
                        val qrCode = remember {
                            "$webBaseUrl/page/${card.id!!}".qr
                        }
                        Img(src = qrCode) {
                            style {
                                borderRadius(1.r)
                            }
                        }
                    }
                }
            }

            item(appString { delete }) {
                scope.launch {
                    // todo: translate
                    val result = dialog(
                        "Delete this page?",
                        "Yes, delete"
                    ) {
                        // todo: translate
                        Text("You cannot undo this.")
                    }

                    if (result == true) {
                        api.deleteCard(card.id!!) {
                            onCardDeleted(card)
                        }
                    }
                }
            }
        }
    }

    var published by remember(card) {
        mutableStateOf(card.active == true)
    }

    PageTopBar(
        title = card.name?.notBlank ?: appString { newCard },
        description = card.hint,
        onTitleClick = if (me?.id == card.person) {
            {
                rename()
            }
        } else null,
        onDescriptionClick = if (me?.id == card.person) {
            {
                rehint()
            }
        } else null,
        navActions = {
            if (card.parent != null) {
                IconButton("arrow_upward", appString { openEnclosingCard }, styles = {
                    marginRight(.5f.r)
                }) {
                    scope.launch {
                        api.card(card.parent!!) {
                            onCard(it)
                        }
                    }
                }
            }
        },
        actions = {
            Switch(
                published,
                { published = it },
                {
                    scope.launch {
                        val previousValue = card.active == true
                        api.updateCard(card.id!!, Card(active = it), onError = {
                            published = previousValue
                        }) {
                            onCardUpdated(it)
                        }
                    }
                },
                // todo: translate
                title = "Page is ${if (published) "published" else "not published"}"
            ) {
                margin(1.r)
            }
        }
    ) {
        menuTarget = if (menuTarget == null) (it.target as HTMLElement).getBoundingClientRect() else null
    }

    val conversation = remember(card) {
        card.getConversation()
    }

    suspend fun saveConversation(value: String): Boolean {
        conversation.message = value
        val conversationString = json.encodeToString(conversation)

        var success = false

        api.updateCard(card.id!!, Card(conversation = conversationString)) {
            success = true
            onCardUpdated(card)
        }

        return success
    }

    Div({
        style {
            flex(1)
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            overflowY("auto")
            overflowX("hidden")
        }
    }) {
        if (card.photo != null || card.video != null) {
            Div({
                style {
                    margin(1.r, 1.r, .5.r, 1.r)
                }
            }) {
                CardPhotoOrVideo(card) {
                    borderRadius(1.r)
                }
            }
        }

        // Todo full conversation support

//            if (conversation.message.isNotBlank()) {
//                Div({
//                    style {
//                        padding(1.r)
//                        whiteSpace("pre-wrap")
//                    }
//                }) {
//                    Text(conversation.message)
//                }
//            }

        EditField(
            value = conversation.message,
            placeholder = appString { details },
            styles = {
                margin(.5.r, 1.r)
                maxHeight(50.vh)
            }
        ) {
            saveConversation(it)
        }

        NewCardInput(
            onSubmit = { name, active ->
                newSubCard(card, name, active)
            }
        )

        if (isLoading) {
            Loading()
        } else {
            Div(
                {
                    classes(CardsPageStyles.layout)
                    style {
                        paddingBottom(1.r)
                    }
                }
            ) {
                cards.forEach { card ->
                    CardItem(card, showTapToOpen = false) {
                        onCard(card)
                    }
                }
            }
        }
    }
}
