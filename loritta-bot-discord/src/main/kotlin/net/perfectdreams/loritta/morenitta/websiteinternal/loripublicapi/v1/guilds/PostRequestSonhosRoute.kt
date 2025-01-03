package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.guilds

import dev.minn.jda.ktx.messages.MessageCreateBuilder
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.UserSnowflake
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransferRequests
import net.perfectdreams.loritta.common.utils.TokenType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosPayExecutor
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.*
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoints
import net.perfectdreams.loritta.serializable.SonhosTransferRequestMetadata
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.insertAndGetId
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class PostRequestSonhosRoute(m: LorittaBot) : LoriPublicAPIGuildRoute(
    m,
    LoriPublicHttpApiEndpoints.REQUEST_SONHOS,
    RateLimitOptions(
        5,
        5.seconds
    )
) {
    override suspend fun onGuildAPIRequest(call: ApplicationCall, tokenInfo: TokenInfo, guild: Guild, member: Member) {
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            call.respondJson("", status = HttpStatusCode.Unauthorized)
            return
        }

        if (tokenInfo.tokenType != TokenType.BOT) {
            call.respondJson(Json.encodeToString(GenericErrorResponse("Only bots can use this endpoint!")), status = HttpStatusCode.BadRequest)
            return
        }

        val request = Json.decodeFromString<RequestSonhosRequest>(call.receiveText())
        val senderSnowflake = UserSnowflake.fromId(request.senderId)
        val channel = guild.getGuildMessageChannelById(call.parameters.getOrFail("channelId"))
        if (channel == null) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Unknown Channel"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        // These checks should be the SAME checks used in SonhosPayExecutor!
        val howMuch = request.quantity
        if (0L >= howMuch) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "You can't request zero or negative sonhos!"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        if (member.idLong == request.senderId) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "You can't request sonhos from yourself!"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        if (request.expiresAfterMillis !in SonhosPayExecutor.TIME_TO_LIVE_RANGE) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "expiresAfterMillis is outside of allowed TTL range! (${SonhosPayExecutor.TIME_TO_LIVE_RANGE})"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        if (request.reason.isBlank()) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Reason cannot be blank!"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        if (request.reason.length !in 1..100) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Reason must be between 1..100!"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val senderProfile = m.getLorittaProfile(request.senderId)

        if (senderProfile == null || howMuch > senderProfile.money) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Sender does not have enough sonhos!"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val senderAccountOldEnoughResult = SonhosPayExecutor.checkIfAccountIsOldEnoughToSendSonhos(senderSnowflake)

        when (senderAccountOldEnoughResult) {
            is SonhosPayExecutor.Companion.OtherAccountOldEnoughResult.NotOldEnough -> {
                call.respondJson(
                    Json.encodeToString(
                        GenericErrorResponse(
                            "Sender account is not old enough!"
                        )
                    ),
                    status = HttpStatusCode.BadRequest
                )
                return
            }

            SonhosPayExecutor.Companion.OtherAccountOldEnoughResult.Success -> {}
        }

        val senderAccountGotDailyAtLeastOnceResult = SonhosPayExecutor.checkIfAccountGotDailyAtLeastOnce(m, senderSnowflake)

        when (senderAccountGotDailyAtLeastOnceResult) {
            SonhosPayExecutor.Companion.AccountGotDailyAtLeastOnceResult.HaventGotDailyOnce -> {
                call.respondJson(
                    Json.encodeToString(
                        GenericErrorResponse(
                            "Sender account has not received daily at least once!"
                        )
                    ),
                    status = HttpStatusCode.BadRequest
                )
                return
            }
            SonhosPayExecutor.Companion.AccountGotDailyAtLeastOnceResult.Success -> {}
        }

        // Check if the user is banned from using Loritta
        val userBannedState = m.pudding.users.getUserBannedState(UserId(request.senderId))

        if (userBannedState != null) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Sender is Loritta Banned!"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val now = Instant.now()
        val nowPlusTimeToLive = now.plusMillis(request.expiresAfterMillis)

        // Load the server config beecause we need the i18nContext
        val serverConfig = m.getOrCreateServerConfig(guild.idLong)
        val i18nContext = m.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

        // Attempt to initiate a transfer
        val sonhosTransferRequestId = m.transaction {
            SonhosTransferRequests.insertAndGetId {
                it[SonhosTransferRequests.giver] = request.senderId
                it[SonhosTransferRequests.receiver] = member.idLong
                it[SonhosTransferRequests.quantity] = request.quantity
                it[SonhosTransferRequests.requestedAt] = now
                it[SonhosTransferRequests.expiresAt] = nowPlusTimeToLive
                it[SonhosTransferRequests.receiverAcceptedAt] = now // The bot should automatically accept the transfer request
                it[SonhosTransferRequests.metadata] = Json.encodeToString<SonhosTransferRequestMetadata>(
                    SonhosTransferRequestMetadata.APIInitiatedSonhosTransferRequestMetadata(
                        request.reason
                    )
                )
            }
        }

        // Attempt to send the message
        val messageId = channel.sendMessage(
            MessageCreateBuilder {
                mentions {
                    user(senderSnowflake)
                }

                styled(
                    "**${i18nContext.get(I18nKeysData.Commands.Command.Pay.ApiInitiatedTransfers(member.asMention, senderSnowflake.asMention))}**",
                    Emotes.LoriMegaphone
                )

                styled(
                    i18nContext.get(I18nKeysData.Commands.Command.Pay.ApiInitiatedTransferReason("`${request.reason.stripCodeMarks()}`")),
                    Emotes.PageFacingUp
                )

                val message = SonhosPayExecutor.createSonhosTransferMessageThirdPerson(
                    i18nContext,
                    senderSnowflake,
                    member,
                    howMuch,
                    nowPlusTimeToLive,
                    sonhosTransferRequestId.value,
                    1 // The receiver (ourselves) should ALWAYS have the transfer pre-accepted!
                )

                message.invoke(this)
            }.build()
        ).await()

        call.respondJson(
            Json.encodeToString(
                RequestSonhosResponse(
                    sonhosTransferRequestId.value,
                    messageId.idLong
                )
            ),
            status = HttpStatusCode.OK
        )
        return
    }

    @Serializable
    data class RequestSonhosRequest(
        @LoriPublicAPIParameter
        @Serializable(LongAsStringSerializer::class)
        val senderId: Long,
        @LoriPublicAPIParameter
        val quantity: Long,
        @LoriPublicAPIParameter
        val reason: String,
        @LoriPublicAPIParameter
        val expiresAfterMillis: Long = 15.minutes.inWholeMilliseconds
    )

    @Serializable
    data class RequestSonhosResponse(
        @LoriPublicAPIParameter
        @Serializable(LongAsStringSerializer::class)
        val sonhosTransferRequestId: Long,
        @LoriPublicAPIParameter
        @Serializable(LongAsStringSerializer::class)
        val messageId: Long
    )
}