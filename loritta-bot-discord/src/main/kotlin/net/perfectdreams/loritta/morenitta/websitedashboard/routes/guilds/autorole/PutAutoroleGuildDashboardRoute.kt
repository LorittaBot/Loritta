package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.autorole

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.AutoroleConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme

class PutAutoroleGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/autorole") {
    @Serializable
    data class SaveAutoroleRequest(
        val enabled: Boolean,
        val roles: Set<Long> = setOf(),
        val giveRolesAfter: Long,
        val giveOnlyAfterMessageWasSent: Boolean
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val request = Json.decodeFromString<SaveAutoroleRequest>(call.receiveText())

        website.loritta.newSuspendedTransaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)

            val autorole = serverConfig.autoroleConfig

            val newConfig = autorole ?: AutoroleConfig.new {
                this.enabled = request.enabled
                this.roles = request.roles.toList()
                this.giveRolesAfter = request.giveRolesAfter
                this.giveOnlyAfterMessageWasSent = request.giveOnlyAfterMessageWasSent
            }

            newConfig.enabled = request.enabled
            newConfig.roles = request.roles.toList()
            newConfig.giveRolesAfter = request.giveRolesAfter.coerceIn(0L..60L)
            newConfig.giveOnlyAfterMessageWasSent = request.giveOnlyAfterMessageWasSent

            serverConfig.autoroleConfig = newConfig
        }

        call.respondConfigSaved(i18nContext)
    }
}