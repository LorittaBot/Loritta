package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.loritta.cinnamon.common.emotes.Emote
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.entities.LorittaReply

/**
 * Appends a Loritta-styled formatted message to the builder's message content.
 *
 * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`.
 *
 * If there's already content present in the builder, a new line will be inserted before the styled replied!
 *
 * @param content the already built LorittaReply
 */
fun MessageBuilder.styled(content: String, prefix: Emote) = styled(content, prefix.asMention)

/**
 * Appends a Loritta-styled formatted message to the builder's message content.
 *
 * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`.
 *
 * If there's already content present in the builder, a new line will be inserted before the styled replied!
 *
 * @param content the content of the message
 * @param prefix  the prefix of the message
 */
fun MessageBuilder.styled(content: String, prefix: String = Emotes.DefaultStyledPrefix.asMention) = styled(LorittaReply(content, prefix))

/**
 * Appends a Loritta-styled formatted message to the builder's message content.
 *
 * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`.
 *
 * If there's already content present in the builder, a new line will be inserted before the styled replied!
 *
 * @param content the already built LorittaReply
 */
fun MessageBuilder.styled(reply: LorittaReply) {
    if (content != null) {
        content += "\n"
        content += "${reply.prefix} **|** ${reply.content}"
    } else {
        content = "${reply.prefix} **|** ${reply.content}"
    }
}