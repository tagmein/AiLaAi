package app.scripts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import api
import app.ailaai.api.createScript
import app.ailaai.api.myScripts
import app.dialog.inputDialog
import app.nav.NavMenu
import app.nav.NavMenuItem
import app.nav.NavTopBar
import appString
import application
import bulletedString
import com.queatz.db.Script
import components.IconButton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.marginRight
import r

sealed class ScriptsNav {
    data object None : ScriptsNav()
    data class Script(val script: com.queatz.db.Script) : ScriptsNav()
}

@Composable
fun ScriptsNavPage(
    updates: MutableSharedFlow<Script>,
    selected: ScriptsNav,
    onSelected: (ScriptsNav) -> Unit,
    onCreated: (Script) -> Unit,
    onProfileClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var scripts by remember { mutableStateOf<List<Script>>(emptyList()) }

    LaunchedEffect(Unit) {
        api.myScripts {
            scripts = it
        }

        updates.collectLatest {
            api.myScripts {
                scripts = it
            }
        }
    }

    LaunchedEffect(scripts) {
        (selected as? ScriptsNav.Script)?.script?.id?.let { scriptId ->
            scripts.firstOrNull { it.id == scriptId }?.let { script ->
                onSelected(ScriptsNav.Script(script))
            }
        }
    }

    val me by application.me.collectAsState()

    val title = appString { title }
    val create = appString { create }

    NavTopBar(me, appString { this.scripts }, onProfileClick = onProfileClick) {
        IconButton(
            name = "add",
            title = appString { this.createCard },
            styles = {
                marginRight(.5.r)
            }
        ) {
            scope.launch {
                val result = inputDialog(
                    // todo: translate
                    title = "New script",
                    placeholder = title,
                    confirmButton = create
                )

                if (result == null) return@launch

                api.createScript(Script(name = result)) {
                    onCreated(it)
                    onSelected(ScriptsNav.Script(it))
                }
            }
        }
    }

    NavMenu {
        scripts.forEach { script ->
            NavMenuItem(
                icon = null,
                title = script.name.orEmpty(),
                description = bulletedString(
                    script.categories?.firstOrNull(),
                    script.description
                ),
                selected = (selected as? ScriptsNav.Script)?.script?.id == script.id
            ) {
                onSelected(ScriptsNav.Script(script = script))
            }
        }
    }
}
