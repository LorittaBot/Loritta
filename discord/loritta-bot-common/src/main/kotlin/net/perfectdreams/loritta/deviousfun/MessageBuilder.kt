package net.perfectdreams.loritta.deviousfun

import dev.kord.common.entity.AllowedMentionType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.AllowedMentionsBuilder
import net.perfectdreams.loritta.deviousfun.entities.Message
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import java.io.File

class MessageBuilder {
    companion object {
        operator fun invoke(content: String) = MessageBuilder().apply { setContent(content) }

        /**
         * Creates a [MessageBuilder] from an already created [DeviousMessage]
         */
        operator fun invoke(message: DeviousMessage): MessageBuilder {
            val builder = MessageBuilder()
            builder.setContent(message.contentRaw)

            for (embed in message.embeds) {
                builder.addEmbed(embed)
            }

            for (file in message.files) {
                builder.addFile(file.data, file.fileName)
            }

            val refId = message.referenceId
            if (refId != null)
                builder.reference(refId)

            builder.allowedMentionsBuilder = message.allowedMentionsBuilder

            return builder
        }

        private val allowedMentionTypes = setOf(
            AllowedMentionType.UserMentions,
            AllowedMentionType.RoleMentions,
            AllowedMentionType.EveryoneMentions
        )
    }

    val content = StringBuilder()
    private val files = mutableListOf<FileToBeSent>()
    private var referenceId: Snowflake? = null
    private val embeds = mutableListOf<DeviousEmbed>()
    private var allowedMentionsBuilder: AllowedMentionsBuilder? = null
    val isEmpty: Boolean
        get() = content.isEmpty() && files.isEmpty() && embeds.isEmpty()

    fun append(text: String): MessageBuilder {
        content.append(text)
        return this
    }

    fun setContent(text: String): MessageBuilder {
        content.clear()
        content.append(text)
        return this
    }

    fun addEmbed(embed: DeviousEmbed): MessageBuilder {
        embeds.add(embed)
        return this
    }

    fun setEmbed(embed: DeviousEmbed?): MessageBuilder {
        embeds.clear()

        if (embed != null)
            embeds.add(embed)
        return this
    }

    fun allowMentions(vararg types: AllowedMentionType): MessageBuilder {
        val currentAllowedMentions = (allowedMentionsBuilder ?: AllowedMentionsBuilder())
        currentAllowedMentions.apply {
            for (type in types) {
                add(type)
            }
        }
        this.allowedMentionsBuilder = currentAllowedMentions
        return this
    }

    fun denyMentions(vararg types: AllowedMentionType): MessageBuilder {
        val currentAllowedMentions = (allowedMentionsBuilder ?: AllowedMentionsBuilder())
        currentAllowedMentions.apply {
            for (type in (types.toSet() - allowedMentionTypes)) {
                add(type)
            }
        }
        return this
    }

    fun setAllowedMentions(types: List<AllowedMentionType>): MessageBuilder {
        val currentAllowedMentions = AllowedMentionsBuilder()
        currentAllowedMentions.apply {
            for (type in types) {
                add(type)
            }
        }
        this.allowedMentionsBuilder = currentAllowedMentions
        return this
    }

    fun addFile(file: File, fileName: String = file.name): MessageBuilder {
        files.add(FileToBeSent(fileName, file.readBytes()))
        return this
    }

    fun addFile(bytes: ByteArray, fileName: String): MessageBuilder {
        files.add(FileToBeSent(fileName, bytes))
        return this
    }

    fun reference(messageId: Snowflake): MessageBuilder {
        this.referenceId = messageId
        return this
    }

    /**
     * Make the message a reply to the referenced message.
     *
     * This checks if the bot has [net.dv8tion.jda.api.Permission.ReadMessageHistory] and, if it has, the message is referenced.
     *
     * @param message The target message
     *
     * @return Updated MessageAction for chaining convenience
     */
    suspend fun referenceIfPossible(message: Message): MessageBuilder {
        if (message.isFromGuild && !message.guild.retrieveSelfMember()
                .hasPermission(message.textChannel, Permission.ReadMessageHistory)
        )
            return this
        return this.reference(message.idSnowflake)
    }

    /**
     * Make the message a reply to the referenced message.
     *
     * This has the same checks as [referenceIfPossible] plus a check to see if [addInlineReply] is enabled and to check if [ServerConfig.deleteMessageAfterCommand] is false.
     *
     * @param message The target message
     *
     * @return Updated MessageAction for chaining convenience
     */
    suspend fun referenceIfPossible(
        message: Message,
        serverConfig: ServerConfig,
        addInlineReply: Boolean = true
    ): MessageBuilder {
        // We check if deleteMessageAfterCommand is true because it doesn't matter trying to reply to a message that's going to be deleted.
        if (!addInlineReply || serverConfig.deleteMessageAfterCommand)
            return this
        return this.referenceIfPossible(message)
    }

    fun build() = DeviousMessage(
        content.toString(),
        embeds,
        files,
        referenceId,
        allowedMentionsBuilder
    )
}