package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserPocketLorittaSettings
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.ActivityLevel
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissCloseModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configSaved
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.upsert
import java.time.Instant

class PutLorittaSpawnerSettingsUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/loritta-spawner") {
    @Serializable
    data class UpdateLorittaSpawnerRequest(
        val lorittaCount: Int,
        val pantufaCount: Int,
        val gabrielaCount: Int,
        val activityLevel: ActivityLevel
    )

    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val request = Json.decodeFromString<UpdateLorittaSpawnerRequest>(call.receiveText())

        website.loritta.transaction {
            UserPocketLorittaSettings.upsert(UserPocketLorittaSettings.id) {
                it[UserPocketLorittaSettings.id] = session.userId
                it[UserPocketLorittaSettings.lorittaCount] = request.lorittaCount
                it[UserPocketLorittaSettings.pantufaCount] = request.pantufaCount
                it[UserPocketLorittaSettings.gabrielaCount] = request.gabrielaCount
                it[UserPocketLorittaSettings.activityLevel] = request.activityLevel
                it[UserPocketLorittaSettings.updatedAt] = Instant.now()
            }
        }

        call.respondHtml(
            createHTML(false)
                .body {
                    blissCloseModal()

                    configSaved(i18nContext)
                }
        )
    }
}