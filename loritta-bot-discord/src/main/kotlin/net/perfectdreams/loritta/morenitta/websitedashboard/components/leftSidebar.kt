package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.nav

fun FlowContent.leftSidebar(block: FlowContent.() -> (Unit)) {
    // We *could* include "is-closed" here, but this causes issues when loading the page from scratch (without the CSS cached)
    // Where the closing animation is played when it shouldn't
    nav {
        id = "left-sidebar"

        block()
    }

    div {
        id = "left-sidebar-reserved-space"
    }
}

fun FlowContent.leftSidebarHr() {
    hr(classes = "divider") {}
}