package net.perfectdreams.loritta.morenitta.utils.giveaway

import dev.minn.jda.ktx.interactions.components.Container
import dev.minn.jda.ktx.interactions.components.Section
import dev.minn.jda.ktx.interactions.components.TextDisplay
import dev.minn.jda.ktx.interactions.components.Thumbnail
import dev.minn.jda.ktx.messages.MessageCreate
import io.ktor.server.application.ServerConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.json.Json
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.toJavaColor
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserNotificationSettings
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GiveawayParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GiveawayRoleExtraEntries
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.NotificationType
import net.perfectdreams.loritta.common.utils.WeightedRandom
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.servers.Giveaway
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.DiscordEmote
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.*
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.substringIfNeeded
import net.perfectdreams.loritta.serializable.GiveawayRoleExtraEntry
import net.perfectdreams.loritta.serializable.GiveawayRoles
import net.perfectdreams.sequins.text.StringUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import java.awt.Color
import java.sql.Connection
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import net.perfectdreams.loritta.cinnamon.emotes.Emotes as CinnamonEmotes

class GiveawayManager(val loritta: LorittaBot) {
    companion object {
        const val GIVEAWAY_JOIN_COMPONENT_PREFIX = "giveaway_join"
        const val GIVEAWAY_PARTICIPANTS_COMPONENT_PREFIX = "giveaway_participants"
        val I18N_PREFIX = I18nKeysData.Giveaway
    }

    var giveawayTasks = ConcurrentHashMap<Long, Job>()
    private val logger by HarmonyLoggerFactory.logger {}
    val giveawayMessageUpdateMutexes = ConcurrentHashMap<Long, Mutex>()
    val giveawayMessageUpdateJobs = ConcurrentHashMap<Long, Job>()

    fun getReactionMention(reaction: String): String {
        if (reaction.startsWith("discord:"))
            return DiscordEmote(reaction).asMention

        val emoteId = reaction.toLongOrNull()

        // Old giveaways still has the emote ID stored in the database, so we need to handle that.
        // This can be removed in the future, or maybe handled in a different way?
        if (emoteId != null) {
            val mention = loritta.lorittaShards.getEmoteById(emoteId.toString())?.asMention
            if (mention != null)
                return mention
        }

        return reaction
    }

