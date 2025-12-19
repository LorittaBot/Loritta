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
        text("Projetos")
    }
}
