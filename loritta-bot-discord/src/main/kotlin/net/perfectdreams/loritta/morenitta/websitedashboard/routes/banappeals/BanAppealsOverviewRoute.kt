package net.perfectdreams.loritta.morenitta.websitedashboard.routes.banappeals

import io.ktor.server.application.*
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.p
import kotlinx.html.style
import net.perfectdreams.etherealgambi.client.EtherealGambiClient
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.banAppealForm
import net.perfectdreams.loritta.morenitta.websitedashboard.components.banAppealWrapperBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.simpleHeroImage
import net.perfectdreams.loritta.morenitta.websitedashboard.components.websiteBase
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.UserId

class BanAppealsOverviewRoute(website: LorittaDashboardWebServer) : RequiresUserAuthBanAppealsLocalizedRoute(website, "/") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val userId = call.parameters["userId"]?.toLongOrNull()

        val bannedUserId = userId ?: session.userId
        val banState = website.loritta.pudding.users.getUserBannedState(UserId(bannedUserId))
        val userInfo = website.loritta.lorittaShards.retrieveUserInfoById(bannedUserId) ?: error("Unknown user!")

        call.respondHtml {
            banAppealWrapperBase(
                i18nContext,
                "Apelo de Bans",
                theme,
                shimejiSettings
            ) {
                heroWrapper {
                    simpleHeroImage("https://stuff.loritta.website/emotes/lori-bonk.png")

                    heroText {
                        h1 {
                            text("Central de Apelos da Loritta")
                        }

                        p {
                            text("Se você foi banido da Loritta, você pode enviar um apelo para conseguir uma segunda chance.")
                        }
                    }
                }

                hr {}

                if (banState == null) {
                    div {
                        style = "text-align: center;"

                        h2 {
                            text("Você não está banido da Loritta!")
                        }

                        div {
                            text("Obrigado por respeitar as regras da Loritta!")
                        }

                        discordButton(ButtonStyle.PRIMARY) {
                            style = "margin-top: 24px;"
                            attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/override"
                            attributes["bliss-swap:200"] = "#ban-appeal-content (innerHTML) -> #ban-appeal-content (innerHTML)"
                            attributes["bliss-push-url:200"] = "true"

                            text("Eu estou banido em outra conta")
                        }
                    }
                } else {
                    div {
                        style = "text-align: center;"

                        h2 {
                            text("Você está banido da Loritta!")
                        }

                        div {
                            text("Motivo: ${banState.reason}!")
                        }

                        discordButton(ButtonStyle.PRIMARY) {
                            style = "margin-top: 24px;"
                            attributes["bliss-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/form"
                            attributes["bliss-swap:200"] = "#ban-appeal-content (innerHTML) -> #ban-appeal-content (innerHTML)"
                            attributes["bliss-push-url:200"] = "true"

                            text("Enviar Apelo")
                        }
                    }
                }
            }
        }
    }
}