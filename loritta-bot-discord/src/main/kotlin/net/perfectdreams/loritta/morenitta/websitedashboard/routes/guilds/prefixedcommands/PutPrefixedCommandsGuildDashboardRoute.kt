package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.prefixedcommands

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
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme

class PutPrefixedCommandsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/prefixed-commands") {
    @Serializable
    data class SavePrefixedCommandsRequest(
        val prefix: String,
        val deleteMessageAfterCommand: Boolean,
        val warnOnUnknownCommand: Boolean,
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val request = Json.decodeFromString<SavePrefixedCommandsRequest>(call.receiveText())

        website.loritta.transaction {
            val config = website.loritta.getOrCreateServerConfig(guild.idLong)

            config.commandPrefix = request.prefix
            config.deleteMessageAfterCommand = request.deleteMessageAfterCommand
            config.warnOnUnknownCommand = request.warnOnUnknownCommand
        }

        call.respondConfigSaved(i18nContext)
    }
}