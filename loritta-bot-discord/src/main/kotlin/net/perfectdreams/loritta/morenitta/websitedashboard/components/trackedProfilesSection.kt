package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.BUTTON
import kotlinx.html.FlowContent
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.textInput
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserCreatedProfilePresets
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube.YouTubeChannel
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.profilepresets.ProfilePresetsListView
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedConfirmDeletionModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick
import org.jetbrains.exposed.sql.ResultRow

/**
 * A generic "tracked social profiles" section component
 */
fun FlowContent.trackedProfilesSection(
    i18nContext: I18nContext,
    guild: Guild,
    cardHeaderTitle: String,
    cardHeaderDescription: String,
    addProfileButton: BUTTON.() -> (Unit),
    sectionEndpointPathPart: String,
    trackedProfiles: List<TrackedProfile>,
) {
    cardsWithHeader {
        cardHeader {
            cardHeaderInfo {
                cardHeaderTitle {
                    text(cardHeaderTitle)
                }

                cardHeaderDescription {
                    text(cardHeaderDescription)
                }
            }

            discordButton(ButtonStyle.SUCCESS) {
                addProfileButton()
            }
        }

        if (trackedProfiles.isNotEmpty()) {
            div(classes = "cards") {
                for (profile in trackedProfiles) {
                    val postingInChannel = guild.getGuildChannelById(profile.channelId)

                    div(classes = "card") {
                        style = "flex-direction: row; align-items: center; gap: 0.5em;"

                        div {
                            style = "flex-grow: 1; display: flex;\n" +
                                    "  align-items: center;\n" +
                                    "  flex-direction: row;\n" +
                                    "  gap: 16px;"

                            img(src = profile.avatarUrl) {
                                style = "border-radius: 99999px;"
                                width = "48"
                                height = "48"
                            }

                            div {
                                style = "display: flex; flex-direction: column;"

                                div {
                                    span {
                                        style = "font-weight: bold;"

                                        text(profile.name ?: "???")
                                    }

                                    text(" em ")

                                    span(classes = "discord-mention") {
                                        text("#${postingInChannel?.name ?: "???"}")
                                    }
                                }

                                div {
                                    text(profile.profileId)
                                }
                            }
                        }

                        div {
                            style = "display: grid;grid-template-columns: 1fr 1fr;grid-column-gap: 0.5em;"

                            discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                                openModalOnClick(
                                    createEmbeddedConfirmDeletionModal(i18nContext) {
                                        attributes["bliss-delete"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/$sectionEndpointPathPart/${profile.entryId}"
                                        attributes["bliss-swap:200"] = "body (innerHTML) -> #section-config (innerHTML)"
                                    }
                                )

                                text("Excluir")
                            }

                            discordButtonLink(ButtonStyle.PRIMARY, href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/$sectionEndpointPathPart/${profile.entryId}") {
                                swapRightSidebarContentsAttributes()

                                text("Editar")
                            }
                        }
                    }
                }
            }
        } else {
            emptySection(i18nContext)
        }
    }
}

data class TrackedProfile(
    val name: String?,
    val avatarUrl: String?,
    val profileId: String,
    val entryId: Long,
    val channelId: Long
)