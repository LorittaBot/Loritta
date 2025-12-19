package net.perfectdreams.dora.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.hr
import kotlinx.html.img
import kotlinx.html.id
import kotlinx.html.style
import net.perfectdreams.dora.LanguageDashboardSection
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.tables.LanguageTargets
import net.perfectdreams.dora.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.components.svgIcon
import net.perfectdreams.luna.components.sectionEntry
import org.jetbrains.exposed.sql.ResultRow

fun FlowContent.languageLeftSidebarEntries(
    project: Project,
    languageTarget: ResultRow,
    selectedSection: LanguageDashboardSection,
    translatedCount: Int,
    totalCount: Int
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

    goBackToPreviousSectionButton("/projects/${project.slug}") {
        text("Voltar ao Projeto")
    }

    hr {}

    div {
        attributes["bliss-sse"] = "/projects/${project.slug}/languages/${languageTarget[LanguageTargets.languageId]}/language-progress"
        attributes["bliss-preserve"] = "true"
        id = "left-sidebar-language-progress-wrapper"

        div {
            id = "left-sidebar-language-progress"
            languageProgressBar(translatedCount, totalCount)
        }
    }

    hr {}

    sectionEntry("/projects/${project.slug}/languages/${languageTarget[LanguageTargets.languageId]}", selectedSection == LanguageDashboardSection.OVERVIEW) {
        attributes["bliss-get"] = "[href]"
        attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
        attributes["bliss-push-url:200"] = "true"
        attributes["bliss-sync"] = "#left-sidebar"
        attributes["bliss-indicator"] = "this, #right-sidebar-wrapper, #left-sidebar .entry.selected"

        text("Visão Geral")
    }

    sectionEntry(
        "/projects/${project.slug}/languages/${languageTarget[LanguageTargets.languageId]}/table",
        selectedSection == LanguageDashboardSection.STRINGS
    ) {
        attributes["bliss-get"] = "[href]"
        attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
        attributes["bliss-push-url:200"] = "true"
        attributes["bliss-sync"] = "#left-sidebar"
        attributes["bliss-indicator"] = "this, #right-sidebar-wrapper, #left-sidebar .entry.selected"

        text("Strings")
    }

    sectionEntry(
        "/projects/${project.slug}/languages/${languageTarget[LanguageTargets.languageId]}/table?filters=UNTRANSLATED",
        selectedSection == LanguageDashboardSection.UNTRANSLATED_STRINGS
    ) {
        attributes["bliss-get"] = "[href]"
        attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
        attributes["bliss-push-url:200"] = "true"
        attributes["bliss-sync"] = "#left-sidebar"
        attributes["bliss-indicator"] = "this, #right-sidebar-wrapper, #left-sidebar .entry.selected"

        text("Strings (Não Traduzidas)")
    }
}