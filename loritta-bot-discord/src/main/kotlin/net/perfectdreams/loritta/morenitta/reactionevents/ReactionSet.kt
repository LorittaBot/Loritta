package net.perfectdreams.loritta.morenitta.reactionevents

import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.common.emojis.LorittaEmojiReference
import java.time.Instant
import java.util.*

data class ReactionSet(
    val reactionSetId: UUID,
    val spawnTimeRange: SpawnTimeRange?,
    val reaction: LorittaEmojiReference,
    val chanceProvider: (guild: Guild?) -> (Double),
    val pointsPayout: Int
) {
    data class SpawnTimeRange(
        val startsAt: Instant,
        val endsAt: Instant
    )
}