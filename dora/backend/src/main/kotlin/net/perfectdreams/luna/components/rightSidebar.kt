package net.perfectdreams.luna.components

import kotlinx.html.FlowContent
import kotlinx.html.article
import kotlinx.html.aside
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.ins
import kotlinx.html.p
import kotlinx.html.section
import kotlinx.html.style
import kotlinx.serialization.json.Json
import net.perfectdreams.luna.modals.EmbeddedModal
import kotlin.collections.set

fun FlowContent.rightSidebar(block: FlowContent.() -> (Unit)) {
    section {
        id = "right-sidebar"

        div {
            id = "right-sidebar-wrapper"

            article {
                id = "right-sidebar-contents"

                block()
            }
        }

        aside {
            id = "that-wasnt-very-cash-money-of-you-fixed-sidebar-reserved-space"
        }
    }
}