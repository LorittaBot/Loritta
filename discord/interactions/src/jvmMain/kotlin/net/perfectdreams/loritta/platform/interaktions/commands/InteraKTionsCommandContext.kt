package net.perfectdreams.loritta.platform.interaktions.commands

import net.perfectdreams.loritta.common.builder.LorittaMultiReplyBuilder
import net.perfectdreams.loritta.common.builder.LorittaReplyBuilder
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.entities.MessageChannel
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.platform.interaktions.LorittaInteraKTions

class InteraKTionsCommandContext(
    override val loritta: LorittaInteraKTions,
    locale: BaseLocale,
    channel: MessageChannel
) : CommandContext(loritta, locale, channel) {
    override suspend fun sendReply(block: LorittaReplyBuilder.() -> Unit) {
        val builder = LorittaReplyBuilder().apply(block)

        sendMessage {
            content = "${builder.prefix} **|** ${builder.content}"
            isEphemeral = builder.isEphemeral
            allowedMentions = builder.allowedMentions
        }
    }

    override suspend fun sendMultiReply(block: LorittaMultiReplyBuilder.() -> Unit) {
        val builder = LorittaMultiReplyBuilder().apply(block)
        sendMessage {
            content = builder.replies.joinToString("\n") { "${it.prefix} **|** ${it.content}" }
            isEphemeral = builder.isEphemeral
        }
    }
}