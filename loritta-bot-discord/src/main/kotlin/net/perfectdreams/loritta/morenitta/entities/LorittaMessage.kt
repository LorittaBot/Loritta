package net.perfectdreams.loritta.morenitta.entities

import net.perfectdreams.loritta.common.entities.AllowedMentions
import net.perfectdreams.loritta.common.entities.LorittaEmbed

class LorittaMessage(
    val content: String?,
    val replies: List<LorittaReply>,
    val embed: LorittaEmbed?,
    val files: Map<String, ByteArray>,
    val isEphemeral: Boolean,
    val allowedMentions: AllowedMentions,
    val messageReferenceId: Long?
)