package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.mines

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.Thumbnail
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.editMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.pudding.tables.MinesSinglePlayerMatches
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.common.utils.*
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import net.perfectdreams.loritta.morenitta.mines.MinesPlayfield
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.NumberUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.serializable.StoredMinesJoinedTransaction
import net.perfectdreams.loritta.serializable.StoredMinesPayoutTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*

class MinesCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Mines
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY, UUID.fromString("410c7bcf-9af9-4b21-86a7-e9bb8df337c2")) {
        this.enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        subcommand(I18N_PREFIX.Play.Label, I18N_PREFIX.Play.Description, UUID.fromString("fd39aecd-80f2-43a5-8e61-fdb4c0fe1e51")) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                this.add("mines")
            }
            executor = MinesPlayExecutor(loritta)
        }

        subcommand(I18N_PREFIX.Stats.Label, I18N_PREFIX.Stats.Description, UUID.fromString("4b24deb2-727c-49dd-9ffc-6f4935e82b7e")) {
            executor = MinesStatsExecutor(loritta)
        }
    }

    class MinesPlayExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val mines = long("mines", I18N_PREFIX.Play.Options.Mines.Text, requiredRange = MinesUtils.ALLOWED_MINES_RANGE_LONG)
            val sonhos = optionalString("sonhos", I18N_PREFIX.Play.Options.Sonhos.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val mines = args[options.mines].toInt()

            val now = ZonedDateTime.now(Constants.LORITTA_TIMEZONE)
            val incentivizeGuildsEndDate = ZonedDateTime.of(2025, 12, 15, 0, 0, 0, 0, Constants.LORITTA_TIMEZONE)
            val memberCount = context.guildOrNull?.memberCount

            if ((memberCount == null || 250 > memberCount) && incentivizeGuildsEndDate > now && context.guildOrNull?.idLong != 268353819409252352 && context.channelOrNull?.idLong != 1169984595131904010 && context.channelOrNull?.idLong != 1326327003535773757L && context.channelOrNull?.idLong != 1082340413156892682L && context.channelOrNull?.idLong != 691041345275691021L) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(
                            I18N_PREFIX.Play.OnlyAvailableOnGuildsForNow(
                                250,
                                DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(incentivizeGuildsEndDate.toInstant())
                            )
                        ),
                        net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriHeart
                    )
                }
                return
            }

            if (mines !in MinesUtils.ALLOWED_MINES_RANGE) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Play.InvalidMines(MinesUtils.ALLOWED_MINES_RANGE.first, MinesUtils.ALLOWED_MINES_RANGE.last))
                    )
                }
                return
            }

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
                if (0 >= totalEarnings) {
                    context.reply(true) {
                        styled(
                            context.locale["commands.command.flipcoinbet.zeroMoney"],
                            Constants.ERROR
                        )
                    }
                    return
                }

                if (totalEarnings !in MinesUtils.MINIMUM_BET..MinesUtils.MAXIMUM_BET) {
                    context.reply(true) {
                        styled(
                            context.i18nContext.get(
                                I18N_PREFIX.Play.InvalidBet(
                                    SonhosUtils.getSonhosEmojiOfQuantity(MinesUtils.MINIMUM_BET),
                                    MinesUtils.MINIMUM_BET,
                                    SonhosUtils.getSonhosEmojiOfQuantity(MinesUtils.MAXIMUM_BET),
                                    MinesUtils.MAXIMUM_BET,
                                )
                            ),
                            Constants.ERROR
                        )
                    }
                    return
                }

                if (totalEarnings > selfUserProfile.money) {
                    context.reply(true) {
                        this.styled(
                            context.locale["commands.command.flipcoinbet.notEnoughMoneySelf"],
                            Constants.ERROR
                        )

                        this.styled(
                            context.i18nContext.get(
                                GACampaigns.sonhosBundlesUpsellDiscordMessage(
                                    loritta.config.loritta.dashboard.url,
                                    "mines",
                                    "bet-not-enough-sonhos"
                                )
                            ),
                            Emotes.LORI_RICH.asMention
                        )
                    }
                    return
                }
            }

            context.deferChannelMessage(context.wasInitiallyDeferredEphemerally == true)

            startMinesMatch(
                context,
                mines,
                totalEarnings
            )
        }

        suspend fun startMinesMatch(
            context: UnleashedContext,
            totalMines: Int,
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
                                MinesSinglePlayerMatches.insert {
                                    it[MinesSinglePlayerMatches.lorittaClusterId] = loritta.clusterId
                                    it[MinesSinglePlayerMatches.minesManagerUniqueId] = loritta.minesManager.uniqueId
                                    it[MinesSinglePlayerMatches.user] = context.user.idLong
                                    it[MinesSinglePlayerMatches.guild] = context.guildId
                                    it[MinesSinglePlayerMatches.channel] = context.channel.idLong
                                    it[MinesSinglePlayerMatches.bet] = matchBet
                                    it[MinesSinglePlayerMatches.totalMines] = totalMines
                                    it[MinesSinglePlayerMatches.pickedTiles] = null
                                    it[MinesSinglePlayerMatches.payout] = null
                                    it[MinesSinglePlayerMatches.refunded] = false
                                    it[MinesSinglePlayerMatches.autoStand] = false
                                    it[MinesSinglePlayerMatches.startedAt] = Instant.now()
                                    it[MinesSinglePlayerMatches.finishedAt] = null
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
                                TransactionType.MINES,
                                matchBet,
                                StoredMinesJoinedTransaction(result[MinesSinglePlayerMatches.id].value),
                            )

                            return@transaction CreateGameResult.Success(result[MinesSinglePlayerMatches.id].value)
                        }
                        is SonhosUtils.SonhosCheckResult.NotEnoughSonhos -> {
                            return@transaction CreateGameResult.NotEnoughSonhos
                        }
                    }
                } else {
                    // If the player has not provided a bet, we can just insert things without any values
                    val result = loritta.transaction {
                        MinesSinglePlayerMatches.insert {
                            it[MinesSinglePlayerMatches.lorittaClusterId] = loritta.clusterId
                            it[MinesSinglePlayerMatches.minesManagerUniqueId] = loritta.minesManager.uniqueId
                            it[MinesSinglePlayerMatches.user] = context.user.idLong
                            it[MinesSinglePlayerMatches.guild] = context.guildId
                            it[MinesSinglePlayerMatches.channel] = context.channel.idLong
                            it[MinesSinglePlayerMatches.bet] = null
                            it[MinesSinglePlayerMatches.totalMines] = totalMines
                            it[MinesSinglePlayerMatches.pickedTiles] = null
                            it[MinesSinglePlayerMatches.payout] = null
                            it[MinesSinglePlayerMatches.refunded] = false
                            it[MinesSinglePlayerMatches.autoStand] = false
                            it[MinesSinglePlayerMatches.startedAt] = Instant.now()
                            it[MinesSinglePlayerMatches.finishedAt] = null
                        }
                    }
                    return@transaction CreateGameResult.Success(result[MinesSinglePlayerMatches.id].value)
                }
            }

            when (result) {
                is CreateGameResult.Success -> {
                    val matchId = MinesMatchId(result.matchId)
                    val minesPlayfield = MinesPlayfield(loritta.random, totalMines, 0.03)
                    val mutex = Mutex()

                    lateinit var minesButtons: MinesButtons

                    val buttons = mutableListOf<Button>()

                    val payoutButton = loritta.interactivityManager.buttonForUser(
                        context.user,
                        context.alwaysEphemeral,
                        ButtonStyle.PRIMARY,
                        "Payout"
                    ) { context ->
                        mutex.withLock {
                            val hook = context.deferEditAsync()

                            if (minesPlayfield.gameState != MinesPlayfield.GameState.Playing) {
                                context.reply(false) {
                                    styled(
                                        context.i18nContext.get(I18N_PREFIX.Play.Errors.CannotDoThisNow)
                                    )
                                }
                                return@buttonForUser
                            }

                            val payoutResult = minesPlayfield.payout()

                            when (payoutResult) {
                                MinesPlayfield.PayoutResult.NoTilesPicked -> {
                                    context.reply(false) {
                                        styled(
                                            context.i18nContext.get(I18N_PREFIX.Play.Errors.YouNeedToPickAtLeastOneTileBeforePayingOut)
                                        )
                                    }
                                }
                                is MinesPlayfield.PayoutResult.Success -> {
                                    loritta.transaction {
                                        if (matchBet != null) {
                                            val payoutValue = (matchBet * payoutResult.payoutMultiplier).toLong()

                                            Profiles.update({ Profiles.id eq context.user.idLong }) {
                                                it[Profiles.money] = Profiles.money + payoutValue
                                            }

                                            SimpleSonhosTransactionsLogUtils.insert(
                                                context.user.idLong,
                                                Instant.now(),
                                                TransactionType.MINES,
                                                payoutValue,
                                                StoredMinesPayoutTransaction(matchId.matchId)
                                            )

                                            finishMines(matchId, payoutValue, minesPlayfield.getPickedCount(), false)
                                        } else {
                                            finishMines(matchId, null, minesPlayfield.getPickedCount(), false)
                                        }
                                    }

                                    hook.await()
                                        .editMessage {
                                            createMessage(context, matchId, minesPlayfield, minesButtons, matchBet)
                                        }.await()
                                }
                            }
                        }
                    }

                    suspend fun pick(context: ComponentContext, playfieldX: Int, playfieldY: Int) {
                        val hook = context.deferEditAsync()

                        if (minesPlayfield.gameState != MinesPlayfield.GameState.Playing) {
                            context.reply(false) {
                                styled(
                                    context.i18nContext.get(I18N_PREFIX.Play.Errors.CannotDoThisNow)
                                )
                            }
                            return
                        }

                        val pick = minesPlayfield.pick(playfieldX, playfieldY)

                        when (pick) {
                            MinesPlayfield.PickResult.AlreadyPicked -> {
                                context.reply(false) {
                                    styled(
                                        context.i18nContext.get(I18N_PREFIX.Play.Errors.YouNeedToPickAtLeastOneTileBeforePayingOut)
                                    )
                                }
                            }
                            MinesPlayfield.PickResult.Mine -> {
                                loritta.transaction {
                                    finishMines(matchId, null, minesPlayfield.getPickedCount(), false)
                                }

                                hook.await()
                                    .editMessage {
                                        createMessage(context, matchId, minesPlayfield, minesButtons, matchBet)
                                    }
                                    .await()
                            }
                            is MinesPlayfield.PickResult.Success -> {
                                if (pick.clearedPlayfield) {
                                    loritta.transaction {
                                        if (matchBet != null) {
                                            val payoutValue = (matchBet * pick.payoutMultiplier).toLong()

                                            Profiles.update({ Profiles.id eq context.user.idLong }) {
                                                it[Profiles.money] = Profiles.money + payoutValue
                                            }

                                            SimpleSonhosTransactionsLogUtils.insert(
                                                context.user.idLong,
                                                Instant.now(),
                                                TransactionType.MINES,
                                                payoutValue,
                                                StoredMinesPayoutTransaction(matchId.matchId)
                                            )

                                            finishMines(matchId, payoutValue, minesPlayfield.getPickedCount(), false)
                                        } else {
                                            finishMines(matchId, null, minesPlayfield.getPickedCount(), false)
                                        }
                                    }
                                }

                                hook.await()
                                    .editMessage {
                                        createMessage(context, matchId, minesPlayfield, minesButtons, matchBet)
                                    }
                                    .await()
                            }
                        }
                    }

                    for (playfieldY in 0 until MinesPlayfield.PLAYFIELD_HEIGHT) {
                        for (playfieldX in 0 until MinesPlayfield.PLAYFIELD_WIDTH) {
                            buttons.add(
                                loritta.interactivityManager.buttonForUser(
                                    context.user,
                                    context.alwaysEphemeral,
                                    ButtonStyle.SECONDARY,
                                    builder = {
                                        emoji = Emoji.fromUnicode("⚫")
                                    }
                                ) { context ->
                                    mutex.withLock {
                                        pick(context, playfieldX, playfieldY)
                                    }
                                }
                            )
                        }
                    }

                    val randomPickButton = loritta.interactivityManager.buttonForUser(
                        context.user,
                        context.alwaysEphemeral,
                        ButtonStyle.PRIMARY,
                        context.i18nContext.get(I18N_PREFIX.Play.Random),
                        {
                            // Loritta has a custom die emoji, however I do feel that the default Twemoji red die is nicer on the blue background
                            emoji = Emoji.fromUnicode("\uD83C\uDFB2")
                        }
                    ) { context ->
                        mutex.withLock {
                            while (true) {
                                // Failed to pick - For some reason the current state is GameOver !?
                                // This should NEVER happen!
                                if (minesPlayfield.gameState is MinesPlayfield.GameState.GameOver)
                                    return@buttonForUser

                                val playfieldX = loritta.random.nextInt(0, MinesPlayfield.PLAYFIELD_WIDTH)
                                val playfieldY = loritta.random.nextInt(0, MinesPlayfield.PLAYFIELD_HEIGHT)

                                val alreadyPicked = minesPlayfield.pickedTiles[playfieldX][playfieldY]

                                if (alreadyPicked)
                                    continue

                                pick(context, playfieldX, playfieldY)
                                return@buttonForUser
                            }
                        }
                    }

                    minesButtons = MinesButtons(
                        buttons,
                        randomPickButton,
                        payoutButton
                    )

                    val message = context.reply(false) {
                        createMessage(context, matchId, minesPlayfield, minesButtons, matchBet)
                    }

                    // Start the auto stand job
                    // We need to start it here instead of before creating the message because we NEED to update the original message
                    val autoStandJob = GlobalScope.launch {
                        delay(MinesUtils.AUTO_STAND_DELAY)

                        mutex.withLock {
                            if (minesPlayfield.gameState is MinesPlayfield.GameState.Playing) {
                                val payoutResult = minesPlayfield.payout()
                                val pickedCount = minesPlayfield.getPickedCount()

                                loritta.transaction {
                                    when (payoutResult) {
                                        is MinesPlayfield.PayoutResult.Success -> {
                                            if (matchBet != null) {
                                                val payoutValue = (matchBet * payoutResult.payoutMultiplier).toLong()

                                                Profiles.update({ Profiles.id eq context.user.idLong }) {
                                                    it[Profiles.money] = Profiles.money + payoutValue
                                                }

                                                SimpleSonhosTransactionsLogUtils.insert(
                                                    context.user.idLong,
                                                    Instant.now(),
                                                    TransactionType.MINES,
                                                    payoutValue,
                                                    StoredMinesPayoutTransaction(matchId.matchId)
                                                )

                                                finishMines(matchId, payoutValue, pickedCount, true)
                                            } else {
                                                finishMines(matchId, null, pickedCount, true)
                                            }
                                        }
                                        MinesPlayfield.PayoutResult.NoTilesPicked -> {
                                            finishMines(matchId, null, pickedCount, true)
                                        }
                                    }
                                }

                                // Update the message to show the game ended due to timeout
                                message.editMessage {
                                    createMessage(
                                        context,
                                        matchId,
                                        minesPlayfield,
                                        minesButtons,
                                        matchBet
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
                CreateGameResult.NotEnoughSonhos -> {
                    context.reply(true) {
                        this.styled(
                            context.locale["commands.command.flipcoinbet.notEnoughMoneySelf"],
                            Constants.ERROR
                        )

                        this.styled(
                            context.i18nContext.get(
                                GACampaigns.sonhosBundlesUpsellDiscordMessage(
                                    loritta.config.loritta.dashboard.url,
                                    "mines",
                                    "bet-not-enough-sonhos"
                                )
                            ),
                            Emotes.LORI_RICH.asMention
                        )
                    }
                }
            }
        }

        fun InlineMessage<*>.createMessage(
            context: UnleashedContext,
            matchId: MinesMatchId,
            minesPlayfield: MinesPlayfield,
            buttons: MinesButtons,
            sonhosBet: Long?
        ) {
            val gameState = minesPlayfield.gameState

            val multiplier = minesPlayfield.calculateMinesPayoutMultiplier()
            val nextMultiplier = minesPlayfield.calculateNextMinesPayoutMultiplier()

            this.useComponentsV2 = true

            // Useful to know that this is FOR YOU!!
            text(context.user.asMention)

            val color = when (gameState) {
                is MinesPlayfield.GameState.GameOver -> {
                    if (gameState.askedForPayout) {
                        LorittaColors.MinesWon.rgb
                    } else {
                        LorittaColors.MinesLost.rgb
                    }
                }
                MinesPlayfield.GameState.Playing -> {
                    LorittaColors.MinesPlayerTurn.rgb
                }
            }

            container {
                this.accentColorRaw = color

                this.text(
                    buildString {
                        appendLine("## ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.Bomb} Mines")

                        if (sonhosBet != null) {
                            appendLine("${net.perfectdreams.loritta.cinnamon.emotes.Emotes.CoinHeads} ${context.i18nContext.get(I18N_PREFIX.Play.CurrentBet(SonhosUtils.getSonhosEmojiOfQuantity(sonhosBet), sonhosBet))}")
                        } else {
                            appendLine("${net.perfectdreams.loritta.cinnamon.emotes.Emotes.CoinHeads} ${context.i18nContext.get(I18N_PREFIX.Play.CurrentBetJustForFun)}")
                        }
                        appendLine("${net.perfectdreams.loritta.cinnamon.emotes.Emotes.Bomb} ${context.i18nContext.get(I18N_PREFIX.Play.CurrentMines(minesPlayfield.totalMines, minesPlayfield.getPickedCount(), MinesPlayfield.TOTAL_TILES - minesPlayfield.totalMines))}")
                        appendLine("\uD83D\uDD25 ${context.i18nContext.get(I18N_PREFIX.Play.CurrentMultiplier(multiplier))}")

                        if (nextMultiplier != null) {
                            if (sonhosBet != null) {
                                val nextPayout = (sonhosBet * nextMultiplier).toLong()
                                appendLine("\uD83D\uDCC8 ${context.i18nContext.get(I18N_PREFIX.Play.NextMultiplier(nextMultiplier, SonhosUtils.getSonhosEmojiOfQuantity(nextPayout), nextPayout))}")
                            } else {
                                appendLine("\uD83D\uDCC8 ${context.i18nContext.get(I18N_PREFIX.Play.NextMultiplierNoBet(nextMultiplier))}")
                            }
                        } else {
                            appendLine("\uD83D\uDCC8 ${context.i18nContext.get(I18N_PREFIX.Play.NextMultiplierCleared)}")
                        }
                    }
                )

                separator(isDivider = true)

                for (playfieldY in 0 until MinesPlayfield.PLAYFIELD_HEIGHT) {
                    val rowButtons = mutableListOf<Button>()

                    for (playfieldX in 0 until MinesPlayfield.PLAYFIELD_WIDTH) {
                        val index = playfieldX + (playfieldY * MinesPlayfield.PLAYFIELD_WIDTH)
                        val picked = minesPlayfield.pickedTiles[playfieldX][playfieldY]

                        val emoji = when (val gameState = minesPlayfield.gameState) {
                            MinesPlayfield.GameState.Playing -> {
                                if (picked) {
                                    Emoji.fromUnicode("\uD83D\uDC8E")
                                } else {
                                    null
                                }
                            }
                            is MinesPlayfield.GameState.GameOver -> {
                                val isBomb = minesPlayfield.tiles[playfieldX][playfieldY]
                                val explodedAt = gameState.explodedAt

                                if (isBomb) {
                                    if (explodedAt != null && explodedAt.x == playfieldX && explodedAt.y == playfieldY) {
                                        Emoji.fromUnicode("\uD83D\uDCA5")
                                    } else {
                                        Emoji.fromUnicode("\uD83D\uDCA3")
                                    }
                                } else {
                                    Emoji.fromUnicode("\uD83D\uDC8E")
                                }
                            }
                        }

                        val disabledButton = when (minesPlayfield.gameState) {
                            MinesPlayfield.GameState.Playing -> {
                                picked
                            }
                            is MinesPlayfield.GameState.GameOver -> {
                                true
                            }
                        }

                        val buttonStyle = if (gameState is MinesPlayfield.GameState.GameOver) {
                            if (gameState.explodedAt != null && gameState.explodedAt.x == playfieldX && gameState.explodedAt.y == playfieldY) {
                                ButtonStyle.DANGER
                            } else if (picked) {
                                ButtonStyle.SUCCESS
                            } else {
                                ButtonStyle.SECONDARY
                            }
                        } else {
                            if (picked) {
                                ButtonStyle.SUCCESS
                            } else {
                                ButtonStyle.SECONDARY
                            }
                        }

                        rowButtons.add(
                            buttons.playfieldButtons[playfieldX + playfieldY * MinesPlayfield.PLAYFIELD_WIDTH]
                                .withDisabled(disabledButton)
                                .withStyle(buttonStyle)
                                .let {
                                    if (emoji != null)
                                        it.withEmoji(emoji).withLabel("")
                                    else
                                        it.withEmoji(null).withLabel((index + 1).toString())
                                }
                        )
                    }

                    this.actionRow(rowButtons)
                }

                this.actionRow(
                    buttons.randomPickButton
                        .withDisabled(minesPlayfield.gameState !is MinesPlayfield.GameState.Playing)
                )

                this.text(
                    buildString {
                        appendLine("-# ${context.i18nContext.get(I18N_PREFIX.Play.HowToPlay)}")
                        appendLine("-# ${context.i18nContext.get(I18N_PREFIX.Play.MatchId(matchId.matchId))}")
                    }
                )
            }

            when (val gameState = minesPlayfield.gameState) {
                MinesPlayfield.GameState.Playing -> {
                    if (sonhosBet != null) {
                        val sonhosPayout = (sonhosBet * multiplier).toLong()

                        this.actionRow(
                            buttons.payoutButton
                                .withLabel(context.i18nContext.get(I18N_PREFIX.Play.WithdrawSonhos(sonhosPayout)))
                                .withEmoji(SonhosUtils.getSonhosEmojiOfQuantity(sonhosPayout).toJDA())
                                .let {
                                    if (minesPlayfield.canPayout())
                                        it
                                    else
                                        it.asDisabled()
                                }
                        )
                    } else {
                        this.actionRow(
                            buttons.payoutButton
                                .withLabel(context.i18nContext.get(I18N_PREFIX.Play.End))
                                .withEmoji(Emoji.fromUnicode("\uD83C\uDFC3"))
                                .let {
                                    if (minesPlayfield.canPayout())
                                        it
                                    else
                                        it.asDisabled()
                                }
                        )
                    }
                }
                is MinesPlayfield.GameState.GameOver -> {
                    val picks = minesPlayfield.getPickedCount()

                    this.container {
                        this.accentColorRaw = color

                        this.section(Thumbnail(
                            if (gameState.askedForPayout) {
                                "https://stuff.loritta.website/mines/loritta-mines-success.png"
                            } else {
                                "https://stuff.loritta.website/mines/loritta-mines-fail.png"
                            }
                        )) {
                            this.text(
                                buildString {
                                    if (gameState.askedForPayout) {
                                        if (sonhosBet != null) {
                                            val paidOut = (sonhosBet * gameState.payoutMultiplier).toLong()
                                            val profit = paidOut - sonhosBet

                                            appendLine("### \uD83D\uDC8E ${context.i18nContext.get(I18N_PREFIX.Play.Congratulations)}")
                                            appendLine(context.i18nContext.get(I18N_PREFIX.Play.YouWon(picks, SonhosUtils.getSonhosEmojiOfQuantity(paidOut), paidOut, SonhosUtils.getSonhosEmojiOfQuantity(profit), profit)))
                                        } else {
                                            appendLine(context.i18nContext.get(I18N_PREFIX.Play.YouWonNoBet(picks)))
                                        }
                                    } else {
                                        appendLine(context.i18nContext.get(I18N_PREFIX.Play.YouLost))
                                    }
                                }
                            )
                        }
                    }

                    this.actionRow(
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
                            ButtonStyle.SUCCESS,
                            context.i18nContext.get(I18N_PREFIX.Play.StartNewMatch)
                        ) { context ->
                            context.deferChannelMessage(context.wasInitiallyDeferredEphemerally == true)

                            startMinesMatch(context, minesPlayfield.totalMines, sonhosBet)
                        }
                    )
                }
            }
        }

        fun finishMines(
            matchId: MinesMatchId,
            payout: Long?,
            pickedTiles: Int,
            autoStand: Boolean
        ) {
            // Mark this match as finished!
            MinesSinglePlayerMatches.update({ MinesSinglePlayerMatches.id eq matchId.matchId }) {
                it[MinesSinglePlayerMatches.finishedAt] = Instant.now()
                if (payout != null) {
                    it[MinesSinglePlayerMatches.payout] = payout
                }
                it[MinesSinglePlayerMatches.payout] = payout
                it[MinesSinglePlayerMatches.pickedTiles] = pickedTiles
                it[MinesSinglePlayerMatches.autoStand] = autoStand
                it[MinesSinglePlayerMatches.autoStand] = autoStand
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val mines = args.getOrNull(0)?.toLongOrNull() ?: run {
                context.explain()
                return null
            }

            val sonhos = args.getOrNull(1)

            return mapOf(
                options.mines to mines,
                options.sonhos to sonhos
            )
        }

        sealed class CreateGameResult {
            data class Success(val matchId: Long) : CreateGameResult()
            object NotEnoughSonhos : CreateGameResult()
        }

        @JvmInline
        value class MinesMatchId(val matchId: Long)
    }

    class MinesStatsExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
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
                val countField = MinesSinglePlayerMatches.id.count()
                val payoutSumField = MinesSinglePlayerMatches.payout.sum()

                val handsMatches = MinesSinglePlayerMatches.select(
                    countField,
                    payoutSumField
                )
                    .where {
                        MinesSinglePlayerMatches.user eq user.idLong and (MinesSinglePlayerMatches.bet.isNotNull())
                    }
                    .firstOrNull()

                if (handsMatches != null) {
                    return@transaction Result(
                        handsMatches[countField],
                        handsMatches[payoutSumField] ?: 0L
                    )
                } else {
                    return@transaction Result(
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
                            appendLine("# ${net.perfectdreams.loritta.cinnamon.emotes.Emotes.Bomb} ${context.i18nContext.get(I18N_PREFIX.Stats.YourStats)}")
                            appendLine(context.i18nContext.get(I18N_PREFIX.Stats.TotalMatches(result.total)))
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
            val totalPayout: Long,
        )
    }
}