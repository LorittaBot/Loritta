package net.perfectdreams.loritta.utils.giveaway

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.retrieveMemberOrNull
import com.mrpowergamerbr.loritta.utils.extensions.sendMessageAsync
import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.perfectdreams.loritta.dao.servers.Giveaway
import net.perfectdreams.loritta.platform.discord.legacy.entities.DiscordEmote
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.FeatureFlags
import net.perfectdreams.sequins.text.StringUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

object GiveawayManager {
    var giveawayTasks = ConcurrentHashMap<Long, Job>()
    private val logger = KotlinLogging.logger {}

    fun getReactionMention(reaction: String): String {
        if (reaction.startsWith("discord:"))
            return DiscordEmote(reaction).asMention

        val emoteId = reaction.toLongOrNull()

        // Old giveaways still has the emote ID stored in the database, so we need to handle that.
        // This can be removed in the future, or maybe handled in a different way?
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
                        setTitle("\uD83C\uDF81 ${reason.substringIfNeeded(0 until 200)}")
                        setDescription("$description\n\nUse ${getReactionMention(reaction)} para entrar!")
                        // addField("⏰ Tempo restante", message, true)
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
        var validReaction = reaction

        logger.trace { "Can I use emote $validReaction in $channel?" }

        try {
            if (reaction.startsWith("discord:")) {
                // If it starts with "discord:", then it means it is a Discord emote, so we are going to wrap it in our object
                val discordEmote = DiscordEmote(reaction)

                // Then use the "reactionEmote" field from our object!
                // Also, it seems that we don't need to add "a:" to reactions, even if the emote is animated!
                message.addReaction(discordEmote.reactionCode).await()
            } else {
                logger.trace { "Emote $validReaction doesn't look like a valid snowflake..."}
                message.addReaction(reaction).await()
            }
        } catch (e: IllegalArgumentException) {
            logger.debug(e) { "Looks like the emote $validReaction doesn't exist, falling back to the default emote (if possible)"}
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

        val giveaway = loritta.newSuspendedTransaction {
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

                    val guild= getGiveawayGuild(giveaway,true) ?: run {
                        giveawayTasks.remove(giveaway.id.value)
                        return@launch
                    }

                    val channel = getGiveawayTextChannel(giveaway, guild, true) ?: run {
                        giveawayTasks.remove(giveaway.id.value)
                        return@launch
                    }

                    val diff = giveaway.finishAt - System.currentTimeMillis()

                    val locale = loritta.localeManager.getLocaleById(giveaway.locale)

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
                    // message.editMessage(giveawayMessage).await()
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
        val reaction = giveaway.reaction

        val messageReaction: MessageReaction?

        var isDiscordEmote = false
        val reactionId = if (reaction.startsWith("discord:")) {
            isDiscordEmote = true
            DiscordEmote(reaction).id
        } else {
            // Workaround: Do not break old giveaways before the emote change by checking if the reaction is a valid snowflake
            val emoteId = reaction.toLongOrNull()
            if (emoteId != null) {
                isDiscordEmote = true
                emoteId.toString()
            } else {
                reaction
            }
        }

        messageReaction = if (isDiscordEmote) {
            message.reactions.firstOrNull { it.reactionEmote.isEmote && it.reactionEmote.emote.id == reactionId }
        } else {
            message.reactions.firstOrNull { it.reactionEmote.name == giveaway.reaction }
        }

        val serverConfig = loritta.getOrCreateServerConfig(message.guild.idLong)
        val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)

        if (messageReaction != null) {
            logger.info { "Retrieving reactions for the giveaway ${giveaway.id.value}, using ${messageReaction.count} (total reaction count) for the takeAsync(...)" }
            val users = messageReaction.retrieveUsers()
                    // "retrieveUsers()" uses pagination, and we want to get all the users that reacted in the giveaway
                    // So we need to use .takeAsync(...) with a big value that would cover all reactions (in this case, we are going to use the reaction count, heh)
                    // Before we did use Int.MAX_VALUE, but that allocates a gigantic array, whoops.
                    // Of course, if a message has A LOT of reactions, that would cause a lot of issues, but I guess that is going to be very rare.
                    .takeAsync(messageReaction.count)
                    .await()

            if (users.size == 1 && users[0].id == loritta.discordConfig.discord.clientId) { // Ninguém participou do giveaway! (Só a Lori, mas ela não conta)
                message.channel.sendMessageAsync("\uD83C\uDF89 **|** ${locale["commands.command.giveaway.noWinner"]} ${Emotes.LORI_TEMMIE}")
            } else {
                val winners = mutableListOf<User>()
                val reactedUsers = users
                        .asSequence()
                        .filter { it.id != loritta.discordConfig.discord.clientId }
                        .toMutableList()

                while (true) {
                    if (giveaway.numberOfWinners == winners.size)
                        break
                    if (reactedUsers.isEmpty())
                        break

                    val user = reactedUsers.random()

                    val member = message.guild.retrieveMemberOrNull(user)

                    if (member != null)
                        winners.add(user)

                    reactedUsers.remove(user)
                }
                
                val messageBuilder = MessageBuilder()

                if (winners.size == 1) { // Apenas um ganhador
                    val winner = winners.first()
                    messageBuilder
                        .setAllowedMentions(listOf(Message.MentionType.USER, Message.MentionType.CHANNEL, Message.MentionType.EMOTE))
                        .setContent("\uD83C\uDF89 **|** ${locale["commands.command.giveaway.oneWinner", winner.asMention, "**${giveaway.reason}**"]} ${Emotes.LORI_HAPPY}")
                    message.channel.sendMessageAsync(messageBuilder.build())
                } else { // Mais de um ganhador
                    val replies = mutableListOf("\uD83C\uDF89 **|** ${locale["commands.command.giveaway.multipleWinners", "**${giveaway.reason}**"]} ${Emotes.LORI_HAPPY}")

                    repeat(giveaway.numberOfWinners) {
                        val user = winners.getOrNull(it)

                        if (user != null) {
                            replies.add("⭐ **|** ${user.asMention}")
                        } else {
                            replies.add("⭐ **|** ¯\\_(ツ)_/¯")
                        }
                    }

                    val fullString = replies.joinToString("\n")

                    // Okay, it seems very dumb to split by 1000 chars, but the reason this happens is because Discord's message renderer sucks and
                    // the last entries are hidden for no reason.
                    //
                    // This can be increased to 2000 when the bug is fixed, to check if it is fixed, copy this message and copy into chat and see if the last
                    // entries era displayed correctly.
                    // https://gist.github.com/MrPowerGamerBR/078a4f3ad44c8541e3b5241c9823335e
                    val chunkedResponse = StringUtils.chunkedLines(fullString, 1_000, forceSplit = true, forceSplitOnSpaces = true)
                    chunkedResponse.forEach {
                        messageBuilder
                                .setAllowedMentions(listOf(Message.MentionType.USER, Message.MentionType.CHANNEL, Message.MentionType.EMOTE))
                                .setContent(it)
                        message.channel.sendMessageAsync(messageBuilder.build())
                    }
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
            setFooter(locale["commands.command.giveaway.giveawayEnded"], null)
        }

        message.editMessage(embed.build()).await()
    }

    suspend fun finishGiveaway(message: Message, giveaway: Giveaway) {
        logger.info { "Finishing giveaway ${giveaway.id.value}, let's party! \uD83C\uDF89" }

        rollWinners(message, giveaway)

        loritta.newSuspendedTransaction {
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
