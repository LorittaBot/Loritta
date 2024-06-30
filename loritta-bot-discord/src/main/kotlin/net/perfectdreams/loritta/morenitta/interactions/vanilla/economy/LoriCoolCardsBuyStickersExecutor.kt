package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.LoadingEmojis
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils.appendUserHaventGotDailyTodayOrUpsellSonhosBundles
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.*
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
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
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import net.perfectdreams.loritta.morenitta.loricoolcards.StickerAlbumTemplate
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.serializable.StoredLoriCoolCardsBoughtBoosterPackSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.*
import java.time.Instant
import kotlin.random.Random
import kotlin.time.measureTimedValue

class LoriCoolCardsBuyStickersExecutor(val loritta: LorittaBot, private val loriCoolCardsCommand: LoriCoolCardsCommand) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Loricoolcards.Buy
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.deferChannelMessage(false)

        buyStickers(context)
    }

    suspend fun buyStickers(context: UnleashedContext) {
        if (SonhosUtils.checkIfEconomyIsDisabled(context))
            return

        // We expect that this is already deferred by the caller
        val now = Instant.now()

        logger.info { "User ${context.user.idLong} is *starting* to buy a booster pack! Let's get the event info..." }

        // Load the current active event
        val (result, time) = measureTimedValue {
            loritta.transaction {
                // First we will get the active cards event to get the album template
                val event = LoriCoolCardsEvents.select {
                    LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
                }.firstOrNull() ?: return@transaction BuyStickersPreResult.EventUnavailable

                BuyStickersPreResult.Success(Json.decodeFromString(event[LoriCoolCardsEvents.template]))
            }
        }

        logger.info { "Got event info information for ${context.user.idLong}'s *starting* to buy a booster pack thingy! - Took $time" }

        when (result) {
            BuyStickersPreResult.EventUnavailable -> {
                context.reply(false) {
                    styled(
                        "Nenhum evento de figurinhas ativo"
                    )
                }
            }
            is BuyStickersPreResult.Success -> {
                val buyBoosterPack1xButton = UnleashedButton.of(
                    ButtonStyle.PRIMARY,
                    context.i18nContext.get(I18N_PREFIX.BuyAndOpenBoosterPack(1, result.template.sonhosPrice))
                )

                val buyBoosterPack2xButton = UnleashedButton.of(
                    ButtonStyle.PRIMARY,
                    context.i18nContext.get(I18N_PREFIX.BuyAndOpenBoosterPack(2, result.template.sonhosPrice * 2))
                )

                val buyBoosterPack3xButton = UnleashedButton.of(
                    ButtonStyle.PRIMARY,
                    context.i18nContext.get(I18N_PREFIX.BuyAndOpenBoosterPack(3, result.template.sonhosPrice * 3))
                )

                val buyBoosterPack4xButton = UnleashedButton.of(
                    ButtonStyle.PRIMARY,
                    context.i18nContext.get(I18N_PREFIX.BuyAndOpenBoosterPack(4, result.template.sonhosPrice * 4))
                )

                val buyBoosterPack5xButton = UnleashedButton.of(
                    ButtonStyle.PRIMARY,
                    context.i18nContext.get(I18N_PREFIX.BuyAndOpenBoosterPack(5, result.template.sonhosPrice * 5))
                )

                val clickCallback: suspend (ComponentContext, Int) -> (Unit) = { it, stickerPacksCount ->
                    val future = it.editMessageAsync {
                        actionRow(
                            buyBoosterPack1xButton
                                .withEmoji(LoadingEmojis.random().toJDA())
                                .asDisabled()
                        )
                    }

                    logger.info { "User ${context.user.idLong} is buying a booster pack! Giving stickers and stuff..." }

                    val (result, time) = measureTimedValue {
                        loritta.transaction {
                            // First we will get the active cards event
                            // OPTIMIZATION: Only get the event ID, we don't need the rest of the things (like the template data) anyway
                            val event = LoriCoolCardsEvents.slice(LoriCoolCardsEvents.id).select {
                                LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
                            }.firstOrNull() ?: return@transaction BuyStickersResult.EventUnavailable

                            // Check if we have enough money to buy the booster pack
                            val userProfile = loritta.getOrCreateLorittaProfile(context.user.idLong)
                            val priceOfAllStickerPacksThatTheUserWants = result.template.sonhosPrice * stickerPacksCount
                            if (priceOfAllStickerPacksThatTheUserWants * stickerPacksCount > userProfile.money)
                                return@transaction BuyStickersResult.NotEnoughSonhos(userProfile.money, priceOfAllStickerPacksThatTheUserWants)

                            Profiles.update({ Profiles.id eq context.user.idLong }) {
                                with(SqlExpressionBuilder) {
                                    it.update(Profiles.money, Profiles.money - priceOfAllStickerPacksThatTheUserWants)
                                }
                            }

                            // This is hard actually, because we need to calculate the chance for each rarity
                            // So we will select everything (ew) and then randomize from there
                            val cards = LoriCoolCardsEventCards.select {
                                LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id]
                            }.toList()

                            // Now we need to calculate each probability
                            val stickerIdsWithWeights = cards.associate {
                                it[LoriCoolCardsEventCards.id].value to result.template.stickerProbabilityWeights[it[LoriCoolCardsEventCards.rarity]]!!
                            }

                            // TODO: This should consider stickers that are already sticked in the album
                            // We also need to calculate how many cards the user had before starting
                            val cardDistinctField = LoriCoolCardsUserOwnedCards.card.countDistinct()
                            val unmodifiableCountBeforeAddingCards = LoriCoolCardsUserOwnedCards.slice(cardDistinctField)
                                .select {
                                    LoriCoolCardsUserOwnedCards.user eq context.user.idLong and (LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id])
                                }.first()[cardDistinctField]
                            val selectedCardsWithMetadata = mutableListOf<BuyStickersResult.Success.CardResult>()

                            // Technically this is *bad* for performance, but it is what it is rn
                            repeat(stickerPacksCount) {
                                val boosterPackId = LoriCoolCardsUserBoughtBoosterPacks.insertAndGetId {
                                    it[LoriCoolCardsUserBoughtBoosterPacks.user] = context.user.idLong
                                    it[LoriCoolCardsUserBoughtBoosterPacks.event] = event[LoriCoolCardsEvents.id]
                                    it[LoriCoolCardsUserBoughtBoosterPacks.boughtAt] = Instant.now()
                                }

                                // Cinnamon transactions log
                                SimpleSonhosTransactionsLogUtils.insert(
                                    context.user.idLong,
                                    now,
                                    TransactionType.LORI_COOL_CARDS,
                                    result.template.sonhosPrice,
                                    StoredLoriCoolCardsBoughtBoosterPackSonhosTransaction(
                                        event[LoriCoolCardsEvents.id].value,
                                        boosterPackId.value
                                    )
                                )

                                val selectedStickersIds = weightedRandomSelection(stickerIdsWithWeights, result.template.stickersInPack)
                                // We use "map" instead of filter because we WANT duplicated stickers
                                val selectedCards = selectedStickersIds.map { stickerId -> cards.first { it[LoriCoolCardsEventCards.id].value == stickerId } }

                                // Now that we selected the cards, we will mark them as seen + owned
                                // OPTIMIZATION: Get all seen stickers beforehand, this way we don't need to do an individual select for each sticker
                                val stickersThatWeHaveAlreadySeenBeforeBasedOnTheSelectedStickers = LoriCoolCardsSeenCards.slice(LoriCoolCardsSeenCards.card).select {
                                    LoriCoolCardsSeenCards.card inList selectedStickersIds and (LoriCoolCardsSeenCards.user eq context.user.idLong)
                                }.map { it[LoriCoolCardsSeenCards.card] }

                                // OPTIMIZATION: Don't get each sticker count in the loop, get all of them at once
                                val stickerCount = LoriCoolCardsUserOwnedCards.card.count()
                                val howManyStickersOfTheseStickersCardIdWeHave = LoriCoolCardsUserOwnedCards.slice(LoriCoolCardsUserOwnedCards.card, stickerCount).select {
                                    LoriCoolCardsUserOwnedCards.user eq context.user.idLong and (LoriCoolCardsUserOwnedCards.card inList selectedStickersIds)
                                }.groupBy(LoriCoolCardsUserOwnedCards.card)
                                    .associate { it[LoriCoolCardsUserOwnedCards.card].value to it[stickerCount] }
                                    .toMutableMap()

                                for (card in selectedCards) {
                                    // Have we already seen this card before?
                                    val haveWeAlreadySeenThisCardBefore = card[LoriCoolCardsEventCards.id] in stickersThatWeHaveAlreadySeenBeforeBasedOnTheSelectedStickers

                                    LoriCoolCardsUserOwnedCards.insert {
                                        it[LoriCoolCardsUserOwnedCards.card] = card[LoriCoolCardsEventCards.id]
                                        it[LoriCoolCardsUserOwnedCards.user] = context.user.idLong
                                        it[LoriCoolCardsUserOwnedCards.event] = event[LoriCoolCardsEvents.id]
                                        it[LoriCoolCardsUserOwnedCards.receivedAt] = now
                                        it[LoriCoolCardsUserOwnedCards.sticked] = false
                                        it[LoriCoolCardsUserOwnedCards.boosterPack] = boosterPackId
                                    }

                                    // "Seen cards" just mean that the card won't be unknown (???) when the user looks it up, even if they give the card away
                                    if (!haveWeAlreadySeenThisCardBefore) {
                                        LoriCoolCardsSeenCards.insert {
                                            it[LoriCoolCardsSeenCards.card] = card[LoriCoolCardsEventCards.id]
                                            it[LoriCoolCardsSeenCards.user] = context.user.idLong
                                            it[LoriCoolCardsSeenCards.seenAt] = now
                                        }
                                    }

                                    // Count how many cards of this specific type we have
                                    val howManyCardsOfThisCardIdWeHave = (howManyStickersOfTheseStickersCardIdWeHave[card[LoriCoolCardsEventCards.id].value] ?: 0) + 1 // If not present, then it means we had 0 stickers (+1, because now we have one of them (yay))
                                    howManyStickersOfTheseStickersCardIdWeHave[card[LoriCoolCardsEventCards.id].value] = howManyCardsOfThisCardIdWeHave // Update the map (required because there may be duplicate stickers)

                                    // We also need to calculate how many cards the user now has for each "step" of the journey
                                    val unmodifiableCount = LoriCoolCardsUserOwnedCards.slice(cardDistinctField)
                                        .select {
                                            LoriCoolCardsUserOwnedCards.user eq context.user.idLong and (LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id])
                                        }.first()[cardDistinctField]

                                    selectedCardsWithMetadata.add(
                                        BuyStickersResult.Success.CardResult(
                                            card,
                                            howManyCardsOfThisCardIdWeHave,
                                            unmodifiableCount,
                                            haveWeAlreadySeenThisCardBefore
                                        )
                                    )
                                }
                            }

                            val alreadyStickedCardsCount = LoriCoolCardsUserOwnedCards.innerJoin(LoriCoolCardsEventCards).select {
                                LoriCoolCardsUserOwnedCards.sticked eq true and (LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id]) and (LoriCoolCardsUserOwnedCards.user eq context.user.idLong)
                            }.count()

                            BuyStickersResult.Success(cards.size, unmodifiableCountBeforeAddingCards, selectedCardsWithMetadata, alreadyStickedCardsCount)
                        }
                    }

                    logger.info { "User ${context.user.idLong} bought a booster pack! - Took $time" }

                    val hook = future.await()

                    when (result) {
                        BuyStickersResult.EventUnavailable -> {
                            context.reply(true) {
                                styled(
                                    "Nenhum evento de figurinhas ativo"
                                )
                            }
                        }
                        is BuyStickersResult.NotEnoughSonhos -> {
                            context.reply(true) {
                                styled(
                                    context.i18nContext.get(SonhosUtils.insufficientSonhos(result.userSonhos, result.howMuch)),
                                    Emotes.LoriSob
                                )

                                appendUserHaventGotDailyTodayOrUpsellSonhosBundles(
                                    context.loritta,
                                    context.i18nContext,
                                    UserId(context.user.idLong),
                                    "lori-cool-cards",
                                    "buy-booster-pack-not-enough-sonhos"
                                )
                            }
                        }
                        is BuyStickersResult.Success -> {
                            val cards = result.cards.toMutableList()

                            suspend fun getCurrentStickerAndCreateMessage(): InlineMessage<*>.() -> (Unit) {
                                val currentCard = cards.removeFirst()

                                // val discordInfo = loritta.lorittaShards.retrieveUserInfoById(currentProfileId)
                                // val backgroundUrl = loritta.profileDesignManager.getUserProfileBackgroundUrl(currentProfileId)

                                return {
                                    embed {
                                        val title = currentCard.card[LoriCoolCardsEventCards.title]
                                        val cardId = currentCard.card[LoriCoolCardsEventCards.fancyCardId]
                                        val cardReceivedImageUrl = currentCard.card[LoriCoolCardsEventCards.cardReceivedImageUrl]

                                        if (currentCard.haveWeAlreadySeenThisCardBefore) {
                                            this.title = "${currentCard.card[LoriCoolCardsEventCards.rarity].emoji} $cardId - $title"
                                        } else {
                                            this.title = "${currentCard.card[LoriCoolCardsEventCards.rarity].emoji} **[NOVO!]** $cardId - $title"
                                        }

                                        this.description = "${context.i18nContext.get(I18N_PREFIX.NowYouHaveXStickersOfThisType(currentCard.howManyCardsOfThisCardIdWeHave))}\n${context.i18nContext.get(I18N_PREFIX.AlbumProgress(currentCard.totalAlbumCompletionCount, result.albumCardsCount))}"
                                        this.color = currentCard.card[LoriCoolCardsEventCards.rarity].color.rgb
                                        // image = backgroundUrl

                                        this.image = cardReceivedImageUrl
                                    }

                                    if (cards.isNotEmpty()) {
                                        actionRow(
                                            loritta.interactivityManager.buttonForUser(
                                                context.user,
                                                ButtonStyle.PRIMARY,
                                                context.i18nContext.get(I18N_PREFIX.NextSticker(result.cards.size - cards.size, result.cards.size)),
                                                {
                                                    this.loriEmoji = Emotes.LoriCoolSticker
                                                }
                                            ) {
                                                // We don't need to defer here because the getCurrentProfileIdAndCreateMessage does not do any database related things here!
                                                // (if it does, then we need to update the code)
                                                it.event.editMessage(MessageEdit { apply(getCurrentStickerAndCreateMessage()) })
                                                    .apply {
                                                        this.isReplace = true
                                                    }
                                                    .await()
                                            }
                                        )
                                    } else {
                                        actionRow(
                                            loritta.interactivityManager.buttonForUser(
                                                context.user,
                                                ButtonStyle.PRIMARY,
                                                context.i18nContext.get(I18N_PREFIX.ViewSummary),
                                                {
                                                    this.loriEmoji = Emotes.LoriReading
                                                }
                                            ) {
                                                val groupedCards = result.cards.groupBy { it.card[LoriCoolCardsEventCards.id] }
                                                    .toList()
                                                    .sortedByDescending { it.second.size }

                                                val stickStickersButton = UnleashedButton.of(
                                                    ButtonStyle.PRIMARY,
                                                    context.i18nContext.get(I18N_PREFIX.StickStickers),
                                                    Emotes.LoriCoolSticker
                                                )

                                                val buyMoreStickerPacksButton = UnleashedButton.of(
                                                    ButtonStyle.SECONDARY,
                                                    context.i18nContext.get(I18N_PREFIX.BuyStickerBoosterPack),
                                                    Emotes.LoriCard
                                                )

                                                // Don't defer, let's edit the original message directly because we don't need to access the database here
                                                it.editMessage(
                                                    isReplace = false,
                                                ) {
                                                    embed {
                                                        title = "${Emotes.LoriCoolSticker} ${context.i18nContext.get(I18N_PREFIX.Summary)}"
                                                        color = LorittaColors.LorittaAqua.rgb

                                                        val description = buildString {
                                                            groupedCards.forEach {
                                                                val cardReference = it.second.first().card
                                                                append("* ${it.second.size}x ${cardReference[LoriCoolCardsEventCards.rarity].emoji} ${cardReference[LoriCoolCardsEventCards.fancyCardId]} - ${cardReference[LoriCoolCardsEventCards.title]}")
                                                                if (!it.second.any { it.haveWeAlreadySeenThisCardBefore })
                                                                    append(" **[NOVO!]**")
                                                                appendLine()
                                                            }

                                                            appendLine("**Progresso do Álbum:** ${currentCard.totalAlbumCompletionCount}/${result.albumCardsCount} figurinhas (+${currentCard.totalAlbumCompletionCount - result.totalAlbumCompletionCountBeforeBuying})")
                                                            appendLine("**Progresso do Álbum:** ${result.alreadyStickedCardsCount}/${result.albumCardsCount} figurinhas coladas")
                                                        }

                                                        this.description = description
                                                    }

                                                    if (result.alreadyStickedCardsCount != currentCard.totalAlbumCompletionCount) {
                                                        // If the number is different, then it means that we have new stickers to be sticked!
                                                        actionRow(
                                                            loritta.interactivityManager
                                                                .buttonForUser(
                                                                    context.user,
                                                                    stickStickersButton
                                                                ) {
                                                                    loriCoolCardsCommand.stickStickers.stickStickers(it)
                                                                },
                                                            loritta.interactivityManager
                                                                .buttonForUser(
                                                                    context.user,
                                                                    buyMoreStickerPacksButton
                                                                ) {
                                                                    it.deferChannelMessage(false)

                                                                    buyStickers(it)
                                                                }
                                                        )
                                                    } else {
                                                        // If not, just disable the button
                                                        actionRow(
                                                            stickStickersButton.asDisabled(),
                                                            loritta.interactivityManager
                                                                .buttonForUser(
                                                                    context.user,
                                                                    buyMoreStickerPacksButton
                                                                ) {
                                                                    it.deferChannelMessage(false)

                                                                    buyStickers(it)
                                                                }
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            hook.editOriginal(MessageEdit { apply(getCurrentStickerAndCreateMessage()) })
                                .apply {
                                    this.isReplace = true
                                }
                                .await()
                        }
                    }
                }

                context.reply(false) {
                    embed {
                        title = "${Emotes.LoriCoolSticker} Pacote de Figurinhas"
                        description = "${Emotes.Scissors} ${context.i18nContext.get(I18N_PREFIX.HowManyBoosterPacksDoYouWantToBuy)}"
                        color = LorittaColors.LorittaAqua.rgb

                        image = result.template.stickerPackImageUrl
                    }

                    actionRow(
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            buyBoosterPack1xButton
                        ) {
                            clickCallback.invoke(it, 1)
                        },
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            buyBoosterPack2xButton
                        ) {
                            clickCallback.invoke(it, 2)
                        },
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            buyBoosterPack3xButton
                        ) {
                            clickCallback.invoke(it, 3)
                        },
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            buyBoosterPack4xButton
                        ) {
                            clickCallback.invoke(it, 4)
                        },
                        loritta.interactivityManager.buttonForUser(
                            context.user,
                            buyBoosterPack5xButton
                        ) {
                            clickCallback.invoke(it, 5)
                        }
                    )
                }
            }
        }
    }

    // Thanks ChatGPT xoxo
    private fun weightedRandomSelection(weights: Map<Long, Double>, n: Int): List<Long> {
        val weightedValues = ArrayList<Pair<Long, Double>>(weights.size)
        var totalWeight = 0.0

        for ((rarity, weight) in weights) {
            totalWeight += weight
            weightedValues.add(rarity to totalWeight)
        }

        return List(n) {
            val randomValue = Random.nextDouble(totalWeight)
            binarySearch(weightedValues, randomValue)
        }
    }

    private fun binarySearch(weightedValues: List<Pair<Long, Double>>, randomValue: Double): Long {
        var low = 0
        var high = weightedValues.size - 1

        while (low < high) {
            val mid = (low + high) / 2
            if (weightedValues[mid].second < randomValue)
                low = mid + 1
            else
                high = mid
        }
        return weightedValues[low].first
    }

    sealed class BuyStickersPreResult {
        data object EventUnavailable : BuyStickersPreResult()
        class Success(
            val template: StickerAlbumTemplate
        ) : BuyStickersPreResult()
    }

    sealed class BuyStickersResult {
        data object EventUnavailable : BuyStickersResult()
        class NotEnoughSonhos(val userSonhos: Long, val howMuch: Long) : BuyStickersResult()
        class Success(
            val albumCardsCount: Int,
            val totalAlbumCompletionCountBeforeBuying: Long,
            val cards: List<CardResult>,
            val alreadyStickedCardsCount: Long
        ) : BuyStickersResult() {
            class CardResult(
                val card: ResultRow,
                val howManyCardsOfThisCardIdWeHave: Long,
                val totalAlbumCompletionCount: Long,
                val haveWeAlreadySeenThisCardBefore: Boolean
            )
        }
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?>? {
        return mapOf()
    }
}