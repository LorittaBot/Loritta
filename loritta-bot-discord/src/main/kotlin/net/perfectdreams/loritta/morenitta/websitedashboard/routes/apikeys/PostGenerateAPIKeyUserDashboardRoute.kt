package net.perfectdreams.loritta.morenitta.websitedashboard.routes.apikeys

import io.ktor.server.application.ApplicationCall
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserLorittaAPITokens
import net.perfectdreams.loritta.common.utils.TokenType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.Base58
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.tokenInputWrapper
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

class PostGenerateAPIKeyUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/api-keys/generate") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val requesterId = session.userId

        val apiToken = website.loritta.transaction {
            // Delete all tokens that are related to this user
            UserLorittaAPITokens.deleteWhere {
                UserLorittaAPITokens.tokenCreatorId eq requesterId
            }

            val randomBytes = ByteArray(32).apply {
                website.loritta.random.nextBytes(this)
            }
            val token = "lorixp_${Base58.encode(randomBytes)}"

            // And now we'll insert a new token!
            UserLorittaAPITokens.insert {
                it[UserLorittaAPITokens.tokenCreatorId] = requesterId
                it[UserLorittaAPITokens.tokenUserId] = requesterId
                it[UserLorittaAPITokens.tokenType] = TokenType.USER
                it[UserLorittaAPITokens.token] = token
                it[UserLorittaAPITokens.generatedAt] = java.time.Instant.now()
            }

            return@transaction token
        }

        call.respondHtmlFragment {
            blissShowToast(
                createEmbeddedToast(
                    EmbeddedToast.Type.SUCCESS,
                    i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.Toast.TokenRegeneratedTitle),
                    {
                        text(i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.Toast.TokenRegeneratedDescription))
                    }
                )
            )

            tokenInputWrapper(i18nContext, apiToken)
        }
    }
}