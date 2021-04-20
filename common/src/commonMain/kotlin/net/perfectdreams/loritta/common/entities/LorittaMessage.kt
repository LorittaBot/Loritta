package net.perfectdreams.loritta.common.entities

class LorittaMessage(
    val content: String,
    val embed: LorittaEmbed?,
    val files: Map<String, ByteArray>,
    val isEphemeral: Boolean,
    val allowedMentions: AllowedMentions
)