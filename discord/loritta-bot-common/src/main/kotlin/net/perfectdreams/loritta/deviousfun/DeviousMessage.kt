package net.perfectdreams.loritta.deviousfun

import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.AllowedMentionsBuilder

class DeviousMessage(
    val contentRaw: String,
    val embeds: List<DeviousEmbed>,
    val files: List<FileToBeSent>,
    val referenceId: Snowflake?,
    val allowedMentionsBuilder: AllowedMentionsBuilder?,
)