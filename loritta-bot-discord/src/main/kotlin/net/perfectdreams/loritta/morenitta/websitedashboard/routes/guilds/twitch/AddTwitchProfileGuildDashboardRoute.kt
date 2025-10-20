package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.stream.createHTML
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch.TwitchWebUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme

class AddTwitchProfileGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/twitch/add") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val userId = call.parameters.getOrFail("userId")
            .toLong()

        val twitchUser = TwitchWebUtils.getCachedUsersInfoById(website.loritta, userId)
            .first()

        call.response.header("Bliss-Push-Url", "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/twitch/add?userId=$userId")
        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.Twitch.Title),
                        session,
                        theme,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.TWITCH)
                        },
                        {
                            if (call.request.headers["Bliss-Trigger-Element-Id"] == "add-profile") {
                                blissCloseModal()
                                blissShowToast(
                                    createEmbeddedToast(
                                        EmbeddedToast.Type.SUCCESS,
                                        "Conta encontrada!"
                                    )
                                )
                            }

                            goBackToPreviousSectionButton(
                                href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/twitch",
                            ) {
                                text("Voltar para a lista de contas da Twitch")
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
                                            attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/twitch/add"
                                            attributes["bliss-swap:200"] = "#section-config (innerHTML) -> #section-config (innerHTML)"
                                            attributes["bliss-headers"] = buildJsonObject {
                                                put("Loritta-Configuration-Reset", "true")
                                            }.toString()
                                            attributes["bliss-vals-query"] = buildJsonObject {
                                                put("userId", twitchUser.id)
                                            }.toString()
                                        }
                                    ) {
                                        attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/twitch"
                                        attributes["bliss-swap:200"] = "body (innerHTML) -> #right-sidebar-content-and-save-bar-wrapper (innerHTML)"
                                        attributes["bliss-include-json"] = "#section-config"
                                        attributes["bliss-vals-json"] = buildJsonObject {
                                            put("userId", twitchUser.id)
                                        }.toString()
                                    }
                                }
                            )
                        }
                    )
                }
        )
    }
}