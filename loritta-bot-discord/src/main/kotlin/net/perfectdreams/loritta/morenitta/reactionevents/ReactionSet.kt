package net.perfectdreams.loritta.morenitta.reactionevents

import net.perfectdreams.loritta.common.emojis.LorittaEmojiReference
import java.time.Instant
import java.util.*

data class ReactionSet(
    val reactionSetId: UUID,
    val spawnTimeRange: SpawnTimeRange?,
    val reaction: LorittaEmojiReference,
    val chance: Double,
    val pointsPayout: Int
) {
    data class SpawnTimeRange(
        val startsAt: Instant,
        val endsAt: Instant
    )
}