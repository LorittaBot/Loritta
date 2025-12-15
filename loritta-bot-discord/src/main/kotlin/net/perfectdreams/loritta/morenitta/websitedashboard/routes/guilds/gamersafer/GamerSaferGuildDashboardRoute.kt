package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.gamersafer

import io.ktor.server.application.*
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButtonLink
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme

class GamerSaferGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/gamersafer-verify") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.GamerSafer.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.GAMERSAFER)
                },
                {
                    div {
                        style = "text-align: center;"
                        h1 { text("GamerSafer") }
                    }

                    div {
                        for (line in i18nContext.get(I18nKeysData.Website.Dashboard.GamerSafer.Description)) {
                            p {
                                text(line)
                            }
                        }

                        div {
                            style = "display: flex; gap: 1em; justify-content: center;"

                            discordButtonLink(
                                ButtonStyle.PRIMARY,
                                href = "https://discord.com/api/oauth2/authorize?client_id=1037108339538153584&permissions=8&redirect_uri=https%3A%2F%2Fdefender.gamersafer.systems%2Fapi%2Fauth%2Fsignin%3Fsource%3Dloritta&response_type=code&scope=identify%20applications.commands%20bot"
                            ) {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.GamerSafer.AddBot))
                            }

                            discordButtonLink(ButtonStyle.PRIMARY, href = "https://docs.gamersafer.com/") {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.GamerSafer.Docs))
                            }

                            discordButtonLink(ButtonStyle.PRIMARY, href = "https://discord.com/invite/65UjScXNFg") {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.GamerSafer.DiscordGamerSafer))
                            }
                        }
                    }
                }
            )
        }
    }
}