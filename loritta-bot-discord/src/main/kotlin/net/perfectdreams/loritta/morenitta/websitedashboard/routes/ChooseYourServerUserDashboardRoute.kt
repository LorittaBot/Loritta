package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.server.application.*
import kotlinx.html.a
import kotlinx.html.b
import kotlinx.html.br
import kotlinx.html.div
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.textInput
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserDashboardSection
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.dashboardBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.loadingSpinnerImage
import net.perfectdreams.loritta.morenitta.websitedashboard.components.userDashLeftSidebarEntries
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme

class ChooseYourServerUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        call.respondHtml {
            dashboardBase(
                i18nContext,
                i18nContext.get(DashboardI18nKeysData.ChooseAServer.Title),
                session,
                theme,
                shimejiSettings,
                userPremiumPlan,
                website.shouldDisplayAds(call, userPremiumPlan, null),
                {
                    userDashLeftSidebarEntries(website.loritta, i18nContext, userPremiumPlan, UserDashboardSection.CHOOSE_YOUR_SERVER)
                },
                {
                    div {
                        style = "display: flex; flex-direction: column; gap: 16px;"

                        div(classes = "alert alert-success") {
                            div {
                                b {
                                    text("Novo Painel da Loritta!")
                                }
                            }

                            div {
                                text("Você está usando o NOVO painel da Loritta! A Loritta merecia um painel melhor que o antigo painel dela, então refizemos o painel dela do ZERO, agora com mais facilidade de usar e, é claro, mais bonito! Ele ainda está em beta, então pode ser que ainda tenha alguns probleminhas por aí, já que para o beta sobra nada.")
                            }

                            br {}

                            div {
                                text("(A Loritta acabou de me avisar que \"beta\" em programação não tem a ver com ser sigma)")
                            }

                            br {}

                            div {
                                text("Achou algum problema, tem sugestões, ou apenas quer mandar um elogio sobre o novo painel? Então entre em nosso ")
                                a(href = "${website.loritta.config.loritta.website.url}/${i18nContext.get(I18nKeysData.Website.LocalePathId)}support", target = "_blank") {
                                    text("servidor de suporte")
                                }
                                text("!")
                            }

                            br {}

                            div {
                                text("Ah, e sabia que agora o tema escuro do painel *realmente* funciona bem? :3")
                            }
                        }

                        div {
                            id = "user-guilds"
                            attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds"
                            attributes["bliss-trigger"] = "load"
                            attributes["bliss-swap:200"] = "body (innerHTML) -> this (innerHTML)"
                            attributes["bliss-indicator"] = "this"

                            div(classes = "fill-loading-screen") {
                                loadingSpinnerImage()

                                text("Carregando Servidores...")
                            }
                        }
                    }
                }
            )
        }
    }
}