package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xpnotifications

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.LevelAnnouncementConfigs
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.LevelConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.levels.LevelUpAnnouncementType
import net.perfectdreams.loritta.serializable.levels.RoleGiveType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

class PutXPNotificationsGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/xp-notifications") {
    @Serializable
    data class SaveXPNotificationsRequest(
        val enabled: Boolean,
        val type: LevelUpAnnouncementType,
        val customChannelId: Long,
        val onlyIfUserReceivedRoles: Boolean,
        val message: String
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme, guild: Guild) {
        val request = Json.decodeFromString<SaveXPNotificationsRequest>(call.receiveText())

        website.loritta.transaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)
            val levelConfig = serverConfig.levelConfig ?: LevelConfig.new {
                this.roleGiveType = RoleGiveType.STACK
                this.noXpChannels = listOf()
                this.noXpRoles = listOf()
            }

            // Announcements
            // Deletar todas que já existem
            LevelAnnouncementConfigs.deleteWhere {
                LevelAnnouncementConfigs.levelConfig eq levelConfig.id
            }

            if (request.enabled) {
                LevelAnnouncementConfigs.insert {
                    it[LevelAnnouncementConfigs.levelConfig] = levelConfig.id
                    it[LevelAnnouncementConfigs.type] = request.type
                    it[LevelAnnouncementConfigs.channelId] = request.customChannelId
                    it[LevelAnnouncementConfigs.onlyIfUserReceivedRoles] = request.onlyIfUserReceivedRoles
                    it[LevelAnnouncementConfigs.message] = request.message
                }
            }

            serverConfig.levelConfig = levelConfig
        }

        call.respondConfigSaved(i18nContext)
    }
}