package com.queatz.ailaai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.dp
import com.queatz.ailaai.ui.theme.elevation
import com.queatz.ailaai.ui.theme.pad

class SearchFilter(
    val name: String,
    val icon: ImageVector,
    val selected: Boolean,
    val onClick: () -> Unit,
)

@Composable
fun Categories(
    categories: List<String>,
    category: String?,
    modifier: Modifier = Modifier,
    filters: List<SearchFilter> = emptyList(),
    visible: Boolean = filters.isNotEmpty() || categories.size > 2,
    onCategory: (String?) -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        var viewport by remember { mutableStateOf(Size(0f, 0f)) }
        val scrollState = rememberScrollState()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.pad),
            modifier = modifier
                .horizontalScroll(scrollState)
                .onPlaced { viewport = it.boundsInParent().size }
                .padding(horizontal = 2.pad)
        ) {
            if (filters.isNotEmpty()) {
                filters.forEachIndexed { index, it ->
                    Button(
                        onClick = {
                            it.onClick()
                        },
                        elevation = ButtonDefaults.elevatedButtonElevation(.5f.elevation),
                        colors = if (!it.selected) ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ) else ButtonDefaults.buttonColors()
                    ) {
                        Icon(it.icon, null, modifier = Modifier.padding(end = 1.pad))
                        Text(it.name)
                    }
                }
                if (categories.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = .25f.pad)
                            .width(2.dp)
                            .height(16.dp)
                            .shadow(1.elevation, MaterialTheme.shapes.medium)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }

            categories.forEachIndexed { index, it ->
                Button(
                    onClick = {
                        onCategory(
                            if (category == it) {
                                null
                            } else {
                                it
                            }
                        )
                    },
                    elevation = ButtonDefaults.elevatedButtonElevation(.5f.elevation),
                    colors = if (category != it) ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ) else ButtonDefaults.buttonColors()
                ) {
                    Text(it)
                }
            }
        }
    }
}
