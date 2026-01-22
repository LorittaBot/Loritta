package net.perfectdreams.loritta.morenitta.rpc.commands

import dev.minn.jda.ktx.messages.MessageCreate
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.perfectdreams.loritta.cinnamon.pudding.tables.PartnerApplications
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.lorittapartners.PartnerGuildInfo
import net.perfectdreams.loritta.morenitta.lorittapartners.PartnerApplicationData
import net.perfectdreams.loritta.morenitta.lorittapartners.PartnerApplicationsUtils.createStaffApplicationMessage
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.rpc.execute
import net.perfectdreams.loritta.morenitta.rpc.payloads.NotifyPartnerApplicationRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.NotifyPartnerApplicationResponse
import net.perfectdreams.loritta.morenitta.rpc.payloads.QueryGuildInfoRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.QueryGuildInfoResponse
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import org.jetbrains.exposed.sql.selectAll

class NotifyPartnerApplicationRPCCommand(val loritta: LorittaBot) : LorittaRPCCommand(LorittaRPC.NotifyPartnerApplication) {
    override suspend fun onRequest(call: ApplicationCall) {
        val request = Json.decodeFromString<NotifyPartnerApplicationRequest>(call.receiveText())

        val guild = loritta.lorittaShards.getGuildById(request.guildId)
        if (guild == null) {
            call.respondRPCResponse<NotifyPartnerApplicationResponse>(NotifyPartnerApplicationResponse.GuildNotFound)
            return
        }
        val channel = guild.getGuildMessageChannelById(request.channelId)
        if (channel == null) {
            call.respondRPCResponse<NotifyPartnerApplicationResponse>(NotifyPartnerApplicationResponse.ChannelNotFound)
            return
        }

        val partnerApplication = loritta.transaction {
            PartnerApplications
                .selectAll()
                .where {
                    PartnerApplications.id eq request.applicationId
                }
                .first()
        }

        val applicationData = PartnerApplicationData(
            id = partnerApplication[PartnerApplications.id].value,
            guildId = partnerApplication[PartnerApplications.guildId],
            inviteLink = partnerApplication[PartnerApplications.inviteLink],
            serverPurpose = partnerApplication[PartnerApplications.serverPurpose],
            whyPartner = partnerApplication[PartnerApplications.whyPartner],
            result = partnerApplication[PartnerApplications.applicationResult],
            submitterPermissionLevel = partnerApplication[PartnerApplications.submitterPermissionLevel],
            reviewedBy = partnerApplication[PartnerApplications.reviewedBy],
            reviewedAt = partnerApplication[PartnerApplications.reviewedAt],
            reviewerNotes = partnerApplication[PartnerApplications.reviewerNotes]
        )

        val submittedByUser = loritta.lorittaShards.retrieveUserInfoById(partnerApplication[PartnerApplications.submittedBy])

        if (submittedByUser == null) {
            call.respondRPCResponse<NotifyPartnerApplicationResponse>(NotifyPartnerApplicationResponse.UserNotFound)
            return
        }

        // Query guild info from the correct cluster since the guild might not be present in this cluster
        val applicationGuildCluster = DiscordUtils.getLorittaClusterForGuildId(loritta, applicationData.guildId)
        val applicationGuildInfoResponse = LorittaRPC.QueryGuildInfo.execute(
            loritta,
            applicationGuildCluster,
            QueryGuildInfoRequest(applicationData.guildId)
        )

        val applicationGuildInfo = when (applicationGuildInfoResponse) {
            is QueryGuildInfoResponse.Success -> PartnerGuildInfo(
                name = applicationGuildInfoResponse.name,
                memberCount = applicationGuildInfoResponse.memberCount,
                iconUrl = applicationGuildInfoResponse.iconUrl,
                ownerId = applicationGuildInfoResponse.ownerId
            )
            is QueryGuildInfoResponse.GuildNotFound -> null
        }

        val message = channel.sendMessage(
            MessageCreate {
                createStaffApplicationMessage(
                    loritta,
                    applicationData,
                    submittedByUser,
                    applicationGuildInfo
                )
            }
        ).await()

        message.createThreadChannel("Candidatura de Parceria #${applicationData.id}")
            .setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_1_WEEK)
            .reason("Thread created for partner application #${applicationData.id}")
            .await()

        call.respondRPCResponse<NotifyPartnerApplicationResponse>(NotifyPartnerApplicationResponse.Success)
    }
}