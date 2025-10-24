package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprates

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ExperienceRoleRates
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

class PutXPRatesGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/xp-rates") {
    @Serializable
    data class SaveXPRewardsRequest(
        val roles: List<RoleRate> = listOf()
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val request = Json.decodeFromString<SaveXPRewardsRequest>(call.receiveText())

        website.loritta.transaction {
            // Deletar todas que já existem
            ExperienceRoleRates.deleteWhere {
                ExperienceRoleRates.guildId eq guild.idLong
            }

            for (roleRate in request.roles) {
                ExperienceRoleRates.insert {
                    it[ExperienceRoleRates.guildId] = guild.idLong
                    it[ExperienceRoleRates.role] = roleRate.roleId
                    it[ExperienceRoleRates.rate] = roleRate.rate
                }
            }
        }

        call.respondConfigSaved(i18nContext)
    }
}