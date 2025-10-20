package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.quirkymode

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.MiscellaneousConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme

class PutQuirkyModeGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/quirky-mode") {
    @Serializable
    data class SaveQuirkyModeRequest(
        val enableQuirky: Boolean
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val request = Json.decodeFromString<SaveQuirkyModeRequest>(call.receiveText())

        website.loritta.newSuspendedTransaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)

            val miscellaneousConfig = serverConfig.miscellaneousConfig

            val newConfig = miscellaneousConfig ?: MiscellaneousConfig.new {
                this.enableQuirky = false
                this.enableBomDiaECia = false
            }

            newConfig.enableQuirky = request.enableQuirky
            newConfig.enableBomDiaECia = false

            serverConfig.miscellaneousConfig = newConfig
        }

        call.respondConfigSaved(i18nContext)
    }
}