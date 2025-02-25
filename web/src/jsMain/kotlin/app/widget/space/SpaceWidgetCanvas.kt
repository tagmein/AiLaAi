package app.widget.space

import app.widget.DrawInfo
import baseUrl
import com.queatz.db.Card
import com.queatz.widgets.widgets.SpaceContent
import com.queatz.widgets.widgets.SpaceItem
import org.jetbrains.compose.web.css.Color
import org.w3c.dom.CENTER
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextAlign
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.MIDDLE
import org.w3c.dom.START
import kotlin.math.PI

fun drawCanvas(
    context: CanvasRenderingContext2D,
    offset: Pair<Double, Double>,
    cardId: String?,
    cardsById: Map<String, Card>,
    items: List<SpaceItem>,
    selectedItem: SpaceItem?,
    darkMode: Boolean,
    drawInfo: DrawInfo?,
) {
    with(context) {
        clearRect(
            x = 0.0,
            y = 0.0,
            w = canvas.width.toDouble(),
            h = canvas.height.toDouble()
        )

        // Apply the offset transformation
        save()
        translate(offset.first, offset.second)

        // Draw the pages
        items.forEachIndexed { index, (item, position) ->
            when (item) {
                is SpaceContent.Line -> {
                    if (item.page == cardId) {
                        save()
                        lineWidth = if (selectedItem?.content == item) 3.0 else 1.0
                        strokeStyle = Color.gray
                        beginPath()
                        moveTo(position.first, position.second)
                        lineTo(item.to.first, item.to.second)
                        stroke()
                        restore()
                    }
                }

                is SpaceContent.Text -> {
                    font = if (selectedItem?.content == item) {
                        "bold 24px ${font.split(" ").last()}"
                    } else {
                        "24px ${font.split(" ").last()}"
                    }

                    textAlign = CanvasTextAlign.START
                    fillText(item.text.orEmpty(), position.first, position.second)
                }

                is SpaceContent.Box -> {
                    if (item.page == cardId) {
                        save()
                        lineWidth = if (selectedItem?.content == item) 3.0 else 1.0
                        strokeStyle = Color.gray
                        beginPath()
                        rect(
                            position.first,
                            position.second,
                            item.to.first - position.first,
                            item.to.second - position.second
                        )
                        stroke()
                        restore()
                    }
                }

                is SpaceContent.Page -> {
                    val card = cardsById[item.id] ?: return@forEachIndexed
                    val (x, y) = position

                    save()

                    if (card.photo != null) {
                        val image = js("new Image()") as HTMLImageElement
                        image.src = "$baseUrl${card.photo!!}"
                        beginPath()
                        arc(x, y, 24.0, 0.0, 2 * PI)
                        clip()
                        val imageAspect = image.width.toDouble() / image.height.toDouble()
                        val circleDiameter = 48.0
                        val circleRadius = circleDiameter / 2

                        if (imageAspect > 1) {
                            val scaledHeight = circleDiameter
                            val scaledWidth = circleDiameter * imageAspect
                            drawImage(
                                image = image,
                                dx = x - circleRadius - (scaledWidth - circleDiameter) / 2,
                                dy = y - circleRadius,
                                dw = scaledWidth,
                                dh = scaledHeight
                            )
                        } else {
                            val scaledWidth = circleDiameter
                            val scaledHeight = circleDiameter / imageAspect
                            drawImage(
                                image = image,
                                dx = x - circleRadius,
                                dy = y - circleRadius - (scaledHeight - circleDiameter) / 2,
                                dw = scaledWidth,
                                dh = scaledHeight
                            )
                        }
                    }

                    beginPath()
                    arc(x, y, 24.0, 0.0, 2 * PI)
                    lineWidth = if (selectedItem?.content == item) 3.0 else 1.0
                    strokeStyle = if (darkMode) Color.gray else Styles.colors.primary
                    stroke()

                    restore()

                    // Draw the page name
                    fillStyle = if (darkMode) Color.white else Color.black
                    font = "18px ${font.split(" ").last()}"
                    textAlign = CanvasTextAlign.CENTER
                    textBaseline = CanvasTextBaseline.MIDDLE

                    fillText(card.name ?: "New page", x, y - 24 - 18)
                }
            }
        }

        // Draw line
        drawInfo?.let { drawInfo ->
            when (drawInfo.tool) {
                is SpaceWidgetTool.Line -> {
                    drawInfo.from?.let { from ->
                        drawInfo.to?.let { to ->
                            save()
                            lineWidth = 2.0
                            strokeStyle = if (darkMode) Color.gray else Styles.colors.primary
                            beginPath()
                            moveTo(from.first, from.second)
                            lineTo(to.first, to.second)
                            stroke()
                            restore()
                        }
                    }
                }
                is SpaceWidgetTool.Box -> {
                    drawInfo.from?.let { from ->
                        drawInfo.to?.let { to ->
                            save()
                            lineWidth = 2.0
                            strokeStyle = if (darkMode) Color.gray else Styles.colors.primary
                            beginPath()
                            rect(
                                from.first,
                                from.second,
                                to.first - from.first,
                                to.second - from.second
                            )
                            stroke()
                            restore()
                        }
                    }
                }
                else -> Unit
            }
        }

        restore()
    }
}

data class SpacePathItem(
    val id: String,
    val card: Card,
    val offset: Pair<Double, Double> = 0.0 to 0.0,
    val selectedItem: SpaceItem? = null,
)
