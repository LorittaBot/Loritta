package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.hr
import kotlinx.html.id

fun FlowContent.rightSidebarContentAndSaveBarWrapper(
    content: FlowContent.() -> (Unit),
    saveBar: FlowContent.() -> (Unit)
) {
    div {
        id = "right-sidebar-content-and-save-bar-wrapper"

        div {
            content()

            hr {}

            saveBarReservedSpace()
        }

        saveBar()
    }
}