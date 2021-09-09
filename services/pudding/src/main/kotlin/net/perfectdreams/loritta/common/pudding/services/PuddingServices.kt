package net.perfectdreams.loritta.cinnamon.common.pudding.services

import io.ktor.client.*
import net.perfectdreams.loritta.cinnamon.common.services.Services
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

    override val users = PuddingUserService(puddingClient)
    override val marriages = PuddingMarriagesService(puddingClient)
    override val shipEffects = PuddingShipEffectsService(puddingClient)
    override val sonhos = PuddingSonhosService(puddingClient)
    override val serverConfigs = PuddingServerConfigsService(puddingClient)
}