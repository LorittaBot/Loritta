package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.resetxp

import io.ktor.server.application.*
import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.simpleHeroImage
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedConfirmDeletionModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.openModalOnClick
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme

class ResetXPGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/reset-xp") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.ResetXp.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.RESET_XP)
                },
                {
                    heroWrapper {
                        simpleHeroImage("https://stuff.loritta.website/loritta-stop-heathecliff.png")

                        heroText {
                            h1 {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.ResetXp.Title))
                            }

                            p {
                                text("Esta ação é irreversível! Todos os membros do seu servidor terão os seus níveis e experiência revertidos para 0!")
                            }

                            p {
                                text("Após confirmar, você não poderá reverter a ação. A ação apenas reverte o nível e a experiência, você terá que reverter as recompensas ganhas ao subirem de nível manualmente.")
                            }

                            discordButton(ButtonStyle.DANGER) {
                                openModalOnClick(
                                    createEmbeddedConfirmDeletionModal(i18nContext) {
                                        attributes["bliss-post"] = "/${i18nContext.get(I18nKeys.Website.LocalePathId)}/guilds/${guild.idLong}/reset-xp"
                                    }
                                )

                                text("Resetar XP")
                            }
                        }
                    }
                }
            )
        }
    }
}