package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.youtube

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.util.getOrFail
import kotlinx.html.*
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube.YouTubeWebUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.goBackToPreviousSectionButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.rightSidebarContentAndSaveBarWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trackedNewProfileEditorSaveBar
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trackedYouTubeChannelEditorWithProfile
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseAllModals
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme

class AddYouTubeChannelGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/youtube/add") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val channelLink = call.parameters.getOrFail("channelLink")
        val result = YouTubeWebUtils.getYouTubeChannelInfoFromURL(website.loritta, channelLink)

        when (result) {
            is YouTubeWebUtils.YouTubeChannelInfoResult.Success -> {
                call.respondHtml {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.Youtube.Title),
                        session,
                        theme,
                        shimejiSettings,
                        userPremiumPlan,
                        null,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.YOUTUBE)
                        },
                        {
                            if (call.request.headers["Bliss-Trigger-Element-Id"] == "add-profile") {
                                blissCloseAllModals()
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
                                website.shouldDisplayAds(call, userPremiumPlan, null),
                                {
                                    trackedYouTubeChannelEditorWithProfile(
                                        i18nContext,
                                        guild,
                                        result.channel,
                                        null,
                                        null
                                    )
                                },
                                {
                                    trackedNewProfileEditorSaveBar(
                                        i18nContext,
                                        guild,
                                        "youtube",
                                        {
                                            put("channelLink", "https://www.youtube.com/channel/${result.channel.channelId}")
                                        },
                                        {
                                            put("youtubeChannelId", result.channel.channelId)
                                        }
                                    )
                                }
                            )
                        }
                    )
                }
            }
            is YouTubeWebUtils.YouTubeChannelInfoResult.Error -> {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Algo deu errado ao tentar pegar as informações do canal!"
                        )
                    )
                }
            }
            YouTubeWebUtils.YouTubeChannelInfoResult.InvalidUrl -> {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "URL inválida!"
                        )
                    )
                }
            }
            YouTubeWebUtils.YouTubeChannelInfoResult.UnknownChannel -> {
                call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                    blissShowToast(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Canal não existe!"
                        )
                    )
                }
            }
        }
    }
}