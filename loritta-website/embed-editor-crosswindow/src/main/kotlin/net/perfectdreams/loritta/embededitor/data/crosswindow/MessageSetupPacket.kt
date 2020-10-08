package net.perfectdreams.loritta.embededitor.data.crosswindow

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.embededitor.data.DiscordMessage

@Serializable
class MessageSetupPacket(
        val message: DiscordMessage,
        val placeholders: List<Placeholder>
) : CrossWindowPacket