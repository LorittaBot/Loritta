package net.perfectdreams.loritta.morenitta.websitedashboard.routes.notifications

import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserNotificationSettings
import net.perfectdreams.loritta.common.utils.NotificationType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.batchUpsert
import java.time.Instant

class PutNotificationsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/notifications") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val request = Json.decodeFromString<Map<NotificationType, Boolean>>(call.receiveText())

        website.loritta.transaction {
            UserNotificationSettings.batchUpsert(request.entries, UserNotificationSettings.userId, UserNotificationSettings.type) {
                this[UserNotificationSettings.userId] = session.userId
                this[UserNotificationSettings.type] = it.key
                this[UserNotificationSettings.enabled] = it.value
                this[UserNotificationSettings.configuredAt] = Instant.now()
            }
        }

        call.respondConfigSaved(i18nContext)
    }
}