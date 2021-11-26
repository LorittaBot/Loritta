package net.perfectdreams.loritta.webapi.routes.api.v1.users

import io.ktor.application.*
import io.ktor.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.webapi.data.PartialDiscordGuild
import net.perfectdreams.sequins.ktor.BaseRoute

class GetCurrentUserGuildsRoute : BaseRoute("/api/v1/users/@me/guilds") {
    override suspend fun onRequest(call: ApplicationCall) {
        call.respondText(
            Json.encodeToString(
                listOf(
                    PartialDiscordGuild(
                        197308318119755776u,
                        "Floppa Shy",
                        "a",
                        true,
                        "1",
                        listOf("VERIFIED")
                    )
                )
            )
        )
    }
}