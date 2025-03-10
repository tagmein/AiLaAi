package com.queatz.widgets.widgets

import kotlinx.serialization.Serializable


@Serializable
data class SpaceData(
    var card: String? = null,
    var items: List<SpaceItem>? = null
)

@Serializable
data class SpaceItem(
    val content: SpaceContent,
    val position: Pair<Double, Double>
)

@Serializable
sealed class SpaceContent {
    @Serializable
    class Page(
        val id: String
    ) : SpaceContent()

    @Serializable
    class Text(
        val page: String? = null,
        val text: String? = null
    ) : SpaceContent()

    @Serializable
    class Line(
        val page: String? = null,
        val to: Pair<Double, Double>
    ) : SpaceContent()

    @Serializable
    class Box(
        val page: String? = null,
        val to: Pair<Double, Double>
    ) : SpaceContent()

    @Serializable
    class Circle(
        val page: String? = null,
        val to: Pair<Double, Double>
    ) : SpaceContent()
}
