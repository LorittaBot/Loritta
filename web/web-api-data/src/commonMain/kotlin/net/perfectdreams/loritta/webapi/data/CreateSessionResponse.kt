package net.perfectdreams.loritta.webapi.data

import kotlinx.serialization.Serializable

@Serializable
data class CreateSessionResponse(
    val token: String
)