package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.ContentBuilder
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement

@Composable
fun HeroBanner(content: ContentBuilder<HTMLDivElement>? = null) = Div(attrs = {
    classes("hero-wrapper")
}) {
    if (content != null) {
        content()
    }
}

@Composable
fun HeroImage(content: ContentBuilder<HTMLDivElement>? = null) = Div(attrs = {
    classes("hero-image")
}) {
    if (content != null) {
        content()
    }
}

@Composable
fun HeroText(content: ContentBuilder<HTMLDivElement>? = null) = Div(attrs = {
    classes("hero-text")
}) {
    if (content != null) {
        content()
    }
}