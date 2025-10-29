package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.a
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
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube.YouTubeChannel
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.profilepresets.ProfilePresetsListView
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedConfirmDeletionModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick
import org.jetbrains.exposed.sql.ResultRow

fun FlowContent.trackedTwitchChannelsSection(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    guild: Guild,
    trackedProfiles: List<TrackedProfile>
) {
    trackedProfilesSection(
        i18nContext,
        guild,
        "Canais que você está seguindo",
        i18nContext.get(DashboardI18nKeysData.Twitch.Channels(trackedProfiles.size)),
        {
            openModalOnClick(
                createEmbeddedModal(
                    "Qual canal você deseja adicionar?",
                    true,
                    {
                        discordButton(ButtonStyle.PRIMARY) {
                            openModalOnClick(
                                createEmbeddedModal(
                                    "Siga as instruções para autorizar a sua conta",
                                    true,
                                    {
                                        div {
                                            attributes["bliss-component"] = "twitch-callback-listener"
                                            attributes["twitch-oauth2-url"] = "https://id.twitch.tv/oauth2/authorize?client_id=${loritta.config.loritta.twitch.clientId}&redirect_uri=${loritta.config.loritta.twitch.redirectUri}&response_type=code"
                                            attributes["twitch-dashboard-url"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/twitch/add"
                                        }
                                    },
                                    listOf {
                                        defaultModalCloseButton(i18nContext)
                                    }
                                )
                            )
                            text("Quero adicionar o meu canal")
                        }

                        discordButton(ButtonStyle.PRIMARY) {
                            openModalOnClick(
                                createEmbeddedModal(
                                    "Adicionar canal de outra pessoa",
                                    true,
                                    {
                                        textInput {
                                            name = "channelLink"
                                            placeholder = "https://www.twitch.tv/lorittamorenitta"
                                        }
                                    },
                                    listOf(
                                        {
                                            defaultModalCloseButton(i18nContext)
                                        },
                                        {
                                            discordButton(ButtonStyle.PRIMARY) {
                                                attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/twitch/add"
                                                attributes["bliss-include-query"] = ".modal [name='channelLink']"
                                                attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD

                                                text("Continuar")
                                            }
                                        }
                                    )
                                )
                            )
                            text("Quero adicionar o canal de outra pessoa")
                        }
                    },
                    listOf {
                        defaultModalCloseButton(i18nContext)
                    }
                )
            )

            text("Adicionar Canal")
        },
        "twitch",
        trackedProfiles
    )
}