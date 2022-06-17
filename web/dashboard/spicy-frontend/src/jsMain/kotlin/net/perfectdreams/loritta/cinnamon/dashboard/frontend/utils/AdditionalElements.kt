package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import androidx.compose.runtime.Composable
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.ContentBuilder
import org.jetbrains.compose.web.dom.ElementBuilder
import org.jetbrains.compose.web.dom.TagElement
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

// From Jetpack Compose Web code
private open class ElementBuilderImplementation<TElement : Element>(private val tagName: String) :
    ElementBuilder<TElement> {
    private val el: Element by lazy { document.createElement(tagName) }
    @Suppress("UNCHECKED_CAST")
    override fun create(): TElement = el.cloneNode() as TElement
}

@Composable
fun Details(
    attrs: AttrBuilderContext<HTMLElement>? = null,
    content: ContentBuilder<HTMLElement>? = null
) {
    TagElement(
        elementBuilder = ElementBuilderImplementation("details"),
        applyAttrs = attrs,
        content = content
    )
}

@Composable
fun Summary(
    attrs: AttrBuilderContext<HTMLElement>? = null,
    content: ContentBuilder<HTMLElement>? = null
) {
    TagElement(
        elementBuilder = ElementBuilderImplementation("summary"),
        applyAttrs = attrs,
        content = content
    )
}