package net.perfectdreams.loritta.legacy.common.entities

class LorittaMessage(
    val content: String?,
    val replies: List<LorittaReply>,
    val embed: LorittaEmbed?,
    val files: Map<String, ByteArray>,
    val isEphemeral: Boolean,
    val allowedMentions: AllowedMentions,
    val impersonation: LorittaImpersonation?,
    val messageReferenceId: Long?
)