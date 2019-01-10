package net.perfectdreams.loritta.utils.giveaway

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.getRandom
import com.mrpowergamerbr.loritta.utils.extensions.sendMessageAsync
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.exceptions.ErrorResponseException
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

    fun createGiveawayMessage(locale: BaseLocale, reason: String, description: String, reaction: String, epoch: Long, guild: Guild, customMessage: String?): Message {
        val diff = epoch - System.currentTimeMillis()
        val diffSeconds = diff / 1000 % 60
        val diffMinutes = diff / (60 * 1000) % 60
        val diffHours = diff / (60 * 60 * 1000) % 24
        val diffDays = diff / (24 * 60 * 60 * 1000)

        val message = when {
            diffDays >= 1 -> "$diffDays dias"
            diffHours >= 1 -> "$diffHours horas"
            diffMinutes >= 1 -> "$diffMinutes minutos"
            diffSeconds >= 1 -> "$diffSeconds segundos"
            else -> "Alguns millissegundos!"
        }

        val builder = MessageBuilder()
                .setContent(" ")

        val customResult = customMessage?.let {
            MessageUtils.generateMessage(
                    it,
                    listOf(),
                    guild,
                    mapOf("time-until-giveaway" to message),
                    true
            )
        }

        if (customResult?.contentRaw?.isNotBlank() == true)
            builder.setContent(customResult.contentRaw)

        if (customResult?.embeds?.isNotEmpty() == true)
            builder.setEmbed(customResult.embeds.first())
        else
            builder.setEmbed(
                    EmbedBuilder().apply {
                        setTitle("\uD83C\uDF81 $reason")
                        setDescription("$description\n\nUse ${getReactionMention(reaction)} para entrar!")
                        addField("⏰ Tempo restante", message, true)
                        setColor(Constants.DISCORD_BLURPLE)
                        setFooter("Acabará em", null)
                        setTimestamp(Instant.ofEpochMilli(epoch))
                    }.build()
            )

        return builder.build()
    }

    suspend fun spawnGiveaway(locale: BaseLocale, channel: TextChannel, reason: String, description: String, reaction: String, epoch: Long, numberOfWinners: Int, customMessage: String?, roleIds: List<String>?): Giveaway {
        val giveawayMessage = createGiveawayMessage(locale, reason, description, reaction, epoch, channel.guild, customMessage)

        val message = channel.sendMessage(giveawayMessage).await()
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

                this.numberOfWinners = numberOfWinners
                this.reason = reason
                this.description = description
                this.finishAt = epoch
                this.reaction = reaction
                this.customMessage = customMessage
                this.locale = locale.id
                this.roleIds = roleIds?.toTypedArray()
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

                    val guild = lorittaShards.getGuildById(giveaway.guildId) ?: run {
                        cancelGiveaway(giveaway)
                        return@launch
                    }
                    val channel = guild.getTextChannelById(giveaway.textChannelId) ?: run {
                        cancelGiveaway(giveaway)
                        return@launch
                    }

                    val diff = System.currentTimeMillis() - giveaway.finishAt

                    val message = channel.getMessageById(giveaway.messageId).await() ?: run {
                        cancelGiveaway(giveaway)
                        return@launch
                    }

                    val locale = loritta.getLocaleById(giveaway.locale)

                    val giveawayMessage = GiveawayManager.createGiveawayMessage(
                            locale,
                            giveaway.reason,
                            giveaway.description,
                            giveaway.reaction,
                            giveaway.finishAt,
                            guild,
                            giveaway.customMessage
                    )

                    // if (embed.fields.first().value != message.embeds.first().fields.first().value) {
                    message.editMessage(giveawayMessage).await()
                    // }

                    if (60_000 >= diff) { // Quanto mais perto do resultado, mais "rápido" iremos atualizar a embed
                        delay(1000) // a cada um segundo
                    } else {
                        // Vamos "alinhar" o update para que seja atualizado exatamente quando passar o minuto (para ficar mais fofis! ...e bom)
                        // Ou seja, se for 15:30:30, o delay será apenas de 30 segundos!
                        // Colocar apenas "60_000" de delay possui vários problemas, por exemplo: Quando a Lori reiniciar, não estará mais "alinhado"
                        delay(60_000 - (System.currentTimeMillis() % 60_000))
                    }
                }

                val guild = lorittaShards.getGuildById(giveaway.guildId) ?: run {
                    cancelGiveaway(giveaway)
                    return@launch
                }
                val channel = guild.getTextChannelById(giveaway.textChannelId) ?: run {
                    cancelGiveaway(giveaway)
                    return@launch
                }
                val message = channel.getMessageById(giveaway.messageId).await() ?: run {
                    cancelGiveaway(giveaway)
                    return@launch
                }

                GiveawayManager.finishGiveaway(message, giveaway)
            } catch (e: Exception) {
                if (e is ErrorResponseException) {
                    if (e.errorCode == 10008) { // Mensagem não existe, vamos cancelar o giveaway!
                        cancelGiveaway(giveaway)
                        return@launch
                    }
                }
                logger.error(e) { "Error when processing giveaway ${giveaway.id.value}" }
            }
        }
    }

    suspend fun cancelGiveaway(giveaway: Giveaway) {
        giveawayTasks[giveaway.id.value]?.cancel()
        giveawayTasks.remove(giveaway.id.value)

        // Se a Lori ainda não iniciou, vamos apenas ignorar por enquanto
        if (lorittaShards.shardManager.shards.any { it.status != JDA.Status.CONNECTED })
            return

        transaction(Databases.loritta) {
            giveaway.delete()
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

        val serverConfig = loritta.getServerConfigForGuild(message.guild.id)
        val locale = loritta.getLocaleById(serverConfig.localeId)

        if (messageReaction != null) {
            val users = messageReaction.users.await()

            if (users.size == 1 && users[0].id == Loritta.config.clientId) { // Ninguém participou do giveaway! (Só a Lori, mas ela não conta)
                message.channel.sendMessageAsync("\uD83C\uDF89 **|** ${locale["commands.fun.giveaway.noWinner"]} ${Emotes.LORI_TEMMIE}")
            } else {
                val winners = mutableListOf<User>()
                val reactedUsers = messageReaction.users.await().filter { it.id != Loritta.config.clientId }.toMutableList()

                repeat(giveaway.numberOfWinners) {
                    if (reactedUsers.isEmpty())
                        return@repeat

                    val user = reactedUsers.getRandom()
                    winners.add(user)
                    reactedUsers.remove(user)
                }

                if (winners.size == 1) { // Apenas um ganhador
                    val winner = winners.first()
                    message.channel.sendMessageAsync("\uD83C\uDF89 **|** ${locale["commands.fun.giveaway.oneWinner", winner.asMention, "`${giveaway.reason}`"]} ${Emotes.LORI_HAPPY}")
                } else { // Mais de um ganhador
                    val replies = mutableListOf("\uD83C\uDF89 **|** ${locale["commands.fun.giveaway.multipleWinners", "`${giveaway.reason}`"]} ${Emotes.LORI_HAPPY}")

                    repeat(giveaway.numberOfWinners) {
                        val user = winners.getOrNull(it)

                        if (user != null) {
                            replies.add("⭐ **|** ${user.asMention}")
                        } else {
                            replies.add("⭐ **|** ¯\\_(ツ)_/¯")
                        }
                    }
                    message.channel.sendMessageAsync(replies.joinToString("\n"))
                }

                if (giveaway.roleIds != null) { // Dar o prêmio para quem ganhou (yay!)
                    val roles = giveaway.roleIds!!.mapNotNull { message.guild.getRoleById(it) }

                    winners.forEach { user ->
                        val member = message.guild.getMember(user)
                        val rolesToBeGiven = roles.filter {
                            !member.roles.contains(it) && message.guild.selfMember.canInteract(it)
                        }

                        if (rolesToBeGiven.isNotEmpty()) {
                            message.guild.controller.addRolesToMember(member, rolesToBeGiven).queue()
                        }
                    }
                }
            }
        } else {
            message.channel.sendMessageAsync("Nenhuma reação válida na mensagem...")
        }

        val embed = EmbedBuilder().apply {
            setTitle("\uD83C\uDF81 ${giveaway.reason}")
            setDescription(giveaway.description)
            setFooter(locale["commands.fun.giveaway.giveawayEnded"], null)
        }

        message.editMessage(embed.build()).await()

        transaction(Databases.loritta) {
            giveaway.delete()
        }

        giveawayTasks[giveaway.id.value]?.cancel()
        giveawayTasks.remove(giveaway.id.value)
    }
}