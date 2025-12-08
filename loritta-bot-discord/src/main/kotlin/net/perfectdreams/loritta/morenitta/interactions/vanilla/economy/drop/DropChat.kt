package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.drop

import dev.minn.jda.ktx.interactions.components.Thumbnail
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.toJavaInstant
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.utils.TimeFormat
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.DropChatParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.DropChats
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.DropsConfigs
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosPayExecutor
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.VacationModeUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.serializable.StoredDropChatTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

class DropChat(
    val loritta: LorittaBot,
    val guild: Guild,
    val creator: User,
    val channel: MessageChannel,
    val sonhos: Long,
    val maxParticipants: Int?,
    val maxWinners: Int,
    val duration: Duration,
    val i18nContext: I18nContext,
    val chargeCreatorSonhos: Boolean
) {
    companion object {
        // We have a max value to avoid overflow issues
        const val MAX_SONHOS_PER_PARTICIPANT = 1_000_000_000_000L
        const val MAX_WINNERS = 30 // We also have a max winners to avoid overflowing the message
    }

    // Should be initialized AFTER the message has been sent
    var originalDropMessage: Message? = null
    var finished = false
    val startedAt = Instant.now()

    val mutex = Mutex()
    val participatingUsers = mutableSetOf<User>()
    val participateButton = loritta.interactivityManager.button(
        false,
        ButtonStyle.PRIMARY,
        i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.Participate(0)),
        {
            emoji = Emotes.LoriSunglasses.toJDA()
        }
    ) { context ->
        val result = mutex.withLock {
            if (this.finished)
                return@withLock DropJoinResult.ThisDropHasEnded

            if (this@DropChat.participatingUsers.contains(context.user))
                return@withLock DropJoinResult.YouAreAlreadyParticipatingOnThisDrop

            if (this@DropChat.chargeCreatorSonhos && context.user.idLong == creator.idLong)
                return@withLock DropJoinResult.YouCannotParticipateOnYourOwnDrop

            when (val result = SonhosPayExecutor.checkIfAccountIsOldEnoughToReceiveSonhos(context.user)) {
                SonhosPayExecutor.Companion.OtherAccountOldEnoughResult.Success -> {}
                is SonhosPayExecutor.Companion.OtherAccountOldEnoughResult.NotOldEnough -> {
                    return@withLock DropJoinResult.SelfAccountIsTooNewToJoinADrop(result.allowedAfterTimestamp)
                }
            }

            when (SonhosPayExecutor.checkIfAccountGotDailyAtLeastOnce(loritta, context.member)) {
                SonhosPayExecutor.Companion.AccountGotDailyAtLeastOnceResult.Success -> {}
                SonhosPayExecutor.Companion.AccountGotDailyAtLeastOnceResult.HaventGotDailyOnce -> {
                    return@withLock DropJoinResult.SelfAccountNeedsToGetDailyToJoinADrop
                }
            }

            // Are we in vacation?
            val vacationUntil = context.lorittaUser.profile.vacationUntil
            if (vacationUntil != null && vacationUntil > Instant.now()) {
                return@withLock DropJoinResult.SelfAccountIsOnVacation(vacationUntil)
            }

            this@DropChat.participatingUsers.add(context.user)

            updateMessageJob?.cancel()
            updateMessageJob = scope.launch {
                // This is a "debounce", where it will be executed after 5 seconds UNLESS if another user enters the drop
                delay(5.seconds)

                originalDropMessage
                    ?.editMessage(
                        MessageEdit {
                            createDropMessage()
                        }
                    )
                    ?.await()
            }

            if (this@DropChat.participatingUsers.size == this@DropChat.maxParticipants) {
                this@DropChat.finishDrop()
            }

            return@withLock DropJoinResult.YouAreNowParticipating
        }

        when (result) {
            DropJoinResult.YouAreNowParticipating -> {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.JoinDrop.YouAreNowParticipating),
                        Emotes.LoriSunglasses
                    )
                }
            }
            is DropJoinResult.SelfAccountIsOnVacation -> {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18nKeysData.Commands.Command.Vacation.YouAreOnVacation(TimeFormat.DATE_TIME_LONG.format(result.vacationUntil))),
                        Emotes.LoriSleeping
                    )
                }
            }
            is DropJoinResult.SelfAccountIsTooNewToJoinADrop -> {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(
                            I18nKeysData.Commands.Command.Drop.SelfAccountIsTooNewToJoinADrop(
                                TimeFormat.DATE_TIME_LONG.format(result.allowedAfterTimestamp.toJavaInstant()),
                                TimeFormat.RELATIVE.format(result.allowedAfterTimestamp.toJavaInstant())
                            )
                        ),
                        Constants.ERROR
                    )
                }
            }
            DropJoinResult.SelfAccountNeedsToGetDailyToJoinADrop -> {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(
                            I18nKeysData.Commands.Command.Drop.SelfAccountNeedsToGetDailyToJoinADrop(loritta.commandMentions.daily)
                        ),
                        Emotes.LoriSob
                    )
                }
            }
            DropJoinResult.ThisDropHasEnded -> {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.JoinDrop.ThisDropHasEnded),
                        Emotes.LoriSob
                    )
                }
            }
            DropJoinResult.YouAreAlreadyParticipatingOnThisDrop -> {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.JoinDrop.YouAlreadyParticipatingOnThisDrop),
                        Emotes.Error
                    )
                }
            }
            DropJoinResult.YouCannotParticipateOnYourOwnDrop -> {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.JoinDrop.YouCannotParticipateInYourOwnDrop),
                        Emotes.Error
                    )
                }
            }
        }
    }

    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    var updateMessageJob: Job? = null

    fun startDropAutoFinishTask() {
        scope.launch {
            delay(duration.toKotlinDuration())

            mutex.withLock {
                if (!this@DropChat.finished)
                    finishDrop()
            }
        }
    }

    fun InlineMessage<*>.createDropMessage() {
        this.useComponentsV2 = true

        container {
            this.accentColorRaw = LorittaColors.SonhosDrop.rgb

            this.section(Thumbnail("https://stuff.loritta.website/loritta-sonhos-glasses-yafyr.gif")) {
                this.text(
                    buildString {
                        appendLine("### ${loritta.emojiManager.get(LorittaEmojis.LoriConfetti)} ${i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.SonhosDrop)}")
                        appendLine("**${i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.CreatorOfTheDrop(creator.asMention))}**")
                        appendLine(i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.SonhosQuantity(SonhosUtils.getSonhosEmojiOfQuantity(sonhos), sonhos)))
                        appendLine(i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.WinnersQuantity(maxWinners)))
                        appendLine()
                        if (maxParticipants != null) {
                            appendLine("-# ${i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.TheDropWillEndWithMaxParticipants(TimeFormat.RELATIVE.format(startedAt.plus(duration)), maxParticipants))}")
                        } else {
                            appendLine("-# ${i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.TheDropWillEnd(TimeFormat.RELATIVE.format(startedAt.plus(duration))))}")
                        }
                    }
                )
            }
        }

        actionRow(
            participateButton
                .withLabel(i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.Participate(participatingUsers.size)))
                .withDisabled(this@DropChat.finished)
        )
    }

    suspend fun finishDrop() {
        require(!this.finished) { "Cannot finish an already finished drop!" }

        this.finished = true

        val originalDropMessage = this.originalDropMessage

        updateMessageJob?.cancel()
        originalDropMessage?.editMessage(
            MessageEdit {
                createDropMessage()
            }
        )?.await()

        val winners = participatingUsers
            .shuffled(loritta.random)
            .take(maxWinners)
            .toSet()

        if (winners.isEmpty()) {
            channel.sendMessage(
                MessageCreate {
                    this.useComponentsV2 = true

                    this.container {
                        this.accentColorRaw = LorittaColors.SonhosDropEmpty.rgb

                        this.text(
                            buildString {
                                appendLine("### ${loritta.emojiManager.get(LorittaEmojis.LoriConfetti)} ${i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.SonhosDropHasEnded)}")
                                appendLine("*${i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.NoOneParticipatedOnTheDrop)}* ${Emotes.LoriSob}")
                                appendLine()
                                appendLine(i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.NoSonhosDistributedNoParticipants))
                            }
                        )
                    }
                }
            ).apply {
                if (originalDropMessage != null)
                    setMessageReference(originalDropMessage.idLong)
            }.failOnInvalidReply(false).await()
        } else {
            fun createDropChat(): EntityID<Long> {
                return DropChats.insertAndGetId {
                    it[DropChats.guildId] = guild.idLong
                    it[DropChats.channelId] = channel.idLong
                    it[DropChats.messageId] = originalDropMessage?.idLong
                    it[DropChats.startedById] = creator.idLong
                    it[DropChats.moneySourceId] = if (chargeCreatorSonhos) creator.idLong else null
                    it[DropChats.startedAt] = this@DropChat.startedAt.atZone(Constants.LORITTA_TIMEZONE).toOffsetDateTime()
                    it[DropChats.endedAt] = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)
                    it[DropChats.participantPayout] = sonhos
                    it[DropChats.maxParticipants] = this@DropChat.maxParticipants
                    it[DropChats.maxWinners] = this@DropChat.maxWinners
                    it[DropChats.participants] = this@DropChat.participatingUsers.size
                    it[DropChats.winners] = winners.size
                }
            }

            // Add sonhos for each winner
            val result = loritta.transaction {
                // Update the guild information with the data
                DropsConfigs.update({ DropsConfigs.id eq guild.idLong and (DropsConfigs.showGuildInformationOnTransactions eq true) }) {
                    it[DropsConfigs.guildName] = guild.name
                }

                val dropChatId = if (this@DropChat.chargeCreatorSonhos) {
                    // Check if the owner has enough sonhos to do this drop
                    val totalSonhosPayout = sonhos * winners.size
                    val result = SonhosUtils.checkIfUserHasEnoughSonhos(creator.idLong, totalSonhosPayout)

                    when (result) {
                        SonhosUtils.SonhosCheckResult.Success -> {
                            val dropChatId = createDropChat()

                            Profiles.update({ Profiles.id eq creator.idLong }) {
                                with(SqlExpressionBuilder) {
                                    it[Profiles.money] = Profiles.money - totalSonhosPayout
                                }
                            }

                            dropChatId
                        }

                        is SonhosUtils.SonhosCheckResult.NotEnoughSonhos -> return@transaction DropResult.MoneySourceNotEnoughSonhos(totalSonhosPayout)
                    }
                } else {
                    createDropChat()
                }

                for (participant in participatingUsers) {
                    DropChatParticipants.insert {
                        it[DropChatParticipants.userId] = participant.idLong
                        it[DropChatParticipants.dropChat] = dropChatId
                        it[DropChatParticipants.won] = participant in winners
                    }
                }

                for (winner in winners) {
                    Profiles.update({ Profiles.id eq winner.idLong }) {
                        it[Profiles.money] = Profiles.money + sonhos
                    }

                    if (this@DropChat.chargeCreatorSonhos) {
                        SimpleSonhosTransactionsLogUtils.insert(
                            creator.idLong,
                            Instant.now(),
                            TransactionType.DROP,
                            sonhos,
                            StoredDropChatTransaction(
                                dropChatId.value,
                                true,
                                creator.idLong,
                                winner.idLong,
                                guild.idLong
                            )
                        )
                    }

                    SimpleSonhosTransactionsLogUtils.insert(
                        winner.idLong,
                        Instant.now(),
                        TransactionType.DROP,
                        sonhos,
                        StoredDropChatTransaction(
                            dropChatId.value,
                            false,
                            if (this@DropChat.chargeCreatorSonhos) creator.idLong else null,
                            winner.idLong,
                            guild.idLong
                        )
                    )
                }

                return@transaction DropResult.Success(dropChatId.value)
            }

            when (result) {
                is DropResult.Success -> {
                    val totalSonhosDistributed = sonhos * winners.size

                    channel.sendMessage(
                        MessageCreate {
                            this.useComponentsV2 = true

                            this.container {
                                this.accentColorRaw = LorittaColors.SonhosDropSuccess.rgb

                                this.text(
                                    buildString {
                                        appendLine("### ${loritta.emojiManager.get(LorittaEmojis.LoriConfetti)} ${i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.SonhosDropHasEnded)}")
                                        for (winner in winners) {
                                            appendLine(i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.UserWonSonhos(winner.asMention, SonhosUtils.getSonhosEmojiOfQuantity(sonhos), sonhos)))
                                        }
                                        appendLine()
                                        if (chargeCreatorSonhos) {
                                            appendLine(i18nContext.get(I18nKeysData.Commands.Command.Drop.TotalSonhosDistributed(SonhosUtils.getSonhosEmojiOfQuantity(totalSonhosDistributed), totalSonhosDistributed, creator.asMention)))
                                        } else {
                                            appendLine(i18nContext.get(I18nKeysData.Commands.Command.Drop.TotalSonhosDistributedAdmin(SonhosUtils.getSonhosEmojiOfQuantity(totalSonhosDistributed), totalSonhosDistributed)))
                                        }
                                        appendLine()
                                        appendLine("-# ${i18nContext.get(I18nKeysData.Commands.Command.Drop.DontForgetToGetDaily(loritta.commandMentions.daily))}")
                                        appendLine()
                                        appendLine("${DropCommand.Licks.random()} **${i18nContext.get(I18nKeysData.Commands.Command.Drop.ThanksTheCreatorForTheDrop(creator.asMention))}**")
                                    }
                                )
                            }
                        }
                    ).apply {
                        if (originalDropMessage != null)
                            setMessageReference(originalDropMessage.idLong)
                    }.failOnInvalidReply(false).await()
                }

                is DropResult.MoneySourceNotEnoughSonhos -> {
                    channel.sendMessage(
                        MessageCreate {
                            this.useComponentsV2 = true

                            this.container {
                                this.accentColorRaw = LorittaColors.SonhosDropEmpty.rgb

                                this.text(
                                    buildString {
                                        appendLine("### ${loritta.emojiManager.get(LorittaEmojis.LoriConfetti)} ${i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.SonhosDropHasEnded)}")
                                        appendLine("*${i18nContext.get(I18nKeysData.Commands.Command.Drop.TheCreatorDoesNotHaveEnoughSonhos(creator.asMention, SonhosUtils.getSonhosEmojiOfQuantity(result.totalSonhosPayout), result.totalSonhosPayout))}* ${Emotes.LoriSob}")
                                        appendLine()
                                        appendLine(i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.NoSonhosDistributedNotEnoughSonhos))
                                    }
                                )
                            }
                        }
                    ).apply {
                        if (originalDropMessage != null)
                            setMessageReference(originalDropMessage.idLong)
                    }.failOnInvalidReply(false).await()
                }
            }
        }

        scope.cancel()
    }

    private sealed class DropJoinResult {
        data object YouAreNowParticipating : DropJoinResult()
        data object ThisDropHasEnded : DropJoinResult()
        data object YouAreAlreadyParticipatingOnThisDrop : DropJoinResult()
        data object YouCannotParticipateOnYourOwnDrop : DropJoinResult()
        data class SelfAccountIsTooNewToJoinADrop(val allowedAfterTimestamp: kotlinx.datetime.Instant) : DropJoinResult()
        data object SelfAccountNeedsToGetDailyToJoinADrop : DropJoinResult()
        data class SelfAccountIsOnVacation(val vacationUntil: Instant) : DropJoinResult()
    }

    private sealed class DropResult {
        data class Success(val dropId: Long) : DropResult()
        data class MoneySourceNotEnoughSonhos(val totalSonhosPayout: Long) : DropResult()
    }
}