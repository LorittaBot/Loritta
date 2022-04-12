package net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor.modules

import dev.kord.common.Color
import dev.kord.common.entity.DiscordMessage
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Reaction
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.UserMessageModifyBuilder
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import kotlinx.datetime.Instant
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.entities.Icon
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.microservices.directmessageprocessor.DiscordGatewayEventsProcessor
import net.perfectdreams.loritta.cinnamon.pudding.tables.StarboardMessages
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.sql.Connection
import java.util.*

class StarboardModule(val m: DiscordGatewayEventsProcessor) {
    companion object {
        private val STAR_REACTION = "⭐"
        private val logger = KotlinLogging.logger {}
    }

    suspend fun handleStarboardReaction(
        channelId: Snowflake,
        guildId: Snowflake,
        messageId: Snowflake,
        emoji: DiscordPartialEmoji
    ) {
        // If it isn't a star, return
        if (emoji.name != STAR_REACTION)
            return

        val serverConfig = m.services.serverConfigs.getServerConfigRoot(guildId.value) ?: return
        val starboardConfig = serverConfig.getStarboardConfig() ?: return
        val i18nContext = serverConfig.localeId

        val starboardId = starboardConfig.starboardChannelId
        // Ignore if someone is trying to be "haha i'm so funni" trying to add stars to the starboard channel
        if (starboardId == channelId.value)
            return

        val lockKey = UUID.nameUUIDFromBytes("starboard-message:$guildId:$channelId:$messageId".toByteArray(Charsets.UTF_8))
            .mostSignificantBits

        // Now we are going to do everything within the transaction, to avoid multiple threads trying to update the same message at the same time
        // We don't need to care about having repeatable reads because we are handling locks at the application level
        // However we want to read data that was already commited by a different transaction
        m.services.transaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED) {
            // This is blocks if a lock is held
            // TODO: Proper starboard hash to avoid locking the entire table just to query
            logger.info { "Trying to hold advisory lock for message ${messageId}..." }

            // TODO: If multiple users are reacting at the same time, batch them and edit the message every X seconds
            exec("SELECT pg_try_advisory_xact_lock($lockKey) AS \"was_unlocked\", pg_advisory_xact_lock($lockKey);") {
                while (it.next()) {
                    logger.info { "Was ${messageId} locked before? ${it.getBoolean("was_unlocked")}" }
                }
            }

            // Get if the current message is already sent in the starboard
            val starboardMessageFromDatabase = StarboardMessages.select {
                StarboardMessages.guildId eq guildId.value.toLong() and (StarboardMessages.messageId eq messageId.value.toLong())
            }
                .limit(1)
                .firstOrNull()

            logger.info { "Does message $messageId have a message in the database? ${starboardMessageFromDatabase != null}" }

            val reactedMessage = m.rest.channel.getMessage(channelId, messageId)

            // Maybe null if there isn't any reactions in the message
            val reactionList = reactedMessage.reactions.value

            // The star may be missing if there isn't any reactions in the message
            val starReaction = reactionList?.firstOrNull { it.emoji.name == STAR_REACTION }

            val starReactionCount = starReaction?.count ?: 0

            logger.info { "Current Star Reaction Count for message $messageId: $starReactionCount" }

            if (starboardMessageFromDatabase != null && starboardConfig.requiredStars > starReactionCount) {
                logger.info { "Starboard message for $messageId is present in the database, but it is below the required stars threshold! Deleting message..." }

                // Star Reaction is less than the required star count, delete the starboard message and from the database
                StarboardMessages.deleteWhere {
                    StarboardMessages.id eq starboardMessageFromDatabase[StarboardMessages.id]
                }

                // Bye starboard message!
                m.rest.channel.deleteMessage(
                    Snowflake(starboardId),
                    Snowflake(starboardMessageFromDatabase[StarboardMessages.embedId])
                )
                return@transaction
            }

            if (starReaction != null) {
                if (starboardMessageFromDatabase != null) {
                    logger.info { "Starboard message for $messageId is present in the database!" }

                    val embedId = starboardMessageFromDatabase[StarboardMessages.embedId]

                    m.rest.channel.editMessage(
                        Snowflake(starboardId),
                        Snowflake(embedId),
                        modifyStarboardMessage(
                            guildId,
                            reactedMessage,
                            starReaction
                        )
                    )
                } else if (starReactionCount >= starboardConfig.requiredStars) {
                    logger.info { "Starboard message for $messageId is not present in the database..." }

                    // Message doesn't exist on the database and we have enough stars to create the message! Create and send...

                    // Don't send messages to the starboard if the source channel is NSFW
                    // TODO: Caching
                    val channel = m.rest.channel.getChannel(channelId)
                    if (channel.nsfw.discordBoolean)
                        return@transaction

                    val newMessage = m.rest.channel.createMessage(
                        Snowflake(starboardId),
                        createStarboardMessage(
                            guildId,
                            reactedMessage,
                            starReaction
                        )
                    )

                    StarboardMessages.insert {
                        it[StarboardMessages.guildId] = guildId.value.toLong()
                        it[StarboardMessages.messageId] = reactedMessage.id.value.toLong()
                        it[StarboardMessages.embedId] = newMessage.id.value.toLong()
                    }
                }
            }
        }

