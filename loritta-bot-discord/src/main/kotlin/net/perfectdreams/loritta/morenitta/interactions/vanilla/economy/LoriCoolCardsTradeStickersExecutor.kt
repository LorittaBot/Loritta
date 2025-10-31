package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.messages.InlineEmbed
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.NumberUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils.appendUserHaventGotDailyTodayOrUpsellSonhosBundles
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.*
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.common.emotes.DiscordEmote
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.modals.options.modalString
import net.perfectdreams.loritta.morenitta.loricoolcards.StickerAlbumTemplate
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.VacationModeUtils
import net.perfectdreams.loritta.serializable.StoredLoriCoolCardsPaymentSonhosTradeTransaction
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.awt.Color
import java.time.Instant
import java.util.*
import kotlin.collections.get

class LoriCoolCardsTradeStickersExecutor(val loritta: LorittaBot, private val loriCoolCardsCommand: LoriCoolCardsCommand) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Loricoolcards.Trade
    }

    inner class Options : ApplicationCommandOptions() {
        val user = user("user", I18N_PREFIX.Options.User.Text)
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        startTrade(context, args[options.user].user)
    }

    suspend fun startTrade(context: UnleashedContext, userThatYouWantToTradeWith: User) {
        if (SonhosUtils.checkIfEconomyIsDisabled(context))
            return

        // TODO: PLEASE PLEASE PLEASE REFACTOR THIS COMMAND
        //  It has a LOT of duplicate code!!
        val selfUser = context.user
        val now = Instant.now()

        if (context.user == userThatYouWantToTradeWith) {
            context.reply(true) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.YouCantTradeStickersWithYourself)
                )
            }
            return
        }

        // Is the other user banned? If yes, don't let them trade!
        if (AccountUtils.checkAndSendMessageIfUserIsBanned(loritta, context, userThatYouWantToTradeWith.idLong))
            return

        // Only allow users to give stickers if they got their daily reward today
        val giverTodayDailyReward = AccountUtils.getUserTodayDailyReward(loritta, context.lorittaUser.profile)
        if (giverTodayDailyReward == null) {
            context.reply(false) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.YouNeedToGetDailyRewardBeforeTradingStickers(loritta.commandMentions.daily)),
                    Constants.ERROR
                )
            }
            return
        }

        val receiverTodayDailyReward = AccountUtils.getUserTodayDailyReward(loritta, userThatYouWantToTradeWith.idLong)
        if (receiverTodayDailyReward == null) {
            context.reply(false) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.ReceiverNeedsToGetDailyRewardBeforeTradingStickers(userThatYouWantToTradeWith.asMention, loritta.commandMentions.daily)),
                    Constants.ERROR
                )
            }
            return
        }

        val result = loritta.transaction {
            val event = LoriCoolCardsEvents.selectAll().where {
                LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
            }.firstOrNull() ?: return@transaction null

            val template = Json.decodeFromString<StickerAlbumTemplate>(event[LoriCoolCardsEvents.template])

            val giverBoughtPacks = LoriCoolCardsUserBoughtBoosterPacks.selectAll().where {
                LoriCoolCardsUserBoughtBoosterPacks.user eq selfUser.idLong and (LoriCoolCardsUserBoughtBoosterPacks.event eq event[LoriCoolCardsEvents.id])
            }.count()

            if (template.minimumBoosterPacksToGive > giverBoughtPacks)
                return@transaction Pair(template, false)

            val receiverBoughtPacks = LoriCoolCardsUserBoughtBoosterPacks.selectAll().where {
                LoriCoolCardsUserBoughtBoosterPacks.user eq userThatYouWantToTradeWith.idLong and (LoriCoolCardsUserBoughtBoosterPacks.event eq event[LoriCoolCardsEvents.id])
            }.count()

            if (template.minimumBoosterPacksToGive > receiverBoughtPacks)
                return@transaction Pair(template, false)

            return@transaction Pair(template, true)
        }

        if (result == null) {
            context.reply(false) {
                styled(
                    "Nenhum evento de figurinhas ativo"
                )
            }
            return
        }

        val template = result.first
        val hasGiveArbitraryStickerCountSupport = result.second

        val usersThatHaveConfirmedTheTrade = mutableSetOf<User>()
        var alreadyProcessed = false
        val mutex = Mutex()

        // Let's add a random emoji just to look cute
        val user1Emote = SonhosUtils.HANGLOOSE_EMOTES.random()
        val user2Emote = SonhosUtils.HANGLOOSE_EMOTES.filter { it != user1Emote }.random()

        val trade = TradeOffer(
            TradeThings(
                mutableListOf(),
                null
            ),
            TradeThings(
                mutableListOf(),
                null
            ),
            hasGiveArbitraryStickerCountSupport
        )

        // We keep it in here to avoid the message changing on every rerender
        val emptyFunnyMessageForPlayer1 = context.i18nContext.get(I18N_PREFIX.NothingTradedYetFunnyMessages).random()
        val emptyFunnyMessageForPlayer2 = context.i18nContext.get(I18N_PREFIX.NothingTradedYetFunnyMessages).random()

        fun getReceiverUser(currentUser: User): User {
            return if (currentUser == userThatYouWantToTradeWith)
                selfUser
            else
                userThatYouWantToTradeWith
        }

        fun createTradeMessage(): InlineMessage<*>.() -> (Unit) = {
            content = "**${context.i18nContext.get(I18N_PREFIX.StickerTradeBetweenUsers(context.user.asMention, userThatYouWantToTradeWith.asMention))}**"

            // The embed trade status for each user
            embed {
                apply(
                    createTradeOfferEmbedStatus(
                        selfUser,
                        usersThatHaveConfirmedTheTrade,
                        trade.player1,
                        user1Emote,
                        emptyFunnyMessageForPlayer1
                    )
                )
            }

            embed {
                apply(
                    createTradeOfferEmbedStatus(
                        userThatYouWantToTradeWith,
                        usersThatHaveConfirmedTheTrade,
                        trade.player2,
                        user2Emote,
                        emptyFunnyMessageForPlayer2
                    )
                )
            }

            // Warning if the user is trying to trade arbitrary stickers
            if (!hasGiveArbitraryStickerCountSupport) {
                embed {
                    color = LorittaColors.LorittaRed.rgb
                    description = context.i18nContext.get(I18N_PREFIX.CantTradeNonMatchingStickersCount(template.minimumBoosterPacksToGive))
                }
            }

            actionRow(
                loritta.interactivityManager.button(
                    context.alwaysEphemeral,
                    ButtonStyle.PRIMARY,
                    context.i18nContext.get(I18N_PREFIX.SetStickers),
                    {
                        loriEmoji = Emotes.LoriCoolSticker
                    }
                ) { context ->
                    val receiverUser = getReceiverUser(context.user)

                    mutex.withLock {
                        if (usersThatHaveConfirmedTheTrade.isNotEmpty()) {
                            context.reply(true) {
                                styled(
                                    if (selfUser == context.user && usersThatHaveConfirmedTheTrade.contains(selfUser)) context.i18nContext.get(I18N_PREFIX.YouCantChangeATradeThatYouHaveApproved) else context.i18nContext.get(I18N_PREFIX.YouCantChangeATradeThatYourFriendHasApproved),
                                    Emotes.LoriRage
                                )
                            }
                            return@button
                        }

                        val playerThatMatchesThisTrade = when (context.user) {
                            selfUser -> trade.player1
                            userThatYouWantToTradeWith -> trade.player2
                            else -> null
                        }

                        if (playerThatMatchesThisTrade == null) {
                            context.reply(true) {
                                styled(
                                    context.i18nContext.get(I18nKeysData.Commands.YouArentTheUserGeneric),
                                    Emotes.LoriRage
                                )
                            }
                            return@button
                        }

                        if (alreadyProcessed) {
                            context.reply(true) {
                                styled(
                                    context.i18nContext.get(I18N_PREFIX.TradeHasAlreadyBeenProcessed),
                                    Emotes.LoriRage
                                )
                            }
                            return@button
                        }

                        val stickerListOption = modalString(
                            context.i18nContext.get(I18N_PREFIX.TradeModal.StickersToBeTraded),
                            TextInputStyle.SHORT
                        )

                        context.sendModal(
                            context.i18nContext.get(I18N_PREFIX.TradeModal.StickerTrade),
                            listOf(ActionRow.of(stickerListOption.toJDA()))
                        ) { context, args ->
                            val deferEdit = context.deferEdit()
                            val stickerFancyIdsList = args[stickerListOption].split(",").map { it.trim() }

                            if (stickerFancyIdsList.size > 100) {
                                context.reply(true) {
                                    styled(
                                        context.i18nContext.get(I18N_PREFIX.TooManyStickers)
                                    )
                                }
                                return@sendModal
                            }

                            mutex.withLock {
                                val result = loritta.transaction {
                                    val event = LoriCoolCardsEvents.selectAll().where {
                                        LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
                                    }.firstOrNull() ?: return@transaction SetStickersResult.EventUnavailable

                                    val template = Json.decodeFromString<StickerAlbumTemplate>(event[LoriCoolCardsEvents.template])

                                    val giverBoughtPacks = LoriCoolCardsUserBoughtBoosterPacks.selectAll().where {
                                        LoriCoolCardsUserBoughtBoosterPacks.user eq context.user.idLong and (LoriCoolCardsUserBoughtBoosterPacks.event eq event[LoriCoolCardsEvents.id])
                                    }.count()

                                    if (template.minimumBoosterPacksToTrade > giverBoughtPacks)
                                        return@transaction SetStickersResult.YouDidntBuyEnoughBoosterPacks(template.minimumBoosterPacksToTrade, giverBoughtPacks)

                                    val receiverBoughtPacks = LoriCoolCardsUserBoughtBoosterPacks.selectAll().where {
                                        LoriCoolCardsUserBoughtBoosterPacks.user eq receiverUser.idLong and (LoriCoolCardsUserBoughtBoosterPacks.event eq event[LoriCoolCardsEvents.id])
                                    }.count()

                                    if (template.minimumBoosterPacksToTrade > receiverBoughtPacks)
                                        return@transaction SetStickersResult.ReceiverDidntBuyEnoughBoosterPacks(template.minimumBoosterPacksToTrade, receiverBoughtPacks)

                                    val stickersToBeGiven = LoriCoolCardsEventCards.selectAll().where {
                                        LoriCoolCardsEventCards.fancyCardId inList stickerFancyIdsList and (LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id])
                                    }.toList()

                                    if (stickersToBeGiven.size != stickerFancyIdsList.size)
                                        return@transaction SetStickersResult.UnknownCard

                                    val matchResult = LoriCoolCardsGiveStickersExecutor.matchStickers(
                                        context.user.idLong,
                                        event,
                                        stickersToBeGiven
                                    )

                                    if (matchResult.missingStickers.isNotEmpty())
                                        return@transaction SetStickersResult.NotEnoughCards(matchResult.missingStickers)

                                    if (matchResult.stickersThatArentStickedButAreTryingToBeGiven.isNotEmpty())
                                        return@transaction SetStickersResult.TryingToGiveStickersThatArentStickedYet(matchResult.stickersThatArentStickedButAreTryingToBeGiven)

                                    return@transaction SetStickersResult.Success(matchResult.stickerIdsToBeGivenMappedToSticker.values.map { it[LoriCoolCardsEventCards.fancyCardId] })
                                }

                                when (result) {
                                    SetStickersResult.EventUnavailable -> {
                                        context.reply(false) {
                                            styled(
                                                "Nenhum evento de figurinhas ativo"
                                            )
                                        }
                                    }
                                    is SetStickersResult.UnknownCard -> {
                                        context.reply(true) {
                                            styled(
                                                context.i18nContext.get(I18N_PREFIX.TryingToGiveAnUnknownSticker)
                                            )
                                        }
                                    }
                                    is SetStickersResult.YouDidntBuyEnoughBoosterPacks -> {
                                        context.reply(true) {
                                            styled(
                                                context.i18nContext.get(I18N_PREFIX.YouDidntBuyEnoughBoosterPacks(result.requiredPacks - result.currentPacks))
                                            )
                                        }
                                    }
                                    is SetStickersResult.ReceiverDidntBuyEnoughBoosterPacks -> {
                                        context.reply(true) {
                                            styled(
                                                context.i18nContext.get(I18N_PREFIX.ReceiverDidntBuyEnoughBoosterPacks(receiverUser.asMention, result.requiredPacks - result.currentPacks))
                                            )
                                        }
                                    }
                                    is SetStickersResult.NotEnoughCards -> {
                                        context.reply(true) {
                                            styled(
                                                context.i18nContext.get(I18N_PREFIX.YouDontHaveEnoughStickers(result.stickersMissing.joinToString { "`${it[LoriCoolCardsEventCards.fancyCardId]}`" }))
                                            )
                                        }
                                    }
                                    is SetStickersResult.TryingToGiveStickersThatArentStickedYet -> {
                                        context.reply(true) {
                                            styled(
                                                context.i18nContext.get(I18N_PREFIX.UserIsTryingToGiveStickersThatArentStickedYet(context.user.asMention, result.stickersMissing.joinToString { "`${it[LoriCoolCardsEventCards.fancyCardId]}`" }))
                                            )
                                        }
                                    }
                                    is SetStickersResult.Success -> {
                                        playerThatMatchesThisTrade.stickerFancyIds.clear()
                                        playerThatMatchesThisTrade.stickerFancyIds.addAll(result.stickerFancyIds)
                                        deferEdit.editOriginal(
                                            MessageEdit {
                                                apply(createTradeMessage())
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                loritta.interactivityManager.button(
                    context.alwaysEphemeral,
                    ButtonStyle.PRIMARY,
                    context.i18nContext.get(I18N_PREFIX.SetStickersFill),
                    {
                        loriEmoji = Emotes.LoriCoolSticker
                    }
                ) { context ->
                    val receiverUser = getReceiverUser(context.user)

                    mutex.withLock {
                        if (usersThatHaveConfirmedTheTrade.isNotEmpty()) {
                            context.reply(true) {
                                styled(
                                    if (selfUser == context.user && usersThatHaveConfirmedTheTrade.contains(selfUser)) context.i18nContext.get(I18N_PREFIX.YouCantChangeATradeThatYouHaveApproved) else context.i18nContext.get(I18N_PREFIX.YouCantChangeATradeThatYourFriendHasApproved),
                                    Emotes.LoriRage
                                )
                            }
                            return@button
                        }

                        val playerThatMatchesThisTrade = when (context.user) {
                            selfUser -> trade.player1
                            userThatYouWantToTradeWith -> trade.player2
                            else -> null
                        }

                        if (playerThatMatchesThisTrade == null) {
                            context.reply(true) {
                                styled(
                                    context.i18nContext.get(I18nKeysData.Commands.YouArentTheUserGeneric),
                                    Emotes.LoriRage
                                )
                            }
                            return@button
                        }

                        if (alreadyProcessed) {
                            context.reply(true) {
                                styled(
                                    context.i18nContext.get(I18N_PREFIX.TradeHasAlreadyBeenProcessed),
                                    Emotes.LoriRage
                                )
                            }
                            return@button
                        }

                        val stickerCountOption = modalString(
                            context.i18nContext.get(I18N_PREFIX.TradeModal.StickersToBeTradedFill),
                            TextInputStyle.SHORT,
                            // Auto fill with the sticker count to make it easier
                            // If all of them are empty, then it will be empty (null)
                            value = trade.player1.stickerFancyIds.ifEmpty { null }?.size?.toString() ?: trade.player2.stickerFancyIds.ifEmpty { null }?.size?.toString()
                        )

                        context.sendModal(
                            context.i18nContext.get(I18N_PREFIX.TradeModal.StickerTrade),
                            listOf(ActionRow.of(stickerCountOption.toJDA())),
                        ) { context, args ->
                            val deferEdit = context.deferEdit()
                            val stickersFillCountRaw = args[stickerCountOption]
                            val stickersFillCount = stickersFillCountRaw.toIntOrNull()?.coerceAtLeast(0)

                            if (stickersFillCount == null) {
                                context.reply(true) {
                                    styled(
                                        context.i18nContext.get(I18nKeysData.Commands.InvalidNumber(stickersFillCountRaw))
                                    )
                                }
                                return@sendModal
                            }

                            if (stickersFillCount > 100) {
                                context.reply(true) {
                                    styled(
                                        context.i18nContext.get(I18N_PREFIX.TooManyStickers)
                                    )
                                }
                                return@sendModal
                            }

                            mutex.withLock {
                                // Fast clear if it is zero!
                                if (stickersFillCount == 0) {
                                    playerThatMatchesThisTrade.stickerFancyIds.clear()
                                    deferEdit.editOriginal(
                                        MessageEdit {
                                            apply(createTradeMessage())
                                        }
                                    )
                                    return@withLock
                                }

                                val result = loritta.transaction {
                                    // Implementing this is quite a bit annoying
                                    // The "auto fill" should NOT just "get random stickers that we have lol"
                                    // It should be "get the stickers that the OTHER user needs, get the stickers that WE have, then fill"
                                    // It isn't impossible, but it is a bit tricky
                                    val event = LoriCoolCardsEvents.selectAll().where {
                                        LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
                                    }.firstOrNull() ?: return@transaction SetStickersFillResult.EventUnavailable

                                    val template = Json.decodeFromString<StickerAlbumTemplate>(event[LoriCoolCardsEvents.template])

                                    val giverBoughtPacks = LoriCoolCardsUserBoughtBoosterPacks.selectAll().where {
                                        LoriCoolCardsUserBoughtBoosterPacks.user eq context.user.idLong and (LoriCoolCardsUserBoughtBoosterPacks.event eq event[LoriCoolCardsEvents.id])
                                    }.count()

                                    if (template.minimumBoosterPacksToTrade > giverBoughtPacks)
                                        return@transaction SetStickersFillResult.YouDidntBuyEnoughBoosterPacks(template.minimumBoosterPacksToTrade, giverBoughtPacks)

                                    val receiverBoughtPacks = LoriCoolCardsUserBoughtBoosterPacks.selectAll().where {
                                        LoriCoolCardsUserBoughtBoosterPacks.user eq receiverUser.idLong and (LoriCoolCardsUserBoughtBoosterPacks.event eq event[LoriCoolCardsEvents.id])
                                    }.count()

                                    if (template.minimumBoosterPacksToTrade > receiverBoughtPacks)
                                        return@transaction SetStickersFillResult.ReceiverDidntBuyEnoughBoosterPacks(template.minimumBoosterPacksToTrade, receiverBoughtPacks)

                                    // Okay, so we CAN trade!
                                    val player1Stickers = LoriCoolCardsCompareStickersExecutor.getStickers(
                                        event[LoriCoolCardsEvents.id].value,
                                        context.user
                                    )

                                    val player2Stickers = LoriCoolCardsCompareStickersExecutor.getStickers(
                                        event[LoriCoolCardsEvents.id].value,
                                        receiverUser
                                    )

                                    val stickerIdsToBeQueried = mutableSetOf<Long>()

                                    // Technically we don't need to add the "in our inventory" to the query list because they are already included
                                    stickerIdsToBeQueried.addAll(player1Stickers.stickerIdsThatWeHave)
                                    stickerIdsToBeQueried.addAll(player2Stickers.stickerIdsThatWeHave)

                                    val eventStickers = LoriCoolCardsEventCards
                                        .selectAll()
                                        .where {
                                            LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id] and (LoriCoolCardsEventCards.id inList stickerIdsToBeQueried)
                                        }.toList()

                                    val friendStickersMissing = LoriCoolCardsCompareStickersExecutor.getMissingStickers(eventStickers, player2Stickers, player1Stickers)

                                    // From here, we already know which stickers the friend needs
                                    // And because we know, we can pass it over
                                    val missingFancy = friendStickersMissing.map {
                                        it[LoriCoolCardsEventCards.fancyCardId]
                                    }.take(stickersFillCount)
                                        .shuffled() // We shuffle it just to avoid always getting the same stickers

                                    if (missingFancy.size != stickersFillCount)
                                        return@transaction SetStickersFillResult.TryingToGiveMoreStickersThatYouHave(friendStickersMissing.size)

                                    return@transaction SetStickersFillResult.Success(missingFancy)
                                }

                                when (result) {
                                    SetStickersFillResult.EventUnavailable -> {
                                        context.reply(false) {
                                            styled(
                                                "Nenhum evento de figurinhas ativo"
                                            )
                                        }
                                    }
                                    is SetStickersFillResult.YouDidntBuyEnoughBoosterPacks -> {
                                        context.reply(true) {
                                            styled(
                                                context.i18nContext.get(I18N_PREFIX.YouDidntBuyEnoughBoosterPacks(result.requiredPacks - result.currentPacks))
                                            )
                                        }
                                    }
                                    is SetStickersFillResult.ReceiverDidntBuyEnoughBoosterPacks -> {
                                        context.reply(true) {
                                            styled(
                                                context.i18nContext.get(I18N_PREFIX.ReceiverDidntBuyEnoughBoosterPacks(receiverUser.asMention, result.requiredPacks - result.currentPacks))
                                            )
                                        }
                                    }
                                    is SetStickersFillResult.TryingToGiveMoreStickersThatYouHave -> {
                                        context.reply(true) {
                                            styled(
                                                context.i18nContext.get(I18N_PREFIX.TryingToGiveMoreStickersThanYouHave(result.maxStickers, receiverUser.asMention))
                                            )
                                        }
                                    }
                                    is SetStickersFillResult.Success -> {
                                        playerThatMatchesThisTrade.stickerFancyIds.clear()
                                        playerThatMatchesThisTrade.stickerFancyIds.addAll(result.stickerFancyIds)
                                        deferEdit.editOriginal(
                                            MessageEdit {
                                                apply(createTradeMessage())
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                loritta.interactivityManager.button(
                    context.alwaysEphemeral,
                    ButtonStyle.PRIMARY,
                    context.i18nContext.get(I18N_PREFIX.SetSonhos),
                    {
                        loriEmoji = Emotes.Sonhos2
                    }
                ) { context ->
                    val receiverUser = getReceiverUser(context.user)

                    mutex.withLock {
                        if (usersThatHaveConfirmedTheTrade.isNotEmpty()) {
                            context.reply(true) {
                                styled(
                                    if (selfUser == context.user && usersThatHaveConfirmedTheTrade.contains(selfUser)) context.i18nContext.get(I18N_PREFIX.YouCantChangeATradeThatYouHaveApproved) else context.i18nContext.get(I18N_PREFIX.YouCantChangeATradeThatYourFriendHasApproved),
                                    Emotes.LoriRage
                                )
                            }
                            return@button
                        }

                        val playerThatMatchesThisTrade = when (context.user) {
                            selfUser -> trade.player1
                            userThatYouWantToTradeWith -> trade.player2
                            else -> null
                        }

                        if (playerThatMatchesThisTrade == null) {
                            context.reply(true) {
                                styled(
                                    context.i18nContext.get(I18nKeysData.Commands.YouArentTheUserGeneric),
                                    Emotes.LoriRage
                                )
                            }
                            return@button
                        }

                        if (alreadyProcessed) {
                            context.reply(true) {
                                styled(
                                    context.i18nContext.get(I18N_PREFIX.TradeHasAlreadyBeenProcessed),
                                    Emotes.LoriRage
                                )
                            }
                            return@button
                        }

                        val sonhosQuantityOption = modalString(
                            context.i18nContext.get(I18N_PREFIX.TradeModal.SonhosToBeTraded),
                            TextInputStyle.SHORT
                        )

                        context.sendModal(
                            context.i18nContext.get(I18N_PREFIX.TradeModal.StickerTrade),
                            listOf(ActionRow.of(sonhosQuantityOption.toJDA()))
                        ) { context, args ->
                            val deferEdit = context.deferEdit()

                            var parsedValue = NumberUtils.convertShortenedNumberToLong(
                                context.i18nContext,
                                args[sonhosQuantityOption]
                            )

                            if (parsedValue != null && 0 >= parsedValue)
                                parsedValue = null

                            mutex.withLock {
                                if (parsedValue != null) {
                                    if (VacationModeUtils.checkIfWeAreOnVacation(context, true))
                                        return@sendModal
                                    if (VacationModeUtils.checkIfUserIsOnVacation(context, receiverUser, true))
                                        return@sendModal

                                    val result = loritta.transaction {
                                        val event = LoriCoolCardsEvents.selectAll().where {
                                            LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
                                        }.firstOrNull() ?: return@transaction SetSonhosResult.EventUnavailable

                                        val template = Json.decodeFromString<StickerAlbumTemplate>(event[LoriCoolCardsEvents.template])

                                        // When trading stickers for sonhos, BOTH the receiver AND the giver must have already finished the album
                                        val giverBoughtPacks = LoriCoolCardsUserBoughtBoosterPacks.selectAll().where {
                                            LoriCoolCardsUserBoughtBoosterPacks.user eq context.user.idLong and (LoriCoolCardsUserBoughtBoosterPacks.event eq event[LoriCoolCardsEvents.id])
                                        }.count()

                                        if (template.minimumBoosterPacksToTradeBySonhos > giverBoughtPacks)
                                            return@transaction SetSonhosResult.YouDidntBuyEnoughBoosterPacks(
                                                template.minimumBoosterPacksToTradeBySonhos,
                                                giverBoughtPacks
                                            )

                                        val receiverBoughtPacks = LoriCoolCardsUserBoughtBoosterPacks.selectAll().where {
                                            LoriCoolCardsUserBoughtBoosterPacks.user eq receiverUser.idLong and (LoriCoolCardsUserBoughtBoosterPacks.event eq event[LoriCoolCardsEvents.id])
                                        }.count()

                                        if (template.minimumBoosterPacksToTradeBySonhos > receiverBoughtPacks)
                                            return@transaction SetSonhosResult.ReceiverDidntBuyEnoughBoosterPacks(
                                                template.minimumBoosterPacksToTradeBySonhos,
                                                receiverBoughtPacks
                                            )

                                        val userSonhos = Profiles.select(Profiles.money).where {
                                            Profiles.id eq context.user.idLong
                                        }.firstOrNull()?.get(Profiles.money) ?: 0

                                        if (parsedValue > userSonhos)
                                            return@transaction SetSonhosResult.NotEnoughSonhos(userSonhos, parsedValue)

                                        return@transaction SetSonhosResult.Success
                                    }

                                    when (result) {
                                        SetSonhosResult.EventUnavailable -> {
                                            context.reply(false) {
                                                styled(
                                                    "Nenhum evento de figurinhas ativo"
                                                )
                                            }
                                        }

                                        is SetSonhosResult.YouDidntBuyEnoughBoosterPacks -> {
                                            context.reply(true) {
                                                styled(
                                                    context.i18nContext.get(
                                                        I18N_PREFIX.YouDidntBuyEnoughBoosterPacksSonhosTrade(
                                                            result.requiredPacks - result.currentPacks
                                                        )
                                                    )
                                                )
                                            }
                                        }

                                        is SetSonhosResult.ReceiverDidntBuyEnoughBoosterPacks -> {
                                            context.reply(true) {
                                                styled(
                                                    context.i18nContext.get(
                                                        I18N_PREFIX.ReceiverDidntBuyEnoughBoosterPacksSonhosTrade(
                                                            receiverUser.asMention,
                                                            result.requiredPacks - result.currentPacks
                                                        )
                                                    )
                                                )
                                            }
                                        }

                                        is SetSonhosResult.NotEnoughSonhos -> {
                                            context.reply(true) {
                                                styled(
                                                    context.i18nContext.get(
                                                        SonhosUtils.insufficientSonhos(
                                                            result.userSonhos,
                                                            result.howMuch
                                                        )
                                                    ),
                                                    Emotes.LoriSob
                                                )

                                                appendUserHaventGotDailyTodayOrUpsellSonhosBundles(
                                                    context.loritta,
                                                    context.i18nContext,
                                                    UserId(context.user.idLong),
                                                    "lori-cool-cards",
                                                    "trade-figurittas-not-enough-sonhos"
                                                )
                                            }
                                        }

                                        SetSonhosResult.Success -> {
                                            // Yay, we do have enough sonhos!
                                            playerThatMatchesThisTrade.sonhos = parsedValue
                                        }
                                    }
                                } else {
                                    // Value is null, no need to check it on the database...
                                    playerThatMatchesThisTrade.sonhos = parsedValue
                                }

                                deferEdit.editOriginal(
                                    MessageEdit {
                                        apply(createTradeMessage())
                                    }
                                )
                            }
                        }
                    }
                }
            )

            val acceptTradeButton = UnleashedButton.of(
                ButtonStyle.SUCCESS,
                context.i18nContext.get(I18N_PREFIX.AcceptTradeButton(usersThatHaveConfirmedTheTrade.size, 2)),
                Emotes.Handshake
            )

            actionRow(
                if (trade.isTradeValid())
                    loritta.interactivityManager.button(
                        context.alwaysEphemeral,
                        acceptTradeButton
                    ) { context ->
                        mutex.withLock {
                            val playerThatMatchesThisTrade = when (context.user) {
                                selfUser -> trade.player1
                                userThatYouWantToTradeWith -> trade.player2
                                else -> null
                            }

                            if (playerThatMatchesThisTrade == null) {
                                context.reply(true) {
                                    styled(
                                        context.i18nContext.get(I18nKeysData.Commands.YouArentTheUserGeneric),
                                        Emotes.LoriRage
                                    )
                                }
                                return@button
                            }

                            if (alreadyProcessed) {
                                context.reply(true) {
                                    styled(
                                        context.i18nContext.get(I18N_PREFIX.TradeHasAlreadyBeenProcessed),
                                        Emotes.LoriRage
                                    )
                                }
                                return@button
                            }

                            if (usersThatHaveConfirmedTheTrade.contains(context.user)) {
                                usersThatHaveConfirmedTheTrade.remove(context.user)

                                context.editMessage(
                                    true,
                                    MessageEdit {
                                        apply(createTradeMessage())
                                    }
                                )

                                context.reply(true) {
                                    content = "Você removeu a sua troca!"
                                }
                            } else {
                                usersThatHaveConfirmedTheTrade.add(context.user)

                                if (usersThatHaveConfirmedTheTrade.size == 2) {
                                    context.invalidateComponentCallback()
                                    alreadyProcessed = true

                                    context.editMessage(
                                        true,
                                        MessageEdit {
                                            apply(createTradeMessage())
                                        }
                                    )

                                    // Now we need to check if everything is actually ok
                                    val response = loritta.transaction {
                                        val event = LoriCoolCardsEvents.selectAll().where {
                                            LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
                                        }.firstOrNull()
                                            ?: return@transaction TradeStickerResult.EventUnavailable

                                        val stickersToBeGivenByPlayer1 = getEventStickersFromStickerFancyIds(event[LoriCoolCardsEvents.id].value, trade.player1.stickerFancyIds)
                                        val stickersToBeGivenByPlayer2 = getEventStickersFromStickerFancyIds(event[LoriCoolCardsEvents.id].value, trade.player2.stickerFancyIds)

                                        // Validate if the stickers that we retrieved from the database do match the amount of sticker IDs that we have
                                        // If it doesn't, then there are unknown stickers on the provided list!
                                        if (stickersToBeGivenByPlayer1.size != trade.player1.stickerFancyIds.size)
                                            return@transaction TradeStickerResult.UnknownCard(selfUser)

                                        if (stickersToBeGivenByPlayer2.size != trade.player2.stickerFancyIds.size)
                                            return@transaction TradeStickerResult.UnknownCard(userThatYouWantToTradeWith)

                                        // Do we have enough sonhos?
                                        val player1Sonhos = getUserSonhos(selfUser)
                                        val player2Sonhos = getUserSonhos(userThatYouWantToTradeWith)

                                        val tradePlayer1Sonhos = trade.player1.sonhos ?: 0
                                        if (tradePlayer1Sonhos > player1Sonhos)
                                            return@transaction TradeStickerResult.NotEnoughSonhos(selfUser)

                                        val tradePlayer2Sonhos = trade.player2.sonhos ?: 0
                                        if (tradePlayer2Sonhos > player2Sonhos)
                                            return@transaction TradeStickerResult.NotEnoughSonhos(userThatYouWantToTradeWith)

                                        // Does each player own each sticker?
                                        val verificationResultPlayer1 = verifyStickers(
                                            selfUser,
                                            stickersToBeGivenByPlayer1
                                        )

                                        if (verificationResultPlayer1 is StickerVerificationResult.NotEnoughCards)
                                            return@transaction TradeStickerResult.NotEnoughCards(
                                                selfUser,
                                                verificationResultPlayer1.missingStickers
                                            )

                                        if (verificationResultPlayer1 is StickerVerificationResult.TryingToGiveStickersThatArentStickedYet)
                                            return@transaction TradeStickerResult.TryingToGiveStickersThatArentStickedYet(
                                                selfUser,
                                                verificationResultPlayer1.stickersMissing
                                            )

                                        // There are only three possible results here anyway
                                        verificationResultPlayer1 as StickerVerificationResult.Success

                                        val verificationResultPlayer2 = verifyStickers(
                                            userThatYouWantToTradeWith,
                                            stickersToBeGivenByPlayer2
                                        )

                                        if (verificationResultPlayer2 is StickerVerificationResult.NotEnoughCards)
                                            return@transaction TradeStickerResult.NotEnoughCards(
                                                userThatYouWantToTradeWith,
                                                verificationResultPlayer2.missingStickers
                                            )

                                        if (verificationResultPlayer2 is StickerVerificationResult.TryingToGiveStickersThatArentStickedYet)
                                            return@transaction TradeStickerResult.TryingToGiveStickersThatArentStickedYet(
                                                userThatYouWantToTradeWith,
                                                verificationResultPlayer2.stickersMissing
                                            )

                                        // There are only three possible results here anyway
                                        verificationResultPlayer2 as StickerVerificationResult.Success

                                        val stickerIdsByPlayer1ToBeGivenToPlayer2MappedToOwnedStickerId = verificationResultPlayer1.stickerIdsToBeGivenMappedToOwnedStickerId
                                        val stickerIdsByPlayer1ToBeGivenToPlayer2MappedToEventStickerId = verificationResultPlayer1.stickerIdsToBeGivenMappedToEventStickerId

                                        val stickerIdsByPlayer2ToBeGivenToPlayer1MappedToOwnedStickerId = verificationResultPlayer2.stickerIdsToBeGivenMappedToOwnedStickerId
                                        val stickerIdsByPlayer2ToBeGivenToPlayer1MappedToEventStickerId = verificationResultPlayer2.stickerIdsToBeGivenMappedToEventStickerId

                                        // Do we have any elements in common?
                                        val anyElementCommon = Collections.disjoint(stickerIdsByPlayer1ToBeGivenToPlayer2MappedToEventStickerId, stickerIdsByPlayer2ToBeGivenToPlayer1MappedToEventStickerId)
                                        if (anyElementCommon)
                                            TradeStickerResult.CantTradeSameStickers

                                        // Delete the old cards
                                        LoriCoolCardsUserOwnedCards.deleteWhere {
                                            LoriCoolCardsUserOwnedCards.id inList stickerIdsByPlayer1ToBeGivenToPlayer2MappedToOwnedStickerId
                                        }

                                        LoriCoolCardsUserOwnedCards.deleteWhere {
                                            LoriCoolCardsUserOwnedCards.id inList stickerIdsByPlayer2ToBeGivenToPlayer1MappedToOwnedStickerId
                                        }

                                        // Give the new stickers
                                        giveStickersToUser(
                                            now,
                                            userThatYouWantToTradeWith,
                                            event[LoriCoolCardsEvents.id].value,
                                            stickerIdsByPlayer1ToBeGivenToPlayer2MappedToEventStickerId,
                                        )

                                        giveStickersToUser(
                                            now,
                                            selfUser,
                                            event[LoriCoolCardsEvents.id].value,
                                            stickerIdsByPlayer2ToBeGivenToPlayer1MappedToEventStickerId,
                                        )

                                        // Track the trade!
                                        val acceptedTradeId = LoriCoolCardsUserTrades.insertAndGetId {
                                            it[LoriCoolCardsUserTrades.user1] = selfUser.idLong
                                            it[LoriCoolCardsUserTrades.user2] = userThatYouWantToTradeWith.idLong
                                            it[LoriCoolCardsUserTrades.event] = event[LoriCoolCardsEvents.id]
                                            it[LoriCoolCardsUserTrades.tradedAt] = Instant.now()
                                            it[LoriCoolCardsUserTrades.tradeOffer] = Json.encodeToString(
                                                trade
                                            )
                                        }.value

                                        // Do the sonhos changes if needed
                                        if (tradePlayer1Sonhos > 0L) {
                                            Profiles.update({ Profiles.id eq userThatYouWantToTradeWith.idLong }) {
                                                with(SqlExpressionBuilder) {
                                                    it.update(money, money + tradePlayer1Sonhos)
                                                }
                                            }

                                            Profiles.update({ Profiles.id eq selfUser.idLong }) {
                                                with(SqlExpressionBuilder) {
                                                    it.update(money, money - tradePlayer1Sonhos)
                                                }
                                            }

                                            // Cinnamon transactions log
                                            SimpleSonhosTransactionsLogUtils.insert(
                                                selfUser.idLong,
                                                now,
                                                TransactionType.LORI_COOL_CARDS,
                                                tradePlayer1Sonhos,
                                                StoredLoriCoolCardsPaymentSonhosTradeTransaction(
                                                    selfUser.idLong,
                                                    userThatYouWantToTradeWith.idLong,
                                                    acceptedTradeId
                                                )
                                            )

                                            SimpleSonhosTransactionsLogUtils.insert(
                                                userThatYouWantToTradeWith.idLong,
                                                now,
                                                TransactionType.LORI_COOL_CARDS,
                                                tradePlayer1Sonhos,
                                                StoredLoriCoolCardsPaymentSonhosTradeTransaction(
                                                    selfUser.idLong,
                                                    userThatYouWantToTradeWith.idLong,
                                                    acceptedTradeId
                                                )
                                            )
                                        }

                                        if (tradePlayer2Sonhos > 0L) {
                                            Profiles.update({ Profiles.id eq selfUser.idLong }) {
                                                with(SqlExpressionBuilder) {
                                                    it.update(money, money + tradePlayer2Sonhos)
                                                }
                                            }

                                            Profiles.update({ Profiles.id eq userThatYouWantToTradeWith.idLong }) {
                                                with(SqlExpressionBuilder) {
                                                    it.update(money, money - tradePlayer2Sonhos)
                                                }
                                            }

                                            // Cinnamon transactions log
                                            SimpleSonhosTransactionsLogUtils.insert(
                                                selfUser.idLong,
                                                now,
                                                TransactionType.LORI_COOL_CARDS,
                                                tradePlayer2Sonhos,
                                                StoredLoriCoolCardsPaymentSonhosTradeTransaction(
                                                    userThatYouWantToTradeWith.idLong,
                                                    selfUser.idLong,
                                                    acceptedTradeId
                                                )
                                            )

                                            SimpleSonhosTransactionsLogUtils.insert(
                                                userThatYouWantToTradeWith.idLong,
                                                now,
                                                TransactionType.LORI_COOL_CARDS,
                                                tradePlayer2Sonhos,
                                                StoredLoriCoolCardsPaymentSonhosTradeTransaction(
                                                    userThatYouWantToTradeWith.idLong,
                                                    selfUser.idLong,
                                                    acceptedTradeId
                                                )
                                            )
                                        }

                                        markAsSeen(
                                            now,
                                            selfUser,
                                            stickerIdsByPlayer2ToBeGivenToPlayer1MappedToEventStickerId
                                        )
                                        markAsSeen(
                                            now,
                                            userThatYouWantToTradeWith,
                                            stickerIdsByPlayer1ToBeGivenToPlayer2MappedToEventStickerId
                                        )

                                        return@transaction TradeStickerResult.Success(
                                            stickersToBeGivenByPlayer2,
                                            tradePlayer2Sonhos,
                                            stickersToBeGivenByPlayer1,
                                            tradePlayer1Sonhos
                                        )
                                    }

                                    when (response) {
                                        TradeStickerResult.EventUnavailable -> {
                                            context.reply(false) {
                                                styled(
                                                    "Nenhum evento de figurinhas ativo"
                                                )
                                            }
                                        }
                                        is TradeStickerResult.UnknownCard -> {
                                            context.reply(false) {
                                                styled(
                                                    context.i18nContext.get(I18N_PREFIX.UserIsTryingToGiveAnUnknownSticker(response.user.asMention))
                                                )
                                            }
                                        }
                                        is TradeStickerResult.NotEnoughCards -> {
                                            context.reply(false) {
                                                styled(
                                                    context.i18nContext.get(I18N_PREFIX.UserDoesNotHaveEnoughStickers(response.user.asMention, response.stickersMissing.joinToString { "`${it[LoriCoolCardsEventCards.fancyCardId]}`" }))
                                                )
                                            }
                                        }
                                        is TradeStickerResult.TryingToGiveStickersThatArentStickedYet -> {
                                            context.reply(false) {
                                                styled(
                                                    context.i18nContext.get(I18N_PREFIX.UserIsTryingToGiveStickersThatArentStickedYet(response.user.asMention, response.stickersMissing.joinToString { "`${it[LoriCoolCardsEventCards.fancyCardId]}`" }))
                                                )
                                            }
                                        }
                                        is TradeStickerResult.NotEnoughSonhos -> {
                                            context.reply(false) {
                                                styled(
                                                    context.i18nContext.get(I18N_PREFIX.UserDoesNotHaveEnoughSonhosForTheTrade(response.user.asMention)),
                                                    Emotes.LoriBonk
                                                )
                                            }
                                        }
                                        TradeStickerResult.CantTradeSameStickers -> {
                                            context.reply(false) {
                                                styled(
                                                    context.i18nContext.get(I18N_PREFIX.CantTradeSameStickers),
                                                    Emotes.LoriBonk
                                                )
                                            }
                                        }
                                        is TradeStickerResult.Success -> {
                                            context.reply(false) {
                                                styled(
                                                    "**${context.i18nContext.get(I18N_PREFIX.TradeSuccessful)}**",
                                                    Emotes.Handshake
                                                )

                                                if (response.givenStickersToPlayer1.isNotEmpty()) {
                                                    styled(
                                                        context.i18nContext.get(
                                                            I18N_PREFIX.TradePlayerReceivedStickers(selfUser.asMention, response.givenStickersToPlayer1.joinToString { it[LoriCoolCardsEventCards.fancyCardId] }),
                                                        ),
                                                        user1Emote
                                                    )
                                                }

                                                if (response.givenSonhosToPlayer1 != 0L) {
                                                    styled(
                                                        context.i18nContext.get(
                                                            I18N_PREFIX.TradePlayerReceivedSonhos(selfUser.asMention, SonhosUtils.getSonhosEmojiOfQuantity(response.givenSonhosToPlayer1), response.givenSonhosToPlayer1)
                                                        ),
                                                        user1Emote
                                                    )
                                                }

                                                if (response.givenStickersToPlayer2.isNotEmpty()) {
                                                    styled(
                                                        context.i18nContext.get(
                                                            I18N_PREFIX.TradePlayerReceivedStickers(userThatYouWantToTradeWith.asMention, response.givenStickersToPlayer2.joinToString { it[LoriCoolCardsEventCards.fancyCardId] }),
                                                        ),
                                                        user2Emote
                                                    )
                                                }

                                                if (response.givenSonhosToPlayer2 != 0L) {
                                                    styled(
                                                        context.i18nContext.get(
                                                            I18N_PREFIX.TradePlayerReceivedSonhos(userThatYouWantToTradeWith.asMention, SonhosUtils.getSonhosEmojiOfQuantity(response.givenSonhosToPlayer1), response.givenSonhosToPlayer2)
                                                        ),
                                                        user2Emote
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    context.giveAchievementAndNotify(selfUser, AchievementType.DEAL_ACCEPTED, false)
                                    context.giveAchievementAndNotify(userThatYouWantToTradeWith, AchievementType.DEAL_ACCEPTED, false)
                                    return@button
                                }

                                context.editMessage(
                                    true,
                                    MessageEdit {
                                        apply(createTradeMessage())
                                    }
                                )

                                context.reply(true) {
                                    styled(
                                        context.i18nContext.get(
                                            I18N_PREFIX.TradeAccepted
                                        ),
                                        Emotes.LoriCoolSticker
                                    )
                                }
                            }
                        }
                    }
                else
                    acceptTradeButton.asDisabled()
            )
        }

        context.reply(false) {
            apply(createTradeMessage())
        }
    }

    private fun createTradeOfferEmbedStatus(
        user: User,
        usersThatHaveConfirmedTheTrade: Set<User>,
        tradeOfferThings: TradeThings,
        userEmoji: DiscordEmote,
        emptyFunnyMessage: String
    ): InlineEmbed.() -> (Unit) = {
        val hasConfirmedTheTrade = usersThatHaveConfirmedTheTrade.contains(user)

        val lockStatus = if (hasConfirmedTheTrade)
            "\uD83D\uDD12"
        else
            "\uD83D\uDD13"

        author(user.name, null, user.effectiveAvatarUrl)
        title = "$userEmoji$lockStatus ${user.name}"
        if (hasConfirmedTheTrade)
            color = Color(35, 165, 90).rgb
        description = buildString {
            if (tradeOfferThings.sonhos != null) {
                appendLine("${Emotes.Sonhos2} ${tradeOfferThings.sonhos} sonhos")
            }

            if (tradeOfferThings.stickerFancyIds.isNotEmpty()) {
                appendLine("${Emotes.LoriCoolSticker} (${tradeOfferThings.stickerFancyIds.size}x) `${tradeOfferThings.stickerFancyIds.sorted().joinToString()}`")
            }
        }.ifEmpty { "*${emptyFunnyMessage}*" }
    }

    private fun getEventStickersFromStickerFancyIds(eventId: Long, stickerFancyIds: List<String>): List<ResultRow> {
        // noop
        if (stickerFancyIds.isEmpty())
            return emptyList()

        return LoriCoolCardsEventCards.selectAll()
            .where {
                LoriCoolCardsEventCards.fancyCardId inList stickerFancyIds and (LoriCoolCardsEventCards.event eq eventId)
            }.toList()
    }

    private fun giveStickersToUser(
        now: Instant,
        user: User,
        eventId: Long,
        stickerIdsToBeGiven: List<Long>
    ) {
        // Insert the new ones WITH BATCH INSERT BECAUSE WE ARE BUILT LIKE THAT!!!
        LoriCoolCardsUserOwnedCards.batchInsert(
            stickerIdsToBeGiven,
            shouldReturnGeneratedValues = false
        ) {
            this[LoriCoolCardsUserOwnedCards.card] = it
            this[LoriCoolCardsUserOwnedCards.user] = user.idLong
            this[LoriCoolCardsUserOwnedCards.event] = eventId
            this[LoriCoolCardsUserOwnedCards.receivedAt] = now
            this[LoriCoolCardsUserOwnedCards.sticked] = false
        }
    }

    private fun getUserSonhos(user: User): Long {
        return Profiles.select(Profiles.money).where {
            Profiles.id eq user.idLong
        }.firstOrNull()?.get(Profiles.money) ?: 0
    }

    private fun verifyStickers(
        user: User,
        stickersToBeGiven: List<ResultRow>,
    ): StickerVerificationResult {
        // noop
        if (stickersToBeGiven.isEmpty())
            return StickerVerificationResult.Success(listOf(), listOf())

        val eventId = stickersToBeGiven.first()[LoriCoolCardsEventCards.event].value

        val event = LoriCoolCardsEvents.selectAll().where {
            LoriCoolCardsEvents.id eq eventId
        }.first()

        val matchResult = LoriCoolCardsGiveStickersExecutor.matchStickers(
            user.idLong,
            event,
            stickersToBeGiven
        )

        // Check if there are missing stickers
        if (matchResult.missingStickers.isNotEmpty())
            return StickerVerificationResult.NotEnoughCards(matchResult.missingStickers)

        // Check if there are stickers that aren't sticked yet but are trying to be given
        if (matchResult.stickersThatArentStickedButAreTryingToBeGiven.isNotEmpty())
            return StickerVerificationResult.TryingToGiveStickersThatArentStickedYet(matchResult.stickersThatArentStickedButAreTryingToBeGiven)

        val stickerIdsToBeGivenMappedToOwnedStickerId = matchResult.stickerIdsToBeGivenMappedToSticker.values.map { it[LoriCoolCardsUserOwnedCards.id].value }
        val stickerIdsToBeGivenMappedToEventStickerId = matchResult.stickerIdsToBeGivenMappedToSticker.values.map { it[LoriCoolCardsEventCards.id].value }

        return StickerVerificationResult.Success(
            stickerIdsToBeGivenMappedToOwnedStickerId,
            stickerIdsToBeGivenMappedToEventStickerId
        )
    }

    sealed class StickerVerificationResult {
        data class NotEnoughCards(val missingStickers: List<ResultRow>) : StickerVerificationResult()
        data class TryingToGiveStickersThatArentStickedYet(val stickersMissing: List<ResultRow>) : StickerVerificationResult()
        data class Success(
            val stickerIdsToBeGivenMappedToOwnedStickerId: List<Long>,
            val stickerIdsToBeGivenMappedToEventStickerId: List<Long>
        ) : StickerVerificationResult()
    }

    private fun markAsSeen(
        now: Instant,
        userThatWillReceiveTheSticker: User,
        stickerIdsToBeGivenMappedToEventStickerId: List<Long>
    ) {
        // Now that we selected the cards, we will mark them as seen + owned
        // OPTIMIZATION: Get all seen stickers beforehand, this way we don't need to do an individual select for each sticker
        val stickersThatWeHaveAlreadySeenBeforeBasedOnTheSelectedStickers =
            LoriCoolCardsSeenCards.select(
                LoriCoolCardsSeenCards.card
            ).where {
                LoriCoolCardsSeenCards.card inList stickerIdsToBeGivenMappedToEventStickerId and (LoriCoolCardsSeenCards.user eq userThatWillReceiveTheSticker.idLong)
            }.map { it[LoriCoolCardsSeenCards.card].value }

        val stickersIdsThatWeHaveNotSeenBefore = mutableListOf<Long>()
        for (eventStickerId in stickerIdsToBeGivenMappedToEventStickerId) {
            if (eventStickerId !in stickersThatWeHaveAlreadySeenBeforeBasedOnTheSelectedStickers)
                stickersIdsThatWeHaveNotSeenBefore.add(eventStickerId)
        }

        if (stickersIdsThatWeHaveNotSeenBefore.isNotEmpty()) {
            // "Seen cards" just mean that the card won't be unknown (???) when the user looks it up, even if they give the card away
            LoriCoolCardsSeenCards.batchInsert(
                stickersIdsThatWeHaveNotSeenBefore,
                shouldReturnGeneratedValues = false
            ) {
                this[LoriCoolCardsSeenCards.card] = it
                this[LoriCoolCardsSeenCards.user] = userThatWillReceiveTheSticker.idLong
                this[LoriCoolCardsSeenCards.seenAt] = now
            }
        }
    }

    @Serializable
    class TradeOffer(
        var player1: TradeThings,
        var player2: TradeThings,
        val hasGiveArbitraryStickerCountSupport: Boolean
    ) {
        fun isTradeValid(): Boolean {
            // A trade cannot be a only sonhos trade
            if (player1.sonhos != null && player2.sonhos != null)
                return false

            if (!hasGiveArbitraryStickerCountSupport && player1.stickerFancyIds.isNotEmpty() && player2.stickerFancyIds.isNotEmpty() && player1.stickerFancyIds.size != player2.stickerFancyIds.size)
                return false

            return player1.isTradeValid() && player2.isTradeValid()
        }
    }

    @Serializable
    class TradeThings(
        val stickerFancyIds: MutableList<String>,
        var sonhos: Long?
    ) {
        fun isTradeValid(): Boolean {
            val sonhos = sonhos ?: 0
            val isInvalid = stickerFancyIds.isEmpty() && (0 >= sonhos)
            return !isInvalid
        }
    }

    sealed class SetStickersResult {
        data object EventUnavailable : SetStickersResult()
        data object UnknownCard : SetStickersResult()
        data class YouDidntBuyEnoughBoosterPacks(val requiredPacks: Int, val currentPacks: Long) : SetStickersResult()
        data class ReceiverDidntBuyEnoughBoosterPacks(val requiredPacks: Int, val currentPacks: Long) : SetStickersResult()
        data class NotEnoughCards(val stickersMissing: List<ResultRow>) : SetStickersResult()
        data class TryingToGiveStickersThatArentStickedYet(val stickersMissing: List<ResultRow>) : SetStickersResult()
        data class Success(val stickerFancyIds: List<String>) : SetStickersResult()
    }

    sealed class SetStickersFillResult {
        data object EventUnavailable : SetStickersFillResult()
        data class YouDidntBuyEnoughBoosterPacks(val requiredPacks: Int, val currentPacks: Long) : SetStickersFillResult()
        data class ReceiverDidntBuyEnoughBoosterPacks(val requiredPacks: Int, val currentPacks: Long) : SetStickersFillResult()
        data class TryingToGiveMoreStickersThatYouHave(val maxStickers: Int) : SetStickersFillResult()
        data class Success(val stickerFancyIds: List<String>) : SetStickersFillResult()
    }

    sealed class SetSonhosResult {
        data object EventUnavailable : SetSonhosResult()
        data class NotEnoughSonhos(val userSonhos: Long, val howMuch: Long) : SetSonhosResult()
        data class YouDidntBuyEnoughBoosterPacks(val requiredPacks: Int, val currentPacks: Long) : SetSonhosResult()
        data class ReceiverDidntBuyEnoughBoosterPacks(val requiredPacks: Int, val currentPacks: Long) : SetSonhosResult()
        data object Success : SetSonhosResult()
    }

    sealed class TradeStickerResult {
        data object EventUnavailable : TradeStickerResult()
        data object CantTradeSameStickers : TradeStickerResult()
        data class UnknownCard(val user: User) : TradeStickerResult()
        data class NotEnoughSonhos(val user: User) : TradeStickerResult()
        data class NotEnoughCards(val user: User, val stickersMissing: List<ResultRow>) : TradeStickerResult()
        data class TryingToGiveStickersThatArentStickedYet(val user: User, val stickersMissing: List<ResultRow>) : TradeStickerResult()
        data class Success(
            val givenStickersToPlayer1: List<ResultRow>,
            val givenSonhosToPlayer1: Long,
            val givenStickersToPlayer2: List<ResultRow>,
            val givenSonhosToPlayer2: Long,
        ) : TradeStickerResult()
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?>? {
        TODO("Not yet implemented")
    }
}
