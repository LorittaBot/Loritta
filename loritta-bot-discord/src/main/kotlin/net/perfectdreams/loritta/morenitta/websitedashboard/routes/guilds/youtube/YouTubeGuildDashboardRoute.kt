package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.youtube

import io.ktor.server.application.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube.YouTubeWebUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.TrackedProfile
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sectionConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trackedYouTubeChannelsSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll
import kotlin.collections.map

class YouTubeGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/youtube") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val trackedYouTubeAccounts = website.loritta.transaction {
            TrackedYouTubeAccounts.selectAll()
                .where {
                    TrackedYouTubeAccounts.guildId eq guild.idLong
                }
                .toList()
        }

        val youtubeChannelsInfo = trackedYouTubeAccounts.map {
            GlobalScope.async {
                YouTubeWebUtils.getYouTubeChannelInfoFromChannelId(website.loritta, it[TrackedYouTubeAccounts.youTubeChannelId])
            }
        }.awaitAll().mapNotNull { (it as? YouTubeWebUtils.YouTubeChannelInfoResult.Success)?.channel }
            .associateBy { it.channelId }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.Youtube.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.YOUTUBE)
                },
                {
                    div(classes = "hero-wrapper") {
                        div(classes = "hero-text") {
                            h1 {
                                text(i18nContext.get(DashboardI18nKeysData.Youtube.Title))
                            }

                            p {
                                text("Anuncie para seus membros quando você posta um novo vídeo no YouTube! Assim, seus fãs não irão perder seus novos vídeos.")
                            }
                        }
                    }

                    hr {}

                    sectionConfig {
                        trackedYouTubeChannelsSection(
                            i18nContext,
                            guild,
                            trackedYouTubeAccounts.map {
                                val youtubeChannelInfo = youtubeChannelsInfo[it[TrackedYouTubeAccounts.youTubeChannelId]]

                                TrackedProfile(
                                    youtubeChannelInfo?.name,
                                    youtubeChannelInfo?.avatarUrl,
                                    it[TrackedYouTubeAccounts.youTubeChannelId],
                                    it[TrackedYouTubeAccounts.id].value,
                                    it[TrackedYouTubeAccounts.channelId]
                                )
                            }
                        )
                    }
                }
            )
        }
    }
}