    fun createGiveawayMessage(
        i18nContext: I18nContext,
        reason: String,
        description: String,
        reaction: String,
        imageUrl: String?,
        thumbnailUrl: String?,
        color: Color?,
        epoch: Long,
        guild: Guild,
        customMessage: String?,
        giveawayDatabaseId: Long?,
        participants: Long,
        allowedRoles: GiveawayRoles?,
        deniedRoles: GiveawayRoles?,
        extraEntries: List<GiveawayRoleExtraEntry>,
        extraEntriesShouldStack: Boolean
    ): MessageCreateData {
        val builder = MessageCreateBuilder()
            .addContent(" ")

        val customResult = customMessage?.let {
            MessageUtils.generateMessage(
                it,
                listOf(),
                guild,
                mapOf(),
                true
            )
        }

        if (customResult?.content?.isNotBlank() == true)
            builder.setContent(customResult.content)

        if (customResult?.embeds?.isNotEmpty() == true)
            builder.setEmbeds(customResult.embeds.first())
        else
            builder.setEmbeds(
                EmbedBuilder().apply {
                    val embedDescription = buildString {
                        this.appendLine(description)
                        this.appendLine()
                        if (extraEntries.isNotEmpty()) {
                            this.appendLine("**${i18nContext.get(I18N_PREFIX.ExtraEntries)}:**")
                            for (extraEntry in extraEntries.sortedByDescending { it.weight }) {
                                if (extraEntriesShouldStack) {
                                    this.appendLine(i18nContext.get(I18N_PREFIX.RoleExtraEntryStacked("<@&${extraEntry.roleId}>", extraEntry.weight)))
                                } else {
                                    this.appendLine(i18nContext.get(I18N_PREFIX.RoleExtraEntry("<@&${extraEntry.roleId}>", extraEntry.weight)))
                                }
                            }
                            if (extraEntriesShouldStack) {
                                this.appendLine("-# ${i18nContext.get(I18N_PREFIX.ExtraEntriesStack)}")
                            } else {
                                this.appendLine("-# ${i18nContext.get(I18N_PREFIX.ExtraEntriesLargestWeight)}")
                            }
                            this.appendLine()
                        }
                        this.appendLine(i18nContext.get(I18N_PREFIX.UseButtonToEnter(getReactionMention(reaction))))

                        if ((allowedRoles != null && allowedRoles.roleIds.isNotEmpty()) || (deniedRoles != null && deniedRoles.roleIds.isNotEmpty())) {
                            this.appendLine()
                            this.appendLine("**${CinnamonEmotes.LoriZap} ${i18nContext.get(I18N_PREFIX.Requirements)}:**")

                            if (allowedRoles != null) {
                                if (allowedRoles.isAndCondition) {
                                    this.appendLine(i18nContext.get(I18N_PREFIX.NeedsToHaveAllRoles(allowedRoles.roleIds.joinToString { "<@&$it>" })))
                                } else {
                                    this.appendLine(i18nContext.get(I18N_PREFIX.NeedsToHaveAnyRoles(allowedRoles.roleIds.joinToString { "<@&$it>" })))
                                }
                            }

                            if (deniedRoles != null) {
                                if (deniedRoles.isAndCondition) {
                                    this.appendLine(i18nContext.get(I18N_PREFIX.CantHaveAllRoles(deniedRoles.roleIds.joinToString { "<@&$it>" })))
                                } else {
                                    this.appendLine(i18nContext.get(I18N_PREFIX.CantHaveAnyRoles(deniedRoles.roleIds.joinToString { "<@&$it>" })))
                                }
                            }
                        }
                    }
                    this.setDescription(embedDescription)
                    setTitle("\uD83C\uDF81 ${reason.substringIfNeeded(0 until 200)}")
                    setImage(imageUrl)
                    setThumbnail(thumbnailUrl)
                    setColor(color ?: LorittaColors.LorittaAqua.toJavaColor())
                    setFooter(i18nContext.get(I18N_PREFIX.EndsAt), null)
                    setTimestamp(Instant.ofEpochMilli(epoch))
                }.build()
            )

        val formattedReaction = if (reaction.startsWith("discord:")) {
            if (reaction.startsWith("discord:a:")) {
                "<${reaction.removePrefix("discord:")}>"
            } else {
                "<${reaction.removePrefix("discord")}>"
            }
        } else {
            reaction
        }

        if (giveawayDatabaseId != null) {
            builder.setComponents(
                ActionRow.of(
                    Button.of(
                        ButtonStyle.PRIMARY,
                        "$GIVEAWAY_JOIN_COMPONENT_PREFIX:$giveawayDatabaseId",
                        i18nContext.get(I18N_PREFIX.Participate(participants)),
                        Emoji.fromFormatted(formattedReaction)
                    ),
                    Button.of(ButtonStyle.SECONDARY, "$GIVEAWAY_PARTICIPANTS_COMPONENT_PREFIX:$giveawayDatabaseId", "Participantes", Emoji.fromCustom(CinnamonEmotes.LoriHi.name, CinnamonEmotes.LoriHi.id, CinnamonEmotes.LoriHi.animated))
                )
            )
        } else {
            builder.setComponents(
                ActionRow.of(
                    Button.of(
                        ButtonStyle.PRIMARY,
                        "$GIVEAWAY_JOIN_COMPONENT_PREFIX:dummy",
                        i18nContext.get(I18N_PREFIX.Participate(participants)),
                        Emoji.fromFormatted(formattedReaction)
                    ).asDisabled(),
                    Button.of(ButtonStyle.SECONDARY, "$GIVEAWAY_PARTICIPANTS_COMPONENT_PREFIX:dummy", "Participantes", Emoji.fromCustom(CinnamonEmotes.LoriHi.name, CinnamonEmotes.LoriHi.id, CinnamonEmotes.LoriHi.animated))
                        .asDisabled()
                )
            )
        }

        return builder.build()
    }

