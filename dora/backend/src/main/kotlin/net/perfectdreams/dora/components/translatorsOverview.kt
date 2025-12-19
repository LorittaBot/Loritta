package net.perfectdreams.dora.components

import kotlinx.html.FlowContent
import kotlinx.html.b
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.style
import kotlinx.html.h1
import kotlinx.html.hr
import net.perfectdreams.dora.Project
import net.perfectdreams.dora.tables.CachedDiscordUserIdentifications
import net.perfectdreams.dora.tables.ProjectUserPermissions
import net.perfectdreams.dora.tables.Users
import org.jetbrains.exposed.sql.ResultRow
import net.perfectdreams.dora.utils.createEmbeddedConfirmDeletionModal
import net.perfectdreams.luna.modals.openModalOnClick

fun FlowContent.translatorsOverview(
    project: Project,
    permissions: List<ResultRow>
) {
    heroWrapper {
        simpleHeroImage("https://upload.wikimedia.org/wikipedia/en/9/99/Dora_the_Explorer_%28character%29.webp")

        heroText {
            h1 {
                text("Tradutores")
            }
        }
    }

    hr {}

    cardsWithHeader {
        cardHeader {
            cardHeaderInfo {
                cardHeaderTitle {
                    text("Tradutores")
                }

                cardHeaderDescription {
                    text("${permissions.size} tradutores")
                }
            }

            discordButtonLink(ButtonStyle.SUCCESS, "/projects/${project.slug}/translators/create") {
                attributes["bliss-get"] = "[href]"
                attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
                attributes["bliss-push-url:200"] = "true"

                text("Adicionar Tradutor")
            }
        }

        div(classes = "cards") {
            for (row in permissions) {
                val username = row.getOrNull(CachedDiscordUserIdentifications.globalName) ?: row.getOrNull(CachedDiscordUserIdentifications.username) ?: "Unknown"
                val avatarId = row.getOrNull(CachedDiscordUserIdentifications.avatarId)
                val discordId = row.getOrNull(CachedDiscordUserIdentifications.id)?.value
                val userId = row[Users.id].value

                val avatarUrl = if (avatarId != null && discordId != null) {
                    val extension = if (avatarId.startsWith("a_")) "gif" else "png"
                    "https://cdn.discordapp.com/avatars/$discordId/$avatarId.$extension"
                } else {
                    // Default avatar (same logic as Translator.defaultAvatarUrl)
                    val idx = (discordId ?: 0L) % 5
                    "https://cdn.discordapp.com/embed/avatars/$idx.png"
                }

                val permissionLevel = row[ProjectUserPermissions.permissionLevel]

                div(classes = "card") {
                    style = "flex-direction: row; align-items: center; gap: 0.75em;"

                    img(src = avatarUrl, alt = username, classes = "section-icon") {
                        style = "width: 48px; height: 48px; border-radius: 99999px;"
                    }

                    div {
                        style = "flex-grow: 1; display: flex; flex-direction: column;"

                        b {
                            text(username)
                        }

                        div {
                            text(permissionLevel.name)
                        }
                    }

                    // Right side actions: Delete button opening a confirm modal
                    div {
                        style = "display: grid; grid-template-columns: 1fr; grid-column-gap: 0.5em; align-items: center;"

                        // Expose a delete action. We'll delete by Users.id (primary user snowflake)
                        discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                            openModalOnClick(
                                createEmbeddedConfirmDeletionModal {
                                    attributes["bliss-delete"] = "/projects/${project.slug}/translators/${userId}"
                                    attributes["bliss-swap:200"] = "body (innerHTML) -> #right-sidebar-contents (innerHTML)"
                                }
                            )

                            text("Excluir")
                        }
                    }
                }
            }
        }
    }
}
