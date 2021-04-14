package net.perfectdreams.loritta.common.builder

class LorittaMultiReplyBuilder {
    val replies = mutableListOf<LorittaReplyBuilder>()

    fun append(block: LorittaReplyBuilder.() -> (Unit)) {
        replies.add(LorittaReplyBuilder().apply(block))
    }
}