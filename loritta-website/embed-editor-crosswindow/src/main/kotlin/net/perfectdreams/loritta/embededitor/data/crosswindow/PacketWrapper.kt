package net.perfectdreams.loritta.embededitor.data.crosswindow

import kotlinx.serialization.Serializable

@Serializable
data class PacketWrapper(
        val m: CrossWindowPacket
)