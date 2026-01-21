package net.perfectdreams.loritta.morenitta.rpc.commands

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.rpc.payloads.QueryGuildInfoRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.QueryGuildInfoResponse

class QueryGuildInfoCommand(val loritta: LorittaBot) : LorittaRPCCommand(LorittaRPC.QueryGuildInfo) {
    override suspend fun onRequest(call: ApplicationCall) {
        val request = Json.decodeFromString<QueryGuildInfoRequest>(call.receiveText())

        val guild = loritta.lorittaShards.getGuildById(request.guildId)
        if (guild == null) {
            call.respondRPCResponse<QueryGuildInfoResponse>(QueryGuildInfoResponse.GuildNotFound)
            return
        }

        call.respondRPCResponse<QueryGuildInfoResponse>(
            QueryGuildInfoResponse.Success(
                guildId = guild.idLong,
                name = guild.name,
                memberCount = guild.memberCount,
                iconUrl = guild.iconUrl
            )
        )
    }
}
