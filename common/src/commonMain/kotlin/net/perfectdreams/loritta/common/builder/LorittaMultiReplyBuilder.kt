package net.perfectdreams.loritta.common.builder

import net.perfectdreams.loritta.common.utils.CinnamonDslMarker

@CinnamonDslMarker
class LorittaMultiReplyBuilder {
    val replies = mutableListOf<LorittaReplyBuilder>()
    var isEphemeral = false

    fun append(block: LorittaReplyBuilder.() -> (Unit)) {
        replies.add(LorittaReplyBuilder().apply(block))
    }
}