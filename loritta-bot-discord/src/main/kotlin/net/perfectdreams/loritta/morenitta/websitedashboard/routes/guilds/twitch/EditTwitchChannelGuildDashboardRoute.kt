package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.twitch

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.html.hr
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch.TwitchWebUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class EditTwitchChannelGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/twitch/{entryId}") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val entryId = call.parameters.getOrFail("entryId").toLong()

        val data = website.loritta.transaction {
            TrackedTwitchAccounts.selectAll()
                .where {
                    TrackedTwitchAccounts.id eq entryId and (TrackedTwitchAccounts.guildId eq guild.idLong)
                }
                .firstOrNull()
        }

        if (data == null) {
            // TODO - bliss-dash: Add a proper page!
            call.respond(HttpStatusCode.NotFound)
            return
        }
        
        val twitchAccountTrackingState = website.loritta.transaction {
            TwitchWebUtils.getTwitchAccountTrackState(data[TrackedTwitchAccounts.twitchUserId])
        }

        val twitchUser = TwitchWebUtils.getCachedUsersInfoById(website.loritta, data[TrackedTwitchAccounts.twitchUserId])
            .first()

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.Twitch.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.TWITCH)
                },
                {
                    goBackToPreviousSectionButton(
                        href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/twitch",
                    ) {
                        text("Voltar para a lista de contas da Twitch")
                    }

                    hr {}

                    rightSidebarContentAndSaveBarWrapper(
                        website.shouldDisplayAds(call, userPremiumPlan, null),
                        {
                            trackedTwitchChannelEditorWithProfile(
                                i18nContext,
                                guild,
                                twitchUser,
                                twitchAccountTrackingState,
                                data[TrackedTwitchAccounts.channelId],
                                data[TrackedTwitchAccounts.message]
                            )
                        },
                        {
                            trackedProfileEditorSaveBar(
                                i18nContext,
                                guild,
                                "twitch",
                                data[TrackedTwitchAccounts.id].value
                            )
                        }
                    )
                }
            )
        }
    }
}