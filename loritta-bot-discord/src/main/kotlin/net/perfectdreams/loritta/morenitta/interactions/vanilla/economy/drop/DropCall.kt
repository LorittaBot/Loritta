package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.drop

import dev.minn.jda.ktx.interactions.components.Thumbnail
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.utils.TimeFormat
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.DropCalls
import net.perfectdreams.loritta.cinnamon.pudding.tables.DropCallsParticipants
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
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.StoredDropCallTransaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.time.toKotlinDuration

class DropCall(
    val loritta: LorittaBot,
    val guild: Guild,
    val creator: User,
    val channel: MessageChannel,
    val voiceChannel: AudioChannel,
    val sonhos: Long,
    val maxWinners: Int,
    val duration: Duration?,
    val i18nContext: I18nContext,
    val chargeCreatorSonhos: Boolean
) {
    companion object {
        // We have a max value to avoid overflow issues
        const val MAX_SONHOS_PER_PARTICIPANT = 1_000_000_000_000L
        const val MAX_WINNERS = 30 // We also have a max winners to avoid overflowing the message
    }

    // Should be initialized AFTER the message has been sent if the duration is not null
    var originalDropMessage: Message? = null
    var finished = false
    val startedAt = Instant.now()

    val mutex = Mutex()
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun startDropAutoFinishTask() {
        requireNotNull(duration) { "When using the auto finish task, the drop call duration cannot be null!" }
        scope.launch {
            delay(duration.toKotlinDuration())

            mutex.withLock {
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
                        appendLine("### ${loritta.emojiManager.get(LorittaEmojis.LoriConfetti)} ${i18nContext.get(I18nKeysData.Commands.Command.Drop.Call.SonhosDrop)}")
                        appendLine("**${i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.CreatorOfTheDrop(creator.asMention))}**")
                        appendLine(i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.SonhosQuantity(SonhosUtils.getSonhosEmojiOfQuantity(sonhos), sonhos)))
                        appendLine(i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.WinnersQuantity(maxWinners)))
                        appendLine()
                        appendLine("**${i18nContext.get(I18nKeysData.Commands.Command.Drop.Call.JoinTheCallToParticipate(voiceChannel.asMention))}**")
                        appendLine()
                        appendLine("-# ${i18nContext.get(I18nKeysData.Commands.Command.Drop.Call.VoiceDropRequirements)}")
                        appendLine()
                        appendLine("-# ${i18nContext.get(I18nKeysData.Commands.Command.Drop.Call.TheDropWillEnd(TimeFormat.RELATIVE.format(startedAt.plus(duration))))}")
                    }
                )
            }
        }
    }

    suspend fun finishDrop(): Message {
        require(!this.finished) { "Cannot finish an already finished drop!" }

        this.finished = true

        val originalDropMessage = this.originalDropMessage

        // This is a bit trickier to figure out, because users may not have all the required requirements to actually PARTICIPATE on this
        // So instead of implementing it like how DropChat works, we will do things differently
        val participantsAtTheTimeOfFinish = voiceChannel.members
            // Filter out bots and filter out the creator of the drop
            .filter { !it.user.isBot && if (chargeCreatorSonhos) it.user.idLong != creator.idLong else true }
            .map { it.user }
            .shuffled(loritta.random)
            .toSet()

        // Add sonhos for each winner
        val result = loritta.transaction {
            val participants = participantsAtTheTimeOfFinish.toMutableList() // Create a copy
            val winners = mutableListOf<User>()

            while (winners.size != maxWinners && participants.isNotEmpty()) {
                val winner = participants.removeFirst()

                // Check the requirements!
                when (SonhosPayExecutor.checkIfAccountIsOldEnoughToReceiveSonhos(winner)) {
                    SonhosPayExecutor.Companion.OtherAccountOldEnoughResult.Success -> {}
                    is SonhosPayExecutor.Companion.OtherAccountOldEnoughResult.NotOldEnough -> continue
                }

                when (SonhosPayExecutor.checkIfAccountGotDailyAtLeastOnce(loritta, winner)) {
                    SonhosPayExecutor.Companion.AccountGotDailyAtLeastOnceResult.Success -> {}
                    SonhosPayExecutor.Companion.AccountGotDailyAtLeastOnceResult.HaventGotDailyOnce -> continue
                }

                // Are we on vacation?
                val lorittaProfile = loritta.getOrCreateLorittaProfile(winner.idLong)
                val vacationUntil = lorittaProfile.vacationUntil
                if (vacationUntil != null && vacationUntil > Instant.now()) {
                    // Yeah, we are!
                    continue
                }

                winners.add(winner)
            }

            if (winners.isEmpty())
                return@transaction DropResult.NoWinners

            fun createDropCall(): EntityID<Long> {
                return DropCalls.insertAndGetId {
                    it[DropCalls.guildId] = guild.idLong
                    it[DropCalls.channelId] = channel.idLong
                    it[DropCalls.voiceChannelId] = voiceChannel.idLong
                    it[DropCalls.messageId] = originalDropMessage?.idLong
                    it[DropCalls.startedById] = creator.idLong
                    it[DropCalls.moneySourceId] = if (chargeCreatorSonhos) creator.idLong else null
                    it[DropCalls.startedAt] = this@DropCall.startedAt.atZone(Constants.LORITTA_TIMEZONE).toOffsetDateTime()
                    it[DropCalls.endedAt] = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)
                    it[DropCalls.participantPayout] = sonhos
                    it[DropCalls.maxWinners] = this@DropCall.maxWinners
                    it[DropCalls.participants] = participantsAtTheTimeOfFinish.size
                    it[DropCalls.winners] = winners.size
                }
            }

            // Update the guild information with the data
            DropsConfigs.update({ DropsConfigs.id eq guild.idLong and (DropsConfigs.showGuildInformationOnTransactions eq true) }) {
                it[DropsConfigs.guildName] = guild.name
            }

            val dropCallId = if (this@DropCall.chargeCreatorSonhos) {
                // Check if the owner has enough sonhos to do this drop
                val totalSonhosPayout = sonhos * winners.size
                val result = SonhosUtils.checkIfUserHasEnoughSonhos(creator.idLong, totalSonhosPayout)

                when (result) {
                    SonhosUtils.SonhosCheckResult.Success -> {
                        val dropCallId = createDropCall()

                        Profiles.update({ Profiles.id eq creator.idLong }) {
                            with(SqlExpressionBuilder) {
                                it[Profiles.money] = Profiles.money - totalSonhosPayout
                            }
                        }

                        dropCallId
                    }

                    is SonhosUtils.SonhosCheckResult.NotEnoughSonhos -> return@transaction DropResult.MoneySourceNotEnoughSonhos(totalSonhosPayout)
                }
            } else {
                createDropCall()
            }

            for (participant in participantsAtTheTimeOfFinish) {
                DropCallsParticipants.insert {
                    it[DropCallsParticipants.userId] = participant.idLong
                    it[DropCallsParticipants.dropCall] = dropCallId
                    it[DropCallsParticipants.won] = participant in winners
                }
            }

            for (winner in winners) {
                Profiles.update({ Profiles.id eq winner.idLong }) {
                    it[Profiles.money] = Profiles.money + sonhos
                }

                if (this@DropCall.chargeCreatorSonhos) {
                    SimpleSonhosTransactionsLogUtils.insert(
                        creator.idLong,
                        Instant.now(),
                        TransactionType.DROP,
                        sonhos,
                        StoredDropCallTransaction(
                            dropCallId.value,
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
                    StoredDropCallTransaction(
                        dropCallId.value,
                        false,
                        if (this@DropCall.chargeCreatorSonhos) creator.idLong else null,
                        winner.idLong,
                        guild.idLong
                    )
                )
            }

            return@transaction DropResult.Success(dropCallId.value, winners)
        }

        val message = when (result) {
            is DropResult.Success -> {
                val totalSonhosDistributed = sonhos * result.winners.size

                channel.sendMessage(
                    MessageCreate {
                        this.useComponentsV2 = true

                        this.container {
                            this.accentColorRaw = LorittaColors.SonhosDropSuccess.rgb

                            this.text(
                                buildString {
                                    appendLine("### ${loritta.emojiManager.get(LorittaEmojis.LoriConfetti)} ${i18nContext.get(I18nKeysData.Commands.Command.Drop.Call.SonhosDropHasEnded)}")
                                    for (winner in result.winners) {
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
                                    appendLine()
                                    appendLine("-# ${i18nContext.get(I18nKeysData.Commands.Command.Drop.Call.VoiceDropRequirements)}")
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
                                    appendLine("### ${loritta.emojiManager.get(LorittaEmojis.LoriConfetti)} ${i18nContext.get(I18nKeysData.Commands.Command.Drop.Call.SonhosDropHasEnded)}")
                                    appendLine("*${i18nContext.get(I18nKeysData.Commands.Command.Drop.TheCreatorDoesNotHaveEnoughSonhos(creator.asMention, SonhosUtils.getSonhosEmojiOfQuantity(result.totalSonhosPayout), result.totalSonhosPayout))}* ${Emotes.LoriSob}")
                                    appendLine()
                                    appendLine(i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.NoSonhosDistributedNotEnoughSonhos))
                                    appendLine()
                                    appendLine("-# ${i18nContext.get(I18nKeysData.Commands.Command.Drop.Call.VoiceDropRequirements)}")
                                }
                            )
                        }
                    }
                ).apply {
                    if (originalDropMessage != null)
                        setMessageReference(originalDropMessage.idLong)
                }.failOnInvalidReply(false).await()
            }

            DropResult.NoWinners -> {
                channel.sendMessage(
                    MessageCreate {
                        this.useComponentsV2 = true

                        this.container {
                            this.accentColorRaw = LorittaColors.SonhosDropEmpty.rgb

                            this.text(
                                buildString {
                                    appendLine("### ${loritta.emojiManager.get(LorittaEmojis.LoriConfetti)} ${i18nContext.get(I18nKeysData.Commands.Command.Drop.Call.SonhosDropHasEnded)}")
                                    appendLine("*${i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.NoOneParticipatedOnTheDrop)}* ${Emotes.LoriSob}")
                                    appendLine()
                                    appendLine(i18nContext.get(I18nKeysData.Commands.Command.Drop.Chat.NoSonhosDistributedNoParticipants))
                                    appendLine()
                                    appendLine("-# ${i18nContext.get(I18nKeysData.Commands.Command.Drop.Call.VoiceDropRequirements)}")
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
        scope.cancel()
        return message
    }

    private sealed class DropResult {
        data class Success(val dropId: Long, val winners: List<User>) : DropResult()
        data object NoWinners : DropResult()
        data class MoneySourceNotEnoughSonhos(val totalSonhosPayout: Long) : DropResult()
    }
}