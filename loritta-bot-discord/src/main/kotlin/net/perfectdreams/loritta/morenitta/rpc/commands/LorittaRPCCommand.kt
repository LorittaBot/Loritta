package net.perfectdreams.loritta.morenitta.rpc.commands

import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.sequins.ktor.BaseRoute

abstract class LorittaRPCCommand(val rpcCommand: LorittaRPC.LorittaRPCCommandName<*, *>) : BaseRoute("/lorirpc/${rpcCommand.name}") {
    override fun getMethod() = HttpMethod.Post

    suspend inline fun <reified T> ApplicationCall.respondRPCResponse(response: T) {
        respondJson(Json.encodeToString<T>(response))
    }
}