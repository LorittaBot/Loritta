package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.html.body
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSettings
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseAllModals
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissSoundEffect
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.upsert
import java.time.Instant

class PostDashboardThemeGuildUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/theme") {
    @Serializable
    data class DashboardThemeRequest(
        val theme: ColorTheme
    )

    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val request = Json.decodeFromString<DashboardThemeRequest>(call.receiveText())

        website.loritta.transaction {
            UserWebsiteSettings.upsert(UserWebsiteSettings.id) {
                it[UserWebsiteSettings.id] = session.userId
                it[UserWebsiteSettings.dashboardColorThemePreference] = request.theme
                it[UserWebsiteSettings.dashboardColorThemePreferenceUpdatedAt] = Instant.now()
            }
        }

        call.respondHtmlFragment {
            blissCloseAllModals()

            blissShowToast(
                createEmbeddedToast(
                    EmbeddedToast.Type.SUCCESS,
                    i18nContext.get(DashboardI18nKeysData.ThemeSelector.ThemeChanged)
                )
            )

            blissSoundEffect("configSaved")

            script(type = "application/json") {
                attributes["bliss-set-attributes"] = "#app-wrapper"
                attributes["bliss-attributes"] = buildJsonObject {
                    put("class", request.theme.className)
                }.toString()
            }
        }
    }
}