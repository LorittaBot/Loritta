package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.ContentBuilder
import org.jetbrains.compose.web.dom.Span
import org.w3c.dom.HTMLSpanElement

@Composable
fun Tag(type: String, attrs: AttrBuilderContext<HTMLSpanElement>? = null, content: ContentBuilder<HTMLSpanElement>? = null) {
    Span(attrs = {
        attrs?.invoke(this)
        classes("tag", type)
    }) {
        content?.invoke(this)
    }
}

@Composable
fun TagPrimary(attrs: AttrBuilderContext<HTMLSpanElement>? = null, content: ContentBuilder<HTMLSpanElement>? = null) = Tag("primary", attrs, content)

@Composable
fun TagWarn(attrs: AttrBuilderContext<HTMLSpanElement>? = null, content: ContentBuilder<HTMLSpanElement>? = null) = Tag("warn", attrs, content)