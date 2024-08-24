package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.lorimessages

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.messageverify.LoriMessageDataUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.GenericErrorResponse
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.LoriPublicAPIGuildRoute
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.RateLimitOptions
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.TokenInfo
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoints
import kotlin.time.Duration.Companion.seconds

class PostSaveMessageRoute(m: LorittaBot) : LoriPublicAPIGuildRoute(
    m,
    LoriPublicHttpApiEndpoints.SAVE_LORITTA_MESSAGE,
    RateLimitOptions(
        2,
        5.seconds
    )
) {
    override suspend fun onGuildAPIRequest(call: ApplicationCall, tokenInfo: TokenInfo, guild: Guild, member: Member) {
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            call.respondJson("", status = HttpStatusCode.Unauthorized)
            return
        }

        if (!guild.selfMember.hasPermission(Permission.ADMINISTRATOR)) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Loritta does not have administrator permission on this server"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val channelId = call.parameters.getOrFail("channelId")
        val messageId = call.parameters.getOrFail("messageId")
        val channel = guild.getGuildMessageChannelById(channelId)
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

        val message = try {
            channel.retrieveMessageById(messageId).await()
        } catch (e: ErrorResponseException) {
            null
        }

        if (message == null) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Unknown Message"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val savedMessage = LoriMessageDataUtils.convertMessageToSavedMessage(message)
        val finalImage = LoriMessageDataUtils.createSignedRenderedSavedMessage(m, savedMessage, true)

        call.respondBytes(
            finalImage,
            ContentType.Image.PNG
        )
    }
}