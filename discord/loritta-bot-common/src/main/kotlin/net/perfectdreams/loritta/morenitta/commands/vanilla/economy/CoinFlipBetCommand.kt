package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`.CaraCoroaCommand
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.onReactionAdd
import net.perfectdreams.loritta.morenitta.utils.removeAllFunctions
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.loritta.deviousfun.entities.User
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.cinnamon.pudding.tables.CoinFlipBetMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.CoinFlipBetSonhosTransactionsLog
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.GACampaigns
import net.perfectdreams.loritta.morenitta.utils.GenericReplies
import net.perfectdreams.loritta.morenitta.utils.NumberUtils
import net.perfectdreams.loritta.morenitta.utils.PaymentUtils
import net.perfectdreams.loritta.morenitta.utils.SonhosPaymentReason
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.utils.extensions.refreshInDeferredTransaction
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.morenitta.utils.sendStyledReply
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import java.time.Instant

class CoinFlipBetCommand(val m: LorittaBot) : DiscordAbstractCommandBase(
    m,
    listOf("coinflip", "flipcoin", "girarmoeda", "caracoroa")
        .flatMap { listOf("$it bet", "$it apostar") },
    net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY
) {
    companion object {
        // Used to avoid dupes
        private val mutex = Mutex()
    }

    override fun command() = create {
        localizedDescription("commands.command.flipcoinbet.description")
        localizedExamples("commands.command.flipcoinbet.examples")

        usage {
            arguments {
                argument(ArgumentType.USER) {}
                argument(ArgumentType.NUMBER) {}
            }
        }

        this.canUseInPrivateChannel = false

        executesDiscord {
            if (2 > args.size)
                this.explainAndExit()

            val _user = validate(user(0))
            val invitedUser = _user.toJDA()

            if (invitedUser == user)
                fail(locale["commands.command.flipcoinbet.cantBetSelf"], Constants.ERROR)

            val selfActiveDonations = loritta.getActiveMoneyFromDonationsAsync(discordMessage.author.idLong)
            val otherActiveDonations = loritta.getActiveMoneyFromDonationsAsync(invitedUser.idLong)

            val selfPlan = UserPremiumPlans.getPlanFromValue(selfActiveDonations)
            val otherPlan = UserPremiumPlans.getPlanFromValue(otherActiveDonations)

            val hasNoTax: Boolean
            val whoHasTheNoTaxReward: User?
            val plan: UserPremiumPlans?
            val tax: Long?
            val taxPercentage: Double?
            val quantityAfterTax: Long
            val money: Long

            val number = NumberUtils.convertShortenedNumberToLong(args[1])
                ?: GenericReplies.invalidNumber(this, args[1].stripCodeMarks())

            if (selfPlan.totalCoinFlipReward == 1.0) {
                whoHasTheNoTaxReward = discordMessage.author
                hasNoTax = true
                plan = selfPlan
                taxPercentage = 0.0
                tax = null
                money = number
            } else if (otherPlan.totalCoinFlipReward == 1.0) {
                whoHasTheNoTaxReward = invitedUser
                hasNoTax = true
                plan = otherPlan
                taxPercentage = 0.0
                tax = null
                money = number
            } else {
                whoHasTheNoTaxReward = null
                hasNoTax = false
                plan = UserPremiumPlans.Essential
                taxPercentage =
                    (1.0.toBigDecimal() - selfPlan.totalCoinFlipReward.toBigDecimal()).toDouble() // Avoid rounding errors
                tax = (number * taxPercentage).toLong()
                money = number - tax
            }

            if (!hasNoTax && tax == 0L)
                fail(locale["commands.command.flipcoinbet.youNeedToBetMore"], Constants.ERROR)

            if (0 >= number)
                fail(locale["commands.command.flipcoinbet.zeroMoney"], Constants.ERROR)

            val selfUserProfile = lorittaUser.profile

            if (number > selfUserProfile.money) {
                sendStyledReply {
                    this.append {
                        message = locale["commands.command.flipcoinbet.notEnoughMoneySelf"]
                        prefix = Constants.ERROR
                    }

                    this.append {
                        message = GACampaigns.sonhosBundlesUpsellDiscordMessage(
                            "https://loritta.website/", // Hardcoded, woo
                            "bet-coinflip-legacy",
                            "bet-not-enough-sonhos"
                        )
                        prefix = Emotes.LORI_RICH.asMention
                        mentionUser = false
                    }
                }
                return@executesDiscord
            }

            val invitedUserProfile = loritta.getOrCreateLorittaProfile(invitedUser.id)
            val bannedState = invitedUserProfile.getBannedState(loritta)

            if (number > invitedUserProfile.money || bannedState != null)
                fail(
                    locale["commands.command.flipcoinbet.notEnoughMoneyInvited", invitedUser.asMention],
                    Constants.ERROR
                )

            // Self user check
            run {
                val epochMillis = user.timeCreated.toEpochSecond() * 1000

                // Don't allow users to bet if they are recent accounts
                if (epochMillis + (Constants.ONE_WEEK_IN_MILLISECONDS * 2) > System.currentTimeMillis()) // 14 dias
                    fail(
                        LorittaReply(
                            locale["commands.command.pay.selfAccountIsTooNew", 14] + " ${Emotes.LORI_CRYING}",
                            Constants.ERROR
                        )
                    )
            }

            // Invited user check
            run {
                val epochMillis = invitedUser.timeCreated.toEpochSecond() * 1000

                // Don't allow users to bet if they are recent accounts
                if (epochMillis + (Constants.ONE_WEEK_IN_MILLISECONDS * 2) > System.currentTimeMillis()) // 14 dias
                    fail(
                        LorittaReply(
                            locale["commands.command.pay.otherAccountIsTooNew", 14] + " ${Emotes.LORI_CRYING}",
                            Constants.ERROR
                        )
                    )
            }

            // Only allow users to participate in a coin flip bet if the user got their daily reward today
            AccountUtils.getUserTodayDailyReward(loritta, lorittaUser.profile)
                ?: fail(
                    locale["commands.youNeedToGetDailyRewardBeforeDoingThisAction", serverConfig.commandPrefix],
                    Constants.ERROR
                )

            val message = reply(
                LorittaReply(
                    if (hasNoTax)
                        locale[
                                "commands.command.flipcoinbet.startBetNoTax",
                                invitedUser.asMention,
                                user.asMention,
                                locale["commands.command.flipcoin.heads"],
                                money,
                                locale["commands.command.flipcoin.tails"],
                                whoHasTheNoTaxReward?.asMention ?: "???"
                        ]
                    else
                        locale[
                                "commands.command.flipcoinbet.startBet",
                                invitedUser.asMention,
                                user.asMention,
                                locale["commands.command.flipcoin.heads"],
                                money,
                                locale["commands.command.flipcoin.tails"],
                                number,
                                tax
                        ],
                    Emotes.LORI_RICH,
                    mentionUser = false
                ),
                LorittaReply(
                    locale[
                            "commands.command.flipcoinbet.clickToAcceptTheBet",
                            invitedUser.asMention,
                            "✅"
                    ],
                    "🤝",
                    mentionUser = false
                )
            ).toJDA()

            message.onReactionAdd(this) {
                if (it.reactionEmote.name == "✅") {
                    mutex.withLock {
                        if (loritta.messageInteractionCache.containsKey(it.messageIdLong)) {
                            val usersThatReactedToTheMessage = it.reaction.retrieveUsers()

                            if (invitedUser in usersThatReactedToTheMessage && user in usersThatReactedToTheMessage) {
                                message.removeAllFunctions(loritta)
                                GlobalScope.launch(loritta.coroutineDispatcher) {
                                    mutex.withLock {
                                        listOf(
                                            selfUserProfile.refreshInDeferredTransaction(loritta),
                                            invitedUserProfile.refreshInDeferredTransaction(loritta)
                                        ).awaitAll()

                                        if (number > selfUserProfile.money)
                                            return@withLock

                                        if (number > invitedUserProfile.money)
                                            return@withLock

                                        val isTails = LorittaBot.RANDOM.nextBoolean()
                                        val prefix: String
                                        val message: String

                                        if (isTails) {
                                            prefix = "<:coroa:412586257114464259>"
                                            message = locale["${CaraCoroaCommand.LOCALE_PREFIX}.tails"]
                                        } else {
                                            prefix = "<:cara:412586256409559041>"
                                            message = locale["${CaraCoroaCommand.LOCALE_PREFIX}.heads"]
                                        }

                                        val winner: User
                                        val loser: User
                                        val now = Instant.now()

                                        if (isTails) {
                                            winner = user
                                            loser = invitedUser
                                            loritta.newSuspendedTransaction {
                                                selfUserProfile.addSonhosNested(money)
                                                invitedUserProfile.takeSonhosNested(number)

                                                PaymentUtils.addToTransactionLogNested(
                                                    number,
                                                    SonhosPaymentReason.COIN_FLIP_BET,
                                                    givenBy = invitedUserProfile.id.value,
                                                    receivedBy = selfUserProfile.id.value
                                                )

                                                // Cinnamon transaction system
                                                val mmResult = CoinFlipBetMatchmakingResults.insertAndGetId {
                                                    it[CoinFlipBetMatchmakingResults.timestamp] = now
                                                    it[CoinFlipBetMatchmakingResults.winner] = selfUserProfile.id.value
                                                    it[CoinFlipBetMatchmakingResults.loser] =
                                                        invitedUserProfile.id.value
                                                    it[CoinFlipBetMatchmakingResults.quantity] = number
                                                    it[CoinFlipBetMatchmakingResults.quantityAfterTax] = money
                                                    it[CoinFlipBetMatchmakingResults.tax] = tax
                                                    it[CoinFlipBetMatchmakingResults.taxPercentage] = taxPercentage
                                                }

                                                val winnerTransactionLogId = SonhosTransactionsLog.insertAndGetId {
                                                    it[SonhosTransactionsLog.user] = selfUserProfile.id.value
                                                    it[SonhosTransactionsLog.timestamp] = now
                                                }

                                                CoinFlipBetSonhosTransactionsLog.insert {
                                                    it[CoinFlipBetSonhosTransactionsLog.timestampLog] =
                                                        winnerTransactionLogId
                                                    it[CoinFlipBetSonhosTransactionsLog.matchmakingResult] = mmResult
                                                }

                                                val loserTransactionLogId = SonhosTransactionsLog.insertAndGetId {
                                                    it[SonhosTransactionsLog.user] = invitedUserProfile.id.value
                                                    it[SonhosTransactionsLog.timestamp] = now
                                                }

                                                CoinFlipBetSonhosTransactionsLog.insert {
                                                    it[CoinFlipBetSonhosTransactionsLog.timestampLog] =
                                                        loserTransactionLogId
                                                    it[CoinFlipBetSonhosTransactionsLog.matchmakingResult] = mmResult
                                                }
                                            }
                                        } else {
                                            winner = invitedUser
                                            loser = user
                                            loritta.newSuspendedTransaction {
                                                invitedUserProfile.addSonhosNested(money)
                                                selfUserProfile.takeSonhosNested(number)

                                                PaymentUtils.addToTransactionLogNested(
                                                    number,
                                                    SonhosPaymentReason.COIN_FLIP_BET,
                                                    givenBy = selfUserProfile.id.value,
                                                    receivedBy = invitedUserProfile.id.value
                                                )

                                                // Cinnamon transaction system
                                                val mmResult = CoinFlipBetMatchmakingResults.insertAndGetId {
                                                    it[CoinFlipBetMatchmakingResults.timestamp] = Instant.now()
                                                    it[CoinFlipBetMatchmakingResults.winner] =
                                                        invitedUserProfile.id.value
                                                    it[CoinFlipBetMatchmakingResults.loser] = selfUserProfile.id.value
                                                    it[CoinFlipBetMatchmakingResults.quantity] = number
                                                    it[CoinFlipBetMatchmakingResults.quantityAfterTax] = money
                                                    it[CoinFlipBetMatchmakingResults.tax] = tax
                                                    it[CoinFlipBetMatchmakingResults.taxPercentage] = taxPercentage
                                                }

                                                val winnerTransactionLogId = SonhosTransactionsLog.insertAndGetId {
                                                    it[SonhosTransactionsLog.user] = invitedUserProfile.id.value
                                                    it[SonhosTransactionsLog.timestamp] = now
                                                }

                                                CoinFlipBetSonhosTransactionsLog.insert {
                                                    it[CoinFlipBetSonhosTransactionsLog.timestampLog] =
                                                        winnerTransactionLogId
                                                    it[CoinFlipBetSonhosTransactionsLog.matchmakingResult] = mmResult
                                                }

                                                val loserTransactionLogId = SonhosTransactionsLog.insertAndGetId {
                                                    it[SonhosTransactionsLog.user] = selfUserProfile.id.value
                                                    it[SonhosTransactionsLog.timestamp] = now
                                                }

                                                CoinFlipBetSonhosTransactionsLog.insert {
                                                    it[CoinFlipBetSonhosTransactionsLog.timestampLog] =
                                                        loserTransactionLogId
                                                    it[CoinFlipBetSonhosTransactionsLog.matchmakingResult] = mmResult
                                                }
                                            }
                                        }

                                        reply(
                                            LorittaReply(
                                                "**$message!**",
                                                prefix,
                                                mentionUser = false
                                            ),
                                            LorittaReply(
                                                locale["commands.command.flipcoinbet.congratulations", winner.asMention, money, loser.asMention],
                                                Emotes.LORI_RICH,
                                                mentionUser = false
                                            ),
                                            LorittaReply(
                                                "Psiu, cansado de procurar pessoas que querem apostar com você? Então experimente o `/bet coinflipglobal`, um novo comando que permite você apostar com outros usuários, inclusive de outros servidores, sem precisar sair do conforto da sua ~~casa~~ servidor!",
                                                mentionUser = false
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            runCatching { message.addReaction("✅") }
        }
    }
}