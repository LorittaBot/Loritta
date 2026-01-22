package net.perfectdreams.loritta.morenitta.rpc.commands

import dev.minn.jda.ktx.generics.getChannel
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.attribute.IInviteContainer
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.pudding.tables.LorittaPartnersServerInvites
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.rpc.payloads.CreatePartnerInviteRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.CreatePartnerInviteResponse
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import org.jetbrains.exposed.sql.insert
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

class CreatePartnerInviteCommand(val loritta: LorittaBot) : LorittaRPCCommand(LorittaRPC.CreatePartnerInvite) {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
        const val INVITE_MAX_AGE_SECONDS = 3600L // 1 hour
        const val INVITE_MAX_USES = 1
    }

    override suspend fun onRequest(call: ApplicationCall) {
        val request = Json.decodeFromString<CreatePartnerInviteRequest>(call.receiveText())

        val guild = loritta.lorittaShards.getGuildById(request.partnerGuildId)
        if (guild == null) {
            call.respondRPCResponse<CreatePartnerInviteResponse>(CreatePartnerInviteResponse.GuildNotFound)
            return
        }

        // Get the channel where invites should be created
        val channelId = request.partnerInviteChannelId
        val channel = guild.getChannel(channelId)
        if (channel == null || channel !is IInviteContainer) {
            call.respondRPCResponse<CreatePartnerInviteResponse>(CreatePartnerInviteResponse.ChannelNotFound)
            return
        }

        // Check if we have CREATE_INSTANT_INVITE permission
        val selfMember = guild.selfMember
        if (!selfMember.hasPermission(channel, Permission.CREATE_INSTANT_INVITE)) {
            call.respondRPCResponse<CreatePartnerInviteResponse>(CreatePartnerInviteResponse.MissingPermissions)
            return
        }

        try {
            // Create the invite with restrictions
            val invite = channel.createInvite()
                .setMaxAge(INVITE_MAX_AGE_SECONDS, TimeUnit.SECONDS)
                .setMaxUses(INVITE_MAX_USES)
                .setUnique(true)
                .setTargetUserIds(request.userId)
                .await()

            val now = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)
            val expiresAt = now.plusSeconds(INVITE_MAX_AGE_SECONDS)

            // Log to database
            loritta.transaction {
                LorittaPartnersServerInvites.insert {
                    it[LorittaPartnersServerInvites.userId] = request.userId
                    it[LorittaPartnersServerInvites.guildId] = request.requestedForGuildId
                    it[LorittaPartnersServerInvites.inviteCode] = invite.code
                    it[LorittaPartnersServerInvites.createdAt] = now
                    it[LorittaPartnersServerInvites.expiresAt] = expiresAt
                }
            }

            // Log to Discord channel
            val loggingChannelId = loritta.config.loritta.partnerApplications.inviteLoggingChannelId
            val partnerGuildId = loritta.config.loritta.partnerApplications.partnerGuildId
            val loggingGuild = loritta.lorittaShards.getGuildById(partnerGuildId)
            val loggingChannel = loggingGuild?.getGuildMessageChannelById(loggingChannelId)

            if (loggingChannel != null) {
                loggingChannel.sendMessage("Usuário <@${request.userId}> (`${request.userId}`) criou o convite `${invite.code}` pelo servidor `${request.requestedForGuildId}`").await()
            } else {
                logger.warn { "Could not find logging channel $loggingChannelId in guild $partnerGuildId to log partner invite creation" }
            }

            call.respondRPCResponse<CreatePartnerInviteResponse>(CreatePartnerInviteResponse.Success(inviteCode = invite.code))
        } catch (e: Exception) {
            logger.warn(e) { "Failed to create partner invite for user ${request.userId} in guild ${request.requestedForGuildId}" }
            call.respondRPCResponse<CreatePartnerInviteResponse>(CreatePartnerInviteResponse.InviteCreationFailed)
        }
    }
}
