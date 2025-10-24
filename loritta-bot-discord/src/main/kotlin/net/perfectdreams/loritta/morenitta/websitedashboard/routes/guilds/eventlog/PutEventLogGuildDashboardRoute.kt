package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.eventlog

import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.EventLogConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme
import java.time.Instant

class PutEventLogGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/event-log") {
    @Serializable
    data class SaveEventLogRequest(
        val eventLogChannelId: Long?,

        val memberBanned: Boolean,
        val memberBannedLogChannelId: Long?,

        val memberUnbanned: Boolean,
        val memberUnbannedLogChannelId: Long?,

        val messageEdited: Boolean,
        val messageEditedLogChannelId: Long?,

        val messageDeleted: Boolean,
        val messageDeletedLogChannelId: Long?,

        val nicknameChanges: Boolean,
        val nicknameChangesLogChannelId: Long?,

        val avatarChanges: Boolean,
        val avatarChangesLogChannelId: Long?,

        val voiceChannelJoins: Boolean,
        val voiceChannelJoinsLogChannelId: Long?,

        val voiceChannelLeaves: Boolean,
        val voiceChannelLeavesLogChannelId: Long?,
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans) {
        val request = Json.decodeFromString<SaveEventLogRequest>(call.receiveText())

        website.loritta.newSuspendedTransaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)
            val eventLogConfig = serverConfig.eventLogConfig

            if (request.eventLogChannelId == null) {
                serverConfig.eventLogConfig = null
                eventLogConfig?.delete()
            } else {
                val newConfig = eventLogConfig ?: EventLogConfig.new {
                    this.eventLogChannelId = -1
                }

                newConfig.enabled = true
                newConfig.eventLogChannelId = request.eventLogChannelId
                newConfig.memberBanned = request.memberBanned
                newConfig.memberBannedLogChannelId = request.memberBannedLogChannelId
                newConfig.memberUnbanned = request.memberUnbanned
                newConfig.memberUnbannedLogChannelId = request.memberUnbannedLogChannelId
                newConfig.messageEdited = request.messageEdited
                newConfig.messageEditedLogChannelId = request.messageEditedLogChannelId
                newConfig.messageDeleted = request.messageDeleted
                newConfig.messageDeletedLogChannelId = request.messageDeletedLogChannelId
                newConfig.nicknameChanges = request.nicknameChanges
                newConfig.nicknameChangesLogChannelId = request.nicknameChangesLogChannelId
                newConfig.avatarChanges = request.avatarChanges
                newConfig.avatarChangesLogChannelId = request.avatarChangesLogChannelId
                newConfig.voiceChannelJoins = request.voiceChannelJoins
                newConfig.voiceChannelJoinsLogChannelId = request.voiceChannelJoinsLogChannelId
                newConfig.voiceChannelLeaves = request.voiceChannelLeaves
                newConfig.voiceChannelLeavesLogChannelId = request.voiceChannelLeavesLogChannelId
                newConfig.updatedAt = Instant.now()

                serverConfig.eventLogConfig = newConfig
            }
        }

        call.respondConfigSaved(i18nContext)
    }
}