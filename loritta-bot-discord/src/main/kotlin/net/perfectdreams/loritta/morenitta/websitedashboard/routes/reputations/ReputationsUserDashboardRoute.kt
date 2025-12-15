package net.perfectdreams.loritta.morenitta.websitedashboard.routes.reputations

import io.ktor.server.application.*
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.components.*
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import org.jetbrains.exposed.sql.selectAll

class ReputationsUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/reputations") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val (givenReputations, receivedReputations) = website.loritta.transaction {
            val givenReputations = Reputations.selectAll()
                .where {
                    Reputations.givenById eq session.userId
                }
                .count()

            val receivedReputations = Reputations.selectAll()
                .where {
                    Reputations.receivedById eq session.userId
                }
                .count()

            Pair(givenReputations, receivedReputations)
        }

        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.Reputations.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    userDashLeftSidebarEntries(website.loritta, i18nContext, userPremiumPlan, UserDashboardSection.REPUTATIONS)
                },
                {
                    heroWrapper {
                        heroText {
                            h1 {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.Reputations.Title))
                            }

                            p {
                                text("Reputações servem para você agradecer outro usuário por algo que ele fez. Alguém te ajudou em algo? Alguém contou uma piada e você caiu no chão de tanto rir? Então dê uma reputação para agradecer! Você pode dar reputações para alguém usando o ")
                                span(classes = "discord-mention") {
                                    text("/")
                                    text(i18nContext.get(I18nKeysData.Commands.Command.Rep.Label))
                                    text(" ")
                                    text(i18nContext.get(I18nKeysData.Commands.Command.Rep.Give.Label))
                                }
                                text(".")
                            }

                            p {
                                text("Você também pode deletar reputações! Enquanto você tem que pagar 30.000 sonhos para apagar uma reputação que foi enviada para outra pessoa, você pode deletar uma reputação que foi enviada para você sem precisar tirar sonhos do bolso.")
                            }
                        }
                    }

                    hr {}

                    cardsWithHeader {
                        cardHeader {
                            cardHeaderInfo {
                                cardHeaderTitle {
                                    text("Reputações")
                                }

                                cardHeaderDescription {
                                    text("$givenReputations reputações enviadas e $receivedReputations reputações recebidas")
                                }
                            }
                        }

                        div {
                            style = "display: flex; gap: 16px;"

                            discordButtonLink(ButtonStyle.PRIMARY, href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/reputations/given") {
                                classes += "text-with-icon"
                                style = "flex-grow: 1;"
                                swapRightSidebarContentsAttributes()

                                svgIcon(SVGIcons.ArrowUp)
                                text("Ver lista de reputações enviadas")
                            }
                            discordButtonLink(ButtonStyle.PRIMARY, href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/reputations/received") {
                                classes += "text-with-icon"
                                style = "flex-grow: 1;"
                                swapRightSidebarContentsAttributes()

                                svgIcon(SVGIcons.ArrowDown)
                                text("Ver lista de reputações recebidas")
                            }
                        }
                    }
                }
            )
        }
    }
}