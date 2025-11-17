package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.blackjack

import dev.minn.jda.ktx.interactions.components.Container
import dev.minn.jda.ktx.interactions.components.Thumbnail
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.editMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionHook
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.pudding.tables.BlackjackSinglePlayerMatches
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.emojis.LorittaEmojiReference
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.blackjack.Blackjack
import net.perfectdreams.loritta.morenitta.blackjack.Card
import net.perfectdreams.loritta.morenitta.blackjack.Hand
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.EmojiFightCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.blackjack.BlackjackUtils.createBlackjackHouseRulesMessage
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.blackjack.BlackjackUtils.createBlackjackTutorialMessage
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.NumberUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.StoredBlackjackDoubleDownTransaction
import net.perfectdreams.loritta.serializable.StoredBlackjackInsurancePayoutTransaction
import net.perfectdreams.loritta.serializable.StoredBlackjackInsuranceTransaction
import net.perfectdreams.loritta.serializable.StoredBlackjackPayoutTransaction
import net.perfectdreams.loritta.serializable.StoredBlackjackTiedTransaction
import net.perfectdreams.loritta.serializable.StoredBlackjackJoinedTransaction
import net.perfectdreams.loritta.serializable.StoredBlackjackSplitTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID
import kotlin.collections.iterator
import kotlin.math.ceil
import kotlin.time.Duration.Companion.seconds

class BlackjackCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Blackjack
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY, UUID.fromString("86f1fe8e-fa8d-47b3-a201-26b494720572")) {
        this.enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
        this.alternativeLegacyLabels.apply {
            this.add("bj")
        }

        subcommand(I18N_PREFIX.Play.Label, I18N_PREFIX.Play.Description, UUID.fromString("2b36e46a-d5ce-4027-8ff5-1a95cfc64651")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                this.add("bj")
                this.add("blackjack")
            }
            executor = BlackjackPlayExecutor(loritta)
        }

        subcommand(I18N_PREFIX.Tutorial.Label, I18N_PREFIX.Tutorial.Description, UUID.fromString("b0754c58-396a-455d-b6b8-46b856d99dea")) {
            executor = BlackjackTutorialExecutor(loritta)
        }

        subcommand(I18N_PREFIX.HouseRules.Label, I18N_PREFIX.HouseRules.Description, UUID.fromString("9f29a78c-0527-461b-9430-220285d5d2e8")) {
            executor = BlackjackHouseRulesExecutor(loritta)
        }

        subcommand(I18N_PREFIX.Stats.Label, I18N_PREFIX.Stats.Description, UUID.fromString("9e33e456-16d3-4bdb-a3c2-dd17bc441ec2")) {
            executor = BlackjackStatsExecutor(loritta)
        }
    }

    class BlackjackPlayExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        companion object {
            private val SUIT_EMOJIS = mapOf(
                Card.Suit.HEARTS to LorittaEmojis.CardHearts,
                Card.Suit.SPADES to LorittaEmojis.CardSpades,
                Card.Suit.DIAMONDS to LorittaEmojis.CardDiamonds,
                Card.Suit.CLUBS to LorittaEmojis.CardClubs
            )
        }

        class Options : ApplicationCommandOptions() {
            val sonhos = optionalString("sonhos", I18nKeysData.Commands.Command.Blackjack.Play.Options.Sonhos.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val providedStringSonhosInput = args[options.sonhos]
            val selfUserProfile = context.lorittaUser.profile

            var totalEarnings = if (providedStringSonhosInput != null) {
                NumberUtils.convertShortenedNumberOrUserSonhosSpecificToLong(providedStringSonhosInput, selfUserProfile.money) ?: context.fail(
                    true,
                    context.i18nContext.get(
                        I18nKeysData.Commands.InvalidNumber(providedStringSonhosInput)
                    ),
                    Emotes.LORI_CRYING.asMention
                )
            } else {
                null
            }

            // Allow betting "nothing" to create a 4fun game
            if (totalEarnings == 0L)
                totalEarnings = null

            // Sonhos check if the user provided sonhos' amount
            if (totalEarnings != null) {
                if (0 >= totalEarnings)
                    context.fail(true) {
                        styled(
                            context.locale["commands.command.flipcoinbet.zeroMoney"],
                            Constants.ERROR
                        )
                    }

                if (totalEarnings !in BlackjackUtils.MINIMUM_BET..BlackjackUtils.MAXIMUM_BET) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(
                                I18N_PREFIX.Play.InvalidBet(
                                    SonhosUtils.getSonhosEmojiOfQuantity(BlackjackUtils.MINIMUM_BET),
                                    BlackjackUtils.MINIMUM_BET,
                                    SonhosUtils.getSonhosEmojiOfQuantity(BlackjackUtils.MAXIMUM_BET),
                                    BlackjackUtils.MAXIMUM_BET,
                                )
                            ),
                            Constants.ERROR
                        )
                    }
                    return
                }

                if (totalEarnings > selfUserProfile.money) {
                    context.fail(true) {
                        this.styled(
                            context.locale["commands.command.flipcoinbet.notEnoughMoneySelf"],
                            Constants.ERROR
                        )

                        this.styled(
                            context.i18nContext.get(
                                GACampaigns.sonhosBundlesUpsellDiscordMessage(
                                    loritta.config.loritta.dashboard.url,
                                    "blackjack",
                                    "bet-not-enough-sonhos"
                                )
                            ),
                            Emotes.LORI_RICH.asMention
                        )
                    }
                }
            }

            context.deferChannelMessage(false)

            startBlackjackMatch(context, totalEarnings)
        }

        suspend fun startBlackjackMatch(
            context: UnleashedContext,
            matchBet: Long?
        ) {
            if (SonhosUtils.checkIfEconomyIsDisabled(context))
                return

            val result = loritta.transaction {
                if (matchBet != null) {
                    // Because we NEED the matchId on the transaction, we need to do it in steps
                    val result = SonhosUtils.checkIfUserHasEnoughSonhos(context.user.idLong, matchBet)

                    when (result) {
                        SonhosUtils.SonhosCheckResult.Success -> {
                            val result = loritta.transaction {
                                BlackjackSinglePlayerMatches.insert {
                                    it[BlackjackSinglePlayerMatches.lorittaClusterId] = loritta.clusterId
                                    it[BlackjackSinglePlayerMatches.blackjackManagerUniqueId] = loritta.blackjackManager.uniqueId
                                    it[BlackjackSinglePlayerMatches.user] = context.user.idLong
                                    it[BlackjackSinglePlayerMatches.guild] = context.guildId
                                    it[BlackjackSinglePlayerMatches.channel] = context.channel.idLong
                                    it[BlackjackSinglePlayerMatches.hands] = 1
                                    it[BlackjackSinglePlayerMatches.winningHands] = null
                                    it[BlackjackSinglePlayerMatches.losingHands] = null
                                    it[BlackjackSinglePlayerMatches.tiedHands] = null
                                    it[BlackjackSinglePlayerMatches.paidInsurance] = false
                                    it[BlackjackSinglePlayerMatches.insurancePaidOut] = false
                                    it[BlackjackSinglePlayerMatches.initialBet] = matchBet
                                    it[BlackjackSinglePlayerMatches.bet] = matchBet
                                    it[BlackjackSinglePlayerMatches.paidInsurancePrice] = null
                                    it[BlackjackSinglePlayerMatches.payout] = null
                                    it[BlackjackSinglePlayerMatches.refunded] = false
                                    it[BlackjackSinglePlayerMatches.autoStand] = false
                                    it[BlackjackSinglePlayerMatches.startedAt] = Instant.now()
                                    it[BlackjackSinglePlayerMatches.finishedAt] = null
                                    it[BlackjackSinglePlayerMatches.serializedHands] = null
                                }
                            }

                            Profiles.update({ Profiles.id eq context.user.idLong }) {
                                with(SqlExpressionBuilder) {
                                    it[Profiles.money] = Profiles.money - matchBet
                                }
                            }

                            SimpleSonhosTransactionsLogUtils.insert(
                                context.user.idLong,
                                Instant.now(),
                                TransactionType.BLACKJACK,
                                matchBet,
                                StoredBlackjackJoinedTransaction(result[BlackjackSinglePlayerMatches.id].value),
                            )

                            return@transaction CreateGameResult.Success(result[BlackjackSinglePlayerMatches.id].value)
                        }
                        is SonhosUtils.SonhosCheckResult.NotEnoughSonhos -> {
                            return@transaction CreateGameResult.NotEnoughSonhos
                        }
                    }
                } else {
                    // If the player has not provided a bet, we can just insert things without any values
                    val result = loritta.transaction {
                        BlackjackSinglePlayerMatches.insert {
                            it[BlackjackSinglePlayerMatches.lorittaClusterId] = loritta.clusterId
                            it[BlackjackSinglePlayerMatches.blackjackManagerUniqueId] = loritta.blackjackManager.uniqueId
                            it[BlackjackSinglePlayerMatches.user] = context.user.idLong
                            it[BlackjackSinglePlayerMatches.guild] = context.guildId
                            it[BlackjackSinglePlayerMatches.channel] = context.channel.idLong
                            it[BlackjackSinglePlayerMatches.hands] = 1
                            it[BlackjackSinglePlayerMatches.winningHands] = null
                            it[BlackjackSinglePlayerMatches.losingHands] = null
                            it[BlackjackSinglePlayerMatches.tiedHands] = null
                            it[BlackjackSinglePlayerMatches.initialBet] = null
                            it[BlackjackSinglePlayerMatches.paidInsurance] = false
                            it[BlackjackSinglePlayerMatches.insurancePaidOut] = false
                            it[BlackjackSinglePlayerMatches.bet] = null
                            it[BlackjackSinglePlayerMatches.paidInsurancePrice] = null
                            it[BlackjackSinglePlayerMatches.payout] = null
                            it[BlackjackSinglePlayerMatches.refunded] = false
                            it[BlackjackSinglePlayerMatches.autoStand] = false
                            it[BlackjackSinglePlayerMatches.startedAt] = Instant.now()
                            it[BlackjackSinglePlayerMatches.finishedAt] = null
                            it[BlackjackSinglePlayerMatches.serializedHands] = null
                        }
                    }
                    return@transaction CreateGameResult.Success(result[BlackjackSinglePlayerMatches.id].value)
                }
            }

            when (result) {
                is CreateGameResult.Success -> {
                    val matchId = BlackjackMatchId(result.matchId)
                    val blackjack = Blackjack(loritta.random)
                    val titleSuit = SUIT_EMOJIS.entries.random().value
                    val mutex = Mutex()

                    // We do not mind the lateinit, the only place that this is called before the init is within the callback blocks, and they
                    // won't be called BEFORE this is initialized anyway
                    lateinit var buttons: BlackjackButtons

                    val hitButton = loritta.interactivityManager.buttonForUser(
                        context.user,
                        context.alwaysEphemeral,
                        UnleashedButton.of(
                            ButtonStyle.PRIMARY,
                            context.i18nContext.get(I18N_PREFIX.Play.Buttons.Hit)
                        )
                    ) {
                        // We use deferEditAsync for a "smoother" experience
                        // Because we don't need to wait Discord to acknowledge the edit, we just go ahead and process the things
                        val hook = it.deferEditAsync()

                        mutex.withLock {
                            if (blackjack.gameState !is Blackjack.GameState.PlayerTurn) {
                                context.reply(false) {
                                    styled(
                                        context.i18nContext.get(I18N_PREFIX.Play.Errors.CannotDoThisNow)
                                    )
                                }
                                return@buttonForUser
                            }

                            if (blackjack.activePlayerHand.isStanding) {
                                context.reply(false) {
                                    styled(
                                        context.i18nContext.get(I18N_PREFIX.Play.Errors.CannotHitBecauseYouAreStanding)
                                    )
                                }
                                return@buttonForUser
                            }

                            blackjack.hit()
                            val nextAction = blackjack.processHandAfterAction()

                            when (nextAction) {
                                Blackjack.HandProgressionResult.AllHandsBusted -> {
                                    finishBlackjackAndProcessHandsPayouts(context, blackjack, matchId, matchBet, false)

                                    hook
                                        .await()
                                        .editMessage {
                                            createMessage(it, blackjack, titleSuit, matchId, matchBet, buttons)
                                        }
                                        .await()
                                }

                                Blackjack.HandProgressionResult.Continue, Blackjack.HandProgressionResult.MovedToNextHand -> {
                                    hook
                                        .await()
                                        .editMessage {
                                            createMessage(it, blackjack, titleSuit, matchId, matchBet, buttons)
                                        }
                                        .await()
                                }

                                Blackjack.HandProgressionResult.StartDealerRound -> {
                                    startDealerRound(it, blackjack, titleSuit, hook.await(), matchId, matchBet, buttons, mutex)
                                }
                            }
                        }
                    }

                    val standButton = loritta.interactivityManager.buttonForUser(
                        context.user,
                        context.alwaysEphemeral,
                        UnleashedButton.of(
                            ButtonStyle.PRIMARY,
                            context.i18nContext.get(I18N_PREFIX.Play.Buttons.Stand)
                        )
                    ) {
                        val hook = it.deferEditAsync()

                        mutex.withLock {
                            if (blackjack.gameState !is Blackjack.GameState.PlayerTurn) {
                                context.reply(false) {
                                    styled(
                                        context.i18nContext.get(I18N_PREFIX.Play.Errors.CannotDoThisNow)
                                    )
                                }
                                return@buttonForUser
                            }

                            if (blackjack.activePlayerHand.isStanding) {
                                context.reply(false) {
                                    styled(
                                        context.i18nContext.get(I18N_PREFIX.Play.Errors.AlreadyStanding)
                                    )
                                }
                                return@buttonForUser
                            }

                            blackjack.activePlayerHand.setStanding()

                            val nextAction = blackjack.processHandAfterAction()

                            when (nextAction) {
                                Blackjack.HandProgressionResult.AllHandsBusted -> {
                                    finishBlackjackAndProcessHandsPayouts(context, blackjack, matchId, matchBet, false)

                                    hook.await().editMessage {
                                        createMessage(it, blackjack, titleSuit, matchId, matchBet, buttons)
                                    }.await()
                                }

                                Blackjack.HandProgressionResult.Continue, Blackjack.HandProgressionResult.MovedToNextHand -> {
                                    hook.await().editMessage {
                                        createMessage(it, blackjack, titleSuit, matchId, matchBet, buttons)
                                    }.await()
                                }

                                Blackjack.HandProgressionResult.StartDealerRound -> {
                                    startDealerRound(it, blackjack, titleSuit, hook.await(), matchId, matchBet, buttons, mutex)
                                }
                            }
                        }
                    }

                    val doubleDownButton = loritta.interactivityManager.buttonForUser(
                        context.user,
                        context.alwaysEphemeral,
                        UnleashedButton.of(
                            ButtonStyle.PRIMARY,
                            context.i18nContext.get(I18N_PREFIX.Play.Buttons.DoubleDown)
                        )
                    ) {
                        val hook = it.deferEditAsync()

                        mutex.withLock {
                            if (blackjack.gameState !is Blackjack.GameState.PlayerTurn) {
                                context.reply(false) {
                                    styled(
                                        context.i18nContext.get(I18N_PREFIX.Play.Errors.CannotDoThisNow)
                                    )
                                }
                                return@buttonForUser
                            }

                            if (blackjack.activePlayerHand.wasDoubledDown) {
                                context.reply(false) {
                                    styled(
                                        context.i18nContext.get(I18N_PREFIX.Play.Errors.AlreadyDoubledDown)
                                    )
                                }
                                return@buttonForUser
                            }

                            if (!blackjack.canDoubleDown()) {
                                context.reply(false) {
                                    styled(
                                        context.i18nContext.get(I18N_PREFIX.Play.Errors.CannotDoubleDown)
                                    )
                                }
                                return@buttonForUser
                            }

                            // Check if user has enough money to double down
                            val result = if (matchBet != null) {
                                loritta.transaction {
                                    return@transaction SonhosUtils.takeSonhosAndLogToTransactionLogAndReturn(
                                        context.user.idLong,
                                        matchBet,
                                        TransactionType.BLACKJACK,
                                        StoredBlackjackDoubleDownTransaction(matchId.matchId),
                                        {
                                            return@takeSonhosAndLogToTransactionLogAndReturn DoubleDownResult.NotEnoughSonhos
                                        }
                                    ) {
                                        // Increase the match bet on the database too
                                        BlackjackSinglePlayerMatches.update({ BlackjackSinglePlayerMatches.id eq matchId.matchId }) {
                                            it[BlackjackSinglePlayerMatches.bet] = BlackjackSinglePlayerMatches.bet + matchBet
                                        }
                                        return@takeSonhosAndLogToTransactionLogAndReturn DoubleDownResult.Success
                                    }
                                }
                            } else {
                                DoubleDownResult.Success
                            }

                            when (result) {
                                DoubleDownResult.Success -> {
                                    blackjack.doubleDown()

                                    val nextAction = blackjack.processHandAfterAction()

                                    when (nextAction) {
                                        Blackjack.HandProgressionResult.AllHandsBusted -> {
                                            finishBlackjackAndProcessHandsPayouts(context, blackjack, matchId, matchBet, false)

                                            hook.await().editMessage {
                                                createMessage(it, blackjack, titleSuit, matchId, matchBet, buttons)
                                            }.await()
                                        }

                                        Blackjack.HandProgressionResult.Continue, Blackjack.HandProgressionResult.MovedToNextHand -> {
                                            hook.await().editMessage {
                                                createMessage(it, blackjack, titleSuit, matchId, matchBet, buttons)
                                            }.await()
                                        }

                                        Blackjack.HandProgressionResult.StartDealerRound -> {
                                            startDealerRound(it, blackjack, titleSuit, hook.await(), matchId, matchBet, buttons, mutex)
                                        }
                                    }
                                }

                                DoubleDownResult.NotEnoughSonhos -> {
                                    context.reply(true) {
                                        styled(
                                            context.i18nContext.get(I18N_PREFIX.Play.Errors.NotEnoughSonhosDoubleDown)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val splitButton = loritta.interactivityManager.buttonForUser(
                        context.user,
                        context.alwaysEphemeral,
                        UnleashedButton.of(
                            ButtonStyle.PRIMARY,
                            context.i18nContext.get(I18N_PREFIX.Play.Buttons.Split)
                        )
                    ) {
                        val hook = it.deferEditAsync()

                        mutex.withLock {
                            if (blackjack.gameState !is Blackjack.GameState.PlayerTurn) {
                                context.reply(false) {
                                    styled(
                                        context.i18nContext.get(I18N_PREFIX.Play.Errors.CannotDoThisNow)
                                    )
                                }
                                return@buttonForUser
                            }

                            if (!blackjack.isSplitAvailable()) {
                                context.reply(false) {
                                    styled(
                                        context.i18nContext.get(I18N_PREFIX.Play.Errors.CannotSplit)
                                    )
                                }
                                return@buttonForUser
                            }

                            // Check if user has enough money to split
                            val result = if (matchBet != null) {
                                loritta.transaction {
                                    return@transaction SonhosUtils.takeSonhosAndLogToTransactionLogAndReturn(
                                        context.user.idLong,
                                        matchBet,
                                        TransactionType.BLACKJACK,
                                        StoredBlackjackSplitTransaction(matchId.matchId),
                                        {
                                            return@takeSonhosAndLogToTransactionLogAndReturn SplitResult.NotEnoughSonhos
                                        }
                                    ) {
                                        // Increase the match bet + hands on the database too
                                        BlackjackSinglePlayerMatches.update({ BlackjackSinglePlayerMatches.id eq matchId.matchId }) {
                                            it[BlackjackSinglePlayerMatches.bet] = BlackjackSinglePlayerMatches.bet + matchBet
                                            it[BlackjackSinglePlayerMatches.hands] = BlackjackSinglePlayerMatches.hands + 1
                                        }
                                        return@takeSonhosAndLogToTransactionLogAndReturn SplitResult.Success
                                    }
                                }
                            } else {
                                SplitResult.Success
                            }

                            when (result) {
                                SplitResult.Success -> {
                                    blackjack.splitHand()

                                    hook.await().editMessage {
                                        createMessage(it, blackjack, titleSuit, matchId, matchBet, buttons)
                                    }.await()
                                }

                                SplitResult.NotEnoughSonhos -> {
                                    context.reply(true) {
                                        styled(
                                            context.i18nContext.get(I18N_PREFIX.Play.Errors.NotEnoughSonhosSplit)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val payInsuranceButton = loritta.interactivityManager.buttonForUser(
                        context.user,
                        context.alwaysEphemeral,
                        UnleashedButton.of(
                            ButtonStyle.PRIMARY,
                            context.i18nContext.get(I18N_PREFIX.Play.Buttons.PayInsurance)
                        )
                    ) {
                        val hook = it.deferEditAsync()

                        mutex.withLock {
                            if (blackjack.gameState !is Blackjack.GameState.PlayerTurn) {
                                context.reply(false) {
                                    styled(
                                        context.i18nContext.get(I18N_PREFIX.Play.Errors.CannotDoThisNow)
                                    )
                                }
                                return@buttonForUser
                            }

                            if (!blackjack.isInsuranceAvailable()) {
                                context.reply(false) {
                                    styled(
                                        context.i18nContext.get(I18N_PREFIX.Play.Errors.InsuranceNotAvailable)
                                    )
                                }
                                return@buttonForUser
                            }

                            val dealerHasNaturalBlackjack = blackjack.dealerHand.hasNaturalBlackjack()

                            // We DO NOT need to flip the card! It should only be flipped if the dealer HAS a blackjack (which, in this case, the match ends)

                            // To avoid any weird code, we'll process everything here
                            // That does mean that the process hand code will only return a "Loss", but it is ok
                            val result = if (matchBet != null) {
                                val insuranceValue = ceil(matchBet.toDouble() / 2).toLong()
                                val valueIfRight = (matchBet + insuranceValue) // You break even if you were right

                                loritta.transaction {
                                    val result = SonhosUtils.takeSonhosAndLogToTransactionLogAndReturn(
                                        context.user.idLong,
                                        insuranceValue,
                                        TransactionType.BLACKJACK,
                                        StoredBlackjackInsuranceTransaction(matchId.matchId),
                                        {
                                            return@takeSonhosAndLogToTransactionLogAndReturn InsuranceBuyResult.NotEnoughSonhos
                                        }
                                    ) {
                                        // Increase the match bet on the database too
                                        BlackjackSinglePlayerMatches.update({ BlackjackSinglePlayerMatches.id eq matchId.matchId }) {
                                            it[BlackjackSinglePlayerMatches.bet] = BlackjackSinglePlayerMatches.bet + insuranceValue
                                            it[BlackjackSinglePlayerMatches.paidInsurancePrice] = insuranceValue
                                        }
                                        return@takeSonhosAndLogToTransactionLogAndReturn InsuranceBuyResult.Success
                                    }

                                    when (result) {
                                        InsuranceBuyResult.Success -> {
                                            if (dealerHasNaturalBlackjack) {
                                                Profiles.update({ Profiles.id eq context.user.idLong }) {
                                                    it[Profiles.money] = Profiles.money + valueIfRight
                                                }

                                                SimpleSonhosTransactionsLogUtils.insert(
                                                    context.user.idLong,
                                                    Instant.now(),
                                                    TransactionType.BLACKJACK,
                                                    valueIfRight,
                                                    StoredBlackjackInsurancePayoutTransaction(matchId.matchId)
                                                )

                                                // Technically we always "lose" during insurance, but we can get our money back
                                                finishBlackjack(matchId, valueIfRight, blackjack.playerHands.size, 0, 0, 1, blackjack.paidInsurance, true, autoStand = false, dealerHand = blackjack.dealerHand, playerHands = blackjack.playerHands)

                                                return@transaction InsuranceProcessResult.PlayerInsured
                                            } else {
                                                // Continue...
                                                return@transaction InsuranceProcessResult.PlayerNotInsured
                                            }
                                        }

                                        InsuranceBuyResult.NotEnoughSonhos -> return@transaction InsuranceProcessResult.NotEnoughSonhos
                                    }
                                }
                            } else {
                                if (dealerHasNaturalBlackjack) {
                                    InsuranceProcessResult.PlayerInsured
                                } else {
                                    // Continue...
                                    InsuranceProcessResult.PlayerNotInsured
                                }
                            }

                            when (result) {
                                InsuranceProcessResult.NotEnoughSonhos -> {
                                    context.reply(true) {
                                        styled(
                                            context.i18nContext.get(I18N_PREFIX.Play.Errors.NotEnoughSonhosInsurance)
                                        )
                                    }
                                }

                                InsuranceProcessResult.PlayerInsured -> {
                                    blackjack.setInsuranceAsPaid()

                                    blackjack.startDealerRound()
                                    blackjack.endDealerRound()

                                    // They won! I mean, they paid the insurance and won... which is like, half winning i think
                                    hook.await().editMessage {
                                        createMessage(it, blackjack, titleSuit, matchId, matchBet, buttons)
                                    }.await()
                                }

                                InsuranceProcessResult.PlayerNotInsured -> {
                                    blackjack.setInsuranceAsPaid()

                                    // They didn't win anything... continue the game as is
                                    hook.await().editMessage {
                                        createMessage(it, blackjack, titleSuit, matchId, matchBet, buttons)
                                    }.await()
                                }
                            }
                        }
                    }

                    val howToPlayButton = loritta.interactivityManager.buttonForUser(
                        context.user,
                        context.alwaysEphemeral,
                        UnleashedButton.of(
                            ButtonStyle.SECONDARY,
                            context.i18nContext.get(I18N_PREFIX.Play.Buttons.HowToPlay)
                        )
                    ) {
                        it.reply(true) {
                            createBlackjackTutorialMessage(loritta, context.i18nContext)
                        }
                    }

                    val houseRulesButton = loritta.interactivityManager.buttonForUser(
                        context.user,
                        context.alwaysEphemeral,
                        UnleashedButton.of(
                            ButtonStyle.SECONDARY,
                            context.i18nContext.get(I18N_PREFIX.Play.Buttons.HouseRules)
                        )
                    ) {
                        it.reply(true) {
                            createBlackjackHouseRulesMessage(loritta, context.i18nContext)
                        }
                    }

                    buttons = BlackjackButtons(
                        hitButton,
                        standButton,
                        doubleDownButton,
                        splitButton,
                        payInsuranceButton,
                        howToPlayButton,
                        houseRulesButton
                    )

                    if (blackjack.gameState is Blackjack.GameState.GameOver) {
                        // Ooohh, if the game is already over, probably the player got a natural blackjack on first try! Let's process all hands
                        finishBlackjackAndProcessHandsPayouts(context, blackjack, matchId, matchBet, false)
                    }

                    val message = context.reply(false) {
                        this.useComponentsV2 = true

                        createMessage(context, blackjack, titleSuit, matchId, matchBet, buttons)
                    }

                    if (blackjack.gameState !is Blackjack.GameState.GameOver) {
                        // Start the auto stand job
                        // We need to start it here instead of before creating the message because we NEED to update the original message
                        val autoStandJob = GlobalScope.launch {
                            delay(BlackjackUtils.AUTO_STAND_DELAY)

                            mutex.withLock {
                                if (blackjack.gameState is Blackjack.GameState.PlayerTurn) {
                                    // If we are still on the player's turn, we'll just stop the match
                                    // Stand all hands that are not yet standing
                                    while (blackjack.gameState is Blackjack.GameState.PlayerTurn) {
                                        blackjack.activePlayerHand.setStanding()
                                        val result = blackjack.processHandAfterAction()

                                        if (result == Blackjack.HandProgressionResult.StartDealerRound)
                                            break
                                    }

                                    // Start and complete the dealer round
                                    blackjack.startDealerRound()
                                    while (true) {
                                        val dealerResult = blackjack.executeDealerRound()
                                        if (dealerResult == Blackjack.DealerLogicResult.Stop)
                                            break
                                    }
                                    blackjack.endDealerRound()

                                    // Process payouts and finish the match
                                    finishBlackjackAndProcessHandsPayouts(context, blackjack, matchId, matchBet, true)

                                    // Update the message to show the game ended due to timeout
                                    message.editMessage {
                                        createMessage(
                                            context,
                                            blackjack,
                                            titleSuit,
                                            matchId,
                                            matchBet,
                                            buttons
                                        )
                                    }

                                    // Send the reply, we include the matchId because we don't have access to the jumpUrl
                                    context.reply(true) {
                                        styled(
                                            context.i18nContext.get(I18N_PREFIX.Play.MatchEndedAutomaticallyDueToInactivity(matchId.matchId)),
                                            "⏰"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                CreateGameResult.NotEnoughSonhos -> {
                    context.fail(true) {
                        this.styled(
                            context.locale["commands.command.flipcoinbet.notEnoughMoneySelf"],
                            Constants.ERROR
                        )

                        this.styled(
                            context.i18nContext.get(
                                GACampaigns.sonhosBundlesUpsellDiscordMessage(
                                    loritta.config.loritta.dashboard.url,
                                    "blackjack",
                                    "bet-not-enough-sonhos"
                                )
                            ),
                            Emotes.LORI_RICH.asMention
                        )
                    }
                }
            }
        }

        // TODO: How could we make this better? The constant shuffling between calculating the results is a bit annoying-
        fun InlineMessage<*>.createMessage(
            context: UnleashedContext,
            blackjack: Blackjack,
            titleSuit: LorittaEmojiReference,
            matchId: BlackjackMatchId,
            matchBet: Long?,
            buttons: BlackjackButtons
        ) {
            this.useComponentsV2 = true

            // Useful to know that this is FOR YOU!!
            text(context.user.asMention)

            val dealerScore = blackjack.dealerHand.calculateScore(true)

            val handStates = blackjack.getHandStateForAllHands()
            val anyHandWon = handStates.any { it.value is Blackjack.HandState.Win }
            val anyHandPushed = handStates.any { it.value is Blackjack.HandState.Push }

            this.components += Container {
                this.accentColorRaw = when (blackjack.gameState) {
                    Blackjack.GameState.DealerTurn -> {
                        LorittaColors.BlackjackDealerTurn.rgb
                    }
                    Blackjack.GameState.PlayerTurn -> {
                        LorittaColors.BlackjackPlayerTurn.rgb
                    }
                    Blackjack.GameState.GameOver -> {
                        if (anyHandWon) {
                            LorittaColors.BlackjackWon.rgb
                        } else if (anyHandPushed) {
                            LorittaColors.BlackjackYellow.rgb
                        } else {
                            LorittaColors.BlackjackLost.rgb
                        }
                    }
                }

                this.section(
                    Thumbnail("https://cdn.discordapp.com/attachments/739823666891849729/1437848679875612915/Discord_CN6EeYtMio.png?ex=6914bc6f&is=69136aef&hm=21de8f039b818dc2cfd9ccd738810454c7d1655d18f4aa8ffd5bb66fafedcf40&")
                ) {
                    this.text(
                        buildString {
                            append("## ${loritta.emojiManager.get(titleSuit)} Blackjack")

                            appendLine()

                            when (blackjack.gameState) {
                                Blackjack.GameState.PlayerTurn -> {
                                    if (blackjack.isInsuranceAvailable()) {
                                        append("${loritta.emojiManager.get(LorittaEmojis.LoriComfy)} *${context.i18nContext.get(I18N_PREFIX.Play.DealerMessages.YourTurnInsurance)}*")
                                    } else if (blackjack.paidInsurance) {
                                        append("${loritta.emojiManager.get(LorittaEmojis.LoriComfy)} *${context.i18nContext.get(I18N_PREFIX.Play.DealerMessages.YourTurnInsurancePaid)}*")
                                    } else {
                                        append("${loritta.emojiManager.get(LorittaEmojis.LoriComfy)} *${context.i18nContext.get(I18N_PREFIX.Play.DealerMessages.YourTurn)}*")
                                    }
                                }

                                Blackjack.GameState.DealerTurn -> {
                                    append("${loritta.emojiManager.get(LorittaEmojis.LoriExpressionless)} *${context.i18nContext.get(I18N_PREFIX.Play.DealerMessages.DealerTurn)}*")
                                }

                                Blackjack.GameState.GameOver -> {
                                    if (anyHandWon) {
                                        append("${loritta.emojiManager.get(LorittaEmojis.LoriAchocolatado)} *${context.i18nContext.get(I18N_PREFIX.Play.DealerMessages.YouWon)}*")
                                    } else if (anyHandPushed) {
                                        append("${loritta.emojiManager.get(LorittaEmojis.LoriCoffee)} *${context.i18nContext.get(I18N_PREFIX.Play.DealerMessages.Tied)}*")
                                    } else {
                                        append("${loritta.emojiManager.get(LorittaEmojis.LoriSmug)} ")
                                        val messages = context.i18nContext.get(I18N_PREFIX.Play.DealerMessages.YouLost)
                                        append("*${messages.random()}*")
                                    }
                                }
                            }

                            appendLine()
                            appendLine()

                            var idx = 0
                            for ((hand, handState) in handStates) {
                                if (idx != 0)
                                    appendLine()

                                append("**${context.i18nContext.get(I18N_PREFIX.Play.HandStatus.Hand(idx + 1))}:** ")
                                when (handState) {
                                    Blackjack.HandState.Busted -> append(context.i18nContext.get(I18N_PREFIX.Play.HandStatus.Busted))
                                    Blackjack.HandState.InProgress -> append(context.i18nContext.get(I18N_PREFIX.Play.HandStatus.InProgress))
                                    Blackjack.HandState.Push -> {
                                        append(context.i18nContext.get(I18N_PREFIX.Play.HandStatus.Tied))
                                        if (matchBet != null) {
                                            val payoutValue = calculateHandPayout(matchBet, hand, handState)

                                            append(" (${context.i18nContext.get(I18N_PREFIX.Play.HandStatus.SonhosReward(SonhosUtils.getSonhosEmojiOfQuantity(payoutValue), payoutValue))})")
                                        }
                                    }
                                    is Blackjack.HandState.Lose -> {

                                        if (blackjack.paidInsurance && handState.isNaturalBlackjack) {
                                            append(context.i18nContext.get(I18N_PREFIX.Play.HandStatus.LostButInsured))

                                            if (matchBet != null) {
                                                val insuranceValue = ceil(matchBet.toDouble() / 2).toLong()
                                                val valueIfRight = (matchBet + insuranceValue) // You break even if you were right

                                                append(" (${context.i18nContext.get(I18N_PREFIX.Play.HandStatus.SonhosReward(SonhosUtils.getSonhosEmojiOfQuantity(valueIfRight), valueIfRight))})")
                                            }
                                        } else {
                                            append(context.i18nContext.get(I18N_PREFIX.Play.HandStatus.Lost))
                                        }
                                    }

                                    is Blackjack.HandState.Win -> {
                                        append(context.i18nContext.get(I18N_PREFIX.Play.HandStatus.Won))
                                        if (matchBet != null) {
                                            val payoutValue = calculateHandPayout(matchBet, hand, handState)

                                            append(" (${context.i18nContext.get(I18N_PREFIX.Play.HandStatus.SonhosReward(SonhosUtils.getSonhosEmojiOfQuantity(payoutValue), payoutValue))})")
                                        }
                                    }
                                }
                                idx++
                            }

                            appendLine()

                            append("### ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriCard} ${context.i18nContext.get(I18N_PREFIX.Play.Entities.Loritta)} ")
                            append("(")
                            append(formatScore(dealerScore))
                            append(")")
                            append(":")
                            appendLine()
                            for (card in blackjack.dealerHand.cards) {
                                append("${BlackjackUtils.createAsEmoji(loritta, card)} ")
                            }

                            // Add additional "padding" between Loritta's hands and our hands
                            appendLine()
                            appendLine()

                            for ((index, hand) in blackjack.playerHands.withIndex()) {
                                if (index != 0)
                                    appendLine()

                                val playerScore = hand.calculateScore(true)

                                append("### ")
                                if (blackjack.activePlayerHand == hand) {
                                    append("\uD83C\uDCCF")
                                } else {
                                    append(loritta.emojiManager.get(LorittaEmojis.Nothing))
                                }
                                append(" ${context.i18nContext.get(I18N_PREFIX.Play.Entities.You)} ")
                                append("(")
                                append(formatScore(playerScore))
                                append(")")
                                if (hand.wasDoubledDown) {
                                    append(" [${context.i18nContext.get(I18N_PREFIX.Play.HandStatus.Doubled)}]")
                                }
                                append(":")
                                appendLine()
                                for (card in hand.cards) {
                                    append("${BlackjackUtils.createAsEmoji(loritta, card)} ")
                                }
                            }
                        }
                    )
                }

                if (blackjack.gameState is Blackjack.GameState.PlayerTurn) {
                    this.actionRow(
                        buttons.hitButton,
                        buttons.standButton,
                        if (blackjack.canDoubleDown()) buttons.doubleDownButton else buttons.doubleDownButton.asDisabled(),
                        if (blackjack.isSplitAvailable()) buttons.splitButton else buttons.splitButton.asDisabled(),
                        if (blackjack.isInsuranceAvailable()) buttons.payInsuranceButton else buttons.payInsuranceButton.asDisabled(),
                    )
                } else {
                    this.actionRow(
                        buttons.hitButton.asDisabled(),
                        buttons.standButton.asDisabled(),
                        buttons.doubleDownButton.asDisabled(),
                        buttons.splitButton.asDisabled(),
                        buttons.payInsuranceButton.asDisabled(),
                    )
                }

                this.text(
                    buildString {
                        appendLine("-# Loritta para em soft 17, Blackjack paga 6:5")
                        appendLine("-# ${context.i18nContext.get(I18N_PREFIX.Play.MatchId(matchId.matchId))}")
                    }
                )
            }

            if (blackjack.gameState is Blackjack.GameState.GameOver) {
                this.actionRow(
                    loritta.interactivityManager.buttonForUser(
                        context.user,
                        context.alwaysEphemeral,
                        ButtonStyle.SUCCESS,
                        context.i18nContext.get(I18N_PREFIX.Play.StartNewMatch),
                    ) {
                        it.deferChannelMessage(context.wasInitiallyDeferredEphemerally == true)
                        startBlackjackMatch(it, matchBet)
                    }
                )
            } else {
                this.actionRow(
                    buttons.howToPlayButton,
                    buttons.houseRulesButton
                )
            }
        }

        /**
         * Starts the dealer logic
         */
        suspend fun startDealerRound(
            context: UnleashedContext,
            blackjack: Blackjack,
            titleSuit: LorittaEmojiReference,
            hook: InteractionHook,
            matchId: BlackjackMatchId,
            matchBet: Long?,
            buttons: BlackjackButtons,
            mutex: Mutex
        ) {
            blackjack.startDealerRound()

            hook.editMessage {
                createMessage(context, blackjack, titleSuit, matchId, matchBet, buttons)
            }.await()

            // Add an initial delay to make it a bit nicer
            delay(BlackjackUtils.DEALER_ACTION_DELAY)

            while (true) {
                val result = blackjack.executeDealerRound()

                when (result) {
                    Blackjack.DealerLogicResult.Continue -> {
                        hook.editMessage {
                            createMessage(context, blackjack, titleSuit, matchId, matchBet, buttons)
                        }.await()
                    }
                    Blackjack.DealerLogicResult.Stop -> {
                        blackjack.endDealerRound()

                        finishBlackjackAndProcessHandsPayouts(context, blackjack, matchId, matchBet, false)

                        hook.editMessage {
                            createMessage(context, blackjack, titleSuit, matchId, matchBet, buttons)
                        }.await()
                        return
                    }
                }

                delay(BlackjackUtils.DEALER_ACTION_DELAY)
            }
        }

        fun finishBlackjack(
            matchId: BlackjackMatchId,
            payout: Long?,
            hands: Int,
            winningHands: Int,
            tiedHands: Int,
            losingHands: Int,
            paidInsurance: Boolean,
            insurancePaidOut: Boolean,
            autoStand: Boolean,
            dealerHand: Hand,
            playerHands: List<Hand>
        ) {
            // Mark this match as finished!
            BlackjackSinglePlayerMatches.update({ BlackjackSinglePlayerMatches.id eq matchId.matchId }) {
                it[BlackjackSinglePlayerMatches.finishedAt] = Instant.now()
                if (payout != null) {
                    it[BlackjackSinglePlayerMatches.payout] = payout
                }
                it[BlackjackSinglePlayerMatches.hands] = hands
                it[BlackjackSinglePlayerMatches.winningHands] = winningHands
                it[BlackjackSinglePlayerMatches.tiedHands] = tiedHands
                it[BlackjackSinglePlayerMatches.losingHands] = losingHands
                it[BlackjackSinglePlayerMatches.paidInsurance] = paidInsurance
                it[BlackjackSinglePlayerMatches.insurancePaidOut] = insurancePaidOut
                it[BlackjackSinglePlayerMatches.autoStand] = autoStand
                it[BlackjackSinglePlayerMatches.serializedHands] = Json.encodeToString(BlackjackUtils.convertToSerializableStateHands(dealerHand, playerHands))
            }
        }

        suspend fun finishBlackjackAndProcessHandsPayouts(
            context: UnleashedContext,
            blackjack: Blackjack,
            matchId: BlackjackMatchId,
            matchBet: Long?,
            autoStand: Boolean
        ) {
            val handsStatus = blackjack.getHandStateForAllHands()

            loritta.transaction {
                var winningHands = 0
                var tiedHands = 0
                var losingHands = 0
                if (matchBet != null) {
                    // Used to update the final blackjack match result
                    var payout = 0L

                    for ((hand, handState) in handsStatus) {
                        when (handState) {
                            Blackjack.HandState.InProgress -> {}
                            Blackjack.HandState.Busted, is Blackjack.HandState.Lose -> {
                                losingHands++
                            }
                            Blackjack.HandState.Push -> {
                                tiedHands++

                                // It was a tie!
                                val payoutValue = calculateHandPayout(
                                    matchBet,
                                    hand,
                                    handState
                                )

                                Profiles.update({ Profiles.id eq context.user.idLong }) {
                                    it[Profiles.money] = Profiles.money + payoutValue
                                }

                                SimpleSonhosTransactionsLogUtils.insert(
                                    context.user.idLong,
                                    Instant.now(),
                                    TransactionType.BLACKJACK,
                                    payoutValue, // We pay as is
                                    StoredBlackjackTiedTransaction(matchId.matchId)
                                )

                                payout += payoutValue
                            }

                            is Blackjack.HandState.Win -> {
                                winningHands++

                                // We won, yay!!!
                                val payoutValue = calculateHandPayout(
                                    matchBet,
                                    hand,
                                    handState
                                )

                                Profiles.update({ Profiles.id eq context.user.idLong }) {
                                    it[Profiles.money] = Profiles.money + payoutValue
                                }

                                SimpleSonhosTransactionsLogUtils.insert(
                                    context.user.idLong,
                                    Instant.now(),
                                    TransactionType.BLACKJACK,
                                    payoutValue,
                                    StoredBlackjackPayoutTransaction(matchId.matchId)
                                )

                                payout += payoutValue
                            }
                        }
                    }

                    // Mark this match as finished!
                    finishBlackjack(matchId, payout, handsStatus.size, winningHands, tiedHands, losingHands, blackjack.paidInsurance, false, autoStand, blackjack.dealerHand, blackjack.playerHands)
                } else {
                    // Mark this match as finished!
                    finishBlackjack(matchId, null, handsStatus.size, winningHands, tiedHands, losingHands, blackjack.paidInsurance, false, autoStand, blackjack.dealerHand, blackjack.playerHands)
                }
            }
        }

        private fun calculateHandPayout(
            matchBet: Long,
            hand: Hand,
            handState: Blackjack.HandState
        ): Long {
            when (handState) {
                Blackjack.HandState.Busted -> {}
                Blackjack.HandState.InProgress -> {}
                is Blackjack.HandState.Lose -> {}
                Blackjack.HandState.Push -> {
                    // It was a tie!
                    var payoutValue = matchBet

                    // If it was doubled down, we need to add the bet value again
                    if (hand.wasDoubledDown) {
                        payoutValue += matchBet
                    }

                    return payoutValue
                }

                is Blackjack.HandState.Win -> {
                    // We won, yay!!!
                    // First, we will get the REAL value of the bet
                    val totalBet = if (hand.wasDoubledDown) {
                        matchBet * 2
                    } else {
                        matchBet
                    }

                    val ratio = if (hand.hasNaturalBlackjack()) {
                        // If it has a natural Blackjack, we will add 1.2x! (6:5)
                        1.2
                    } else {
                        // If it doesn't... then it is a 1:1
                        1.0
                    }

                    val payoutValue = totalBet + (totalBet * ratio).toLong()

                    return payoutValue
                }
            }

            return 0
        }

        private fun formatScore(score: Hand.HandScore): String = buildString {
            if (score.hasBusted())
                append("~~")

            if (score.minScore == score.maxScore) {
                append("${score.maxScore}")
            } else {
                append(score.minScore)
                append(" ou ")
                if (score.maxScore > 21 && !score.hasBusted()) {
                    append("~~")
                }
                append(score.maxScore)
                if (score.maxScore > 21 && !score.hasBusted()) {
                    append("~~")
                }
            }

            if (score.hasBusted())
                append("~~")
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return mapOf(options.sonhos to args.joinToString(" ").ifEmpty { null })
        }

        sealed class CreateGameResult {
            data class Success(val matchId: Long) : CreateGameResult()
            object NotEnoughSonhos : CreateGameResult()
        }

        sealed class SplitResult {
            object Success : SplitResult()
            object NotEnoughSonhos : SplitResult()
        }

        sealed class InsuranceBuyResult {
            object Success : InsuranceBuyResult()
            object NotEnoughSonhos : InsuranceBuyResult()
        }

        sealed class InsuranceProcessResult {
            object PlayerInsured : InsuranceProcessResult()
            object PlayerNotInsured : InsuranceProcessResult()
            object NotEnoughSonhos : InsuranceProcessResult()
        }

        sealed class DoubleDownResult {
            object Success : DoubleDownResult()
            object NotEnoughSonhos : DoubleDownResult()
        }

        @JvmInline
        value class BlackjackMatchId(val matchId: Long)
    }

    class BlackjackTutorialExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.reply(false) {
                createBlackjackTutorialMessage(loritta, context.i18nContext)
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ) = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }

    class BlackjackHouseRulesExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.reply(false) {
                createBlackjackHouseRulesMessage(loritta, context.i18nContext)
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ) = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }

    class BlackjackStatsExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val user = optionalUser(
                "user",
                I18N_PREFIX.Stats.Options.User.Text
            )
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val user = args[options.user]?.user ?: context.user

            val result = loritta.transaction {
                val countField = BlackjackSinglePlayerMatches.id.count()
                val winningHandsSumField = BlackjackSinglePlayerMatches.winningHands.sum()
                val tiedHandsSumField = BlackjackSinglePlayerMatches.tiedHands.sum()
                val losingHandsSumField = BlackjackSinglePlayerMatches.losingHands.sum()
                val payoutSumField = BlackjackSinglePlayerMatches.payout.sum()

                val handsMatches = BlackjackSinglePlayerMatches.select(
                    countField,
                    winningHandsSumField,
                    tiedHandsSumField,
                    losingHandsSumField,
                    payoutSumField
                )
                    .where {
                        BlackjackSinglePlayerMatches.user eq user.idLong and (BlackjackSinglePlayerMatches.initialBet.isNotNull())
                    }
                    .firstOrNull()

                if (handsMatches != null) {
                    return@transaction Result(
                        handsMatches[countField],
                        handsMatches[winningHandsSumField] ?: 0,
                        handsMatches[tiedHandsSumField] ?: 0,
                        handsMatches[losingHandsSumField] ?: 0,
                        handsMatches[payoutSumField] ?: 0L
                    )
                } else {
                    return@transaction Result(
                        0,
                        0,
                        0,
                        0,
                        0L
                    )
                }
            }

            context.reply(false) {
                this.useComponentsV2 = true

                container {
                    this.text(
                        buildString {
                            appendLine("# ${loritta.emojiManager.get(LorittaEmojis.CardSpades)} ${context.i18nContext.get(I18N_PREFIX.Stats.YourStats)}")
                            appendLine(context.i18nContext.get(I18N_PREFIX.Stats.TotalMatches(result.total)))
                            appendLine(context.i18nContext.get(I18N_PREFIX.Stats.WonHands(result.winningHands)))
                            appendLine(context.i18nContext.get(I18N_PREFIX.Stats.LostHands(result.losingHands)))
                            appendLine(context.i18nContext.get(I18N_PREFIX.Stats.TiedHands(result.tiedHands)))
                            appendLine(context.i18nContext.get(I18N_PREFIX.Stats.SonhosPayout(SonhosUtils.getSonhosEmojiOfQuantity(result.totalPayout), result.totalPayout)))
                        }
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            val userAndMember = context.getUserAndMember(0)

            return mapOf(options.user to userAndMember)
        }

        private data class Result(
            val total: Long,
            val winningHands: Int,
            val tiedHands: Int,
            val losingHands: Int,
            val totalPayout: Long,
        )
    }
}