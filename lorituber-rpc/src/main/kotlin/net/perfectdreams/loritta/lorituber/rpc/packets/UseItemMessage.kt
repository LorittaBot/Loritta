package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable

@Serializable
sealed class UseItemMessage {
    @Serializable
    data object Test
}