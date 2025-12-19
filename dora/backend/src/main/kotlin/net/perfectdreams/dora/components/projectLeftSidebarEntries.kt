package net.perfectdreams.dora.components

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.hr
import kotlinx.html.img
import kotlinx.html.style
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.ProjectDashboardSection
import net.perfectdreams.dora.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.components.svgIcon
import net.perfectdreams.luna.components.sectionEntry

fun FlowContent.projectLeftSidebarEntries(
    project: Project,
    selectedSection: ProjectDashboardSection
) {
    div(classes = "guild-icon-wrapper") {
        style = "text-align: center;"

        div(classes = "discord-server-icon") {
            val iconUrl = project.iconUrl
            if (iconUrl != null) {
                img(src = iconUrl) {}
            }
        }
    }

    div(classes = "entry guild-name") {
        text(project.name)
    }

    goBackToPreviousSectionButton("/") {
        text("Voltar aos Projetos")
    }

    hr {}

    sectionEntry("/projects/${project.slug}", selectedSection == ProjectDashboardSection.OVERVIEW) {
        attributes["bliss-get"] = "[href]"
        attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
        attributes["bliss-push-url:200"] = "true"

        text("Visão Geral")
    }

    sectionEntry("/projects/${project.slug}/info", selectedSection == ProjectDashboardSection.INFO) {
        attributes["bliss-get"] = "[href]"
        attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
        attributes["bliss-push-url:200"] = "true"

        text("Informações")
    }

    sectionEntry("/projects/${project.slug}/translators", selectedSection == ProjectDashboardSection.TRANSLATORS) {
        attributes["bliss-get"] = "[href]"
        attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
        attributes["bliss-push-url:200"] = "true"

        text("Tradutores")
    }

    sectionEntry("/projects/${project.slug}/sync", selectedSection == ProjectDashboardSection.SYNC) {
        attributes["bliss-get"] = "[href]"
        attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
        attributes["bliss-push-url:200"] = "true"

        text("Sincronizar")
    }
}
