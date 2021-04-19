package net.perfectdreams.loritta.common.builder

class LorittaMultiReplyBuilder {
    val replies = mutableListOf<LorittaReplyBuilder>()
    var isEphemeral = false

    fun append(block: LorittaReplyBuilder.() -> (Unit)) {
        replies.add(LorittaReplyBuilder().apply(block))
    }
}