package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.Color
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.channel.ChannelPermissionModifyBuilder
import dev.kord.rest.builder.channel.TextChannelModifyBuilder
import dev.kord.rest.builder.channel.VoiceChannelModifyBuilder
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.json.request.BulkDeleteRequest
import dev.kord.rest.route.Position
import dev.kord.rest.service.editRolePermission
import dev.kord.rest.service.patchTextChannel
import dev.kord.rest.service.patchVoiceChannel
import io.ktor.client.request.forms.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.toKotlinInstant
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.deviouscache.data.DeviousChannelData
import net.perfectdreams.loritta.deviousfun.DeviousEmbed
import net.perfectdreams.loritta.deviousfun.DeviousMessage
import net.perfectdreams.loritta.deviousfun.DeviousFun
import net.perfectdreams.loritta.deviousfun.MessageBuilder
import net.perfectdreams.loritta.deviousfun.cache.DeviousMessageFragmentData
import net.perfectdreams.loritta.deviousfun.utils.DeviousUserUtils
import kotlin.math.ceil

class Channel(
    val deviousFun: DeviousFun,
    val guildOrNull: Guild?,
    val channel: DeviousChannelData
) : IdentifiableSnowflake {
    companion object {
        private val ALWAYS_CAN_TALK_CHANNEL_TYPES = setOf(
            ChannelType.DM
        )
    }

    override val idSnowflake: Snowflake
        get() = channel.id
    val name: String?
        get() = channel.name
    val type: ChannelType
        get() = channel.type
    val guildId: Snowflake?
        get() = channel.guildId
    val position: Int?
        get() = channel.position
    val topic: String?
        get() = channel.topic
    val isNSFW: Boolean
        get() = channel.nsfw
    val guild: Guild
        get() = guildOrNull ?: error("This channel is not in a guild!")
    val permissionOverwrites: List<Overwrite>
        get() = channel.permissionOverwrites
            ?.map {
                Overwrite(deviousFun, this, it)
            } ?: listOf()
    val history: MessageHistory
        get() = MessageHistory(this)

    val asMention: String
        get() = "<#${idSnowflake}>"

    fun getPermissionOverride(role: Role) = permissionOverwrites.firstOrNull { it.id == role.idSnowflake }

    suspend fun createPermissionOverride(role: Role, builder: ChannelPermissionModifyBuilder.() -> Unit) =
        deviousFun.loritta.rest.channel.editRolePermission(channel.id, role.idSnowflake, builder)

    suspend fun modifyTextChannel(builder: TextChannelModifyBuilder.() -> (Unit)) {
        deviousFun.loritta.rest.channel.patchTextChannel(channel.id, builder)
    }

    suspend fun modifyVoiceChannel(builder: VoiceChannelModifyBuilder.() -> (Unit)) {
        deviousFun.loritta.rest.channel.patchVoiceChannel(channel.id, builder)
    }

    suspend fun sendMessage(content: String) = sendMessage(
        MessageBuilder(content)
            .build()
    )

    suspend fun sendMessage(embed: DeviousEmbed) = sendMessage(
        MessageBuilder()
            .setEmbed(embed)
            .build()
    )

    suspend fun sendMessage(message: DeviousMessage): Message {
        val newMessage = deviousFun.loritta.rest.channel.createMessage(channel.id) {
            this.content = message.contentRaw
            this.messageReference = message.referenceId
            this.failIfNotExists = false
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
                    this.image = embed.image
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
        val authorId = newMessage.author.id
        val user = deviousFun.cacheManager.createUser(
            newMessage.author,
            !DeviousUserUtils.isSenderWebhookOrSpecial(newMessage)
        )
        val member = guildOrNull?.let { deviousFun.retrieveMemberById(it, authorId) }

        return Message(
            deviousFun,
            this,
            user,
            member,
            guildOrNull,
            DeviousMessageFragmentData.from(newMessage)
        )
    }

    suspend fun retrieveMessageById(id: String): Message = retrieveMessageById(id.toLong())
    suspend fun retrieveMessageById(id: Long): Message {
        val retrievedMessage = deviousFun.loritta.rest.channel.getMessage(channel.id, Snowflake(id))
        val user = deviousFun.cacheManager.createUser(
            retrievedMessage.author,
            !DeviousUserUtils.isSenderWebhookOrSpecial(retrievedMessage)
        )
        val member = guildOrNull?.let { deviousFun.retrieveMemberById(it, retrievedMessage.author.id) }

        return Message(
            deviousFun,
            this@Channel,
            user,
            member,
            guildOrNull,
            DeviousMessageFragmentData.from(retrievedMessage)
        )
    }

    // Used in Pebble templates
    fun canTalkBlocking(): Boolean = runBlocking { canTalk() }

    suspend fun canTalk(): Boolean {
        if (type in ALWAYS_CAN_TALK_CHANNEL_TYPES)
            return true

        val guild = guildOrNull ?: error("Can't check if the bot can talk in a channel that has a null guild!")

        val member = guild.getMember(deviousFun.retrieveSelfUser()) ?: return false // Not a member, so get out of here!
        return canTalk(member)
    }

    suspend fun canTalk(member: Member): Boolean {
        // Technically a member won't ever be able to speak in a DM channel, but oh well
        if (type in ALWAYS_CAN_TALK_CHANNEL_TYPES)
            return true

        val guild = guildOrNull ?: error("Can't check if the bot can talk in a channel that has a null guild!")

        val lazyLoadedPermissions =
            deviousFun.loritta.cache.getLazyCachedPermissions(guild.idSnowflake, channel.id, member.idSnowflake)
        return lazyLoadedPermissions.canTalk()
    }

    suspend fun deleteMessageById(id: String) {
        deviousFun.loritta.rest.channel.deleteMessage(channel.id, Snowflake(id))
    }

    suspend fun deleteMessageById(id: Long) {
        deviousFun.loritta.rest.channel.deleteMessage(channel.id, Snowflake(id))
    }

    suspend fun purgeMessages(messages: List<Message>) = purgeMessagesById(messages.map { it.idLong })

    suspend fun purgeMessagesById(messageIds: List<Long>) {
        val messageIdsAsSnowflakes = messageIds.map { Snowflake(it) }
        val chunkedMessagesToBeDeleted = messageIdsAsSnowflakes.chunked(100)
        for (messagesToBeDeleted in chunkedMessagesToBeDeleted) {
            deviousFun.loritta.rest.channel.bulkDelete(
                channel.id,
                BulkDeleteRequest(messagesToBeDeleted)
            )
        }
    }

    suspend fun retrieveWebhooks(): List<Webhook> {
        return deviousFun.loritta.rest.webhook.getChannelWebhooks(channel.id)
            .map {
                Webhook(
                    deviousFun,
                    deviousFun.retrieveChannelById(guild, it.channelId),
                    it.user.value?.let { deviousFun.cacheManager.createUser(it, false) },
                    it
                )
            }
    }

    suspend fun createWebhook(name: String): Webhook {
        val webhook = deviousFun.loritta.rest.webhook.createWebhook(channel.id, name) {}
        return Webhook(
            deviousFun,
            this,
            webhook.user.value?.let { deviousFun.cacheManager.createUser(it, false) },
            deviousFun.loritta.rest.webhook.createWebhook(channel.id, name) {}
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Channel)
            return false

        return this.idSnowflake == other.idSnowflake
    }

    override fun hashCode() = this.idSnowflake.hashCode()

    class MessageHistory(val channel: Channel) {
        var position = Position.Before(Snowflake.max)

        suspend fun retrievePast(search: Int): List<Message> {
            val howManySearchesShouldBeDone = ceil(search / 100.0).toInt()

            val messagesFound = mutableListOf<Message>()

            for (i in 0 until howManySearchesShouldBeDone) {
                val messages = channel.deviousFun.loritta.rest.channel.getMessages(
                    channel.idSnowflake,
                    position = position,
                    limit = search
                )

                if (messages.isEmpty())
                    break

                messagesFound.addAll(
                    messages.map {
                        val guild = channel.guildOrNull
                        val authorId = it.author.id
                        val member = guild?.retrieveMemberOrNullById(authorId.toLong())

                        Message(
                            channel.deviousFun,
                            channel,
                            channel.deviousFun.retrieveUserById(it.author.id),
                            member,
                            guild,
                            DeviousMessageFragmentData.from(it)
                        )
                    }
                )

                position = Position.Before(messagesFound.minOf { it.idSnowflake })
            }

            return messagesFound
        }
    }
}