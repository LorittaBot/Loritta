package net.perfectdreams.loritta.utils

import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply

/**
 * Class used for building styled replies with DSL to make creating
 * multiple replies easier and nicer!
 */
class ReplyBuilder {

    private val replies = mutableListOf<LorittaReply>()

    /**
     * Adds a standard [LorittaReply] to the [replies]
     * list, that will be present on the [build] result
     *
     * @param reply The reply that will be appended
     */
    fun append(reply: LorittaReply) =
            replies.add(reply)

    /**
     * Appends a reply without using DSL, it's good for
     * compact replies
     *
     * @param message The message that will be displayed after the prefix
     * @param prefix The first thing displayed on the built message (usually an emoji/emote)
     * @param hasPadding If [prefix] is null and this value is set to true, the prefix will be the blue diamond emoji (🔹)
     * @param mentionUser If set to true, we'll mention the user on the reply
     */
    fun append(message: String = " ", prefix: String? = null, forceMention: Boolean = false, hasPadding: Boolean = true, mentionUser: Boolean = true) =
            append(LorittaReply(message, prefix, forceMention, hasPadding, mentionUser))

    /**
     * Appends a new reply with the provided data
     * at the [reply] scope
     *
     * @param reply The reply data/message
     */
    inline fun append(reply: StyledReplyWrapper.() -> Unit) =
            append(StyledReplyWrapper().apply(reply).asLorittaReply())

    /**
     * If the [condition] is true, we'll append the provided
     * reply wrapper
     *
     * @param condition The condition that will be checked
     * @param reply The reply that will be added if [condition] is true
     */
    inline fun appendIf(condition: Boolean, reply: StyledReplyWrapper.() -> Unit) {
        if (condition) {
            append(reply)
        }
    }

    fun build(): List<LorittaReply> = replies

}

/**
 * A mutable [LorittaReply] wrapper to provide a better DSL
 * experience to replies
 *
 * @param message The message that will be displayed after the prefix
 * @param prefix The first thing displayed on the built message (usually an emoji/emote)
 * @param hasPadding If [prefix] is null and this value is set to true, the prefix will be the blue diamond emoji (🔹)
 * @param mentionUser If set to true, we'll mention the user on the reply
 */
data class StyledReplyWrapper(
        var message: String = " ",
        var prefix: String? = null,
        var forceMention: Boolean = false,
        var hasPadding: Boolean = true,
        var mentionUser: Boolean = true
) {

    /**
     * Create a [LorittaReply] instance with all the provided data
     * at the constructor, or the data that was implemented later.
     *
     * @return The wrapped [LorittaReply]
     */
    fun asLorittaReply() = LorittaReply(message, prefix, forceMention, hasPadding, mentionUser)

}

/**
 * Creates a [ReplyBuilder], this makes replies much nicer and
 * smoother, and of course, provides the extraordinary DSL experience!
 *
 * @param builder The reply builder
 */
suspend fun CommandContext.sendStyledReply(builder: ReplyBuilder.() -> Unit) =
        reply(buildStyledReply(builder))

/**
 * Just creates a [MutableList<LorittaReply>] of all the
 * appended replies inside the DSL
 *
 * @param builder The reply builder
 * @return All the provided replies inside the [builder] scope
 */
fun buildStyledReply(builder: ReplyBuilder.() -> Unit) =
        ReplyBuilder().apply(builder).build()