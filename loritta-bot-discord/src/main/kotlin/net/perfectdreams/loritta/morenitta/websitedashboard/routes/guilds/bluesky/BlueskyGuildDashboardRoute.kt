package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.bluesky

import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.server.application.*
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedBlueskyAccounts
import net.perfectdreams.loritta.common.utils.JsonIgnoreUnknownKeys
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky.BlueskyProfile
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky.BlueskyProfiles
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.TrackedProfile
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sectionConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.components.simpleHeroImage
import net.perfectdreams.loritta.morenitta.websitedashboard.components.trackedBlueskyProfilesSection
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll
import kotlin.collections.map

class BlueskyGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/bluesky") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val trackedBlueskyProfiles = website.loritta.transaction {
            TrackedBlueskyAccounts.selectAll()
                .where {
                    TrackedBlueskyAccounts.guildId eq guild.idLong
                }
                .toList()
        }

        val blueskyProfiles = mutableMapOf<String, BlueskyProfile>()
        if (trackedBlueskyProfiles.isNotEmpty()) {
            val http = website.loritta.http.get("https://public.api.bsky.app/xrpc/app.bsky.actor.getProfiles") {
                // The docs are wrong, this is a "array", as in, you need to specify multiple parameters
                for (trackedBlueskyAccount in trackedBlueskyProfiles.take(25)) {
                    parameter("actors", trackedBlueskyAccount[TrackedBlueskyAccounts.repo])
                }
            }

            val profiles = JsonIgnoreUnknownKeys.decodeFromString<BlueskyProfiles>(http.bodyAsText(Charsets.UTF_8).also { println(it) })
            blueskyProfiles.putAll(profiles.profiles.associateBy { it.did })
        }

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
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.BLUESKY)
                },
                {
                    heroWrapper {
                        simpleHeroImage("https://stuff.loritta.website/monica-ata-bluetero.jpeg")

                        heroText {
                            h1 {
                                text(i18nContext.get(DashboardI18nKeysData.Bluesky.Title))
                            }

                            p {
                                text("Anuncie para seus membros quando você posta algo no Bluesky! Assim, seus fãs não irão perder as suas opiniões filosóficas.")
                            }
                        }
                    }

                    hr {}

                    sectionConfig {
                        trackedBlueskyProfilesSection(
                            i18nContext,
                            guild,
                            trackedBlueskyProfiles.map {
                                val profileInfo = blueskyProfiles[it[TrackedBlueskyAccounts.repo]]

                                TrackedProfile(
                                    profileInfo?.handle,
                                    profileInfo?.avatar,
                                    it[TrackedBlueskyAccounts.repo],
                                    it[TrackedBlueskyAccounts.id].value,
                                    it[TrackedBlueskyAccounts.channelId]
                                )
                            }
                        )
                    }
                }
            )
        }
    }
}