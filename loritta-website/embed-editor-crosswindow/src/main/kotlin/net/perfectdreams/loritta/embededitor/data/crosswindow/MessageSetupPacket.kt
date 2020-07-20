package net.perfectdreams.loritta.embededitor.data.crosswindow

import kotlinx.serialization.Serializable

@Serializable
class MessageSetupPacket(
        val content: String,
        val placeholders: List<Placeholder>
) : CrossWindowPacket