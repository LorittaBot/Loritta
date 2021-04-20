package net.perfectdreams.loritta.common.commands

import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.builder.LorittaMultiReplyBuilder
import net.perfectdreams.loritta.common.builder.LorittaReplyBuilder
import net.perfectdreams.loritta.common.builder.MessageBuilder
import net.perfectdreams.loritta.common.entities.AllowedMentions
import net.perfectdreams.loritta.common.entities.LorittaEmbed
import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.common.entities.MessageChannel
import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.embed.EmbedBuilder

abstract class CommandContext(
    // Nifty trick: By keeping it "open", implementations can override this variable.
    // By doing this, classes can use their own platform implementation (example: LorittaDiscord instead of LorittaBot)
    // If you don't keep it "open", the type will always be "LorittaBot", which sucks.
    open val loritta: LorittaBot,
    val locale: BaseLocale,
    val user: User,
    val channel: MessageChannel
) {
    abstract suspend fun sendReply(block: LorittaReplyBuilder.() -> (Unit))
    abstract suspend fun sendMultiReply(block: LorittaMultiReplyBuilder.() -> (Unit))

    suspend fun sendMessage(message: String, embed: LorittaEmbed? = null) {
        channel.sendMessage(
            LorittaMessage(
                message,
                embed,
                emptyMap(),
                isEphemeral = false,
                AllowedMentions(setOf(), true)
            )
        )
    }

    suspend fun sendMessage(block: MessageBuilder.() -> (Unit)) = channel.sendMessage(block)

    suspend fun sendEmbed(message: String = "", embed: EmbedBuilder.() -> Unit) {
        sendMessage(message, EmbedBuilder().apply(embed).build())
    }
}