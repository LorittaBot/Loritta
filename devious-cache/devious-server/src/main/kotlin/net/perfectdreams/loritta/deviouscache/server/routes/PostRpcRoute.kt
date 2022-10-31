package net.perfectdreams.loritta.deviouscache.server.routes

import com.github.luben.zstd.Zstd
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.*
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import net.perfectdreams.loritta.deviouscache.responses.NotFoundResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.extensions.readAllBytes
import net.perfectdreams.loritta.deviouscache.server.utils.extensions.respondJson
import net.perfectdreams.loritta.deviouscache.utils.ZstdDictionaries
import net.perfectdreams.sequins.ktor.BaseRoute

class PostRpcRoute(val m: DeviousCache) : BaseRoute("/rpc") {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun onRequest(call: ApplicationCall) {
        try {
            val compressionHeader = call.request.header("X-Devious-Cache-Compression")
            val bodyAsJson = if (compressionHeader == null) {
                withContext(Dispatchers.IO) { call.receiveText() }
            } else {
                val (type, _) = compressionHeader.split(":")
                require(type == "zstd") { "Only zstd is supported as a compression method!" }

                val contentLength = call.request.contentLength()?.toInt() ?: error("Missing Content-Length!")

                // For now, we won't check the dictionary
                val payload = withContext(Dispatchers.IO) {
                    // We use readAllBytes to avoid allocating multiple ByteArrays when resizing the backed ByteArray
                    // Java does have a readNBytes method on InputStream, but the DEFAULT_BUFFER_SIZE is so small, that it would cause multiple resizes anyhow, so
                    // it is better to use our own readAllBytes method.
                    //
                    // If the Content-Length is known (example: images on Discord's CDN do have Content-Length on the response header)
                    // we can allocate the array with exactly the same size that the Content-Length provides, this way we avoid a lot of unnecessary Arrays.copyOf!
                    // Of course, this could be abused to allocate a gigantic array that causes Loritta to crash, but if the Content-Length is present, Loritta checks the size
                    // before trying to download it, so no worries :)
                    //
                    // While this does not provide a huge *reading* performance boost, it does reduce the amount of allocations required, sweet!
                    call.receiveStream().readAllBytes(contentLength, contentLength)
                }

                Zstd.decompress(payload, Zstd.decompressedSize(payload).toInt())
                    .toString(Charsets.UTF_8)
            }

            // Check based on type
            val response = when (val request = Json.decodeFromString<DeviousRequest>(bodyAsJson)) {
                is GetUserRequest -> m.processors.getUserProcessor.process(request)
                is PutUserRequest -> m.processors.putUserProcessor.process(request)

                is GetGuildRequest -> m.processors.getGuildProcessor.process(request)
                is GetIfGuildExistsRequest -> m.processors.getIfGuildExistsProcessor.process(request)
                is GetGuildWithEntitiesRequest -> m.processors.getGuildWithEntitiesProcessor.process(request)
                is PutGuildRequest -> m.processors.putGuildProcessor.process(request)
                is PutGuildsBulkRequest -> m.processors.putGuildsBulkProcessor.process(request)
                is DeleteGuildRequest -> m.processors.deleteGuildProcessor.process(request)
                is GetGuildCountRequest -> m.processors.getGuildCountProcessor.process(request)
                is PutGuildMemberRequest -> m.processors.putGuildMemberProcesor.process(request)
                is DeleteGuildMemberRequest -> m.processors.deleteGuildMemberProcessor.process(request)
                is GetGuildIdsOfShardRequest -> m.processors.getGuildIdsOfShardProcessor.process(request)
                is GetGuildMembersRequest -> m.processors.getGuildMembersProcessor.process(request)
                is GetGuildMembersWithRolesRequest -> m.processors.getGuildMembersWithRolesProcessor.process(request)
                is GetGuildBoostersRequest -> m.processors.getGuildBoostersProcessor.process(request)
                is GetVoiceStateRequest -> m.processors.getVoiceStateProcessor.process(request)
                is PutVoiceStateRequest -> m.processors.putVoiceStateProcessor.process(request)
                is GetGuildMemberRequest -> m.processors.getGuildMemberProcessor.process(request)

                is GetChannelRequest -> m.processors.getChannelProcessor.process(request)
                is PutChannelRequest -> m.processors.putChannelProcessor.process(request)
                is DeleteChannelRequest -> m.processors.deleteChannelProcessor.process(request)

                is PutGuildRoleRequest -> m.processors.putGuildRoleProcessor.process(request)
                is DeleteGuildRoleRequest -> m.processors.deleteGuildRoleProcessor.process(request)

                is PutGuildEmojisRequest -> m.processors.putGuildEmojisProcessor.process(request)

                is GetGatewaySessionRequest -> m.processors.getGatewaySessionProcessor.process(request)
                is PutGatewaySessionRequest -> m.processors.putGatewaySessionProcessor.process(request)
                is PutGatewaySequenceRequest -> m.processors.putGatewaySequenceProcessor.process(request)
                is LockConcurrentLoginRequest -> m.processors.lockConcurrentLoginProcessor.process(request)
                is UnlockConcurrentLoginRequest -> m.processors.unlockConcurrentLoginProcessor.process(request)

                is GetMiscellaneousDataRequest -> m.processors.getMiscellaneousDataProcessor.process(request)
                is PutMiscellaneousDataRequest -> m.processors.putMiscellaneousDataProcessor.process(request)

                else -> error("I don't know how to handle ${request::class}!")
            }

            val requestAsJson = Json.encodeToString<DeviousResponse>(response)
            val compressedBody = Zstd.compress(requestAsJson.toByteArray(Charsets.UTF_8), 2)

            call.response.header("X-Devious-Cache-Compression", "zstd:${ZstdDictionaries.Dictionary.NO_DICTIONARY.name}")

            call.respondBytes(
                compressedBody,
                status = if (response is NotFoundResponse)
                    HttpStatusCode.NotFound
                else
                    HttpStatusCode.OK
            )
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while trying to process the RPC request!" }
        }
    }
}