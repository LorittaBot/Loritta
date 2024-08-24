package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.guilds

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.servers.Giveaway
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.GenericErrorResponse
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.LoriPublicAPIGuildRoute
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.RateLimitOptions
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.TokenInfo
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoints
import kotlin.time.Duration.Companion.seconds

class PostRerollGiveawayRoute(m: LorittaBot) : LoriPublicAPIGuildRoute(
    m,
    LoriPublicHttpApiEndpoints.REROLL_GUILD_GIVEAWAY,
    RateLimitOptions(
        2,
        5.seconds
    )
) {
    override suspend fun onGuildAPIRequest(call: ApplicationCall, tokenInfo: TokenInfo, guild: Guild, member: Member) {
        if (!member.hasPermission(Permission.MESSAGE_MANAGE)) {
            call.respondJson("", status = HttpStatusCode.Unauthorized)
            return
        }

        val request = Json.decodeFromString<RerollGiveawayRequest>(call.receiveText().ifEmpty { "{}" })
        if (request.rerollCount != null && request.rerollCount !in 1..100) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Number of Winners is not in valid range (1..100)"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val giveawayId = call.parameters.getOrFail("giveawayId").toLong()

        val giveaway = m.newSuspendedTransaction {
            Giveaway.findById(giveawayId)
        }

        if (giveaway == null) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Unknown Giveaway"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        if (!giveaway.finished) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Cannot reroll a giveaway that has not finished"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val textChannel = guild.getGuildMessageChannelById(giveaway.textChannelId)

        if (textChannel == null) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Unknown Giveaway Channel"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val message = textChannel.retrieveMessageById(giveaway.messageId).await()

        if (message == null) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Unknown Giveaway Message"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        m.giveawayManager.rollWinners(message, giveaway, numberOfWinnersOverride = request.rerollCount)

        call.respondJson("", status = HttpStatusCode.NoContent)
    }

    @Serializable
    data class RerollGiveawayRequest(
        val rerollCount: Int? = null
    )
}