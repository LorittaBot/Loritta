package net.perfectdreams.loritta.morenitta.websitedashboard.routes.banappeals

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import io.ktor.server.response.header
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.banAppealForm
import net.perfectdreams.loritta.morenitta.websitedashboard.components.goBackToPreviousSectionButton
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroText
import net.perfectdreams.loritta.morenitta.websitedashboard.components.heroWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.UserId

class PostBanAppealsOverrideRoute(website: LorittaDashboardWebServer) : RequiresUserAuthBanAppealsLocalizedRoute(website, "/override") {
    @Serializable
    data class BanAppealOverrideRequest(
        val userId: String
    )

    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val request = Json.decodeFromString<BanAppealOverrideRequest>(call.receiveText())
        val userId = request.userId.trim().toLongOrNull()

        if (userId == null) {
            call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        "Você não colocou um ID válido!"
                    )
                )
            }
            return
        }

        val bannedState = website.loritta.pudding.users.getUserBannedState(UserId(userId))
        if (bannedState == null) {
            call.respondHtmlFragment(status = HttpStatusCode.BadRequest) {
                blissShowToast(
                    createEmbeddedToast(
                        EmbeddedToast.Type.WARN,
                        "Usuário não está banido!"
                    )
                )
            }
            return
        }

        call.response.header("Bliss-Push-Url", "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/form?userId=$userId")

        val userInfo = website.loritta.lorittaShards.retrieveUserInfoById(userId) ?: error("Unknown user!")

        call.respondHtmlFragment {
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

            banAppealForm(i18nContext, userId, userInfo, bannedState)
        }
    }
}