    suspend fun spawnGiveaway(
        locale: BaseLocale,
        i18nContext: I18nContext,
        user: User?,
        channel: GuildMessageChannel,
        reason: String,
        description: String,
        imageUrl: String?,
        thumbnailUrl: String?,
        color: Color?,
        reaction: String,
        epoch: Long,
        numberOfWinners: Int,
        customMessage: String?,
        roleIds: List<String>?,
        allowedRoles: GiveawayRoles?,
        deniedRoles: GiveawayRoles?,
        needsToGetDailyBeforeParticipating: Boolean,
        selfServerEmojiFightBetVictories: Int?,
        selfServerEmojiFightBetLosses: Int?,
        messagesRequired: Int?,
        messagesTimeThreshold: Long?,
        extraEntries: List<GiveawayRoleExtraEntry>,
        extraEntriesShouldStack: Boolean
    ): Giveaway {
        logger.debug { "Spawning Giveaway! locale = $locale, i18nContext = $i18nContext, channel = $channel, reason = $reason, description = $description, reason = $reason, epoch = $epoch, numberOfWinners = $numberOfWinners, customMessage = $customMessage, roleIds = $roleIds" }

        var validReaction = reaction

        val message = try {
            channel.sendMessage(
                createGiveawayMessage(i18nContext, reason, description, reaction, imageUrl, thumbnailUrl, color, epoch, channel.guild, customMessage, null, 0, allowedRoles, deniedRoles, extraEntries, extraEntriesShouldStack)
            ).await()
        } catch (e: ErrorResponseException) {
            if (e.errorCode == 50035) {
                logger.debug(e) { "Looks like the emote $validReaction doesn't exist, falling back to the default emote (if possible)" }
                validReaction = "\uD83C\uDF89"
                channel.sendMessage(
                    createGiveawayMessage(i18nContext, reason, description, "\uD83C\uDF89", imageUrl, thumbnailUrl, color, epoch, channel.guild, customMessage, null, 0, allowedRoles, deniedRoles, extraEntries, extraEntriesShouldStack)
                ).await()
            } else throw e
        }

        val messageId = message.idLong

        logger.debug { "Using reaction $validReaction for the giveaway..." }

        logger.trace { "Storing giveaway info..." }

        val giveaway = loritta.newSuspendedTransaction {
            val giveaway = Giveaway.new {
                this.guildId = channel.guild.idLong
                this.textChannelId = channel.idLong
                this.messageId = messageId

                this.numberOfWinners = numberOfWinners
                this.reason = reason
                this.description = description
                this.finishAt = epoch
                this.reaction = validReaction
                this.imageUrl = imageUrl
                this.thumbnailUrl = thumbnailUrl
                this.color = color?.let { String.format("#%02x%02x%02x", it.red, it.green, it.blue) }
                this.customMessage = customMessage
                this.locale = locale.id
                this.roleIds = roleIds
                if (allowedRoles != null)
                    this.allowedRoles = Json.encodeToString(allowedRoles)
                if (deniedRoles != null)
                    this.deniedRoles = Json.encodeToString(deniedRoles)
                this.needsToGetDailyBeforeParticipating = needsToGetDailyBeforeParticipating
                this.selfServerEmojiFightBetVictories = selfServerEmojiFightBetVictories
                this.selfServerEmojiFightBetLosses = selfServerEmojiFightBetLosses
                if (messagesRequired != null && messagesTimeThreshold != null) {
                    this.messagesRequired = messagesRequired
                    this.messagesTimeThreshold = messagesTimeThreshold
                }
                this.extraEntriesShouldStack = extraEntriesShouldStack
                this.createdAt = Instant.now()
                this.createdBy = user?.idLong
                this.finished = false

                this.version = 2
            }

            GiveawayRoleExtraEntries.batchInsert(extraEntries) {
                this[GiveawayRoleExtraEntries.giveawayId] = giveaway.id
                this[GiveawayRoleExtraEntries.roleId] = it.roleId
                this[GiveawayRoleExtraEntries.weight] = it.weight
            }

            giveaway
        }

        logger.trace { "Success! Giveaway ID is ${giveaway.id.value}, editing message and creating job..." }

        message.editMessage(
            MessageEditData.fromCreateData(
                createGiveawayMessage(
                    i18nContext,
                    reason,
                    description,
                    validReaction,
                    imageUrl,
                    thumbnailUrl,
                    color,
                    epoch,
                    channel.guild,
                    customMessage,
                    giveaway.id.value,
                    0,
                    allowedRoles,
                    deniedRoles,
                    extraEntries,
                    extraEntriesShouldStack
                )
            )
        ).await()

        createGiveawayJob(giveaway)

        return giveaway
    }

