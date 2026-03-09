package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.drop

import dev.minn.jda.ktx.interactions.components.Thumbnail
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.future.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.toJavaInstant
import net.dv8tion.jda.api.components.buttons.Button
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
import net.perfectdreams.loritta.cinnamon.pudding.tables.DropChatChoiceParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.DropChatChoices
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.DropsConfigs
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosPayExecutor
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.VacationModeUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.serializable.StoredDropChatChoiceTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

class DropChatChoice(
    val loritta: LorittaBot,
    val guild: Guild,
    val creator: User,
    val channel: MessageChannel,
    val sonhos: Long,
    val maxParticipants: Int?,
    val duration: Duration,
    val i18nContext: I18nContext,
    val chargeCreatorSonhos: Boolean,
    val choices: List<String>,
    val correctChoice: String,
    val variant: DropChatChoiceVariant = DropChatChoiceVariant.Generic
) {
    companion object {
        const val MAX_SONHOS_PER_PARTICIPANT = 1_000_000_000_000L
        const val MAX_WINNERS = 100
        const val MAX_CHOICES = 5
        const val MIN_CHOICES = 2
    }

    var originalDropMessage: Message? = null
    var finished = false
    val startedAt = Instant.now()

    val mutex = Mutex()
    val participantChoices = mutableMapOf<User, String>()

    val choiceButtons = choices.map { choice ->
        val button = loritta.interactivityManager.button(
            false,
            ButtonStyle.SECONDARY,
            i18nContext.get(I18nKeysData.Commands.Command.Drop.Choice.Participate(choice, 0)),
            {}
        ) { context ->
            val result = mutex.withLock {
                if (this.finished)
                    return@withLock DropJoinResult.ThisDropHasEnded

                if (this@DropChatChoice.participantChoices.containsKey(context.user))
                    return@withLock DropJoinResult.YouAreAlreadyParticipatingOnThisDrop

                if (this@DropChatChoice.chargeCreatorSonhos && context.user.idLong == creator.idLong)
                    return@withLock DropJoinResult.YouCannotParticipateOnYourOwnDrop

                when (val result = SonhosPayExecutor.checkIfAccountIsOldEnoughToReceiveSonhos(context.user)) {
                    SonhosPayExecutor.Companion.OtherAccountOldEnoughResult.Success -> {}
                    is SonhosPayExecutor.Companion.OtherAccountOldEnoughResult.NotOldEnough -> {
                        return@withLock DropJoinResult.SelfAccountIsTooNewToJoinADrop(result.allowedAfterTimestamp)
                    }
                }

                if (AccountUtils.getUserTodayDailyReward(loritta, context.user.idLong) == null)
                    return@withLock DropJoinResult.SelfAccountNeedsToGetDailyToJoinADrop

                val vacationUntil = context.lorittaUser.profile.vacationUntil
                if (vacationUntil != null && vacationUntil > Instant.now()) {
                    return@withLock DropJoinResult.SelfAccountIsOnVacation(vacationUntil)
                }

                this@DropChatChoice.participantChoices[context.user] = choice

                updateMessageJob?.cancel()
                updateMessageJob = scope.launch {
                    delay(5.seconds)

                    originalDropMessage
                        ?.editMessage(
                            MessageEdit {
                                createDropMessage()
                            }
                        )
                        ?.await()
                }

                if (this@DropChatChoice.maxParticipants != null && this@DropChatChoice.participantChoices.size == this@DropChatChoice.maxParticipants) {
                    this@DropChatChoice.finishDrop()
                }

                return@withLock DropJoinResult.YouAreNowParticipating(choice)
            }

            when (result) {
                is DropJoinResult.YouAreNowParticipating -> {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(I18nKeysData.Commands.Command.Drop.Choice.JoinDrop.YouAreNowParticipating(result.choice)),
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
                            context.i18nContext.get(I18nKeysData.Commands.Command.Drop.Choice.JoinDrop.ThisDropHasEnded),
                            Emotes.LoriSob
                        )
                    }
                }
                DropJoinResult.YouAreAlreadyParticipatingOnThisDrop -> {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(I18nKeysData.Commands.Command.Drop.Choice.JoinDrop.YouAlreadyParticipatingOnThisDrop),
                            Emotes.Error
                        )
                    }
                }
                DropJoinResult.YouCannotParticipateOnYourOwnDrop -> {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(I18nKeysData.Commands.Command.Drop.Choice.JoinDrop.YouCannotParticipateInYourOwnDrop),
                            Emotes.Error
                        )
                    }
                }
            }
        }

        choice to button
    }

    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    var updateMessageJob: Job? = null

    fun startDropAutoFinishTask() {
        scope.launch {
            delay(duration.toKotlinDuration())

            mutex.withLock {
                if (!this@DropChatChoice.finished)
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
                        appendLine("### ${loritta.emojiManager.get(LorittaEmojis.LoriConfetti)} ${variant.getDropTitle(i18nContext)}")
                        appendLine("**${i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.CreatorOfTheDrop(creator.asMention))}**")
                        appendLine(i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.SonhosQuantity(SonhosUtils.getSonhosEmojiOfQuantity(sonhos), sonhos)))
                        appendLine()
                        appendLine(variant.getInstructionText(i18nContext, creator.asMention))
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
            choiceButtons.map { (choice, button) ->
                val count = participantChoices.values.count { it == choice }
                var updatedButton = button
                    .withLabel(i18nContext.get(I18nKeysData.Commands.Command.Drop.Choice.Participate(choice, count)))
                    .withDisabled(this@DropChatChoice.finished)
                val emoji = variant.getChoiceEmoji(choice)
                if (emoji != null)
                    updatedButton = updatedButton.withEmoji(emoji)
                updatedButton
            }
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

        val correctChoiceParticipants = participantChoices
            .filter { it.value == correctChoice }
            .keys

        val winners = correctChoiceParticipants
            .shuffled(loritta.random)
            .take(MAX_WINNERS)
            .toSet()

        if (participantChoices.isEmpty()) {
            channel.sendMessage(
                MessageCreate {
                    this.useComponentsV2 = true

                    this.container {
                        this.accentColorRaw = LorittaColors.SonhosDropEmpty.rgb

                        this.text(
                            buildString {
                                appendLine("### ${loritta.emojiManager.get(LorittaEmojis.LoriConfetti)} ${variant.getDropEndedTitle(i18nContext)}")
                                appendLine("*${i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.NoOneParticipatedOnTheDrop)}* ${Emotes.LoriSob}")
                                appendLine()
                                val extraContext = variant.getExtraContextText(i18nContext)
                                if (extraContext != null) appendLine(extraContext)
                                appendLine(variant.getCorrectChoiceRevealText(i18nContext, correctChoice, creator.asMention))
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
        } else if (winners.isEmpty()) {
            channel.sendMessage(
                MessageCreate {
                    this.useComponentsV2 = true

                    this.container {
                        this.accentColorRaw = LorittaColors.SonhosDropEmpty.rgb

                        this.text(
                            buildString {
                                appendLine("### ${loritta.emojiManager.get(LorittaEmojis.LoriConfetti)} ${variant.getDropEndedTitle(i18nContext)}")
                                appendLine("*${i18nContext.get(I18nKeysData.Commands.Command.Drop.Choice.NoOneChoseCorrectly)}* ${Emotes.LoriSob}")
                                appendLine()
                                val extraContext = variant.getExtraContextText(i18nContext)
                                if (extraContext != null) appendLine(extraContext)
                                appendLine(variant.getCorrectChoiceRevealText(i18nContext, correctChoice, creator.asMention))
                                appendLine()
                                appendLine(i18nContext.get(I18nKeysData.Commands.Command.Drop.Choice.NoSonhosDistributedNoCorrectChoice))
                            }
                        )
                    }
                }
            ).apply {
                if (originalDropMessage != null)
                    setMessageReference(originalDropMessage.idLong)
            }.failOnInvalidReply(false).await()
        } else {
            fun createDropChatChoice(): EntityID<Long> {
                return DropChatChoices.insertAndGetId {
                    it[DropChatChoices.guildId] = guild.idLong
                    it[DropChatChoices.channelId] = channel.idLong
                    it[DropChatChoices.messageId] = originalDropMessage?.idLong
                    it[DropChatChoices.startedById] = creator.idLong
                    it[DropChatChoices.moneySourceId] = if (chargeCreatorSonhos) creator.idLong else null
                    it[DropChatChoices.startedAt] = this@DropChatChoice.startedAt.atZone(Constants.LORITTA_TIMEZONE).toOffsetDateTime()
                    it[DropChatChoices.endedAt] = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)
                    it[DropChatChoices.participantPayout] = sonhos
                    it[DropChatChoices.maxParticipants] = this@DropChatChoice.maxParticipants
                    it[DropChatChoices.participants] = this@DropChatChoice.participantChoices.size
                    it[DropChatChoices.winners] = winners.size
                    it[DropChatChoices.choices] = Json.encodeToString(this@DropChatChoice.choices)
                    it[DropChatChoices.correctChoice] = this@DropChatChoice.correctChoice
                    it[DropChatChoices.variant] = this@DropChatChoice.variant.variantType
                }
            }

            val result = loritta.transaction {
                DropsConfigs.update({ DropsConfigs.id eq guild.idLong and (DropsConfigs.showGuildInformationOnTransactions eq true) }) {
                    it[DropsConfigs.guildName] = guild.name
                }

                val dropChatChoiceId = if (this@DropChatChoice.chargeCreatorSonhos) {
                    val totalSonhosPayout = sonhos * winners.size
                    val result = SonhosUtils.checkIfUserHasEnoughSonhos(creator.idLong, totalSonhosPayout)

                    when (result) {
                        SonhosUtils.SonhosCheckResult.Success -> {
                            val dropChatChoiceId = createDropChatChoice()

                            Profiles.update({ Profiles.id eq creator.idLong }) {
                                with(SqlExpressionBuilder) {
                                    it[Profiles.money] = Profiles.money - totalSonhosPayout
                                }
                            }

                            dropChatChoiceId
                        }

                        is SonhosUtils.SonhosCheckResult.NotEnoughSonhos -> return@transaction DropResult.MoneySourceNotEnoughSonhos(totalSonhosPayout)
                    }
                } else {
                    createDropChatChoice()
                }

                for ((participant, participantChoice) in participantChoices) {
                    DropChatChoiceParticipants.insert {
                        it[DropChatChoiceParticipants.userId] = participant.idLong
                        it[DropChatChoiceParticipants.dropChatChoice] = dropChatChoiceId
                        it[DropChatChoiceParticipants.choice] = participantChoice
                        it[DropChatChoiceParticipants.won] = participant in winners
                    }
                }

                for (winner in winners) {
                    Profiles.update({ Profiles.id eq winner.idLong }) {
                        it[Profiles.money] = Profiles.money + sonhos
                    }

                    if (this@DropChatChoice.chargeCreatorSonhos) {
                        SimpleSonhosTransactionsLogUtils.insert(
                            creator.idLong,
                            Instant.now(),
                            TransactionType.DROP,
                            sonhos,
                            StoredDropChatChoiceTransaction(
                                dropChatChoiceId.value,
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
                        StoredDropChatChoiceTransaction(
                            dropChatChoiceId.value,
                            false,
                            if (this@DropChatChoice.chargeCreatorSonhos) creator.idLong else null,
                            winner.idLong,
                            guild.idLong
                        )
                    )
                }

                return@transaction DropResult.Success(dropChatChoiceId.value)
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
                                        appendLine("### ${loritta.emojiManager.get(LorittaEmojis.LoriConfetti)} ${variant.getDropEndedTitle(i18nContext)}")
                                        val extraContext = variant.getExtraContextText(i18nContext)
                                        if (extraContext != null) appendLine(extraContext)
                                        appendLine(variant.getCorrectChoiceRevealText(i18nContext, correctChoice, creator.asMention))
                                        appendLine()
                                        appendLine(i18nContext.get(I18nKeysData.Commands.Command.Drop.Choice.UsersWonSonhos(winners.joinToString(", ") { it.asMention }, winners.size, SonhosUtils.getSonhosEmojiOfQuantity(sonhos), sonhos)))
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
                                        appendLine("### ${loritta.emojiManager.get(LorittaEmojis.LoriConfetti)} ${variant.getDropEndedTitle(i18nContext)}")
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
        data class YouAreNowParticipating(val choice: String) : DropJoinResult()
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
