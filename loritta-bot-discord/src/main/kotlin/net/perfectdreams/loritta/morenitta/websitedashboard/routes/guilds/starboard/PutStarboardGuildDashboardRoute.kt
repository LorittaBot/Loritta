package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.starboard

import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.StarboardConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme

class PutStarboardGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/starboard") {
    @Serializable
    data class SaveStarboardRequest(
        val enabled: Boolean,
        val starboardChannelId: Long,
        val requiredStars: Int
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val request = Json.decodeFromString<SaveStarboardRequest>(call.receiveText())

        website.loritta.transaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)
            val starboardConfig = serverConfig.starboardConfig

            if (!request.enabled) {
                serverConfig.starboardConfig = null
                starboardConfig?.delete()
            } else {
                val newConfig = starboardConfig ?: StarboardConfig.new {
                    this.enabled = false
                    this.starboardChannelId = -1
                    this.requiredStars = 1
                }

                newConfig.enabled = true
                newConfig.starboardChannelId = request.starboardChannelId
                newConfig.requiredStars = request.requiredStars

                serverConfig.starboardConfig = newConfig
            }
        }

        call.respondConfigSaved(i18nContext)
    }
}