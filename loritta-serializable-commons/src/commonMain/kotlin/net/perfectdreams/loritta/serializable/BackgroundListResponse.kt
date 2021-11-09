package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class BackgroundListResponse(
    val dreamStorageServiceUrl: String,
    val namespace: String,
    val backgrounds: List<Background>
)