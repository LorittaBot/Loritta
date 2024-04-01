package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils.appendUserHaventGotDailyTodayOrUpsellSonhosBundles
import net.perfectdreams.loritta.cinnamon.pudding.tables.AprilFoolsCoinFlipBugs
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightMatches
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightParticipants
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.morenitta.utils.AprilFools
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.PaymentUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import net.perfectdreams.loritta.serializable.StoredEmojiFightBetSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import java.time.Instant
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import net.perfectdreams.loritta.cinnamon.emotes.Emotes as CinnamonEmotes

/**
 * Creates a Emoji Fight
 */
class EmojiFight(
    private val context: UnleashedContext,
    private val entryPrice: Long?, // null = only for fun emoji fight
    private val maxPlayers: Int = DEFAULT_MAX_PLAYER_COUNT
) {
    val loritta = context.loritta
    private val availableEmotes = emojis.toMutableList()
    private val participatingUsers = ConcurrentHashMap<User, String>()
    private val updatingMessageMutex = Mutex()
    private val addingUserToEventMutex = Mutex()
    private val finishingEventMutex = Mutex()
    private var eventFinished = false
    val startedAt = Instant.now()

    /**
     * Starts the Emoji Fight
     */
    suspend fun start() {
        if (entryPrice != null) {
            // This always checks the FREE REWARD since other FREE REWARD users may join the emoji fight
            val tax = (entryPrice * (1.0 - UserPremiumPlans.Free.totalCoinFlipReward)).toLong()

            if (tax == 0L) {
                context.reply(false) {
                    styled(
                        context.locale["commands.command.flipcoinbet.youNeedToBetMore"],
                        Constants.ERROR
                    )
                }
                return
            }
        }

        val state = addToFightEvent(context.user)

        val baseEmbed = getEventEmbed()

        // We update the event message after the event is finished because we hold a lock for 3s, and sometimes we want to update the message after the event has finished
        // but sometimes the event may finish during that 3s cooldown! That's why there is a boolean to bypass the lock
        val participateInTheEventButtonInteraction = context.loritta.interactivityManager.button(
            ButtonStyle.PRIMARY,
            context.i18nContext.get(I18N_PREFIX.JoinTheEmojiFight),
            { emoji = Emoji.fromUnicode("\uD83D\uDC14") }
        ) { context ->
            val reactedUser = context.user

            when (val state = addToFightEvent(reactedUser)) {
                EmojiFightJoinState.AccountTooNew -> {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.JoinState.AccountTooNew(14)),
                            CinnamonEmotes.LoriSob
                        )
                    }
                }

                EmojiFightJoinState.DidntGetDailyReward -> {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.JoinState.DidntGetDailyReward(loritta.commandMentions.daily)),
                            CinnamonEmotes.LoriSob
                        )
                    }
                }

                EmojiFightJoinState.EventFinished -> {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.JoinState.EventFinished),
                            CinnamonEmotes.LoriSob
                        )
                    }
                }

                is EmojiFightJoinState.NotEnoughMoney -> {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(SonhosUtils.insufficientSonhos(state.money, state.howMuch)),
                            CinnamonEmotes.LoriSob
                        )

                        this.appendUserHaventGotDailyTodayOrUpsellSonhosBundles(
                            loritta,
                            context.i18nContext,
                            UserId(context.user.idLong),
                            "pay",
                            "transfer-not-enough-sonhos"
                        )
                    }
                }

                EmojiFightJoinState.TooManyPlayers -> {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.JoinState.TooManyPlayers),
                            CinnamonEmotes.LoriSob
                        )
                    }
                }

                EmojiFightJoinState.UserIsBot -> {
                    context.reply(true) {
                        content = "Você é um bot! ...E eu acho que isso jamais deve acontecer!"
                    }

                }

                EmojiFightJoinState.YouAreAlreadyParticipating -> {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.JoinState.YouAreAlreadyParticipating),
                            CinnamonEmotes.LoriSob
                        )
                    }
                }

                is EmojiFightJoinState.Success -> {
                    val randomJoinMessages = listOf(
                        I18N_PREFIX.JoinState.SuccessState.Success1(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success2(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success3(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success4(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success5(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success6(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success7(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success8(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success9(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success10(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success11(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success12(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success13(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success14(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success15(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success16(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success17(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success18(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success19(state.emoji),
                        I18N_PREFIX.JoinState.SuccessState.Success20(state.emoji),
                    )

                    context.reply(true) {
                        styled(
                            context.i18nContext.get(randomJoinMessages.random()),
                            CinnamonEmotes.LoriHi
                        )
                    }

                    updateEventMessage(context.event.message)

                    finishingEventMutex.withLock {
                        if (!eventFinished && participatingUsers.size >= maxPlayers) {
                            finishEvent()
                            updateEventMessage(context.event.message, true)
                        }
                    }
                }
            }
        }

        val endTheEventButtonInteraction = context.loritta.interactivityManager.buttonForUser(
            context.user,
            ButtonStyle.PRIMARY,
            context.i18nContext.get(I18N_PREFIX.StartTheEmojiFight),
            { emoji = Emoji.fromUnicode("✅") }
        ) { context ->
            if (eventFinished) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.JoinState.EventFinished),
                        CinnamonEmotes.LoriSob
                    )
                }
                return@buttonForUser
            }

            // Defer the edit
            context.deferEdit()

            finishingEventMutex.withLock {
                if (!eventFinished) {
                    finishEvent()
                    updateEventMessage(context.event.message, true)
                }
            }
        }

        val message = context.reply(false) {
            embeds += baseEmbed

            actionRow(participateInTheEventButtonInteraction, endTheEventButtonInteraction)
        }.retrieveOriginal()

        GlobalScope.launch(loritta.coroutineDispatcher) {
            updatingMessageMutex.withLock {
                // This looks incredibly dumb, but it exists for a reason!
                // Because most users will react in the beginning of the event, we are going to delay for 3s to lock the Mutex
                // this avoids updating the message multiple times in a quick succession!
                delay(3_000)
            }
        }

        GlobalScope.launch(loritta.coroutineDispatcher) {
            delay(60_000) // Automatically finishes the event after 60s
            finishingEventMutex.withLock {
                if (!eventFinished) {
                    finishEvent()
                    updateEventMessage(message, true)
                }
            }
        }
    }

    private fun getEventEmbed(): MessageEmbed {
        val baseEmbed = EmbedBuilder()
            .setTitle("${Emotes.LORI_BAN_HAMMER} ${context.locale["commands.command.emojifight.fightTitle"]}")
            .setDescription(
                if (entryPrice != null) {
                    context.locale
                        .getList(
                            "commands.command.emojifightbet.fightDescription",
                            entryPrice,
                            entryPrice * (participatingUsers.size - 1), // Needs to subtract -1 because the winner *won't* pay for his win
                            "\uD83D\uDC14",
                            context.user.asMention,
                            "✅",
                            maxPlayers
                        ).joinToString("\n") + "\n\n**" + context.locale["commands.command.emojifight.participants", participatingUsers.size] + "**\n"
                } else {
                    context.locale
                        .getList(
                            "commands.command.emojifight.fightDescription",
                            Emotes.LORI_PAT,
                            context.config.commandPrefix,
                            "\uD83D\uDC14",
                            context.user.asMention,
                            "✅",
                            maxPlayers
                        ).joinToString("\n") + "\n\n**" + context.locale["commands.command.emojifight.participants", participatingUsers.size] + "**\n"
                }
            )
            .setColor(Constants.ROBLOX_RED)

        participatingUsers.entries.forEach {
            baseEmbed.appendDescription("${it.value}: ${it.key.asMention}\n")
        }

        return baseEmbed.build()
    }

    private suspend fun updateEventMessage(message: Message, bypassLock: Boolean = false) {
        if (!bypassLock && updatingMessageMutex.isLocked) // If it is already locked, no need to update it again
            return

        val shouldUpdateAgain = updatingMessageMutex.withLock {
            val onStartCount = participatingUsers.size

            message.editMessageEmbeds(getEventEmbed())
                .setReplace(false) // We don't want to replace the buttons, only the embed!
                .await()

            val onEndCount = participatingUsers.size

            onStartCount != onEndCount
        }

        if (shouldUpdateAgain)
            updateEventMessage(message)
    }

    /**
     * Returns "true" if the user was added to the event, "false" if not.
     */
    private suspend fun addToFightEvent(user: User): EmojiFightJoinState {
        addingUserToEventMutex.withLock {
            // Event is already finished (probably the user clicked to enter while the mutex was locked)
            // So, just ignore the request!
            if (eventFinished)
                return EmojiFightJoinState.EventFinished

            if (user.isBot)
                return EmojiFightJoinState.UserIsBot

            // If there is already way too much users here, just ignore the add request
            if (participatingUsers.size >= maxPlayers)
                return EmojiFightJoinState.TooManyPlayers

            if (participatingUsers.containsKey(user))
                return EmojiFightJoinState.YouAreAlreadyParticipating

            if (entryPrice != null) {
                val profile = loritta.getLorittaProfile(user.idLong) ?: return EmojiFightJoinState.NotEnoughMoney(0, entryPrice)

                if (entryPrice > profile.money || profile.getBannedState(loritta) != null)
                    return EmojiFightJoinState.NotEnoughMoney(profile.money, entryPrice)

                // If the user didn't get daily today, they can't participate in the event
                if (AccountUtils.getUserTodayDailyReward(loritta, profile) == null)
                    return EmojiFightJoinState.DidntGetDailyReward

                val epochMillis = user.timeCreated.toEpochSecond() * 1000

                // Don't allow users to bet if they are recent accounts
                if (epochMillis + (Constants.ONE_WEEK_IN_MILLISECONDS * 2) > System.currentTimeMillis()) // 14 dias
                    return EmojiFightJoinState.AccountTooNew
            }

            val randomEmoji = loritta.newSuspendedTransaction {
                val emojiFightEmoji = loritta._getLorittaProfile(user.idLong)?.settings?.emojiFightEmoji
                val donationPlan = UserPremiumPlans.getPlanFromValue(loritta._getActiveMoneyFromDonations(user.idLong))

                if (donationPlan.customEmojisInEmojiFight && emojiFightEmoji != null) {
                    emojiFightEmoji
                } else {
                    val randomEmote = availableEmotes.random()
                    availableEmotes.remove(randomEmote)
                    randomEmote
                }
            }

            participatingUsers[user] = randomEmoji
            return EmojiFightJoinState.Success(randomEmoji)
        }
    }

    private suspend fun finishEvent() {
        eventFinished = true

        val result = loritta.newSuspendedTransaction {
            val now = Instant.now()

            // We need to filter the "real" valid users.
            // Since some may have lost sonhos since the event start, so we are going to remove all of them.
            val realValidParticipatingUsers = mutableMapOf<User, String>()

            val userProfiles = mutableMapOf<User, Profile>()

            for (participatingUser in participatingUsers) {
                val profile = loritta.getLorittaProfile(participatingUser.key.idLong)

                if (profile == null) // If the profile is null, ignore them.
                    continue
                else
                    if (entryPrice != null && entryPrice > profile.money) // Player doesn't has enough money
                        continue
                    else {
                        realValidParticipatingUsers[participatingUser.key] = participatingUser.value
                        userProfiles[participatingUser.key] = profile
                    }
            }

            // If we don't have enough valid participating users, let's just exit out with a null
            if (2 > realValidParticipatingUsers.size)
                return@newSuspendedTransaction null

            // Cinnamon emoji fight match stats
            val emojiFightMatch = EmojiFightMatches.insertAndGetId {
                it[EmojiFightMatches.createdBy] = context.user.idLong
                it[EmojiFightMatches.guild] = context.guildId
                it[EmojiFightMatches.createdAt] = startedAt
                it[EmojiFightMatches.finishedAt] = now
                it[EmojiFightMatches.maxPlayers] = this@EmojiFight.maxPlayers
            }

            val databaseParticipatingUserEntries = realValidParticipatingUsers.map { (user, emoji) ->
                user to EmojiFightParticipants.insertAndGetId {
                    it[EmojiFightParticipants.match] = emojiFightMatch
                    it[EmojiFightParticipants.user] = user.idLong
                    it[EmojiFightParticipants.emoji] = emoji
                }
            }.toMap()

            val winner = realValidParticipatingUsers.entries.random()
            val losers = realValidParticipatingUsers.entries.apply {
                this.remove(winner)
            }

            if (entryPrice != null) {
                val selfActiveDonations = loritta._getActiveMoneyFromDonations(winner.key.idLong)

                val selfPlan = UserPremiumPlans.getPlanFromValue(selfActiveDonations)

                val winnerProfile = userProfiles[winner.key]!!
                val taxPercentage = (1.0.toBigDecimal() - SonhosUtils.getSpecialTotalCoinFlipReward(context.guild, selfPlan.totalCoinFlipReward).value.toBigDecimal()).toDouble() // Avoid rounding errors
                val tax = (entryPrice * taxPercentage).toLong()
                val taxedEntryPrice = entryPrice - tax

                val realBeforeTaxesPrize = entryPrice * losers.size
                val realAfterTaxesPrize = taxedEntryPrice * losers.size

                val resultId = EmojiFightMatchmakingResults.insertAndGetId {
                    it[EmojiFightMatchmakingResults.winner] = databaseParticipatingUserEntries[winner.key] ?: error("Participating user is null! This should never happen!!")
                    it[EmojiFightMatchmakingResults.entryPrice] = this@EmojiFight.entryPrice
                    it[EmojiFightMatchmakingResults.entryPriceAfterTax] = taxedEntryPrice
                    if (taxPercentage != 0.0) {
                        it[EmojiFightMatchmakingResults.tax] = tax
                        it[EmojiFightMatchmakingResults.taxPercentage] = taxPercentage
                    }
                    it[EmojiFightMatchmakingResults.match] = emojiFightMatch
                }

                winnerProfile.addSonhosNested(realAfterTaxesPrize)
                PaymentUtils.addToTransactionLogNested(
                    realAfterTaxesPrize,
                    SonhosPaymentReason.EMOJI_FIGHT,
                    receivedBy = winnerProfile.id.value
                )

                // Cinnamon transaction system
                SimpleSonhosTransactionsLogUtils.insert(
                    winnerProfile.id.value,
                    now,
                    TransactionType.EMOJI_FIGHT_BET,
                    realAfterTaxesPrize,
                    StoredEmojiFightBetSonhosTransaction(resultId.value)
                )

                for (loser in losers) {
                    val loserProfile = userProfiles[loser.key]!!
                    loserProfile.takeSonhosNested(entryPrice)
                    PaymentUtils.addToTransactionLogNested(
                        entryPrice,
                        SonhosPaymentReason.EMOJI_FIGHT,
                        givenBy = loserProfile.id.value
                    )

                    // Cinnamon transaction system
                    SimpleSonhosTransactionsLogUtils.insert(
                        loserProfile.id.value,
                        now,
                        TransactionType.EMOJI_FIGHT_BET,
                        entryPrice,
                        StoredEmojiFightBetSonhosTransaction(resultId.value)
                    )
                }

                val aprilFoolsWinnerBugMessage = if (AprilFools.isAprilFools()) {
                    AprilFoolsCoinFlipBugs.select {
                        AprilFoolsCoinFlipBugs.userId eq winnerProfile.id.value and (AprilFoolsCoinFlipBugs.year eq LocalDateTime.now(
                            Constants.LORITTA_TIMEZONE
                        ).year)
                    }.limit(1)
                        .orderBy(AprilFoolsCoinFlipBugs.beggedAt, SortOrder.DESC)
                        .lastOrNull()
                        ?.get(AprilFoolsCoinFlipBugs.bug)
                } else null

                DbResponse(winner, losers, realBeforeTaxesPrize, realAfterTaxesPrize, aprilFoolsWinnerBugMessage)
            } else {
                val resultId = EmojiFightMatchmakingResults.insertAndGetId {
                    it[EmojiFightMatchmakingResults.winner] = databaseParticipatingUserEntries[winner.key] ?: error("Participating user is null! This should never happen!!")
                    it[EmojiFightMatchmakingResults.entryPrice] = 0
                    it[EmojiFightMatchmakingResults.entryPriceAfterTax] = 0
                    it[EmojiFightMatchmakingResults.tax] = null
                    it[EmojiFightMatchmakingResults.taxPercentage] = null
                    it[EmojiFightMatchmakingResults.match] = emojiFightMatch
                }

                DbResponse(winner, losers, 0, 0, null)
            }
        }

        val (winner, losers, realPrize, taxedRealPrize, aprilFoolsWinnerBugMessage) = result ?: run {
            // Needs to use "reply" because if we use "fail", the exception is triggered on the onReactionAddByAuthor
            context.reply(false) {
                styled(
                    context.locale["commands.command.emojifight.needsMorePlayers"],
                    Emotes.LORI_CRYING.asMention
                )
            }
            return
        }

        if (entryPrice != null) {
            val tax = (realPrize - taxedRealPrize)

            // If the tax == 0, then it means that the user is premium!
            val localeKey = if (tax == 0L)
                "commands.command.emojifightbet.wonBet"
            else
                "commands.command.emojifightbet.wonBetTaxed"

            context.reply(false) {
                // Mention the fight creator and the winner
                mentions {
                    user(context.user)
                    user(winner.key)
                }

                styled(
                    context.locale[
                            localeKey,
                            winner.value,
                            winner.key.asMention,
                            taxedRealPrize,
                            tax,
                            losers.size,
                            entryPrice,
                            context.user.asMention
                    ],
                    Emotes.LORI_RICH.asMention,
                )

                if (aprilFoolsWinnerBugMessage != null) {
                    styled(
                        "${winner.key.asMention} estava com o Bug do Coin Flip™ ativado! Bug: `${aprilFoolsWinnerBugMessage}` - Faça o seu bug também! ${loritta.commandMentions.coinFlipBetBug}",
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriFire
                    )
                }
            }
        } else {
            context.reply(false) {
                // Mention the fight creator and the winner
                mentions {
                    user(context.user)
                    user(winner.key)
                }

                styled(
                    context.locale[
                            "commands.command.emojifight.wonBet",
                            winner.value,
                            winner.key.asMention,
                            context.user.asMention
                    ],
                    Emotes.LORI_SMILE.asMention
                )
            }
        }
    }

    private data class DbResponse(
        val winner: MutableMap.MutableEntry<User, String>,
        val losers: MutableSet<MutableMap.MutableEntry<User, String>>,
        val realPrize: Long,
        val taxedPrize: Long,
        val aprilFoolsWinnerBugMessage: String?
    )

    sealed class EmojiFightJoinState {
        object EventFinished : EmojiFightJoinState()
        object UserIsBot : EmojiFightJoinState()
        object TooManyPlayers : EmojiFightJoinState()
        class NotEnoughMoney(val money: Long, val howMuch: Long) : EmojiFightJoinState()
        object DidntGetDailyReward : EmojiFightJoinState()
        object AccountTooNew : EmojiFightJoinState()
        object YouAreAlreadyParticipating : EmojiFightJoinState()
        class Success(val emoji: String) : EmojiFightJoinState()
    }

    companion object {
        const val DEFAULT_MAX_PLAYER_COUNT = 50
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Emojifight

        val emojis = mutableListOf(
            "🙈",
            "🙉",
            "🙊",
            "🐵",
            "🐒",
            "🦍",
            "🦧",
            "🐶",
            "🐕",
            "🦮",
            "🐕‍🦺",
            "🐩",
            "🐺",
            "🦊",
            "🦝",
            "🐱",
            "🐈",
            "🦁",
            "🐯",
            "🐅",
            "🐆",
            "🐴",
            "🐎",
            "🦄",
            "🦓",
            "🦌",
            "🐮",
            "🐂",
            "🐃",
            "🐄",
            "🐷",
            "🐖",
            "🐗",
            "🐏",
            "🐑",
            "🐐",
            "🐪",
            "🐫",
            "🦙",
            "🦒",
            "🐘",
            "🦏",
            "🦛",
            "🐭",
            "🐁",
            "🐀",
            "🐹",
            "🐰",
            "🐇",
            "🐿",
            "🦔",
            "🦇",
            "🐻",
            "🐨",
            "🐼",
            "🦥",
            "🦦",
            "🦨",
            "🦘",
            "🦡",
            "🦃",
            "🐔",
            "🐓",
            "🐣",
            "🐤",
            "🐥",
            "🐦",
            "🐧",
            "🕊",
            "🦅",
            "🦆",
            "🦢",
            "🦉",
            "🦩",
            "🦚",
            "🦜",
            "🐸",
            "🐊",
            "🐢",
            "🦎",
            "🐍",
            "🐲",
            "🐉",
            "🦕",
            "🦖",
            "🐳",
            "🐋",
            "🐬",
            "🐟",
            "🐠",
            "🐡",
            "🦈",
            "🐙",
            "🐚",
            "🐌",
            "🦋",
            "🐛",
            "🐜",
            "🐝",
            "🐞",
            "🦗",
            "🕷",
            "🦂",
            "🦟",
            "🦠",
            "\uD83E\uDD80",
            "\uD83E\uDD9E",
            "\uD83E\uDD90",
            "\uD83E\uDD91"
        )
    }
}