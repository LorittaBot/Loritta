package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.membercounter

import io.ktor.server.application.*
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.GuildDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
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
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIconUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import java.awt.Color

class MemberCounterGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/member-counter") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.MemberCounter.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    guildDashLeftSidebarEntries(i18nContext, guild, userPremiumPlan, GuildDashboardSection.MEMBER_COUNTER)
                },
                {
                    val validChannels = guild.channels.filterIsInstance<StandardGuildMessageChannel>()

                    cardsWithHeader {
                        cardHeader {
                            cardHeaderInfo {
                                cardHeaderTitle {
                                    text(i18nContext.get(DashboardI18nKeysData.MemberCounter.GuildChannels))
                                }

                                cardHeaderDescription {
                                    text(i18nContext.get(DashboardI18nKeysData.MemberCounter.Channels(validChannels.size)))
                                }
                            }
                        }

                        div(classes = "cards") {
                            for (channel in validChannels) {
                                div(classes = "card") {
                                    style = "flex-direction: row; align-items: center; gap: 0.5em;"

                                    div {
                                        style = "flex-grow: 1; display: flex; gap: 0.5em; align-items: center;"

                                        val svgIcon = SVGIconUtils.getSVGIconForChannel(guild, channel)

                                        svgIcon(svgIcon) {
                                            attr("style", "width: 1.5em; height: 1.5em;")
                                        }

                                        div {
                                            text(channel.name)
                                        }
                                    }

                                    div {
                                        style = "display: grid;grid-template-columns: 1fr;grid-column-gap: 0.5em;"

                                        discordButtonLink(ButtonStyle.PRIMARY, href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/member-counter/${channel.idLong}") {
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