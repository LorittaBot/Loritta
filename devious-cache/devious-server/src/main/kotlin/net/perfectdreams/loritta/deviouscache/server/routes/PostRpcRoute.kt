package net.perfectdreams.loritta.deviouscache.server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.*
import net.perfectdreams.loritta.deviouscache.responses.NotFoundResponse
import net.perfectdreams.loritta.deviouscache.server.DeviousCache
import net.perfectdreams.loritta.deviouscache.server.utils.extensions.respondJson
import net.perfectdreams.sequins.ktor.BaseRoute

class PostRpcRoute(val m: DeviousCache) : BaseRoute("/rpc") {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun onRequest(call: ApplicationCall) {
        try {
            val body = call.receiveText()

            // Check based on type
            val response = when (val request = Json.decodeFromString<DeviousRequest>(body)) {
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

            call.respondJson(
                response,
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