package net.perfectdreams.dora.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.style
import net.perfectdreams.dora.tables.Projects
import net.perfectdreams.dora.utils.createEmbeddedConfirmDeletionModal
import net.perfectdreams.luna.modals.openModalOnClick
import org.jetbrains.exposed.sql.ResultRow

fun FlowContent.projects(projects: List<ResultRow>) {
    cardsWithHeader {
        cardHeader {
            cardHeaderInfo {
                cardHeaderTitle {
                    text("Projetos")
                }

                cardHeaderDescription {
                    text("${projects.size} projetos")
                }
            }

            discordButtonLink(ButtonStyle.SUCCESS, "/projects/create") {
                attributes["bliss-get"] = "[href]"
                attributes["bliss-swap:200"] = "#right-sidebar-contents (innerHTML) -> #right-sidebar-contents (innerHTML)"
                attributes["bliss-push-url:200"] = "true"
                text("Criar Projeto")
            }
        }

        div(classes = "cards") {
            for (project in projects.sortedBy { it[Projects.fancyName] }) {
                div(classes = "card") {
                    style = "flex-direction: row; align-items: center; gap: 0.75em;"

                    // Icon (if present)
                    val iconUrl = project.getOrNull(Projects.iconUrl)
                    if (iconUrl != null) {
                        img(src = iconUrl, alt = project[Projects.fancyName], classes = "section-icon") {
                            style = "width: 48px; height: 48px; border-radius: 99999px;"
                        }
                    }

                    div {
                        style = "flex-grow: 1;"

                        text(project[Projects.fancyName])
                    }

                    div {
                        style = "display: grid;grid-template-columns: 1fr 1fr;grid-column-gap: 0.5em;"

                        discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                            openModalOnClick(
                                createEmbeddedConfirmDeletionModal {
                                    attributes["bliss-delete"] = "/projects/${project[Projects.slug]}"
                                    attributes["bliss-swap:200"] = PARTIAL_SWAP_WITH_ENTRIES_DASHBOARD
                                }
                            )

                            text("Excluir")
                        }

                        discordButtonLink(ButtonStyle.PRIMARY, href = "/projects/${project[Projects.slug]}") {
                            attributes["bliss-get"] = "[href]"
                            attributes["bliss-swap:200"] = "#right-sidebar-contents (innerHTML) -> #right-sidebar-contents (innerHTML), .entries (innerHTML) -> .entries (innerHTML)"
                            attributes["bliss-push-url:200"] = "true"

                            text("Ver")
                        }
                    }
                }
            }
        }
    }
}