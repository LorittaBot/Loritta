package net.perfectdreams.loritta.morenitta.rpc.commands

import dev.minn.jda.ktx.messages.MessageCreate
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.tables.BanAppeals
import net.perfectdreams.loritta.cinnamon.pudding.tables.BannedUsers
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.banappeals.BanAppeal
import net.perfectdreams.loritta.morenitta.banappeals.BanAppealsUtils
import net.perfectdreams.loritta.morenitta.banappeals.BanAppealsUtils.createStaffAppealMessage
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.rpc.payloads.NotifyBanAppealRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.NotifyBanAppealResponse
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.serializable.UserBannedState
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.selectAll

class NotifyBanAppealCommand(val loritta: LorittaBot) : LorittaRPCCommand(LorittaRPC.NotifyBanAppeal) {
    override suspend fun onRequest(call: ApplicationCall) {
        val request = Json.decodeFromString<NotifyBanAppealRequest>(call.receiveText())

        val guild = loritta.lorittaShards.getGuildById(request.guildId)!!
        val channel = guild.getGuildMessageChannelById(request.channelId)!!

        val appealRow = loritta.transaction {
            BanAppeals
                .innerJoin(BannedUsers)
                .selectAll()
                .where {
                    BanAppeals.id eq request.appealId
                }
                .first()
        }

        val appeal = BanAppeal(
            appealRow[BanAppeals.id].value,
            appealRow[BanAppeals.submittedBy],
            appealRow[BanAppeals.userId],
            appealRow[BanAppeals.whatDidYouDo],
            appealRow[BanAppeals.whyDidYouBreakThem],
            appealRow[BanAppeals.accountIds],
            appealRow[BanAppeals.whyShouldYouBeUnbanned],
            appealRow[BanAppeals.additionalComments],
            appealRow[BanAppeals.files],
            UserBannedState(
                appealRow[BannedUsers.id].value,
                appealRow[BannedUsers.valid],
                Instant.fromEpochMilliseconds(appealRow[BannedUsers.bannedAt]),
                appealRow[BannedUsers.expiresAt]?.let { Instant.fromEpochMilliseconds(it) },
                appealRow[BannedUsers.reason],
                appealRow[BannedUsers.bannedBy]?.let { UserId(it.toULong()) },
                appealRow[BannedUsers.staffNotes]
            ),
            appealRow[BanAppeals.submittedAt],
            appealRow[BanAppeals.languageId],
            appealRow[BanAppeals.reviewedBy],
            appealRow[BanAppeals.reviewedAt],
            appealRow[BanAppeals.reviewerNotes],
            appealRow[BanAppeals.appealResult]
        )
        
        val (submittedBy, appealFor) = BanAppealsUtils.getCachedUserInfoForAppeal(loritta, appeal)

        if (submittedBy == null || appealFor == null) {
            call.respondRPCResponse<NotifyBanAppealResponse>(NotifyBanAppealResponse.UserNotFound)
            return
        }

        channel.sendMessage(
            MessageCreate {
                createStaffAppealMessage(
                    loritta,
                    appeal,
                    submittedBy,
                    appealFor
                )
            }
        ).await()

        call.respondRPCResponse<NotifyBanAppealResponse>(NotifyBanAppealResponse.Success)
    }
}