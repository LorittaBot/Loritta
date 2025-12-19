package net.perfectdreams.dora.components

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.hr
import kotlinx.html.style
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectDashboardSection
import net.perfectdreams.dora.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.components.svgIcon
import net.perfectdreams.luna.components.sectionEntry

fun FlowContent.homeLeftSidebarEntries() {
    sectionEntry("/projects", true) {
        attributes["bliss-get"] = "[href]"
        attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
        attributes["bliss-push-url:200"] = "true"
        attributes["bliss-sync"] = "#left-sidebar"
        attributes["bliss-indicator"] = "this, #right-sidebar-wrapper, #left-sidebar .entry.selected"

        text("Projetos")
    }
}
