package net.perfectdreams.loritta.utils.giveaway

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.getRandom
import com.mrpowergamerbr.loritta.utils.extensions.sendMessageAsync
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.perfectdreams.loritta.dao.Giveaway
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.FeatureFlags
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

object GiveawayManager {
    var giveawayTasks = ConcurrentHashMap<Long, Job>()
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
        logger.debug { "Spawning Giveaway! locale = $locale, channel = $channel, reason = $reason, description = $description, reason = $reason, epoch = $epoch, numberOfWinners = $numberOfWinners, customMessage = $customMessage, roleIds = $roleIds" }

        val giveawayMessage = createGiveawayMessage(locale, reason, description, reaction, epoch, channel.guild, customMessage)

        val message = channel.sendMessage(giveawayMessage).await()
        val messageId = message.idLong

        val emoteId = reaction.toLongOrNull()
        var validReaction = reaction

        logger.trace { "Can I use emote $emoteId in $channel?"}

        try {
            if (emoteId != null) {
                val mention = lorittaShards.getEmoteById(emoteId.toString())
                logger.trace { "Mention is $mention in $channel" }

                if (mention != null)
                    message.addReaction(mention).await()
            } else {
                logger.trace { "Emote $emoteId doesn't look like a valid snowflake..."}
                message.addReaction(reaction).await()
            }
        } catch (e: IllegalArgumentException) {
            logger.debug(e) { "Looks like the emote $emoteId doesn't exist, falling back to the default emote (if possible)"}
            message.addReaction("\uD83C\uDF89").await()
            validReaction = "\uD83C\uDF89"
        }catch (e: InsufficientPermissionException) {
            logger.error(e) { "Looks like we can't create a giveaway here! Missing some checks, huh? ;3" }
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Error while trying to add emotes for the giveaway!" }
            throw e
        }

        logger.debug { "Using reaction $validReaction for the giveaway..." }

        logger.trace { "Storing giveaway info..." }

        val giveaway = transaction(Databases.loritta) {
            Giveaway.new {
                this.guildId = channel.guild.idLong
                this.textChannelId = channel.idLong
                this.messageId = messageId

                this.numberOfWinners = numberOfWinners
                this.reason = reason
                this.description = description
                this.finishAt = epoch
                this.reaction = validReaction
                this.customMessage = customMessage
                this.locale = locale.id
                this.roleIds = roleIds?.toTypedArray()
                this.finished = false
            }
        }

        logger.trace { "Success! Giveaway ID is ${giveaway.id.value}, creating job..." }

        createGiveawayJob(giveaway)

