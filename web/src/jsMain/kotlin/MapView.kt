import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import app.ailaai.api.cards
import app.components.Spacer
import com.queatz.db.Card
import com.queatz.db.Geo
import components.CardContent
import components.SearchField
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lib.getCameraLngLat
import lib.mapboxgl
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.AlignSelf
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.alignSelf
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.backgroundImage
import org.jetbrains.compose.web.css.backgroundPosition
import org.jetbrains.compose.web.css.backgroundRepeat
import org.jetbrains.compose.web.css.backgroundSize
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.boxSizing
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.cursor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.fontSize
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.lineHeight
import org.jetbrains.compose.web.css.maxWidth
import org.jetbrains.compose.web.css.opacity
import org.jetbrains.compose.web.css.padding
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.paddingRight
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.B
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun MapView(header: (@Composable () -> Unit)? = null) {
    var searchText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf(listOf<Card>()) }
    var geo by remember { mutableStateOf<Geo?>(null) }
    var map by remember { mutableStateOf<mapboxgl.Map?>(null) }
    var markers by remember { mutableStateOf(emptyList<mapboxgl.Marker>()) }
    var selectedCard by remember { mutableStateOf<Card?>(null) }
    val scope = rememberCoroutineScope()
    val hasHash = remember { window.location.hash.isBlank() }

    LaunchedEffect(geo, searchText) {
        geo ?: return@LaunchedEffect
        isLoading = true
        delay(250)

        api.cards(geo!!, search = searchText.notBlank, public = true) {
            searchResults = it
        }

        isLoading = false
    }

    fun update() {
        map ?: return

        val cameraLngLat = map!!.getCameraLngLat()
        val altitude = map!!.getFreeCameraOptions().position.toAltitude() as Double

        markers.forEach {
            val groundDistance = cameraLngLat.distanceTo(it.getLngLat())
            val scale = 100.0 / sqrt(groundDistance.pow(2.0) + altitude.pow(2.0))
            val element = it.getElement().firstElementChild as HTMLElement
            element.style.transform = "scale(${scale.coerceIn(0.125, 100.0)})"
            it.getElement().style.zIndex = (scale * 1000.0).toInt().toString()
            val ele = it.getElement() // for the following line
            js("ele.style.pointerEvents = \"none\"")
        }

        // prevent mapbox from overriding pointer-events
        scope.launch {
            delay(100)
            markers.forEach {
                val ele = it.getElement() // for the following line
                js("ele.style.pointerEvents = \"none\"")
            }
        }
    }

    LaunchedEffect(searchResults, map) {
        map ?: return@LaunchedEffect

        markers.forEach {
            it.remove()
        }

        markers = searchResults.map { card ->
            val element = document.createElement("div") as HTMLDivElement

            val options: mapboxgl.MarkerOptions = js("{}")
            options.element = element
            options.anchor = "bottom"

            val latlng: mapboxgl.LngLat = js("{}")
            latlng.lat = card.geo!![0]
            latlng.lng = card.geo!![1]

            mapboxgl.Marker(options)
                .setLngLat(latlng)
                .addTo(map!!).also {
                    renderComposable(root = element) {
                        val isNpc = !card.npc?.photo.isNullOrBlank()

                        Div({
                            style {
                                display(DisplayStyle.Flex)
                                flexDirection(FlexDirection.Column)
                                alignItems(AlignItems.Center)
                                gap(2.r)
                                cursor("pointer")
                                property("pointer-events", "auto")
                                property("will-change", "transform")
                                property("transform-origin", "bottom center")
                            }

                            onClick {
                                it.stopPropagation()

                                if (it.ctrlKey) {
                                    window.open("$webBaseUrl/page/${card.id!!}", target = "_blank")
                                } else {
                                    selectedCard = if (selectedCard?.id == card.id) {
                                        null
                                    } else {
                                        card
                                    }
                                }
                            }
                        }) {
                            Div({
                                style {
                                    textAlign("center")
                                    fontSize(64.px)
                                    color(Color.black)
                                    padding(2.r)
                                    lineHeight(120.percent)

                                    if (isNpc) {
                                        property("box-shadow", "0 2px 16px rgba(0, 0, 0, 0.125)")
                                        borderRadius(2.r)
                                        backgroundColor(Color.white)
                                    }
                                }
                            }) {
                                if (isNpc) {
                                    Div({
                                        style {
                                            textAlign("left")
                                        }
                                    }) {
                                        B {
                                            Text(card.npc?.name.orEmpty())
                                        }
                                        Span({
                                            style {
                                                opacity(.5)
                                                paddingLeft(1.r)
                                                fontSize(80.percent)
                                            }
                                        }) {
                                            Text("${card.name}")
                                        }
                                        card.npc?.text?.notBlank?.let {
                                            Br()
                                            Text(it)
                                        }
                                    }
                                } else {
                                    Text("${card.name}")
                                }
                            }

                            Div({
                                style {
                                    backgroundRepeat("no-repeat")
                                    if (isNpc) {
                                        width(32.r)
                                        height(32.r)
                                        backgroundImage("url(\"$baseUrl/${card.npc!!.photo}\")")
                                        backgroundPosition("center")
                                        backgroundSize("contain")
                                    } else if (!card.photo.isNullOrBlank()) {
                                        width(16.r)
                                        height(16.r)
                                        borderRadius(16.r)
                                        backgroundColor(Styles.colors.background)
                                        backgroundImage("url($baseUrl${card.photo!!})")
                                        backgroundPosition("center")
                                        backgroundSize("cover")
                                        property("border", "${2.px} solid ${Color.white}")
                                    }
                                }
                            }) {}
                        }
                    }
                }
        }

        update()
    }

    Div({
        style {
            property("inset", "0")
            position(Position.Absolute)
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            property("z-index", "0")
        }

        ref { ref ->
            val locateMeControl = mapboxgl.GeolocateControl()

            val options: mapboxgl.MapOptions = js("{}")
            options.container = ref
            options.boxZoom = false
            options.hash = true
            map = mapboxgl.Map(options).apply {
                addControl(locateMeControl, "bottom-right")

                on("load") {
                    geo = getCameraLngLat().let { Geo(it.lat, it.lng) }

                    if (hasHash) {
                        locateMeControl.trigger()
                    }
                }

                on("moveend") {
                    geo = if (getPitch() > 60.0) {
                        map!!.getCenter()
                    } else {
                        getCameraLngLat()
                    }.let { Geo(it.lat, it.lng) }
                }

                on("render") {
                    update()
                }

                on("click") {
                    selectedCard = null
                }
            }

            onDispose {
                map?.remove()
            }
        }
    }) {}

    if (header == null) {
        Spacer()
    } else {
        header()
    }

    Div({
        classes(Styles.mapContainer)
    }) {
        Div(
            {
                classes(Styles.mapUi)
            }
        ) {
            Div({
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    maxWidth(800.px)
                    width(100.percent)
                    alignSelf(AlignSelf.Center)
                    paddingLeft(1.r)
                    paddingRight(1.r)
                    boxSizing("border-box")
                }
            }) {
                SearchField(searchText, appString { search },
                    shadow = true,
                    styles = {
                    }) {
                    searchText = it
                }
            }
        }
        if (selectedCard != null) {
            Div({
                classes(Styles.navContainer, Styles.mapPanel)
            }) {
                Div({
                    classes(Styles.navContent)
                }) {
                    Div {
                        selectedCard?.let {
                            CardContent(it)
                        }
                    }
                }
            }
        }
    }
}
