package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.punishmentlog

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationPunishmentMessagesConfig
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.ModerationConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

class PutPunishmentLogGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/punishment-log") {
    @Serializable
    data class SavePunishmentLogRequest(
        val sendPunishmentViaDirectMessage: Boolean,
        val sendPunishmentToPunishLog: Boolean,
        val punishLogChannelId: Long,
        val punishLogMessage: String,
        val enableMessageOverrideBan: Boolean,
        val punishLogMessageBan: String,
        val enableMessageOverrideKick: Boolean,
        val punishLogMessageKick: String,
        val enableMessageOverrideMute: Boolean,
        val punishLogMessageMute: String,
        val enableMessageOverrideWarn: Boolean,
        val punishLogMessageWarn: String,
        val enableMessageOverrideUnban: Boolean,
        val punishLogMessageUnban: String,
        val enableMessageOverrideUnmute: Boolean,
        val punishLogMessageUnmute: String
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val request = Json.decodeFromString<SavePunishmentLogRequest>(call.receiveText())

        website.loritta.newSuspendedTransaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)

            val moderationConfig = serverConfig.moderationConfig ?: ModerationConfig.new {
                this.sendPunishmentToPunishLog = false
                this.sendPunishmentViaDm = false
                this.punishLogMessage = null
                this.punishLogChannelId = null
            }

            ModerationPunishmentMessagesConfig.deleteWhere {
                ModerationPunishmentMessagesConfig.guild eq guild.idLong
            }

            moderationConfig.sendPunishmentViaDm = request.sendPunishmentViaDirectMessage
            moderationConfig.sendPunishmentToPunishLog = request.sendPunishmentToPunishLog
            moderationConfig.punishLogChannelId = request.punishLogChannelId
            moderationConfig.punishLogMessage = request.punishLogMessage

            serverConfig.moderationConfig = moderationConfig

            if (request.enableMessageOverrideBan) {
                ModerationPunishmentMessagesConfig.insert {
                    it[ModerationPunishmentMessagesConfig.guild] = serverConfig.id
                    it[ModerationPunishmentMessagesConfig.punishmentAction] = PunishmentAction.BAN
                    it[ModerationPunishmentMessagesConfig.punishLogMessage] = request.punishLogMessageBan
                }
            }

            if (request.enableMessageOverrideKick) {
                ModerationPunishmentMessagesConfig.insert {
                    it[ModerationPunishmentMessagesConfig.guild] = serverConfig.id
                    it[ModerationPunishmentMessagesConfig.punishmentAction] = PunishmentAction.KICK
                    it[ModerationPunishmentMessagesConfig.punishLogMessage] = request.punishLogMessageKick
                }
            }

            if (request.enableMessageOverrideMute) {
                ModerationPunishmentMessagesConfig.insert {
                    it[ModerationPunishmentMessagesConfig.guild] = serverConfig.id
                    it[ModerationPunishmentMessagesConfig.punishmentAction] = PunishmentAction.MUTE
                    it[ModerationPunishmentMessagesConfig.punishLogMessage] = request.punishLogMessageMute
                }
            }

            if (request.enableMessageOverrideWarn) {
                ModerationPunishmentMessagesConfig.insert {
                    it[ModerationPunishmentMessagesConfig.guild] = serverConfig.id
                    it[ModerationPunishmentMessagesConfig.punishmentAction] = PunishmentAction.WARN
                    it[ModerationPunishmentMessagesConfig.punishLogMessage] = request.punishLogMessageWarn
                }
            }

            if (request.enableMessageOverrideUnban) {
                ModerationPunishmentMessagesConfig.insert {
                    it[ModerationPunishmentMessagesConfig.guild] = serverConfig.id
                    it[ModerationPunishmentMessagesConfig.punishmentAction] = PunishmentAction.UNBAN
                    it[ModerationPunishmentMessagesConfig.punishLogMessage] = request.punishLogMessageUnban
                }
            }

            if (request.enableMessageOverrideUnmute) {
                ModerationPunishmentMessagesConfig.insert {
                    it[ModerationPunishmentMessagesConfig.guild] = serverConfig.id
                    it[ModerationPunishmentMessagesConfig.punishmentAction] = PunishmentAction.UNMUTE
                    it[ModerationPunishmentMessagesConfig.punishLogMessage] = request.punishLogMessageUnban
                }
            }
        }

        call.respondConfigSaved(i18nContext)
    }
}