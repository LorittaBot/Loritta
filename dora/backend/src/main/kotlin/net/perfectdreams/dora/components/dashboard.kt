package net.perfectdreams.dora.components

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.style
import net.perfectdreams.dora.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.components.svgIcon

val SWAP_EVERYTHING_DASHBOARD = "#right-sidebar-contents (innerHTML) -> #right-sidebar-contents (innerHTML), .entries (innerHTML) -> .entries (innerHTML), #mobile-left-sidebar-title (innerHTML) -> #mobile-left-sidebar-title (innerHTML)"
val PARTIAL_SWAP_WITH_ENTRIES_DASHBOARD = "#right-sidebar-contents (innerHTML) -> #right-sidebar-contents (innerHTML), .entries (innerHTML) -> .entries (innerHTML)"

fun FlowContent.goBackToPreviousSectionButton(
    href: String,
    attrs: FlowContent.() -> Unit = {
        attributes["bliss-get"] = "[href]"
        attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
        attributes["bliss-push-url:200"] = "true"
        attributes["bliss-sync"] = "#left-sidebar"
        attributes["bliss-indicator"] = "this, #right-sidebar-wrapper, #left-sidebar .entry.selected"
    },
    block: FlowContent.() -> (Unit),
) {
    a(classes = "discord-button ${ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT.className} text-with-icon bounce-icon-to-the-left-on-hover", href = href) {
        style = "width: 100%;"

        attrs()
        svgIcon(SVGIcons.CaretLeft)

        block()
    }
}