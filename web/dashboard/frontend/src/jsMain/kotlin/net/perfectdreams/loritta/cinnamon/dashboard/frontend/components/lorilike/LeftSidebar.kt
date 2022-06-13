package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.UIIcon
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Nav

@Composable
fun LeftSidebar(
    isSidebarOpen: MutableState<Boolean>,
    entries: @Composable () -> (Unit)
) {
    Nav(attrs = {
        id("left-sidebar")
        if (isSidebarOpen.value)
            classes("is-open")
        else
            classes("is-closed")
    }) {
        Div(attrs = { classes("entries") }) {
            entries.invoke()
        }
    }

    Nav(attrs = { id("mobile-left-sidebar")}) {
        // We use a button so it can be tabbable and has better accessbility
        Button(
            attrs = {
                classes("hamburger-button")
                attr("aria-label", "Menu Button")

                onClick {
                    isSidebarOpen.value = !isSidebarOpen.value
                }
            }
        ) {
            if (isSidebarOpen.value)
                UIIcon(SVGIconManager.times)
            else
                UIIcon(SVGIconManager.bars)
        }
    }
}