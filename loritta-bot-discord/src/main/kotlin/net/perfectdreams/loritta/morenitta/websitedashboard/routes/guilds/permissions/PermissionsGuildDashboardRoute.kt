package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.permissions

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
import net.perfectdreams.loritta.morenitta.websitedashboard.components.cardHeader
import net.perfectdreams.loritta.morenitta.websitedashboard.components.cardHeaderDescription
import net.perfectdreams.loritta.morenitta.websitedashboard.components.cardHeaderInfo
import net.perfectdreams.loritta.morenitta.websitedashboard.components.cardHeaderTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.cardsWithHeader
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButtonLink
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.components.svgIcon
import net.perfectdreams.loritta.morenitta.websitedashboard.components.swapRightSidebarContentsAttributes
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import java.awt.Color

class PermissionsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/permissions") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.Permissions.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.PERMISSIONS)
                },
                {
                    cardsWithHeader {
                        cardHeader {
                            cardHeaderInfo {
                                cardHeaderTitle {
                                    text(i18nContext.get(DashboardI18nKeysData.Permissions.GuildRoles))
                                }

                                cardHeaderDescription {
                                    text(i18nContext.get(DashboardI18nKeysData.Permissions.Roles(guild.roles.size)))
                                }
                            }
                        }

                        div(classes = "cards") {
                            for (role in guild.roles) {
                                val roleIcon = role.icon
                                val roleColor = role.color ?: Color(153, 170, 181)

                                div(classes = "card") {
                                    style = "flex-direction: row; align-items: center; gap: 0.5em;"

                                    div {
                                        style = "flex-grow: 1; display: flex; gap: 0.5em; align-items: center;"

                                        if (roleIcon != null && !roleIcon.isEmoji) {
                                            img(src = roleIcon.iconUrl) {
                                                style = "width: 1.5em; height: 1.5em; object-fit: contain;"
                                            }
                                        } else {
                                            svgIcon(SVGIcons.RoleShield) {
                                                attr("style", "color: rgb(${roleColor.red}, ${roleColor.green}, ${roleColor.blue}); width: 1.5em; height: 1.5em;")
                                            }
                                        }

                                        div {
                                            text(role.name)
                                        }
                                    }

                                    div {
                                        style = "display: grid;grid-template-columns: 1fr;grid-column-gap: 0.5em;"

                                        discordButtonLink(ButtonStyle.PRIMARY, href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/permissions/${role.idLong}") {
                                            swapRightSidebarContentsAttributes()
                                            text("Editar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}