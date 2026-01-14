package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprewards

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.RolesByExperience
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.TrackedChangeType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.LevelConfig
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.RoleValidationResult
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.WebAuditLogUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondRoleValidationError
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.validateRolesForConfiguration
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

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val request = Json.decodeFromString<SaveXPRewardsRequest>(call.receiveText())

        // Extract role IDs from rewards
        val roleIds = request.roles.map { it.roleId }

        // Validate roles to prevent privilege escalation
        val validationResult = validateRolesForConfiguration(
            guild,
            member,
            roleIds
        )

        if (validationResult !is RoleValidationResult.Success) {
            call.respondRoleValidationError(validationResult, i18nContext)
            return
        }

        website.loritta.transaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)

            val levelConfig = serverConfig.levelConfig ?: LevelConfig.new {
                this.roleGiveType = RoleGiveType.STACK
                this.noXpChannels = listOf()
                this.noXpRoles = listOf()
            }

            // Main
            serverConfig.levelConfig = levelConfig
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

            WebAuditLogUtils.addEntry(
                guild.idLong,
                session.userId,
                call.request.trueIp,
                call.request.userAgent(),
                TrackedChangeType.CHANGED_XP_REWARDS
            )
        }

        call.respondConfigSaved(i18nContext)
    }
}