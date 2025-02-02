package app.components

import androidx.compose.runtime.Composable
import appString
import components.SearchField
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.selectors.CSSSelector.PseudoClass.focus
import org.jetbrains.compose.web.dom.Div
import r

@Composable
fun TopBarSearch(
    value: String,
    onValue: (String) -> Unit,
    focus: Boolean = true,
    placeholder: String? = null,
    content: (@Composable () -> Unit)? = null,
    styles: (StyleScope.() -> Unit)? = null
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            maxWidth(800.px)
            width(100.percent)
            alignSelf(AlignSelf.Center)
        }
    }) {
        SearchField(
            value = value,
            placeholder = placeholder ?: appString { this.search },
            focus = focus,
            styles = {
                if (styles != null) {
                    styles()
                } else {
                    margin(1.r)
                }
            }
        ) {
            onValue(it)
        }
        content?.invoke()
    }
}
