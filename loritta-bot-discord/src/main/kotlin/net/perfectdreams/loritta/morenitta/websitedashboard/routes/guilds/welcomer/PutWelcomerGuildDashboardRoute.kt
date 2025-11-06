package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.welcomer

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
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.WelcomerConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme

class PutWelcomerGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/welcomer") {
    @Serializable
    data class SaveWelcomerRequest(
        val tellOnJoin: Boolean,
        val channelJoinId: Long,
        val deleteJoinMessagesAfter: Long,
        val joinMessage: String,

        val tellOnRemove: Boolean,
        val channelRemoveId: Long,
        val deleteRemoveMessagesAfter: Long,
        val removeMessage: String,

        val tellOnBan: Boolean,
        val bannedMessage: String,

        val tellOnPrivateJoin: Boolean,
        val joinPrivateMessage: String
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val request = Json.decodeFromString<SaveWelcomerRequest>(call.receiveText())

        website.loritta.newSuspendedTransaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)

            val welcomerConfig = serverConfig.welcomerConfig

            val newConfig = welcomerConfig ?: WelcomerConfig.new {
                this.tellOnJoin = false
                this.channelJoinId = null
                this.joinMessage = null
                this.deleteJoinMessagesAfter = null

                this.tellOnRemove = false
                this.channelRemoveId = null
                this.removeMessage = null
                this.deleteRemoveMessagesAfter = null

                this.tellOnPrivateJoin = false
                this.joinPrivateMessage = null

                this.tellOnBan = false
                this.bannedMessage = null
            }

            newConfig.tellOnJoin = request.tellOnJoin
            newConfig.channelJoinId = request.channelJoinId
            newConfig.joinMessage = request.joinMessage
            newConfig.deleteJoinMessagesAfter = request.deleteJoinMessagesAfter.coerceIn(0L..60L)

            newConfig.tellOnRemove = request.tellOnRemove
            newConfig.channelRemoveId = request.channelRemoveId
            newConfig.removeMessage = request.removeMessage
            newConfig.deleteRemoveMessagesAfter = request.deleteRemoveMessagesAfter.coerceIn(0L..60L)

            newConfig.tellOnBan = request.tellOnBan
            newConfig.bannedMessage = request.bannedMessage
            
            newConfig.tellOnPrivateJoin = request.tellOnPrivateJoin
            newConfig.joinPrivateMessage = request.joinPrivateMessage

            serverConfig.welcomerConfig = newConfig
        }

        call.respondConfigSaved(i18nContext)
    }
}