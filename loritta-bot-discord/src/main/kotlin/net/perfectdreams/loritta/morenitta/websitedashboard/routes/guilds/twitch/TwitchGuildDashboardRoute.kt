package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch

import io.ktor.server.application.*
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.html
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch.TwitchWebUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.switchtwitch.data.TwitchUser
import org.jetbrains.exposed.sql.selectAll

class TwitchGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/twitch") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val trackedProfiles = website.loritta.transaction {
            TrackedTwitchAccounts.selectAll()
                .where {
                    TrackedTwitchAccounts.guildId eq guild.idLong
                }
                .toList()
        }

        val profilesInfo = mutableMapOf<Long, TwitchUser>()
        if (trackedProfiles.isNotEmpty()) {
            val accountsInfo = TwitchWebUtils.getCachedUsersInfoById(
                website.loritta,
                *trackedProfiles.map { it[TrackedTwitchAccounts.twitchUserId] }.toLongArray()
            )

            profilesInfo.putAll(accountsInfo.associateBy { it.id })
        }

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
                            heroWrapper {
                                heroText {
                                    h1 {
                                        text(i18nContext.get(DashboardI18nKeysData.Twitch.Title))
                                    }

                                    p {
                                        text("Anuncie para seus membros quando você entra ao vivo na Twitch! Assim, seus fãs não irão perder as suas lives.")
                                    }
                                }
                            }

                            hr {}

                            sectionConfig {
                                trackedTwitchChannelsSection(
                                    website.loritta,
                                    i18nContext,
                                    guild,
                                    trackedProfiles.map {
                                        val profileInfo = profilesInfo[it[TrackedTwitchAccounts.id].value]

                                        TrackedProfile(
                                            profileInfo?.login,
                                            profileInfo?.profileImageUrl,
                                            it[TrackedTwitchAccounts.twitchUserId].toString(),
                                            it[TrackedTwitchAccounts.id].value,
                                            it[TrackedTwitchAccounts.channelId]
                                        )
                                    }
                                )
                            }
                        }
                    )
                }
        )
    }
}