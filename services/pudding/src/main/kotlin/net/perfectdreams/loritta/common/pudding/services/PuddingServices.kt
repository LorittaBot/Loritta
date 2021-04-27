package net.perfectdreams.loritta.common.pudding.services

import io.ktor.client.*
import net.perfectdreams.loritta.common.memory.services.Services
import net.perfectdreams.pudding.client.PuddingClient

class PuddingServices(
    puddingUrl: String,
    authorization: String,
    http: HttpClient
    ) : Services() {
    val puddingClient = PuddingClient(
        puddingUrl,
        authorization,
        http
    )

    override val profiles = PuddingUserProfileService(puddingClient)
}