package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprewards

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.RolesByExperience
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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

class PutXPRewardsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/xp-rewards") {
    @Serializable
    data class SaveXPRewardsRequest(
        val roleGiveType: RoleGiveType,
        val roles: List<RoleReward> = listOf()
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val request = Json.decodeFromString<SaveXPRewardsRequest>(call.receiveText())

        website.loritta.transaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)

            val levelConfig = serverConfig.levelConfig ?: LevelConfig.new {
                this.roleGiveType = RoleGiveType.STACK
            }

            // Main
            levelConfig.roleGiveType = request.roleGiveType

            // Deletar todas que já existem
            RolesByExperience.deleteWhere {
                RolesByExperience.guildId eq serverConfig.guildId
            }

            for (roleByExperience in request.roles) {
                val requiredExperience = roleByExperience.xp

                RolesByExperience.insert {
                    it[RolesByExperience.guildId] = serverConfig.guildId
                    it[RolesByExperience.requiredExperience] = Math.max(Math.min(10000000, requiredExperience), 0)
                    it[RolesByExperience.roles] = listOf(roleByExperience.roleId)
                }
            }
        }

        call.respondConfigSaved(i18nContext)
    }
}