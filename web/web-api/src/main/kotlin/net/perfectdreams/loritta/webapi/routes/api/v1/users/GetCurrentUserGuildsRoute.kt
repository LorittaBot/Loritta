package net.perfectdreams.loritta.webapi.routes.api.v1.users

import io.ktor.application.*
import io.ktor.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.data.discord.PartialDiscordGuild
import net.perfectdreams.sequins.ktor.BaseRoute

class GetCurrentUserGuildsRoute : BaseRoute("/api/v1/users/@me/guilds") {
    override suspend fun onRequest(call: ApplicationCall) {
        call.respondText(
            Json.encodeToString(
                listOf(
                    PartialDiscordGuild(
                        268353819409252352u,
                        "Floppa Shy",
                        "caf959735a24b4bba1d31bb412fef58e",
                        true,
                        "1",
                        listOf("VERIFIED")
                    )
                )
            )
        )
    }
}