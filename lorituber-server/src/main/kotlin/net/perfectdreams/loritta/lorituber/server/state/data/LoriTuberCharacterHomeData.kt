package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable

@Serializable
data class LoriTuberCharacterHomeData(
    val objects: MutableList<LoriTuberObjectData>
)