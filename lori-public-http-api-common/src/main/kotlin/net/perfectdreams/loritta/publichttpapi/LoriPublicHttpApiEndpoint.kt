package net.perfectdreams.loritta.publichttpapi

import io.ktor.http.*

class LoriPublicHttpApiEndpoint(
    val method: HttpMethod,
    val path: String
)