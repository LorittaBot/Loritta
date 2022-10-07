package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.Color
import dev.kord.common.entity.*
import dev.kord.rest.builder.message.modify.embed
import io.ktor.client.request.forms.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.datetime.toKotlinInstant
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.deviousfun.*
import net.perfectdreams.loritta.deviousfun.cache.DeviousMemberData
import net.perfectdreams.loritta.deviousfun.cache.DeviousMessageFragmentData
import net.perfectdreams.loritta.deviousfun.cache.DeviousUserData
import net.perfectdreams.loritta.morenitta.utils.MarkdownSanitizer

class Message(
    val jda: JDA,
    val channel: Channel,
    val author: User,
    val memberOrNull: Member?,
    val guildOrNull: Guild?,
    val message: DeviousMessageFragmentData
) : IdentifiableSnowflake {
    companion object {
        private val DISCORD_EMOJI_REGEX = Regex("<(a)?:([A-z0-9_]+):([0-9]+)>")
    }

    override val idSnowflake: Snowflake
        get() = message.id
    val guild: Guild
        get() = guildOrNull ?: error("This message was not sent in a guild!")
    val member: Member
        get() = memberOrNull ?: error("This message was not sent in a guild!")
    val attachments: List<Attachment>
        get() = message.attachments.map {
            Attachment(jda, it)
        }
    val contentRaw: String
        get() = message.content
    // TODO - DeviousFun
    val contentDisplay: String
        get() = contentRaw
    val contentStripped by lazy {
        MarkdownSanitizer.sanitize(contentRaw)
    }
    val textChannel: Channel
        get() = if (channel.type == ChannelType.GuildText) channel else error("This was not sent in a TextChannel!")
    val emotes: List<GuildEmoteFromMessage>
        get() = DISCORD_EMOJI_REGEX.findAll(contentRaw)
            .map {
                GuildEmoteFromMessage(
                    jda,
                    Snowflake(it.groupValues[3]),
                    it.groupValues[2],
                    it.groupValues[1] == "a"
                )
            }.toList()
    // TODO - DeviousFun
    val embeds: List<MessageEmbed>
        get() = emptyList()
    val mentionedUsers: List<User>
        get() = message.mentions.map {
            User(jda, it.id, DeviousUserData.from(it))
        }
    val mentionedMembers: List<Member>
        get() = message.mentions
            .mapNotNull {
                val memberData = it.member.value ?: return@mapNotNull null

                Member(jda, DeviousMemberData.from(memberData), guild, User(jda, it.id, DeviousUserData.from(it)))
            }
    val mentionedRoles: List<Role>
        get() = TODO()
    val reactions: List<MessageReaction>
        get() = message.reactions.map {
            MessageReaction(
                jda,
                message.channelId,
                message.id,
                it.emoji.let {
                    DiscordPartialEmoji(
                        it.id,
                        it.name,
                        it.animated
                    )
                },
                it.count
            )
        }
    val jumpUrl: String
        get() = if (isFromGuild) {
            "https://discord.com/channels/${guild.id}/${channel.id}/${id}"
        } else {
            "https://discord.com/channels/@me/${channel.id}/${id}"
        }
    val type: MessageType
        get() = message.type
    val isPinned: Boolean
        get() = message.pinned

    val channelType: ChannelType
        get() = channel.type
    val isFromGuild: Boolean
        get() = channel.type == ChannelType.GuildText

    /**
     * Retrieves the referenced message
     */
    suspend fun retrieveReferencedMessage(): Message? {
        val fragmentData = message.referencedMessage ?: return null

        val retrievedMessage = jda.loritta.rest.channel.getMessage(channel.idSnowflake, Snowflake(id))
        val user = jda.cacheManager.createUser(retrievedMessage.author, retrievedMessage.webhookId.value == null)
        // The member seems to be null in a message reference
        val member = guildOrNull?.let { jda.retrieveMemberById(it, retrievedMessage.author.id) }

        return Message(
            jda,
            channel,
            user,
            member,
            guildOrNull,
            fragmentData
        )
    }

    suspend fun editMessage(content: String): Message {
        val newMessage = jda.loritta.rest.channel.editMessage(channel.idSnowflake, idSnowflake) {
            this.content = content
        }
        return Message(
            jda,
            channel,
            author,
            memberOrNull,
            guildOrNull,
            DeviousMessageFragmentData.from(newMessage)
        )
    }

    suspend fun editMessage(message: DeviousMessage): Message {
        val newMessage = jda.loritta.rest.channel.editMessage(channel.idSnowflake, idSnowflake) {
            this.content = message.contentRaw
            this.allowedMentions = message.allowedMentionsBuilder

            for (embed in message.embeds) {
                embed {
                    embed.author?.let {
                        author(it.title!!, it.url, it.iconUrl)
                    }
                    embed.title?.let {
                        title = it.title
                        url = it.url
                    }
                    embed.fields.forEach {
                        field(it.name, it.value, it.inline)
                    }
                    embed.footer?.let {
                        footer(it.text, it.iconUrl)
                    }
                    this.thumbnailUrl = embed.thumbnail
                    this.description = embed.description
                    this.color = embed.color?.let { Color(it) }
                    this.timestamp = embed.timestamp?.toKotlinInstant()
                }
            }

            for (file in message.files) {
                addFile(file.fileName, ChannelProvider { file.data.inputStream().toByteReadChannel() })
            }
        }

        // Yes, it needs to be from the channel, the ID from the newMessage is null
        // The author ID isn't null however
        val guild = channel.guildOrNull
        val authorId = newMessage.author.id
        val member = guild?.retrieveMemberById(authorId.toLong())

        return Message(
            jda,
            channel,
            jda.retrieveUserById(newMessage.author.id),
            member,
            guildOrNull,
            DeviousMessageFragmentData.from(newMessage)
        )
    }

    suspend fun editMessage(content: DeviousEmbed) = editMessage(
        MessageBuilder()
            .setEmbed(content)
            .build()
    )

    suspend fun addReaction(reaction: String) {
        jda.loritta.rest.channel.createReaction(channel.idSnowflake, idSnowflake, reaction)
    }

    suspend fun clearReactions() {
        jda.loritta.rest.channel.deleteAllReactions(channel.idSnowflake, idSnowflake)
    }

    suspend fun delete() {
        jda.loritta.rest.channel.deleteMessage(channel.idSnowflake, idSnowflake)
    }

    fun isFromType(type: ChannelType) = channelType == type

    suspend fun refresh(): Message {
        return Message(
            jda,
            channel,
            author,
            memberOrNull,
            guildOrNull,
            DeviousMessageFragmentData.from(jda.loritta.rest.channel.getMessage(channel.idSnowflake, idSnowflake))
        )
    }
}