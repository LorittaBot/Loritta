package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.classes
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons

fun FlowContent.goBackToPreviousSectionButton(
    href: String,
    block: FlowContent.() -> (Unit),
) {
    discordButtonLink(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, href = href) {
        classes += "text-with-icon bounce-icon-to-the-left-on-hover"

        swapRightSidebarContentsAttributes()
        svgIcon(SVGIcons.CaretLeft)

        block()
    }
}