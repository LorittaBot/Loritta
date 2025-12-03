package net.perfectdreams.loritta.morenitta.websitedashboard.routes.banappeals

import io.ktor.server.application.*
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.id
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
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButtonLink
import net.perfectdreams.loritta.morenitta.websitedashboard.components.goBackToPreviousSectionButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.components.websiteBase
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtml
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.UserId

class BanAppealsFormRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/form") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val userId = call.parameters["userId"]?.toLongOrNull()

        val bannedUserId = userId ?: session.userId
        val banState = website.loritta.pudding.users.getUserBannedState(UserId(bannedUserId))
        val userInfo = website.loritta.lorittaShards.retrieveUserInfoById(bannedUserId) ?: error("Unknown user!")

        // TODO: Check cooldown state here AND on the override POST too!
        //  The check must check who sent the appeal + the session ID

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

                if (banState == null) {
                    div {
                        text("Atualmente você não está banido!")
                    }
                } else {
                    banAppealForm(i18nContext, bannedUserId, userInfo, banState)
                }
            }
        }
    }
}