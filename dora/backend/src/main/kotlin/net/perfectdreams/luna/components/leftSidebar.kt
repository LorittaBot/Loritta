package net.perfectdreams.luna.components

import kotlinx.html.A
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.nav
import kotlin.collections.plus

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

fun FlowContent.sectionEntry(href: String? = null, selected: Boolean, block: A.() -> Unit) {
    a(classes = "entry section-entry", href = href) {
        if (selected)
            classes += "selected"

        block()
    }
}