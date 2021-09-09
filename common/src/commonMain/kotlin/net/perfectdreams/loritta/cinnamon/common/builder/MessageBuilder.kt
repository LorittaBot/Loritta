package net.perfectdreams.loritta.cinnamon.common.builder

import net.perfectdreams.loritta.cinnamon.common.emotes.Emote
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.entities.LorittaImpersonation
import net.perfectdreams.loritta.cinnamon.common.entities.LorittaMessage
import net.perfectdreams.loritta.cinnamon.common.entities.LorittaReply
import net.perfectdreams.loritta.cinnamon.common.entities.User
import net.perfectdreams.loritta.cinnamon.common.images.ImageReference
import net.perfectdreams.loritta.cinnamon.common.utils.CinnamonDslMarker
import net.perfectdreams.loritta.cinnamon.common.utils.embed.EmbedBuilder

@CinnamonDslMarker
class MessageBuilder {
    var content: String? = null
    var replies = mutableListOf<LorittaReply>()
    var embeds: MutableList<EmbedBuilder>? = null
    // There isn't a multiplatform input stream (sad)
    var files = mutableMapOf<String, ByteArray>()
    var isEphemeral = false
    var allowedMentions = AllowedMentionsBuilder()
    var impersonation: LorittaImpersonation? = null

    fun impersonation(username: String, avatar: ImageReference) {
        impersonation = LorittaImpersonation(username, avatar)
    }

    /**
     * Appends an embed to this builder
     *
     * @param embed a embed builder
     */
    fun embed(declaration: EmbedBuilder.() -> Unit) {
        embeds = (embeds ?: mutableListOf()).also {
            it.add(EmbedBuilder().apply(declaration))
        }
    }

    /**
     * Appends a Loritta-styled formatted message to this builder
     *
     * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`, however implementations can change the look and feel of the message.
     *
     * Prefixes should *not* be used for important behavior of the command!
     *
     * @param content the already built LorittaReply
     */
    fun styled(content: String, prefix: Emote) = styled(content, prefix.asMention)

    /**
     * Appends a Loritta-styled formatted message to this builder
     *
     * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`, however implementations can change the look and feel of the message.
     *
     * Prefixes should *not* be used for important behavior of the command!
     *
     * @param content the content of the message
     * @param prefix  the prefix of the message
     */
    fun styled(content: String, prefix: String = Emotes.defaultStyledPrefix.asMention) = styled(LorittaReply(content, prefix))

    /**
     * Appends a Loritta-styled formatted message to this builder
     *
     * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`, however implementations can change the look and feel of the message.
     *
     * Prefixes should *not* be used for important behavior of the command!
     *
     * @param content the already built LorittaReply
     */
    fun styled(reply: LorittaReply) = replies.add(reply)

    /**
     * Mentions a [user] in this message
     *
     * @param user         the user that will be mentioned
     * @param notifyUser if the user should be notified about the message
     * @return the user mention
     */
    fun mentionUser(user: User, notifyUser: Boolean = true): String {
        if (notifyUser)
            allowedMentions.users.add(user)

        return user.asMention
    }

    fun addFile(fileName: String, stream: ByteArray) {
        files[fileName] = stream
    }

    fun build(): LorittaMessage {
        val content = content
        val embed = embeds
        val files = files

        if (content == null && embeds?.isNotEmpty() != true && files.isEmpty() && replies.isEmpty())
            throw UnsupportedOperationException("Message needs to have at least content, embed or a file!")

        return LorittaMessage(
            content,
            replies,
            embed,
            files,
            isEphemeral,
            allowedMentions.build(),
            impersonation
        )
    }
}