        /* if (e.reactionEmote.isEmote("⭐")) {
            val textChannel = guild.getTextChannelById(starboardId) ?: return

            // Caso não tenha permissão para ver o histórico de mensagens, retorne!
            if (!e.guild.selfMember.hasPermission(e.textChannel, Permission.MESSAGE_HISTORY, Permission.MESSAGE_EMBED_LINKS))
                return

            // Criar um mutex da guild, para evitar que envie várias mensagens iguais ao mesmo tempo
            val mutex = mutexes.getOrPut(e.guild.idLong) { Mutex() }

            mutex.withLock {
                val msg = e.textChannel.retrieveMessageById(e.messageId).await() ?: return@withLock

                // Se algum "engracadinho" está enviando reações nas mensagens do starboard, apenas ignore.
                // Também verifique se a Lori pode falar no canal!
                if (textChannel == msg.textChannel || !textChannel.canTalk())
                    return@withLock

                val starboardEmbedMessage = loritta.newSuspendedTransaction {
                    StarboardMessage.find {
                        StarboardMessages.guildId eq e.guild.idLong and (StarboardMessages.messageId eq e.messageIdLong)
                    }.firstOrNull()
                }

                val starboardEmbedMessageId = starboardEmbedMessage?.embedId

                var starboardMessage: Message? = starboardEmbedMessageId?.let {
                    try {
                        textChannel.retrieveMessageById(starboardEmbedMessageId).await()
                    } catch (exception: Exception) {
                        logger.error(exception) { "Error while retrieving starboard embed ID $starboardEmbedMessageId from ${e.guild}"}
                        null
                    }
                }

                val embed = EmbedBuilder()
                val count = e.reaction.retrieveUsers().await().size
                val content = msg.contentRaw

                embed.setAuthor("${msg.author.name}#${msg.author.discriminator} (${msg.author.id})", null, msg.author.effectiveAvatarUrl)
                embed.setTimestamp(msg.timeCreated)
                embed.setColor(Color(255, 255, Math.max(255 - (count * 15), 0)))
                embed.addField("Ir para a mensagem", "[Clique aqui](https://discordapp.com/channels/${msg.guild.id}/${msg.channel.id}/${msg.id})", false)

                var emoji = "⭐"

                if (count >= 5) {
                    emoji = "\uD83C\uDF1F"
                }
                if (count >= 10) {
                    emoji = "\uD83C\uDF20"
                }
                if (count >= 15) {
                    emoji = "\uD83D\uDCAB"
                }
                if (count >= 20) {
                    emoji = "\uD83C\uDF0C"
                }

                var hasImage = false
                if (msg.attachments.isNotEmpty()) { // Se tem attachments...
                    var fieldValue = ""
                    for (attach in msg.attachments) {
                        if (attach.isImage && !hasImage) { // Se é uma imagem...
                            embed.setImage(attach.url) // Então coloque isso como a imagem no embed!
                            hasImage = true
                        }
                        fieldValue += "\uD83D\uDD17 **|** [${attach.fileName}](${attach.url})\n"
                    }
                    embed.addField("Arquivos", fieldValue, false)
                }

                embed.setDescription(content)

                val starCountMessage = MessageBuilder()
                starCountMessage.append("$emoji **$count** - ${e.textChannel.asMention}")
                starCountMessage.setEmbed(embed.build())

                if (starboardMessage != null) {
                    if (starboardConfig.requiredStars > count) { // Remover embed já que o número de stars é menos que o número necessário de estrelas
                        loritta.newSuspendedTransaction {
                            starboardEmbedMessage?.delete() // Remover da database
                        }
                        starboardMessage.delete().await() // Deletar a embed do canal de starboard
                    } else {
                        // Editar a mensagem com a nova mensagem!
                        starboardMessage.editMessage(starCountMessage.build()).await()
                    }
                } else if (count >= starboardConfig.requiredStars) {
                    starboardMessage = textChannel.sendMessage(starCountMessage.build()).await()

                    loritta.newSuspendedTransaction {
                        StarboardMessage.new {
                            this.guildId = e.guild.idLong
                            this.embedId = starboardMessage.idLong
                            this.messageId = msg.idLong
                        }
                    }
                }
            }
        } */
    }

    private fun createStarboardMessage(
        guildId: Snowflake,
        message: DiscordMessage,
        reaction: Reaction
    ): UserMessageCreateBuilder.() -> (Unit) = {
        val emoji = getStarEmojiForReactionCount(reaction.count)

        content = "$emoji **${reaction.count}** - <#${message.channelId}>"

        val embed = createStarboardEmbed(message, reaction)

        embed(embed)

        actionRow {
            linkButton("https://discord.com/channels/$guildId/${message.channelId}/${message.id}") {
                label = "Ir para a mensagem"
            }
        }
    }

    private fun modifyStarboardMessage(
        guildId: Snowflake,
        message: DiscordMessage,
        reaction: Reaction
    ): UserMessageModifyBuilder.() -> (Unit) = {
        val emoji = getStarEmojiForReactionCount(reaction.count)

        content = "$emoji **${reaction.count}** - <#${message.channelId}>"

        val embed = createStarboardEmbed(message, reaction)

        embed(embed)

        actionRow {
            linkButton("https://discord.com/channels/$guildId/${message.channelId}/${message.id}") {
                label = "Ir para a mensagem"
            }
        }
    }

    private fun createStarboardEmbed(
        message: DiscordMessage,
        reaction: Reaction
    ): EmbedBuilder.() -> (Unit) = {
        val count = reaction.count

        // TODO: Refactor
        val avatar = message.author.avatar?.let { Icon.UserAvatar(message.author.id, it) } ?: Icon.DefaultUserAvatar(message.author.discriminator.toInt())
        author(
            message.author.username + "#" + message.author.discriminator + " (${message.author.id})",
            null,
            avatar.cdnUrl.toUrl()
        )

        // Show the message's attachments in the embed
        if (message.attachments.isNotEmpty()) {
            field(
                "${Emotes.FileFolder} Arquivos",
                message.attachments.joinToString("\n") {
                    "[${it.filename}](${it.url})"
                }
            )
        }

        // Cut if the message is too long
        description = message.content.shortenWithEllipsis(2048)

        // Set the embed's image to the first attachment in the message
        // TODO: Only set it as the embed's image if it is really an image
        image = message.attachments.firstOrNull()?.url
        color = Color(255, 255, (255 - (count * 15)).coerceAtLeast(0))
        timestamp = Instant.parse(message.timestamp)
    }

    private fun getStarEmojiForReactionCount(count: Int) = when {
        count == 69 -> Emotes.LoriBonk.toString() // Easter Egg
        count >= 20 -> "\uD83C\uDF0C"
        count >= 15 -> "\uD83D\uDCAB"
        count >= 10 -> "\uD83C\uDF20"
        count >= 5 -> "\uD83C\uDF1F"
        else -> STAR_REACTION
    }
}