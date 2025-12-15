package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.inviteblocker

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
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.InviteBlockerConfig
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme

class PutInviteBlockerGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/invite-blocker") {
    @Serializable
    data class SaveInviteBlockerRequest(
        val enabled: Boolean,
        val allowServerInvites: Boolean,
        val deleteMessageOnInvite: Boolean,
        val sendMessageOnInvite: Boolean,
        val message: String,
        val channels: Set<Long> = setOf()
    )

    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val request = Json.decodeFromString<SaveInviteBlockerRequest>(call.receiveText())

        website.loritta.newSuspendedTransaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)

            val inviteBlocker = serverConfig.inviteBlockerConfig

            val newConfig = inviteBlocker ?: InviteBlockerConfig.new {
                this.enabled = request.enabled
                this.whitelistServerInvites = request.allowServerInvites
                this.deleteMessage = request.deleteMessageOnInvite
                this.tellUser = request.sendMessageOnInvite
                this.warnMessage = request.message
                this.whitelistedChannels = request.channels.toList()
            }

            newConfig.enabled = request.enabled
            newConfig.whitelistServerInvites = request.allowServerInvites
            newConfig.deleteMessage = request.deleteMessageOnInvite
            newConfig.tellUser = request.sendMessageOnInvite
            newConfig.warnMessage = request.message
            newConfig.whitelistedChannels = request.channels.toList()

            serverConfig.inviteBlockerConfig = newConfig
        }

        call.respondConfigSaved(i18nContext)
    }
}