package net.perfectdreams.loritta.utils

import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply

/**
 * Class used for building styled replies with DSL to make creating
 * multiple replies easier and nicer!
 */
class ReplyBuilder {

    private val replies = mutableListOf<LorittaReply>()

    fun append(reply: LorittaReply) =
            replies.add(reply)

    fun append(message: String = " ", prefix: String? = null, forceMention: Boolean = false, hasPadding: Boolean = true, mentionUser: Boolean = true) =
            append(LorittaReply(message, prefix, forceMention, hasPadding, mentionUser))

    inline fun append(reply: StyledReplyWrapper.() -> Unit) =
            append(StyledReplyWrapper().apply(reply).asLorittaReply())

    inline fun appendIf(condition: Boolean, reply: StyledReplyWrapper.() -> Unit) {
        if (condition) {
            append(reply)
        }
    }

    fun build(): List<LorittaReply> = replies

}

data class StyledReplyWrapper(
        var message: String = " ",
        var prefix: String? = null,
        var forceMention: Boolean = false,
        var hasPadding: Boolean = true,
        var mentionUser: Boolean = true
) {

    fun asLorittaReply() = LorittaReply(message, prefix, forceMention, hasPadding, mentionUser)

}

suspend fun CommandContext.styledReply(builder: ReplyBuilder.() -> Unit) =
        reply(buildStyledReply(builder))

fun buildStyledReply(builder: ReplyBuilder.() -> Unit) =
        ReplyBuilder().apply(builder).build()