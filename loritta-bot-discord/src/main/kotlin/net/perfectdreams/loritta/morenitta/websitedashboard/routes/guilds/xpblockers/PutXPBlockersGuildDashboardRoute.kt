package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xpblockers

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.LevelConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.levels.RoleGiveType
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings

class PutXPBlockersGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/xp-blockers") {
    @Serializable
    data class SaveXPBlockersRequest(
        val roles: Set<Long> = setOf(),
        val channels: Set<Long> = setOf()
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val request = Json.decodeFromString<SaveXPBlockersRequest>(call.receiveText())

        website.loritta.transaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)
            val levelConfig = serverConfig.levelConfig ?: LevelConfig.new {
                this.roleGiveType = RoleGiveType.STACK
                this.noXpChannels = listOf()
                this.noXpRoles = listOf()
            }

            levelConfig.noXpChannels = request.channels.toList()
            levelConfig.noXpRoles = request.roles.toList()

            serverConfig.levelConfig = levelConfig
        }

        call.respondConfigSaved(i18nContext)
    }
}