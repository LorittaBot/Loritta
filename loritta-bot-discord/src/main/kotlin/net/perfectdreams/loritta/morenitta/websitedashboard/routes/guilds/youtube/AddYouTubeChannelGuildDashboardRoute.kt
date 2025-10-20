package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.youtube

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.util.getOrFail
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordMessage
import net.perfectdreams.loritta.dashboard.messageeditor.MessageEditorBootstrap
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube.YouTubeWebUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.customGuildCommandTextEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordMessageEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.goBackToPreviousSectionButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.saveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sectionConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trackedYouTubeChannelEditor
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme

class AddYouTubeChannelGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/youtube/add") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val channelLink = call.parameters.getOrFail("channelLink")
        val result = YouTubeWebUtils.getYouTubeChannelInfoFromURL(website.loritta, channelLink)

        when (result) {
            is YouTubeWebUtils.YouTubeChannelInfoResult.Success -> {
                call.respondHtml(
                    createHTML()
                        .html {
                            dashboardBase(
                                i18nContext,
                                i18nContext.get(DashboardI18nKeysData.CustomCommands.Title),
                                session,
                                theme,
                                {
                                    guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.YOUTUBE)
                                },
                                {
                                    if (call.request.headers["Bliss-Trigger-Element-Id"] == "add-profile") {
                                        blissCloseModal()
                                        blissShowToast(
                                            createEmbeddedToast(
                                                EmbeddedToast.Type.SUCCESS,
                                                "Canal encontrado!"
                                            )
                                        )
                                    }

                                    goBackToPreviousSectionButton(
                                        href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/youtube",
                                    ) {
                                        text("Voltar para a lista de canais do YouTube")
                                    }

                                    hr {}

                                    rightSidebarContentAndSaveBarWrapper(
                                        {
                                            sectionConfig {
                                                trackedYouTubeChannelEditor(
                                                    i18nContext,
                                                    guild,
                                                    null,
                                                    "Novo vídeo!"
                                                )
                                            }
                                        },
                                        {
                                            saveBar(
                                                i18nContext,
                                                true,
                                                {
                                                    attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/youtube/add"
                                                    attributes["bliss-swap:200"] = "#section-config (innerHTML) -> #section-config (innerHTML)"
                                                    attributes["bliss-headers"] = buildJsonObject {
                                                        put("Loritta-Configuration-Reset", "true")
                                                    }.toString()
                                                    attributes["bliss-vals-query"] = buildJsonObject {
                                                        put("channelLink", "https://www.youtube.com/channel/${result.channel.channelId}")
                                                    }.toString()
                                                }
                                            ) {
                                                attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/youtube"
                                                attributes["bliss-swap:200"] = "body (innerHTML) -> #right-sidebar-content-and-save-bar-wrapper (innerHTML)"
                                                attributes["bliss-include-json"] = "#section-config"
                                                attributes["bliss-vals-json"] = buildJsonObject {
                                                    put("youtubeChannelId", result.channel.channelId)
                                                }.toString()
                                            }
                                        }
                                    )
                                }
                            )
                        }
                )
            }
            is YouTubeWebUtils.YouTubeChannelInfoResult.Error -> {
                call.respondHtml(
                    createHTML(false)
                        .body {
                            blissShowToast(
                                createEmbeddedToast(
                                    EmbeddedToast.Type.WARN,
                                    "Algo deu errado ao tentar pegar as informações do canal!"
                                )
                            )
                        },
                    status = HttpStatusCode.BadRequest
                )
            }
            YouTubeWebUtils.YouTubeChannelInfoResult.InvalidUrl -> {
                call.respondHtml(
                    createHTML(false)
                        .body {
                            blissShowToast(
                                createEmbeddedToast(
                                    EmbeddedToast.Type.WARN,
                                    "URL inválida!"
                                )
                            )
                        },
                    status = HttpStatusCode.BadRequest
                )
            }
            YouTubeWebUtils.YouTubeChannelInfoResult.UnknownChannel -> {
                call.respondHtml(
                    createHTML(false)
                        .body {
                            blissShowToast(
                                createEmbeddedToast(
                                    EmbeddedToast.Type.WARN,
                                    "Canal não existe!"
                                )
                            )
                        },
                    status = HttpStatusCode.BadRequest
                )
            }
        }
    }
}