    suspend fun updateGiveaway(
        locale: BaseLocale,
        i18nContext: I18nContext,
        channel: GuildMessageChannel,
        message: Message,
        giveaway: Giveaway,
        reason: String,
        description: String,
        imageUrl: String?,
        thumbnailUrl: String?,
        color: Color?,
        reaction: String,
        epoch: Long,
        numberOfWinners: Int,
        customMessage: String?,
        roleIds: List<String>?,
        allowedRoles: GiveawayRoles?,
        deniedRoles: GiveawayRoles?,
        needsToGetDailyBeforeParticipating: Boolean,
        extraEntries: List<GiveawayRoleExtraEntry>,
        extraEntriesShouldStack: Boolean
    ): Giveaway {
        logger.debug { "Updating Giveaway ${giveaway.id.value}!" }

        var validReaction = reaction

        val giveawayParticipants = loritta.newSuspendedTransaction {
            GiveawayParticipants.selectAll().where {
                GiveawayParticipants.giveawayId eq giveaway.id.value
            }.count()
        }

        // Attempt to update the giveaway message BEFORE we do anything
        // We need to do this because we need to validate if the new reaction is actually valid
        try {
            message.editMessage(
                MessageEditData.fromCreateData(createGiveawayMessage(i18nContext, reason, description, reaction, imageUrl, thumbnailUrl, color, epoch, channel.guild, customMessage, giveaway.id.value, giveawayParticipants, allowedRoles, deniedRoles, extraEntries, extraEntriesShouldStack))
            ).await()
        } catch (e: ErrorResponseException) {
            if (e.errorCode == 50035) {
                logger.debug(e) { "Looks like the emote $validReaction doesn't exist, falling back to the default emote (if possible)" }
                validReaction = "\uD83C\uDF89"
                message.editMessage(
                    MessageEditData.fromCreateData(createGiveawayMessage(i18nContext, reason, description, reaction, imageUrl, thumbnailUrl, color, epoch, channel.guild, customMessage, giveaway.id.value, giveawayParticipants, allowedRoles, deniedRoles, extraEntries, extraEntriesShouldStack))
                ).await()
            } else throw e
        }
        val previousFinishAt = giveaway.finishAt

        // Update giveaway in database
        loritta.newSuspendedTransaction {
            giveaway.reason = reason
            giveaway.description = description
            giveaway.imageUrl = imageUrl
            giveaway.thumbnailUrl = thumbnailUrl
            giveaway.color = color?.let { String.format("#%02x%02x%02x", it.red, it.green, it.blue) }
            giveaway.reaction = validReaction
            giveaway.finishAt = epoch
            giveaway.numberOfWinners = numberOfWinners
            giveaway.roleIds = roleIds
            giveaway.allowedRoles = allowedRoles?.let { Json.encodeToString(it) }
            giveaway.deniedRoles = deniedRoles?.let { Json.encodeToString(it) }
            giveaway.needsToGetDailyBeforeParticipating = needsToGetDailyBeforeParticipating
            giveaway.extraEntriesShouldStack = extraEntriesShouldStack
            giveaway.customMessage = customMessage

            // Delete old extra entries and insert new ones
            GiveawayRoleExtraEntries.deleteWhere { GiveawayRoleExtraEntries.giveawayId eq giveaway.id }

            GiveawayRoleExtraEntries.batchInsert(extraEntries) {
                this[GiveawayRoleExtraEntries.giveawayId] = giveaway.id
                this[GiveawayRoleExtraEntries.roleId] = it.roleId
                this[GiveawayRoleExtraEntries.weight] = it.weight
            }
        }

        // If finish time changed, reschedule the job
        if (previousFinishAt != epoch) {
            logger.info { "Finish time changed for giveaway ${giveaway.id.value}, rescheduling job..." }

            // This is a look weird, but hear me out:
            // We NEED to wait the original task to finish BEFORE we create a new job
            // Otherwise, the job will be created while the old coroutine has not "finished" yet, which causes the coroutine to stop and remove our new job
            // So we will cancel the task, wait for the coroutine to finish and THEN we create a new coroutine
            val taskToBeCancelled = giveawayTasks.remove(giveaway.id.value)
            taskToBeCancelled?.cancel()
            try {
                taskToBeCancelled?.join()
            } catch (e: CancellationException) {}
            createGiveawayJob(giveaway)
        }

        return giveaway
    }

