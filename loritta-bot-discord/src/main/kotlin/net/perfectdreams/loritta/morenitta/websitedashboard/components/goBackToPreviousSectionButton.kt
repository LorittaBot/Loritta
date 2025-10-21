package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent

fun FlowContent.goBackToPreviousSectionButton(
    href: String,
    block: FlowContent.() -> (Unit),
) {
    discordButtonLink(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, href = href) {
        swapRightSidebarContentsAttributes()

        block()
    }
}