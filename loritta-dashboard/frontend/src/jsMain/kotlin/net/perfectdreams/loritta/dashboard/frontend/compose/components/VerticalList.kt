package net.perfectdreams.loritta.dashboard.frontend.compose.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.ContentBuilder
import org.jetbrains.compose.web.dom.Div
import web.html.HTMLDivElement

@Composable
fun VerticalList(
    attrs: AttrBuilderContext<HTMLDivElement>? = null,
    content: ContentBuilder<HTMLDivElement>? = null
) = Div(
    attrs = {
        classes("qm", "vertical-list")
        attrs?.invoke(this)
    },
    content = content
)