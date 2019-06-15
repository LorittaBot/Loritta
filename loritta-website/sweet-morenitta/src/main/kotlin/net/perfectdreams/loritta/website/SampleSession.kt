package net.perfectdreams.loritta.website

import java.util.*

data class SampleSession(
    val uniqueId: UUID,
    val discordId: String?,
    val serializedDiscordAuth: String?
)