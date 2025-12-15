package net.perfectdreams.loritta.morenitta.websitedashboard.routes.banappeals

import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import kotlinx.html.div
import kotlinx.html.hiddenInput
import kotlinx.html.style
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.inlineNullableUserDisplay
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.UserId

class PostBanAppealsAccountIdsRoute(website: LorittaDashboardWebServer) : RequiresUserAuthBanAppealsLocalizedRoute(website, "/form/account-ids") {
    @Serializable
    data class AccountIdsRequest(
        val formUserId: Long,
        val accountIdsRaw: String
    )

    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val request = Json.decodeFromString<AccountIdsRequest>(call.receiveText())
        val accountIds = request.accountIdsRaw
            .split(Regex("[,.\n ]"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapNotNull { it.toLongOrNull() }
            .distinct()
            .filter { it != request.formUserId }
            .take(50) // Limit to 50 accounts

        val accounts = accountIds.mapNotNull {
            website.loritta.lorittaShards.retrieveUserInfoById(it)
        }

        val banStateOfEachAccount = accounts.associate {
            it.id to website.loritta.pudding.users.getUserBannedState(UserId(it.id))
        }

        call.respondHtmlFragment {
            for (account in accounts) {
                div {
                    div {
                        style = "color: var(--loritta-green); font-weight: bold;"
                        inlineNullableUserDisplay(account.id, account)
                    }

                    val banState = banStateOfEachAccount[account.id]

                    if (banState != null) {
                        div {
                            text("Conta banida por ${banState.reason}")
                        }
                    } else {
                        div {
                            text("Conta não está banida!")
                        }
                    }
                }

                hiddenInput {
                    attributes["loritta-ban-appeal-attribute"] = "true"
                    name = "accountIds[]"
                    value = account.id.toString()
                }
            }
        }
    }
}