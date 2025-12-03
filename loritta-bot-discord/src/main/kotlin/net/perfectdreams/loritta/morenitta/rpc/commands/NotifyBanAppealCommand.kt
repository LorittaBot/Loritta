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

        val appeal = loritta.transaction {
            BanAppeals
                .innerJoin(BannedUsers)
                .selectAll()
                .where {
                    BanAppeals.id eq request.appealId
                }
                .first()
        }

        val submittedBy = loritta.lorittaShards.retrieveUserInfoById(appeal[BanAppeals.submittedBy])
        val appealFor = loritta.lorittaShards.retrieveUserInfoById(appeal[BanAppeals.userId])

        channel.sendMessage(
            MessageCreate {
                createStaffAppealMessage(
                    BanAppeal(
                        appeal[BanAppeals.id].value,
                        appeal[BanAppeals.submittedBy],
                        appeal[BanAppeals.userId],
                        appeal[BanAppeals.whatDidYouDo],
                        appeal[BanAppeals.whyDidYouBreakThem],
                        appeal[BanAppeals.accountIds],
                        appeal[BanAppeals.whyShouldYouBeUnbanned],
                        appeal[BanAppeals.additionalComments],
                        appeal[BanAppeals.files],
                        UserBannedState(
                            appeal[BannedUsers.id].value,
                            appeal[BannedUsers.valid],
                            Instant.fromEpochMilliseconds(appeal[BannedUsers.bannedAt]),
                            appeal[BannedUsers.expiresAt]?.let { Instant.fromEpochMilliseconds(it) },
                            appeal[BannedUsers.reason],
                            appeal[BannedUsers.bannedBy]?.let { UserId(it.toULong()) },
                            appeal[BannedUsers.staffNotes]
                        ),
                        appeal[BanAppeals.submittedAt],
                        appeal[BanAppeals.reviewedBy],
                        appeal[BanAppeals.reviewedAt],
                        appeal[BanAppeals.reviewerNotes],
                        appeal[BanAppeals.appealResult]
                    ),
                    submittedBy,
                    appealFor
                )
            }
        ).await()

        call.respondRPCResponse<NotifyBanAppealResponse>(NotifyBanAppealResponse.Success)
    }
}