        return giveaway
    }

    /**
     * Gets all entities related to this giveaway.
     *
     * @return a [GiveawayCombo] containing the guild, channel and message. If something is invalid, returns null
     */
    private suspend fun getGiveawayRelatedEntities(giveaway: Giveaway, shouldCancel: Boolean): GiveawayCombo? {
        val guild = getGiveawayGuild(giveaway, shouldCancel) ?: return null
        val channel = getGiveawayTextChannel(giveaway, guild, shouldCancel) ?: return null

        val message = channel.retrieveMessageById(giveaway.messageId).await() ?: run {
            logger.warn { "Cancelling giveaway ${giveaway.id.value}, message doesn't exist!" }

            if (shouldCancel)
                cancelGiveaway(giveaway, true)

            return null
        }

        return GiveawayCombo(guild, channel, message)
    }

    private fun getGiveawayGuild(giveaway: Giveaway, shouldCancel: Boolean): Guild? {
        val guild = lorittaShards.getGuildById(giveaway.guildId) ?: run {
            logger.warn { "Cancelling giveaway ${giveaway.id.value}, guild doesn't exist!" }

            if (shouldCancel)
                cancelGiveaway(giveaway, true)

            return null
        }

        return guild
    }

    private fun getGiveawayTextChannel(giveaway: Giveaway, guild: Guild, shouldCancel: Boolean): TextChannel? {
        val channel = guild.getTextChannelById(giveaway.textChannelId) ?: run {
            logger.warn { "Cancelling giveaway ${giveaway.id.value}, channel doesn't exist!" }

            if (shouldCancel)
                cancelGiveaway(giveaway, true)

            return null
        }

        return channel
    }

    fun createGiveawayJob(giveaway: Giveaway) {
        logger.info { "Creating giveaway ${giveaway.id.value} job..." }

        // Vamos tentar pegar e ver se a guild ou o canal de texto existem
        getGiveawayTextChannel(giveaway, getGiveawayGuild(giveaway, false) ?: return, false) ?: return

        giveawayTasks[giveaway.id.value] = GlobalScope.launch {
            try {
                while (giveaway.finishAt > System.currentTimeMillis()) {
                    if (!this.isActive) { // Oh no, o giveaway acabou então a task não é mais necessária! Ignore...
                        giveawayTasks.remove(giveaway.id.value)
                        return@launch
                    }

                    val (guild, channel, message) = getGiveawayRelatedEntities(giveaway, true) ?: run {
                        giveawayTasks.remove(giveaway.id.value)
                        return@launch
                    }

                    val diff = giveaway.finishAt - System.currentTimeMillis()

                    val locale = loritta.getLocaleById(giveaway.locale)

                    val giveawayMessage = createGiveawayMessage(
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

                    // Quanto mais perto do resultado, mais "rápido" iremos atualizar a embed
                    when {
                        5_000 >= diff && FeatureFlags.isEnabled("detailed-giveaway-time") -> {
                            logger.info { "Delaying giveaway ${giveaway.id.value} for 1000ms (will be finished in less than 5s!) - Giveaway will be finished in ${diff}ms" }
                            delay(1_000) // a cada 1 segundo
                        }
                        15_000 >= diff && FeatureFlags.isEnabled("detailed-giveaway-time") -> {
                            logger.info { "Delaying giveaway ${giveaway.id.value} for 2500ms (will be finished in less than 15s!) - Giveaway will be finished in ${diff}ms" }
                            delay(2_500) // a cada 2.5 segundos
                        }
                        30_000 >= diff && FeatureFlags.isEnabled("detailed-giveaway-time") -> {
                            logger.info { "Delaying giveaway ${giveaway.id.value} for 10000ms (will be finished in less than 30s!) - Giveaway will be finished in ${diff}ms" }
                            delay(10_000) // a cada 10 segundos
                        }
                        60_000 >= diff && FeatureFlags.isEnabled("detailed-giveaway-time") -> {
                            logger.info { "Delaying giveaway ${giveaway.id.value} for 15000ms (will be finished in less than 60s!) - Giveaway will be finished in ${diff}ms" }
                            delay(15_000) // a cada 15 segundos
                        }
                        3_600_000 >= diff && FeatureFlags.isEnabled("detailed-giveaway-time") -> {
                            // Vamos "alinhar" o update para que seja atualizado exatamente quando passar o minuto (para ficar mais fofis! ...e bom)
                            // Ou seja, se for 15:30:30, o delay será apenas de 30 segundos!
                            // Colocar apenas "60_000" de delay possui vários problemas, por exemplo: Quando a Lori reiniciar, não estará mais "alinhado"
                            val delay = 60_000 - (System.currentTimeMillis() % 60_000)
                            logger.info { "Delaying giveaway ${giveaway.id.value} for ${delay}ms (minute) - Giveaway will be finished in ${diff}ms" }
                            delay(60_000 - (System.currentTimeMillis() % 60_000))
                        }
                        else -> {
                            // Para evitar rate limits, vamos apenas atualizar a embed a cada *hora* (já que só vai ter que atualizar o giveaway a cada hora mesmo, né)
                            // Mesma coisa dos minutos, "vamos alinhar, wow!"
                            val delay = 3_600_000 - (System.currentTimeMillis() % 3_600_000)
                            logger.info { "Delaying giveaway ${giveaway.id.value} for ${delay}ms (hour) - Giveaway will be finished in ${diff}ms" }
                            delay(3_600_000 - (System.currentTimeMillis() % 3_600_000))
                        }
                    }
                }

                val (guild, channel, message) = getGiveawayRelatedEntities(giveaway, true) ?: run {
                    giveawayTasks.remove(giveaway.id.value)
                    return@launch
                }

                finishGiveaway(message, giveaway)
            } catch (e: Exception) {
                if (e is ErrorResponseException) {
                    if (e.errorCode == 10008) { // Mensagem não existe, vamos cancelar o giveaway!
                        logger.warn { "ErrorResponseException ${e.errorCode} while processing giveaway ${giveaway.id.value}, cancelling giveaway..." }
                        cancelGiveaway(giveaway, true)
                        return@launch
                    }
                }
                if (e is InsufficientPermissionException) { // Sem permissão, vamos cancelar o giveaway!
                    logger.warn { "InsufficientPermissionException while processing giveaway ${giveaway.id.value}, cancelling giveaway..." }
                    cancelGiveaway(giveaway, true)
                    return@launch
                }

                logger.error(e) { "Error while processing giveaway ${giveaway.id.value}" }
                cancelGiveaway(giveaway, false)
            }
        }
    }

    fun cancelGiveaway(giveaway: Giveaway, deleteFromDatabase: Boolean, forceDelete: Boolean = false) {
        logger.info { "Canceling giveaway ${giveaway.id.value}, deleteFromDatabase = $deleteFromDatabase, forceDelete = $forceDelete"}

        giveawayTasks[giveaway.id.value]?.cancel()
        giveawayTasks.remove(giveaway.id.value)

        if (deleteFromDatabase || forceDelete) {
            if (forceDelete || System.currentTimeMillis() - Constants.ONE_WEEK_IN_MILLISECONDS >= giveaway.finishAt) { // Já se passaram uma semana?
                logger.info { "Deleting giveaway ${giveaway.id.value} from database, one week of failures so the server maybe doesn't exist anymore"}
                transaction(Databases.loritta) {
                    giveaway.delete()
                }
            }
        }
    }

    suspend fun rollWinners(message: Message, giveaway: Giveaway) {
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
            val users = messageReaction.retrieveUsers().await()

            if (users.size == 1 && users[0].id == loritta.discordConfig.discord.clientId) { // Ninguém participou do giveaway! (Só a Lori, mas ela não conta)
                message.channel.sendMessageAsync("\uD83C\uDF89 **|** ${locale["commands.fun.giveaway.noWinner"]} ${Emotes.LORI_TEMMIE}")
            } else {
                val winners = mutableListOf<User>()
                val reactedUsers = messageReaction.retrieveUsers().await()
                        .asSequence()
                        .filter { it.id != loritta.discordConfig.discord.clientId }
                        .filter { message.guild.getMemberById(it.idLong) != null }
                        .toMutableList()

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

                    winners.mapNotNull { message.guild.getMember(it) }.forEach { member ->
                        val rolesToBeGiven = roles.filter {
                            !member.roles.contains(it) && message.guild.selfMember.canInteract(it)
                        }

                        if (rolesToBeGiven.isNotEmpty()) {
                            message.guild.modifyMemberRoles(member, member.roles.toMutableList().apply { this.addAll(rolesToBeGiven) }).queue()
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
    }

    suspend fun finishGiveaway(message: Message, giveaway: Giveaway) {
        logger.info { "Finishing giveaway ${giveaway.id.value}, let's party! \uD83C\uDF89" }

        rollWinners(message, giveaway)

        transaction(Databases.loritta) {
            giveaway.finished = true
        }

        giveawayTasks[giveaway.id.value]?.cancel()
        giveawayTasks.remove(giveaway.id.value)
    }

    private data class GiveawayCombo(
            val guild: Guild,
            val channel: TextChannel,
            val message: Message
    )
}