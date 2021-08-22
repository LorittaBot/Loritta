package net.perfectdreams.loritta.common.pudding.services

import io.ktor.client.*
import net.perfectdreams.loritta.common.services.MarriagesService
import net.perfectdreams.loritta.common.services.Services
import net.perfectdreams.loritta.common.services.ShipEffectsService
import net.perfectdreams.loritta.common.services.SonhosService
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
    override val marriages: MarriagesService
        get() = TODO("Not yet implemented")
    override val shipEffects: ShipEffectsService
        get() = TODO("Not yet implemented")
    override val sonhos: SonhosService
        get() = TODO("Not yet implemented")
    override val serverConfigs = TODO()
}