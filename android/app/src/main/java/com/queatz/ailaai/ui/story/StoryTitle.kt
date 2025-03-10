package com.queatz.ailaai.ui.story

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.queatz.ailaai.R
import com.queatz.ailaai.extensions.scrollToTop
import com.queatz.ailaai.ui.theme.pad
import com.queatz.db.Story
import kotlinx.coroutines.launch

@Composable
fun RowScope.StoryTitle(state: LazyGridState, title: String?) {
    val isScrolled by remember { derivedStateOf { state.firstVisibleItemIndex > 0 } }
    val scope = rememberCoroutineScope()

    Crossfade(
        targetState = isScrolled,
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 1.pad),
        label = ""
    ) {
        when (it) {
            true -> {
                Text(
                    title
                        ?: stringResource(R.string.empty_story_name),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        scope.launch {
                            state.scrollToTop()
                        }
                    }
                )
            }

            false -> {}
        }
    }
}
