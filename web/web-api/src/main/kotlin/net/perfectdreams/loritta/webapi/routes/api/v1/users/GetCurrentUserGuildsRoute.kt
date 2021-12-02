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
                        "Ideias Aleatórias",
                        "caf959735a24b4bba1d31bb412fef58e",
                        true,
                        "1",
                        listOf("VERIFIED")
                    ),
                    PartialDiscordGuild(
                        297732013006389252u,
                        "Apartamento da Loritta",
                        "a_fee7591870d26c60af64179c4ab520ed",
                        true,
                        "1",
                        listOf("VERIFIED")
                    ),
                    PartialDiscordGuild(
                        420626099257475072u,
                        "Loritta's Apartment",
                        "a_2bd17ea2a11c097f35b672209a0c3eb9",
                        true,
                        "1",
                        listOf("VERIFIED")
                    ),
                    PartialDiscordGuild(
                        320248230917046282u,
                        "SparklyPower",
                        "a_5c22281a3f7f80a2fe7892de08db3b0c",
                        true,
                        "1",
                        listOf("VERIFIED")
                    ),
                    PartialDiscordGuild(
                        681477966286422149u,
                        "PerfectDreams",
                        "d96c48b514b64518e70c19e2210d1207",
                        true,
                        "1",
                        listOf("VERIFIED")
                    )
                )
            )
        )
    }
}