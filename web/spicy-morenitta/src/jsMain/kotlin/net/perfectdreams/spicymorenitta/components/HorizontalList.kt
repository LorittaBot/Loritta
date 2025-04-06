package net.perfectdreams.spicymorenitta.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.ContentBuilder
import org.jetbrains.compose.web.dom.Div
import web.html.HTMLDivElement

@Composable
fun HorizontalList(
    attrs: AttrBuilderContext<HTMLDivElement>? = null,
    content: ContentBuilder<HTMLDivElement>? = null
) = Div(
    attrs = {
        classes("qm", "horizontal-list")
        attrs?.invoke(this)
    },
    content = content
)