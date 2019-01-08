package net.perfectdreams.loritta.utils.giveaway

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.getRandom
import com.mrpowergamerbr.loritta.utils.extensions.sendMessageAsync
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.MessageReaction
import net.dv8tion.jda.core.entities.TextChannel
import net.perfectdreams.loritta.dao.Giveaway
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

object GiveawayManager {
    var giveawayTasks = mutableMapOf<Long, Job>()
    private val logger = KotlinLogging.logger {}

    fun getReactionMention(reaction: String): String {
        val emoteId = reaction.toLongOrNull()

        if (emoteId != null) {
            val mention = lorittaShards.getEmoteById(emoteId.toString())?.asMention
            if (mention != null)
                return mention
        }

        return reaction
    }

    fun createEmbed(reason: String, description: String, reaction: String, epoch: Long): MessageEmbed {
        val secondsRemaining = (epoch - System.currentTimeMillis()) / 1000

        val embed = EmbedBuilder().apply {
            setTitle("\uD83C\uDF81 $reason")
            setDescription("$description\n\nUse ${getReactionMention(reaction)} para entrar!\nTempo restante: **$secondsRemaining** segundos")
            setColor(Constants.DISCORD_BLURPLE)
            setFooter("Acabará em", null)
            setTimestamp(Instant.ofEpochMilli(epoch))
        }

        return embed.build()
    }

    suspend fun spawnGiveaway(channel: TextChannel, reason: String, description: String, reaction: String, epoch: Long): Giveaway {
        val embed = createEmbed(reason, description, reaction, epoch)

        val message = channel.sendMessage(embed).await()
        val messageId = message.idLong

        val emoteId = reaction.toLongOrNull()

        if (emoteId != null) {
            val mention = lorittaShards.getEmoteById(emoteId.toString())
            message.addReaction(mention).await()
        } else {
            message.addReaction(reaction).await()
        }

        val giveaway = transaction(Databases.loritta) {
            Giveaway.new {
                this.guildId = channel.guild.idLong
                this.textChannelId = channel.idLong
                this.messageId = messageId

                this.numberOfWinners = 1
                this.reason = reason
                this.description = description
                this.finishAt = epoch
                this.reaction = reaction
            }
        }

        createGiveawayJob(giveaway)

        return giveaway
    }

    fun createGiveawayJob(giveaway: Giveaway) {
        giveawayTasks[giveaway.id.value] = GlobalScope.launch {
            try {
                while (giveaway.finishAt > System.currentTimeMillis()) {
                    if (!this.isActive) // Oh no, o giveaway acabou então a task não é mais necessária! Ignore...
                        return@launch

                    logger.info("Atualizando giveaway ${giveaway.id.value}")

                    val guild = lorittaShards.getGuildById(giveaway.guildId)
                    val channel = guild!!.getTextChannelById(giveaway.textChannelId)
                    val message = channel.getMessageById(giveaway.messageId).await()

                    message.editMessage(GiveawayManager.createEmbed(
                            giveaway.reason,
                            giveaway.description,
                            giveaway.reaction,
                            giveaway.finishAt
                    )).await()


                    delay(5000)
                }

                val guild = lorittaShards.getGuildById(giveaway.guildId)
                val channel = guild!!.getTextChannelById(giveaway.textChannelId)
                val message = channel.getMessageById(giveaway.messageId).await()

                GiveawayManager.finishGiveaway(message, giveaway)
            } catch (e: Exception) {
                logger.error(e) { "Error when processing giveaway ${giveaway.id.value}" }
            }
        }
    }

    suspend fun finishGiveaway(message: Message, giveaway: Giveaway) {
        val emoteId = giveaway.reaction.toLongOrNull()

        val messageReaction: MessageReaction?

        if (emoteId != null) {
            messageReaction = message.reactions.firstOrNull { it.reactionEmote.emote?.idLong == emoteId }
        } else {
            messageReaction = message.reactions.firstOrNull { it.reactionEmote.name == giveaway.reaction }
        }

        if (messageReaction != null) {
            val winner = messageReaction.users.await().getRandom()
            message.channel.sendMessageAsync("Parabéns ${winner.asMention} por ganhar o giveaway!")
        } else {
            message.channel.sendMessageAsync("Nenhuma reação válida na mensagem")
        }

        transaction(Databases.loritta) {
            giveaway.delete()
        }

        giveawayTasks[giveaway.id.value]?.cancel()
        giveawayTasks.remove(giveaway.id.value)
    }
}