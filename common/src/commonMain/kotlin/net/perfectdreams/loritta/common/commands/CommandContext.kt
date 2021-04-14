package net.perfectdreams.loritta.common.commands

import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.builder.LorittaMultiReplyBuilder
import net.perfectdreams.loritta.common.builder.LorittaReplyBuilder
import net.perfectdreams.loritta.common.entities.MessageChannel
import net.perfectdreams.loritta.common.locale.BaseLocale

open class CommandContext(
    // Nifty trick: By keeping it "open", implementations can override this variable.
    // By doing this, classes can use their own platform implementation (example: LorittaDiscord instead of LorittaBot)
    // If you don't keep it "open", the type will always be "LorittaBot", which sucks.
    open val loritta: LorittaBot,
    val locale: BaseLocale,
    val channel: MessageChannel
) {
    suspend fun sendReply(block: LorittaReplyBuilder.() -> (Unit))
            = sendMessage(loritta.builderFactory.createReplyBuilder().apply(block).build())

    suspend fun sendMultiReply(block: LorittaMultiReplyBuilder.() -> (Unit))
            = sendMessage(loritta.builderFactory.createMultiReplyBuilder().apply(block).build())

    suspend fun sendMessage(message: String) {
        channel.sendMessage(message)
    }
}