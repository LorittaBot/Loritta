package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.commandchannels

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.configSaved
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme

class PutCommandChannelsConfigurationGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/command-channels") {
    @Serializable
    data class CommandChannelsRequest(
        val channels: Set<Long> = setOf(),
        val warnIfBlacklisted: Boolean,
        val blockedWarning: String
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val request = Json.decodeFromString<CommandChannelsRequest>(call.receiveText())

        website.loritta.newSuspendedTransaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)

            serverConfig.blacklistedChannels = request.channels.take(DiscordResourceLimits.Guild.Channels)
            serverConfig.warnIfBlacklisted = request.warnIfBlacklisted
            if (request.warnIfBlacklisted) {
                serverConfig.blacklistedWarning = request.blockedWarning
            } else {
                serverConfig.blacklistedWarning = null
            }
        }

        call.respondConfigSaved(i18nContext)
    }
}