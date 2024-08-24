package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.lorimessages

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.perfectdreams.loritta.discordchatmessagerenderer.savedmessage.SavedMessage
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.messageverify.LoriMessageDataUtils
import net.perfectdreams.loritta.morenitta.messageverify.png.PNGChunkUtils
import net.perfectdreams.loritta.morenitta.utils.SimpleImageInfo
import net.perfectdreams.loritta.morenitta.utils.readAllBytes
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.LoriPublicAPI
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.LoriPublicAPIRoute
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.RateLimitOptions
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.TokenInfo
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoints
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

class PostVerifyMessageRoute(m: LorittaBot) : LoriPublicAPIRoute(
    m,
    LoriPublicHttpApiEndpoints.VERIFY_LORITTA_MESSAGE,
    RateLimitOptions(
        5,
        5.seconds
    )
) {
    override suspend fun onAPIRequest(call: ApplicationCall, tokenInfo: TokenInfo) {
        // In curl, you NEED to use --data-binary to send the file to avoid corruption!
        val imageByteArray = call.receiveStream().readAllBytes(limit = 8_388_608)

        val mimeType = try {
            SimpleImageInfo(imageByteArray).mimeType
        } catch (e: IOException) {
            // This may happen if someone submits something that isn't an image "Unsupported image type"
            null
        }

        if (mimeType != "image/png") {
            // Uploaded image is not in png format
            call.respondJson(
                LoriPublicAPI.json.encodeToString(
                    Result.Failure(VerificationResult.IMAGE_IS_NOT_IN_PNG_FORMAT)
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val chunks = try {
            PNGChunkUtils.readChunksFromPNG(imageByteArray)
        } catch (e: Exception) {
            call.respondJson(
                LoriPublicAPI.json.encodeToString(
                    Result.Failure(VerificationResult.COULD_NOT_PARSE_PNG_CHUNKS)
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val textChunks = chunks.filter { it.type == "tEXt" }

        val results = mutableListOf<LoriMessageDataUtils.LoriMessageDataParseResult>()
        for (chunk in textChunks) {
            val result = LoriMessageDataUtils.parseFromPNGChunk(
                m,
                chunks,
                chunk
            )

            if (result is LoriMessageDataUtils.LoriMessageDataParseResult.Success) {
                // Already found, bail out!
                call.respondJson(
                    LoriPublicAPI.json.encodeToString(
                        Result.Success(
                            VerificationResult.SUCCESS,
                            result.savedMessage
                        )
                    )
                )
                return
            } else {
                results.add(result)
            }
        }

        // Oof, no matches, let's see what happened...
        if (results.all { it is LoriMessageDataUtils.LoriMessageDataParseResult.NotATextChunk || it is LoriMessageDataUtils.LoriMessageDataParseResult.NotALorittaMessageData }) {
            call.respondJson(
                LoriPublicAPI.json.encodeToString(
                    Result.Failure(VerificationResult.IMAGE_DOES_NOT_CONTAIN_MESSAGE_DATA)
                )
            )
            return
        }

        if (results.any { it is LoriMessageDataUtils.LoriMessageDataParseResult.InvalidInput }) {
            call.respondJson(
                LoriPublicAPI.json.encodeToString(
                    Result.Failure(VerificationResult.IMAGE_HAS_MESSAGE_DATA_BUT_COULDNT_BE_VALIDATED)
                )
            )
            return
        }

        if (results.any { it is LoriMessageDataUtils.LoriMessageDataParseResult.InvalidSignature }) {
            call.respondJson(
                LoriPublicAPI.json.encodeToString(
                    Result.Failure(VerificationResult.IMAGE_DATA_HAS_BEEN_TAMPERED)
                )
            )
            return
        }

        error("This should never happen! If it did, then there are PNG chunk checks missing! Parse results: $results")
    }

    @Serializable
    sealed class Result {
        abstract val result: VerificationResult

        @Serializable
        data class Success(
            override val result: VerificationResult,
            val messageData: SavedMessage
        ) : Result()

        @Serializable
        data class Failure(
            override val result: VerificationResult
        ) : Result()
    }

    enum class VerificationResult {
        SUCCESS,
        IMAGE_IS_NOT_IN_PNG_FORMAT,
        COULD_NOT_PARSE_PNG_CHUNKS,
        IMAGE_DOES_NOT_CONTAIN_MESSAGE_DATA,
        IMAGE_HAS_MESSAGE_DATA_BUT_COULDNT_BE_VALIDATED,
        IMAGE_DATA_HAS_BEEN_TAMPERED
    }
}