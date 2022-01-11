package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.giveaway

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class JoinButtonData(
    val rolesNotToParticipate: List<Snowflake>?,
    val expiresAt: Instant
)