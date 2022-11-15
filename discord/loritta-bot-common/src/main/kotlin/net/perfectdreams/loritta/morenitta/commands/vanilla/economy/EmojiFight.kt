package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.onReactionAdd
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor
import net.perfectdreams.loritta.morenitta.utils.removeAllFunctions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightMatches
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.CoinFlipBetSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.EmojiFightSonhosTransactionsLog
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.PaymentUtils
import net.perfectdreams.loritta.morenitta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.utils.extensions.addReaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Creates a Emoji Fight
 */
class EmojiFight(
    private val context: DiscordCommandContext,
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
            val tax = (entryPrice * (1.0 - UserPremiumPlans.Free.totalCoinFlipReward)).toLong()

            if (tax == 0L)
                context.fail(context.locale["commands.command.flipcoinbet.youNeedToBetMore"], Constants.ERROR)
        }

        addToFightEvent(context.user)

        val baseEmbed = getEventEmbed()

        val message = context.sendMessageEmbeds(baseEmbed)

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
                    message.removeAllFunctions(loritta)
                    finishEvent()
                    updateEventMessage(message, true)
                }
            }
        }

        // We update the event message after the event is finished because we hold a lock for 3s, and sometimes we want to update the message after the event has finished
        // but sometimes the event may finish during that 3s cooldown! That's why there is a boolean to bypass the lock
        message.onReactionAdd(context) {
            val reactedUser = it.user ?: return@onReactionAdd

            if (it.emoji.name == "\uD83D\uDC14" && !participatingUsers.containsKey(reactedUser)) {
                if (addToFightEvent(reactedUser)) {
                    updateEventMessage(message)

                    finishingEventMutex.withLock {
                        if (!eventFinished && participatingUsers.size >= maxPlayers) {
                            message.removeAllFunctions(loritta)

                            finishEvent()
                            updateEventMessage(message, true)
                        }
                    }
                }
            }
        }

        message.onReactionAddByAuthor(context) {
            if (it.emoji.name == "✅") {
                finishingEventMutex.withLock {
                    if (!eventFinished) {
                        message.removeAllFunctions(loritta)

                        finishEvent()
                        updateEventMessage(message, true)
                    }
                }
            }
        }

        message.addReaction("✅")
            .queue()

        message.addReaction("\uD83D\uDC14")
            .queue()
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
                            context.serverConfig.commandPrefix,
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

            message.editMessageEmbeds(getEventEmbed()).await()

            val onEndCount = participatingUsers.size

            onStartCount != onEndCount
        }

        if (shouldUpdateAgain)
            updateEventMessage(message)
    }

    /**
     * Returns "true" if the user was added to the event, "false" if not.
     */
    private suspend fun addToFightEvent(user: User): Boolean {
        addingUserToEventMutex.withLock {
            // Event is already finished (probably the user clicked to enter while the mutex was locked)
            // So, just ignore the request!
            if (eventFinished)
                return false

            if (user.isBot)
                return false

            // If there is already way too much users here, just ignore the add request
            if (participatingUsers.size >= maxPlayers)
                return false

            if (entryPrice != null) {
                val profile = loritta.getLorittaProfile(user.idLong) ?: return false

                if (entryPrice > profile.money || profile.getBannedState(loritta) != null)
                    return false

                // If the user didn't get daily today, they can't participate in the event
                if (AccountUtils.getUserTodayDailyReward(loritta, profile) == null)
                    return false

                val epochMillis = user.timeCreated.toEpochSecond() * 1000

                // Don't allow users to bet if they are recent accounts
                if (epochMillis + (Constants.ONE_WEEK_IN_MILLISECONDS * 2) > System.currentTimeMillis()) // 14 dias
                    return false
            }

            val randomEmote = availableEmotes.random()
            availableEmotes.remove(randomEmote)
            participatingUsers[user] = randomEmote
            return true
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
                val taxPercentage = (1.0.toBigDecimal() - selfPlan.totalCoinFlipReward.toBigDecimal()).toDouble() // Avoid rounding errors
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
                }

                winnerProfile.addSonhosNested(realAfterTaxesPrize)
                PaymentUtils.addToTransactionLogNested(
                    realAfterTaxesPrize,
                    SonhosPaymentReason.EMOJI_FIGHT,
                    receivedBy = winnerProfile.id.value
                )

                // Cinnamon transaction system
                val winnerTransactionLogId = SonhosTransactionsLog.insertAndGetId {
                    it[SonhosTransactionsLog.user] = winnerProfile.id
                    it[SonhosTransactionsLog.timestamp] = now
                }

                EmojiFightSonhosTransactionsLog.insert {
                    it[EmojiFightSonhosTransactionsLog.timestampLog] = winnerTransactionLogId
                    it[EmojiFightSonhosTransactionsLog.matchmakingResult] = resultId
                }

                for (loser in losers) {
                    val loserProfile = userProfiles[loser.key]!!
                    loserProfile.takeSonhosNested(entryPrice)
                    PaymentUtils.addToTransactionLogNested(
                        entryPrice,
                        SonhosPaymentReason.EMOJI_FIGHT,
                        givenBy = loserProfile.id.value
                    )

                    // Cinnamon transaction system
                    val loserTransactionLogId = SonhosTransactionsLog.insertAndGetId {
                        it[SonhosTransactionsLog.user] = loserProfile.id
                        it[SonhosTransactionsLog.timestamp] = now
                    }

                    EmojiFightSonhosTransactionsLog.insert {
                        it[CoinFlipBetSonhosTransactionsLog.timestampLog] = loserTransactionLogId
                        it[CoinFlipBetSonhosTransactionsLog.matchmakingResult] = resultId
                    }
                }

                DbResponse(winner, losers, realBeforeTaxesPrize, realAfterTaxesPrize)
            } else {
                val resultId = EmojiFightMatchmakingResults.insertAndGetId {
                    it[EmojiFightMatchmakingResults.winner] = databaseParticipatingUserEntries[winner.key] ?: error("Participating user is null! This should never happen!!")
                    it[EmojiFightMatchmakingResults.entryPrice] = 0
                    it[EmojiFightMatchmakingResults.entryPriceAfterTax] = 0
                    it[EmojiFightMatchmakingResults.tax] = null
                    it[EmojiFightMatchmakingResults.taxPercentage] = null
                }

                DbResponse(winner, losers, 0, 0)
            }
        }

        val (winner, losers, realPrize, taxedRealPrize) = result ?: run {
            // Needs to use "reply" because if we use "fail", the exception is triggered on the onReactionAddByAuthor
            context.reply(
                context.locale["commands.command.emojifight.needsMorePlayers"],
                Emotes.LORI_CRYING
            )
            return
        }

        if (entryPrice != null) {
            val tax = (realPrize - taxedRealPrize)

            // If the tax == 0, then it means that the user is premium!
            val localeKey = if (tax == 0L)
                "commands.command.emojifightbet.wonBet"
            else
                "commands.command.emojifightbet.wonBetTaxed"

            context.reply(
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
                Emotes.LORI_RICH,
                mentionUser = false
            )
        } else {
            context.reply(
                context.locale[
                        "commands.command.emojifight.wonBet",
                        winner.value,
                        winner.key.asMention,
                        context.user.asMention
                ],
                Emotes.LORI_SMILE,
                mentionUser = false
            )
        }
    }

    private data class DbResponse(
        val winner: MutableMap.MutableEntry<User, String>,
        val losers: MutableSet<MutableMap.MutableEntry<User, String>>,
        val realPrize: Long,
        val taxedPrize: Long
    )

    companion object {
        val DEFAULT_MAX_PLAYER_COUNT = 30

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