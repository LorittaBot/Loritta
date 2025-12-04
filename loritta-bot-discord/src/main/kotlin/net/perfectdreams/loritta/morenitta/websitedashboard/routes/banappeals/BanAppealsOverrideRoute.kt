package net.perfectdreams.loritta.morenitta.websitedashboard.routes.banappeals

import io.ktor.server.application.*
import kotlinx.html.a
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
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldDescription
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldInformation
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldInformationBlock
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldTitle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.fieldWrappers
import net.perfectdreams.loritta.morenitta.websitedashboard.components.goBackToPreviousSectionButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.websiteBase
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme

class BanAppealsOverrideRoute(website: LorittaDashboardWebServer) : RequiresUserAuthBanAppealsLocalizedRoute(website, "/override") {
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
                        attributes["bliss-swap:200"] = "#ban-appeal-content (innerHTML) -> #ban-appeal-content (innerHTML)"
                        attributes["bliss-push-url:200"] = "true"
                    }
                ) {
                    text("Voltar")
                }

                hr {}

                fieldWrappers {
                    fieldWrapper {
                        fieldInformationBlock {
                            fieldTitle {
                                text("ID da sua conta banida")
                            }

                            fieldDescription {
                                div {
                                    text("Se você perdeu acesso a sua conta banida ou está usando outra conta, você pode enviar um apelo para ela colocando o ID dela aqui!")
                                }

                                div {
                                    text("Não sabe copiar IDs? Então ")
                                    a(href = "https://support.discord.com/hc/pt-br/articles/206346498-Onde-posso-encontrar-minhas-IDs-de-usu%C3%A1rio-servidor-e-mensagem", target = "_blank") {
                                        text("clique aqui")
                                    }
                                    text("!")
                                }
                            }
                        }

                        textInput {
                            name = "userId"
                        }
                    }

                    fieldWrapper {
                        discordButton(ButtonStyle.PRIMARY) {
                            attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/override"
                            attributes["bliss-include-json"] = "[name='userId']"
                            attributes["bliss-swap:200"] = "body (innerHTML) -> #ban-appeal-content (innerHTML)"
                            text("Enviar")
                        }
                    }
                }
            }
        }
    }
}