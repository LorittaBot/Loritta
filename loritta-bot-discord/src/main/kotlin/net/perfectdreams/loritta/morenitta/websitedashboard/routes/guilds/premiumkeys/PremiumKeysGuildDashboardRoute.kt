package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.premiumkeys

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildPremiumKeysAndPremiumInfoPlan
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class PremiumKeysGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/premium-keys") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val guildPremiumKeys = website.loritta.transaction {
            DonationKeys.selectAll()
                .where {
                    DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis())
                }
                .toList()
        }

        val userPremiumKeys = website.loritta.transaction {
            DonationKeys.selectAll()
                .where {
                    DonationKeys.userId eq session.userId and (DonationKeys.expiresAt greaterEq System.currentTimeMillis())
                }
                .toList()
        }

        val plan = ServerPremiumPlans.getPlanFromValue(guildPremiumKeys.sumOf { it[DonationKeys.value] })

        call.respondHtml(
            createHTML()
                .html {
                    dashboardBase(
                        i18nContext,
                        i18nContext.get(DashboardI18nKeysData.PremiumKeys.Title),
                        session,
                        theme,
                        {
                            guildDashLeftSidebarEntries(i18nContext, guild, GuildDashboardSection.PREMIUM_KEYS)
                        },
                        {
                            div(classes = "hero-wrapper") {
                                div(classes = "hero-text") {
                                    h1 {
                                        text("Premium Keys")
                                    }

                                    p {
                                        text("Premium Keys")
                                    }
                                }
                            }

                            hr {}

                            div {
                                id = "section-config"

                                guildPremiumKeysAndPremiumInfoPlan(
                                    i18nContext,
                                    guild,
                                    session,
                                    plan,
                                    guildPremiumKeys,
                                    userPremiumKeys
                                )
                            }
                        }
                    )
                }
        )
    }
}