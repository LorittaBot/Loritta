package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.dailymultiplier

import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.dao.DonationConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme

class PutDailyMultiplierGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/daily-multiplier") {
    @Serializable
    data class SaveDailyMultiplierRequest(
        val enabled: Boolean
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val request = Json.decodeFromString<SaveDailyMultiplierRequest>(call.receiveText())

        website.loritta.newSuspendedTransaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)

            val newConfig = serverConfig.donationConfig ?: DonationConfig.new {
                this.dailyMultiplier = request.enabled
                this.customBadge = false
                this.customBadgeFile = null
                this.customBadgePreferredMediaType = null
            }

            newConfig.dailyMultiplier = request.enabled

            serverConfig.donationConfig = newConfig
        }

        call.respondConfigSaved(i18nContext)
    }
}