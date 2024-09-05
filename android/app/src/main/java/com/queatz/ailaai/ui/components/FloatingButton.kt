package com.queatz.ailaai.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.queatz.ailaai.extensions.px
import com.queatz.ailaai.ui.theme.elevation

/**
 * Copied from FloatingActionButton.kt
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FloatingButton(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    onLongClickLabel: String? = null,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    val absoluteElevation = LocalAbsoluteTonalElevation.current
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalAbsoluteTonalElevation provides absoluteElevation
    ) {
        Box(
            modifier = modifier
                .minimumInteractiveComponentSize()
                .graphicsLayer(
                    shadowElevation = LocalAbsoluteTonalElevation.current.px.toFloat() + 6.dp.px, // todo animate on press
                    shape = shape,
                    clip = false
                )
                .background(
                    color = containerColor,
                    shape = shape
                )
                .clip(shape)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = remember { ripple() },
                    enabled = enabled,
                    onClick = onClick,
                    onLongClick = onLongClick,
                    onClickLabel = onClickLabel,
                    onLongClickLabel = onLongClickLabel
                ),
            propagateMinConstraints = true
        ) {
            Box(
                modifier = Modifier
                    .defaultMinSize(
                        minWidth = 56.0.dp,
                        minHeight = 56.0.dp,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}
