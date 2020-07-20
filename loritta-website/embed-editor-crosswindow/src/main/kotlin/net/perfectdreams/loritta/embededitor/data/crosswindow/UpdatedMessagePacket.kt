package net.perfectdreams.loritta.embededitor.data.crosswindow

import kotlinx.serialization.Serializable

@Serializable
class UpdatedMessagePacket(
        val content: String
) : CrossWindowPacket