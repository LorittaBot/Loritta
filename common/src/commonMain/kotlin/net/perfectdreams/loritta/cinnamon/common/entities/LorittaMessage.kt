package net.perfectdreams.loritta.cinnamon.common.entities

import net.perfectdreams.loritta.cinnamon.common.utils.embed.EmbedBuilder

class LorittaMessage(
    val content: String?,
    val replies: List<LorittaReply>,
    val embeds: List<EmbedBuilder>?,
    val files: Map<String, ByteArray>,
    val isEphemeral: Boolean,
    val allowedMentions: AllowedMentions,
    val impersonation: LorittaImpersonation?
)