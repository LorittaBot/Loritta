package net.perfectdreams.dora.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.style
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.tables.LanguageTargets
import net.perfectdreams.dora.utils.createEmbeddedConfirmDeletionModal
import net.perfectdreams.luna.modals.openModalOnClick
import org.jetbrains.exposed.sql.ResultRow

fun FlowContent.projectOverview(
    project: Project,
    languageTargets: List<ResultRow>
) {
    heroWrapper {
        simpleHeroImage("https://upload.wikimedia.org/wikipedia/en/9/99/Dora_the_Explorer_%28character%29.webp")

        heroText {
            h1 {
                text("Visão Geral")
            }
        }
    }

    hr {}

    cardsWithHeader {
        cardHeader {
            cardHeaderInfo {
                cardHeaderTitle {
                    text("Idiomas")
                }

                cardHeaderDescription {
                    text("${languageTargets.size} idiomas")
                }
            }

            discordButtonLink(ButtonStyle.SUCCESS, "/projects/${project.slug}/languages/create") {
                attributes["bliss-get"] = "[href]"
                attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
                attributes["bliss-push-url:200"] = "true"

                text("Adicionar Idioma")
            }
        }

        div(classes = "cards") {
            for (languageTarget in languageTargets.sortedBy { it[LanguageTargets.languageName] }) {
                div(classes = "card") {
                    style = "flex-direction: row; align-items: center; gap: 0.5em;"

                    div {
                        style = "flex-grow: 1;"

                        text(languageTarget[LanguageTargets.languageName])
                    }

                    div {
                        style = "display: grid;grid-template-columns: 1fr 1fr;grid-column-gap: 0.5em;"

                        discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                            openModalOnClick(
                                createEmbeddedConfirmDeletionModal {
                                    attributes["bliss-delete"] = "/projects/${project.slug}/languages/${languageTarget[LanguageTargets.languageId]}"
                                    attributes["bliss-swap:200"] = "body (innerHTML) -> #right-sidebar-contents (innerHTML)"
                                }
                            )

                            text("Excluir")
                        }

                        discordButtonLink(ButtonStyle.PRIMARY, href = "/projects/${project.slug}/languages/${languageTarget[LanguageTargets.languageId]}") {
                            attributes["bliss-get"] = "[href]"
                            attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
                            attributes["bliss-push-url:200"] = "true"

                            text("Editar")
                        }
                    }
                }
            }
        }
    }
}