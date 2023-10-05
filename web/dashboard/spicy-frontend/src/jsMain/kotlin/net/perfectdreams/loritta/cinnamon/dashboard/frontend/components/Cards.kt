package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement

@Composable
fun Cards(attrs: AttrBuilderContext<HTMLDivElement>? = null, block: @Composable () -> (Unit) = {}) {
    Div(
        attrs = {
            attrs?.invoke(this)
            classes("cards")
        }
    ) {
        block()
    }
}

@Composable
fun Card(attrs: AttrBuilderContext<HTMLDivElement>? = null, block: @Composable () -> (Unit) = {}) {
    Div(
        attrs = {
            attrs?.invoke(this)
            classes("card")
        }
    ) {
        block()
    }
}

@Composable
fun CardsWithHeader(attrs: AttrBuilderContext<HTMLDivElement>? = null, block: @Composable () -> (Unit) = {}) {
    Div(
        attrs = {
            attrs?.invoke(this)
            classes("cards-with-header")
        }
    ) {
        block()
    }
}

@Composable
fun CardHeader(attrs: AttrBuilderContext<HTMLDivElement>? = null, block: @Composable () -> (Unit) = {}) {
    Div(
        attrs = {
            attrs?.invoke(this)
            classes("card-header")
        }
    ) {
        block()
    }
}

@Composable
fun CardHeaderTitle(attrs: AttrBuilderContext<HTMLDivElement>? = null, block: @Composable () -> (Unit) = {}) {
    Div(
        attrs = {
            attrs?.invoke(this)
            classes("card-header-title")
        }
    ) {
        block()
    }
}

@Composable
fun CardHeaderInfo(attrs: AttrBuilderContext<HTMLDivElement>? = null, block: @Composable () -> (Unit) = {}) {
    Div(
        attrs = {
            attrs?.invoke(this)
            classes("card-header-info")
        }
    ) {
        block()
    }
}

@Composable
fun CardHeaderDescription(attrs: AttrBuilderContext<HTMLDivElement>? = null, block: @Composable () -> (Unit) = {}) {
    Div(
        attrs = {
            attrs?.invoke(this)
            classes("card-header-description")
        }
    ) {
        block()
    }
}