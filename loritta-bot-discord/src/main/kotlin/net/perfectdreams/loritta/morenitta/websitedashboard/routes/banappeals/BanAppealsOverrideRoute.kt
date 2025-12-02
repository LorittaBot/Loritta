package net.perfectdreams.loritta.morenitta.websitedashboard.routes.banappeals

import io.ktor.server.application.*
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.style
import kotlinx.html.textInput
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.banAppealWrapperBase
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldInformation
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.goBackToPreviousSectionButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.websiteBase
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme

class BanAppealsOverrideRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/override") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        call.respondHtml {
            banAppealWrapperBase(
                i18nContext,
                "Apelo de Bans",
                theme,
                shimejiSettings
            ) {
                goBackToPreviousSectionButton(
                    "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/",
                    attrs = {
                        attributes["bliss-get"] = "[href]"
                        attributes["bliss-swap:200"] = "#appeal-form (innerHTML) -> #appeal-form (innerHTML)"
                        attributes["bliss-push-url:200"] = "true"
                    }
                ) {
                    text("Voltar")
                }

                hr {}

                heroWrapper {
                    heroText {
                        h1 {
                            text("Apelo de Ban da Loritta")
                        }
                    }
                }

                hr {}

                fieldWrappers {
                    fieldWrapper {
                        fieldInformation("ID da sua conta")

                        textInput {
                            name = "userId"
                        }
                    }

                    fieldWrapper {
                        discordButton(ButtonStyle.PRIMARY) {
                            attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/override"
                            attributes["bliss-include-json"] = "[name='userId']"
                            attributes["bliss-swap:200"] = "body (innerHTML) -> #appeal-form (innerHTML)"
                            text("Enviar")
                        }
                    }
                }
            }
        }
    }
}