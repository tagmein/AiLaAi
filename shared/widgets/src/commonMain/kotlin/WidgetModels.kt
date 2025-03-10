package com.queatz.widgets

import com.queatz.widgets.widgets.FormFieldType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FormValue(
    val key: String,
    val type: FormFieldType,
    val title: String,
    val value: JsonElement
)