    /**
     * Gets all entities related to this giveaway.
     *
     * @return a [GiveawayCombo] containing the guild, channel and message. If something is invalid, returns null
     */
    private suspend fun getGiveawayRelatedEntities(giveaway: Giveaway, shouldCancel: Boolean): GiveawayCombo? {
        val guild = getGiveawayGuild(giveaway, shouldCancel) ?: return null
        val channel = getGiveawayGuildMessageChannel(giveaway, guild, shouldCancel) ?: return null

        val message = channel.retrieveMessageById(giveaway.messageId).await() ?: run {
            logger.warn { "Cancelling giveaway ${giveaway.id.value}, message doesn't exist!" }

            if (shouldCancel)
                cancelGiveaway(giveaway, true)

            return null
        }

        return GiveawayCombo(guild, channel, message)
    }

    private suspend fun getGiveawayGuild(giveaway: Giveaway, shouldCancel: Boolean): Guild? {
        val guild = loritta.lorittaShards.getGuildById(giveaway.guildId) ?: run {
            logger.warn { "Cancelling giveaway ${giveaway.id.value}, guild doesn't exist!" }

            if (shouldCancel)
                cancelGiveaway(giveaway, true)

            return null
        }

        return guild
    }

    private suspend fun getGiveawayGuildMessageChannel(giveaway: Giveaway, guild: Guild, shouldCancel: Boolean): GuildMessageChannel? {
        val channel = guild.getGuildMessageChannelById(giveaway.textChannelId) ?: run {
            logger.warn { "Cancelling giveaway ${giveaway.id.value}, channel doesn't exist!" }

            if (shouldCancel)
                cancelGiveaway(giveaway, true)

            return null
        }

        return channel
    }

