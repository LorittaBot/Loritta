package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.nav

fun FlowContent.leftSidebar(block: FlowContent.() -> (Unit)) {
    nav(classes = "is-closed") {
        id = "left-sidebar"

        block()
    }
}

fun FlowContent.leftSidebarHr() {
    hr(classes = "divider") {}
}