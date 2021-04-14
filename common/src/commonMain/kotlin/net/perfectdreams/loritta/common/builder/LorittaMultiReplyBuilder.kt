package net.perfectdreams.loritta.common.builder

class LorittaMultiReplyBuilder(private val builderFactory: BuilderFactory) {
    val replies = mutableListOf<LorittaReplyBuilder>()

    fun append(block: LorittaReplyBuilder.() -> (Unit)) {
        replies.add(builderFactory.createReplyBuilder().apply(block))
    }

    fun build() = replies.joinToString("\n") { it.build() }
}