    suspend fun createGiveawayJob(giveaway: Giveaway) {
        logger.info { "Creating giveaway ${giveaway.id.value} job..." }

        // Vamos tentar pegar e ver se a guild ou o canal de texto existem
        getGiveawayGuildMessageChannel(giveaway, getGiveawayGuild(giveaway, false) ?: return, false) ?: return

        logger.info { "Giveaway ${giveaway.id.value} has the guild and channel present! Continuing setup..." }
        giveawayTasks[giveaway.id.value] = GlobalScope.launch {
            try {
                while (giveaway.finishAt > System.currentTimeMillis()) {
                    if (!this.isActive) { // Oh no, o giveaway acabou então a task não é mais necessária! Ignore...
                        giveawayTasks.remove(giveaway.id.value)
                        return@launch
                    }

                    val guild = getGiveawayGuild(giveaway,true) ?: run {
                        giveawayTasks.remove(giveaway.id.value)
                        return@launch
                    }

                    val timeToDelay = giveaway.finishAt - System.currentTimeMillis()
                    logger.info { "Delaying Giveaway ${giveaway.id.value} to ${timeToDelay}ms" }
                    delay(timeToDelay)
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

    suspend fun cancelGiveaway(giveaway: Giveaway, deleteFromDatabase: Boolean, forceDelete: Boolean = false) {
        logger.info { "Cancelling giveaway ${giveaway.id.value}, deleteFromDatabase = $deleteFromDatabase, forceDelete = $forceDelete"}

        giveawayTasks[giveaway.id.value]?.cancel()
        giveawayTasks.remove(giveaway.id.value)
        giveawayMessageUpdateMutexes.remove(giveaway.id.value)

        if (deleteFromDatabase || forceDelete) {
            if (forceDelete || System.currentTimeMillis() - Constants.ONE_WEEK_IN_MILLISECONDS >= giveaway.finishAt) { // Já se passaram uma semana?
                logger.info { "Deleting giveaway ${giveaway.id.value} from database, one week of failures so the server maybe doesn't exist anymore"}
                loritta.transaction {
                    giveaway.delete()
                }
            }
        }
    }

    suspend fun rollWinners(message: Message, giveaway: Giveaway, numberOfWinnersOverride: Int? = null) {
        val numberOfWinners = numberOfWinnersOverride ?: giveaway.numberOfWinners
        if (giveaway.version == 2) {
            val serverConfig = loritta.getOrCreateServerConfig(message.guild.idLong)
            val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
            val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(locale.id)

            val participantsIds = loritta.transaction(transactionIsolation = Connection.TRANSACTION_SERIALIZABLE) {
                GiveawayParticipants.selectAll().where {
                    GiveawayParticipants.giveawayId eq giveaway.id.value
                }.map {
                    WeightedRandom.Item(it[GiveawayParticipants.userId], it[GiveawayParticipants.weight])
                }
            }.toMutableList()

            val winners = mutableListOf<GiveawayWinnerEntry>()

            while (true) {
                if (numberOfWinners == winners.size)
                    break
                if (participantsIds.isEmpty())
                    break

                val randomItem = WeightedRandom.random(loritta.random, participantsIds)
                val userId = randomItem.value

                val member = message.guild.retrieveMemberOrNullById(userId)

                if (member != null)
                    winners.add(
                        GiveawayWinnerEntry(
                            member,
                            randomItem.weight
                        )
                    )

                participantsIds.remove(randomItem)
            }

            if (winners.isEmpty()) { // Ninguém participou do giveaway!
                message.channel.sendMessageAsync("\uD83C\uDF89 **|** ${locale["commands.command.giveaway.noWinner"]} ${Emotes.LORI_TEMMIE}")
                return
            }

            val messageBuilder = MessageCreateBuilder()

            if (numberOfWinners == 1) { // Apenas um ganhador
                val winner = winners.first()
                messageBuilder
                    .setAllowedMentions(
                        listOf(
                            Message.MentionType.USER,
                            Message.MentionType.CHANNEL,
                            Message.MentionType.EMOJI
                        )
                    )
                    .apply {
                        if (winner.weight != 1) {
                            setContent("\uD83C\uDF89 **|** ${i18nContext.get(I18nKeysData.Giveaway.OneWinnerWeighted(winner.member.asMention, winner.weight, "**${giveaway.reason}**"))} ${Emotes.LORI_HAPPY}")
                        } else {
                            setContent("\uD83C\uDF89 **|** ${i18nContext.get(I18nKeysData.Giveaway.OneWinner(winner.member.asMention, "**${giveaway.reason}**"))} ${Emotes.LORI_HAPPY}")
                        }
                    }
                message.channel.sendMessageAsync(messageBuilder.build())
            } else { // Mais de um ganhador
                val replies = mutableListOf("\uD83C\uDF89 **|** ${locale["commands.command.giveaway.multipleWinners", "**${giveaway.reason}**"]} ${Emotes.LORI_HAPPY}")

                repeat(numberOfWinners) {
                    val winnerEntry = winners.getOrNull(it)

                    if (winnerEntry != null) {
                        if (winnerEntry.weight != 1) {
                            replies.add("⭐ **|** ${i18nContext.get(I18nKeysData.Giveaway.MultipleWinnersEntryWeighted(winnerEntry.member.asMention, winnerEntry.weight))}")
                        } else {
                            replies.add("⭐ **|** ${i18nContext.get(I18nKeysData.Giveaway.MultipleWinnersEntry(winnerEntry.member.asMention))}")
                        }
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
                        .setAllowedMentions(
                            listOf(
                                Message.MentionType.USER,
                                Message.MentionType.CHANNEL,
                                Message.MentionType.EMOJI
                            )
                        )
                        .setContent(it)
                    message.channel.sendMessageAsync(messageBuilder.build())
                }
            }

            if (giveaway.roleIds != null) { // Dar o prêmio para quem ganhou (yay!)
                val roles = giveaway.roleIds!!.mapNotNull { message.guild.getRoleById(it) }

                winners.forEach { (member, _) ->
                    val rolesToBeGiven = roles.filter {
                        !member.roles.contains(it) && message.guild.selfMember.canInteract(it)
                    }

                    if (rolesToBeGiven.isNotEmpty()) {
                        message.guild.modifyMemberRoles(
                            member,
                            member.roles.toMutableList().apply { this.addAll(rolesToBeGiven) }).queue()
                    }
                }
            }
        } else {
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
                message.reactions.firstOrNull { it.emoji.type == Emoji.Type.CUSTOM && it.emoji.asCustom().id == reactionId }
            } else {
                message.reactions.firstOrNull { it.emoji.name == giveaway.reaction }
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

                if (users.size == 1 && users[0].id == loritta.config.loritta.discord.applicationId.toString()) { // Ninguém participou do giveaway! (Só a Lori, mas ela não conta)
                    message.channel.sendMessageAsync("\uD83C\uDF89 **|** ${locale["commands.command.giveaway.noWinner"]} ${Emotes.LORI_TEMMIE}")
                } else {
                    val winners = mutableListOf<User>()
                    val reactedUsers = users
                        .asSequence()
                        .filter { it.id != loritta.config.loritta.discord.applicationId.toString() }
                        .toMutableList()

                    while (true) {
                        if (numberOfWinners == winners.size)
                            break
                        if (reactedUsers.isEmpty())
                            break

                        val user = reactedUsers.random()

                        val member = message.guild.retrieveMemberOrNull(user)

                        if (member != null)
                            winners.add(user)

                        reactedUsers.remove(user)
                    }

                    val messageBuilder = MessageCreateBuilder()

                    if (winners.size == 1) { // Apenas um ganhador
                        val winner = winners.first()
                        messageBuilder
                            .setAllowedMentions(
                                listOf(
                                    Message.MentionType.USER,
                                    Message.MentionType.CHANNEL,
                                    Message.MentionType.EMOJI
                                )
                            )
                            .setContent("\uD83C\uDF89 **|** ${locale["commands.command.giveaway.oneWinner", winner.asMention, "**${giveaway.reason}**"]} ${Emotes.LORI_HAPPY}")
                        message.channel.sendMessageAsync(messageBuilder.build())
                    } else { // Mais de um ganhador
                        val replies =
                            mutableListOf("\uD83C\uDF89 **|** ${locale["commands.command.giveaway.multipleWinners", "**${giveaway.reason}**"]} ${Emotes.LORI_HAPPY}")

                        repeat(numberOfWinners) {
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
                        val chunkedResponse =
                            StringUtils.chunkedLines(fullString, 1_000, forceSplit = true, forceSplitOnSpaces = true)
                        chunkedResponse.forEach {
                            messageBuilder
                                .setAllowedMentions(
                                    listOf(
                                        Message.MentionType.USER,
                                        Message.MentionType.CHANNEL,
                                        Message.MentionType.EMOJI
                                    )
                                )
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
                                message.guild.modifyMemberRoles(
                                    member,
                                    member.roles.toMutableList().apply { this.addAll(rolesToBeGiven) }).queue()
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

            message.editMessageEmbeds(embed.build()).await()
        }
    }

    suspend fun finishGiveaway(message: Message, giveaway: Giveaway) {
        logger.info { "Finishing giveaway ${giveaway.id.value}, let's party! \uD83C\uDF89" }

        rollWinners(message, giveaway)

        loritta.newSuspendedTransaction {
            giveaway.finished = true
        }

        val createdBy = giveaway.createdBy
        if (createdBy != null) {
            val (hasNotificationEnabled, serverConfig) = loritta.newSuspendedTransaction {
                val hasNotificationEnabled = UserNotificationSettings.selectAll()
                    .where {
                        UserNotificationSettings.userId eq createdBy and (UserNotificationSettings.type eq NotificationType.GIVEAWAY_ENDED) and (UserNotificationSettings.enabled eq false)
                    }
                    .count() == 0L

                Pair(hasNotificationEnabled, loritta.getOrCreateServerConfig(message.guildIdLong))
            }

            if (hasNotificationEnabled) {
                val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                val guild = message.guild
                val iconUrl = guild.iconUrl

                try {
                    val privateChannel = loritta.getOrRetrievePrivateChannelForUserOrNullIfUserDoesNotExist(createdBy)
                    if (privateChannel != null) {
                        privateChannel.sendMessage(
                            MessageCreate {
                                this.useComponentsV2 = true

                                this.components += Container {
                                    this.accentColorRaw = LorittaColors.LorittaAqua.rgb

                                    val textDisplay = TextDisplay(i18nContext.get(I18N_PREFIX.GiveawayEndedDirectMessage.GiveawayEnded(giveaway.reason, guild.name)))
                                    if (iconUrl != null) {
                                        this.components += Section(Thumbnail(iconUrl)) {
                                            this.components += textDisplay
                                        }
                                    } else {
                                        this.components += textDisplay
                                    }
                                }

                                this.components += ActionRow.of(
                                    Button.link(message.jumpUrl, i18nContext.get(I18N_PREFIX.GiveawayEndedDirectMessage.JumpToGiveaway)),
                                )
                            }
                        ).await()
                    }
                } catch (e: Exception) {
                    logger.warn(e) { "Something went wrong while trying to notify $createdBy about the giveaway finish!" }
                }
            }
        }

        giveawayTasks.remove(giveaway.id.value)?.cancel()
    }

    private data class GiveawayCombo(
        val guild: Guild,
        val channel: GuildMessageChannel,
        val message: Message
    )

    private data class GiveawayWinnerEntry(
        val member: Member,
        val weight: Int
    )
}
