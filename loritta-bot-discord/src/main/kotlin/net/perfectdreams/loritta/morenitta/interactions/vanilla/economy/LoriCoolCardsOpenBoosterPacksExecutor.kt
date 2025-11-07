package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.*
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.serialization.json.Json
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.separator.Separator
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.*
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.loricoolcards.StickerAlbumTemplate
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import java.time.Instant
import kotlin.random.Random
import kotlin.time.measureTimedValue

class LoriCoolCardsOpenBoosterPacksExecutor(val loritta: LorittaBot, private val loriCoolCardsCommand: LoriCoolCardsCommand) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Loricoolcards.Buy
        private val logger by HarmonyLoggerFactory.logger {}
    }

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.deferChannelMessage(false)

        openBoosterPacks(context)
    }

    suspend fun openBoosterPacks(context: UnleashedContext) {
        if (SonhosUtils.checkIfEconomyIsDisabled(context))
            return

        // We expect that this is already deferred by the caller
        val now = Instant.now()

        logger.info { "User ${context.user.idLong} is *starting* to buy a booster pack! Let's get the event info..." }

        // Load the current active event
        val (result, time) = measureTimedValue {
            loritta.transaction {
                // First we will get the active cards event to get the album template
                val event = LoriCoolCardsEvents.selectAll().where {
                    LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
                }.firstOrNull() ?: return@transaction OpenBoosterPacksResult.EventUnavailable

                val unopenedBoosterPacks = LoriCoolCardsUserBoughtBoosterPacks.selectAll()
                    .where {
                        LoriCoolCardsUserBoughtBoosterPacks.event eq event[LoriCoolCardsEvents.id] and (LoriCoolCardsUserBoughtBoosterPacks.user eq context.user.idLong) and (LoriCoolCardsUserBoughtBoosterPacks.openedAt.isNull())
                    }
                    .toList()

                // If we don't have any unopened booster packs, bail out!
                if (unopenedBoosterPacks.isEmpty())
                    return@transaction OpenBoosterPacksResult.YouDontHaveAnyPendingBoosterPacks

                val template = Json.decodeFromString<StickerAlbumTemplate>(event[LoriCoolCardsEvents.template])

                // This is hard actually, because we need to calculate the chance for each rarity
                // So we will select everything (ew) and then randomize from there
                val cards = LoriCoolCardsEventCards.selectAll().where {
                    LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id]
                }.toList()

                // Now we need to calculate each probability
                val stickerIdsWithWeights = cards.associate {
                    it[LoriCoolCardsEventCards.id].value to template.stickerProbabilityWeights[it[LoriCoolCardsEventCards.rarity]]!!
                }

                // TODO: This should consider stickers that are already sticked in the album
                // We also need to calculate how many cards the user had before starting
                val cardDistinctField = LoriCoolCardsUserOwnedCards.card.countDistinct()
                val unmodifiableCountBeforeAddingCards = LoriCoolCardsUserOwnedCards.select(cardDistinctField)
                    .where {
                        LoriCoolCardsUserOwnedCards.user eq context.user.idLong and (LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id])
                    }.first()[cardDistinctField]
                val selectedCardsWithMetadata = mutableListOf<OpenBoosterPacksResult.Success.CardResult>()

                // Technically this is *bad* for performance, but it is what it is rn
                for (boosterPackId in unopenedBoosterPacks.map { it[LoriCoolCardsUserBoughtBoosterPacks.id] }) {
                    LoriCoolCardsUserBoughtBoosterPacks.update({ LoriCoolCardsUserBoughtBoosterPacks.id eq boosterPackId }) {
                        it[LoriCoolCardsUserBoughtBoosterPacks.openedAt] = now
                    }

                    val selectedStickersIds = weightedRandomSelection(stickerIdsWithWeights, template.stickersInPack)
                    // We use "map" instead of filter because we WANT duplicated stickers
                    val selectedCards = selectedStickersIds.map { stickerId -> cards.first { it[LoriCoolCardsEventCards.id].value == stickerId } }

                    // Now that we selected the cards, we will mark them as seen + owned
                    // OPTIMIZATION: Get all seen stickers beforehand, this way we don't need to do an individual select for each sticker
                    val stickersThatWeHaveAlreadySeenBeforeBasedOnTheSelectedStickers = LoriCoolCardsSeenCards.select(LoriCoolCardsSeenCards.card).where {
                        LoriCoolCardsSeenCards.card inList selectedStickersIds and (LoriCoolCardsSeenCards.user eq context.user.idLong)
                    }.map { it[LoriCoolCardsSeenCards.card] }

                    // OPTIMIZATION: Don't get each sticker count in the loop, get all of them at once
                    val stickerCount = LoriCoolCardsUserOwnedCards.card.count()
                    val howManyStickersOfTheseStickersCardIdWeHave = LoriCoolCardsUserOwnedCards.select(LoriCoolCardsUserOwnedCards.card, stickerCount).where {
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
                        val unmodifiableCount = LoriCoolCardsUserOwnedCards.select(cardDistinctField)
                            .where {
                                LoriCoolCardsUserOwnedCards.user eq context.user.idLong and (LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id])
                            }.first()[cardDistinctField]

                        selectedCardsWithMetadata.add(
                            OpenBoosterPacksResult.Success.CardResult(
                                card,
                                howManyCardsOfThisCardIdWeHave,
                                unmodifiableCount,
                                haveWeAlreadySeenThisCardBefore
                            )
                        )
                    }
                }

                val alreadyStickedCardsCount = LoriCoolCardsUserOwnedCards.innerJoin(LoriCoolCardsEventCards).selectAll().where {
                    LoriCoolCardsUserOwnedCards.sticked eq true and (LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id]) and (LoriCoolCardsUserOwnedCards.user eq context.user.idLong)
                }.count()

                return@transaction OpenBoosterPacksResult.Success(template, unopenedBoosterPacks.size, cards.size, unmodifiableCountBeforeAddingCards, selectedCardsWithMetadata, alreadyStickedCardsCount)
            }
        }

        logger.info { "Got event info information for ${context.user.idLong}'s *starting* to buy a booster pack thingy! - Took $time" }

        when (result) {
            OpenBoosterPacksResult.EventUnavailable -> {
                context.reply(false) {
                    styled(
                        "Nenhum evento de figurinhas ativo"
                    )
                }
            }
            OpenBoosterPacksResult.YouDontHaveAnyPendingBoosterPacks -> {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18nKeysData.Commands.Command.Loricoolcards.Open.YouDontHaveAnyUnopenedPacks(loritta.commandMentions.daily, loritta.commandMentions.loriCoolCardsBuy)),
                        Emotes.LoriBonk
                    )
                }
            }
            is OpenBoosterPacksResult.Success -> {
                val cards = result.cards.toMutableList()

                fun getCurrentStickerAndCreateMessage(): InlineMessage<*>.() -> (Unit) {
                    val currentCard = cards.removeFirst()

                    // val discordInfo = loritta.lorittaShards.retrieveUserInfoById(currentProfileId)
                    // val backgroundUrl = loritta.profileDesignManager.getUserProfileBackgroundUrl(currentProfileId)

                    return {
                        this.useComponentsV2 = true

                        this.components += Container {
                            val title = currentCard.card[LoriCoolCardsEventCards.title]
                            val cardId = currentCard.card[LoriCoolCardsEventCards.fancyCardId]
                            val cardReceivedImageUrl = currentCard.card[LoriCoolCardsEventCards.cardReceivedImageUrl]

                            val embedLikeTitle = if (currentCard.haveWeAlreadySeenThisCardBefore) {
                                "${currentCard.card[LoriCoolCardsEventCards.rarity].emoji} $cardId - $title"
                            } else {
                                "${currentCard.card[LoriCoolCardsEventCards.rarity].emoji} **[NOVO!]** $cardId - $title"
                            }

                            this.components += TextDisplay("### $embedLikeTitle")

                            this.components += MediaGallery {
                                this.item(cardReceivedImageUrl)
                            }

                            this.components += TextDisplay(
                                "${context.i18nContext.get(I18N_PREFIX.NowYouHaveXStickersOfThisType(currentCard.howManyCardsOfThisCardIdWeHave))}\n${
                                    context.i18nContext.get(
                                        I18N_PREFIX.AlbumProgress(
                                            currentCard.totalAlbumCompletionCount,
                                            result.albumCardsCount
                                        )
                                    )
                                }"
                            )

                            this.accentColorRaw = currentCard.card[LoriCoolCardsEventCards.rarity].color.rgb
                        }

                        val nextStickerButton = UnleashedButton.of(
                            ButtonStyle.PRIMARY,
                            context.i18nContext.get(
                                I18N_PREFIX.NextSticker(
                                    result.cards.size - cards.size,
                                    result.cards.size
                                )
                            ),
                            Emotes.LoriCoolSticker
                        )

                        val viewSummaryButton = UnleashedButton.of(
                            ButtonStyle.SECONDARY,
                            context.i18nContext.get(I18N_PREFIX.ViewSummary),
                            Emotes.LoriReading
                        )

                        actionRow(
                            if (cards.isNotEmpty()) {
                                loritta.interactivityManager.buttonForUser(
                                    context.user,
                                    context.alwaysEphemeral,
                                    nextStickerButton
                                ) {
                                    // We don't need to defer here because the getCurrentProfileIdAndCreateMessage does not do any database related things here!
                                    // (if it does, then we need to update the code)
                                    it.event.editMessage(MessageEdit { apply(getCurrentStickerAndCreateMessage()) })
                                        .apply {
                                            this.isReplace = true
                                        }
                                        .await()
                                }
                            } else nextStickerButton.asDisabled(),
                            loritta.interactivityManager.buttonForUser(
                                context.user,
                                context.alwaysEphemeral,
                                viewSummaryButton
                            ) {
                                // We need to do this because the user may have clicked the "View Summary" button before looking at all the stickers
                                val lastStickerOfThePacks = if (cards.isNotEmpty())
                                    cards.last()
                                else currentCard

                                val groupedCards = result.cards.groupBy { it.card[LoriCoolCardsEventCards.id] }
                                    .toList()
                                    // This is an amalgamation, but it does work
                                    .sortedWith(
                                        compareBy<Pair<EntityID<Long>, List<OpenBoosterPacksResult.Success.CardResult>>> { it.second.first().haveWeAlreadySeenThisCardBefore }
                                            .thenByDescending { it.second.size }
                                            .thenBy { it.second.first().card[LoriCoolCardsEventCards.fancyCardId] }
                                    )

                                val stickStickersButton = UnleashedButton.of(
                                    ButtonStyle.PRIMARY,
                                    context.i18nContext.get(I18N_PREFIX.StickStickers),
                                    Emotes.LoriCoolSticker
                                )

                                // Don't defer, let's edit the original message directly because we don't need to access the database here
                                it.editMessage(
                                    isReplace = true,
                                ) {
                                    this.useComponentsV2 = true

                                    this.components += Container {
                                        this.accentColorRaw = LorittaColors.LorittaAqua.rgb

                                        this.components += TextDisplay("### ${Emotes.LoriCoolSticker} ${context.i18nContext.get(I18N_PREFIX.Summary)}")
                                        val description = buildString {
                                            groupedCards.take(25).forEach {
                                                val cardReference = it.second.first().card
                                                append("* ${it.second.size}x ${cardReference[LoriCoolCardsEventCards.rarity].emoji} ${cardReference[LoriCoolCardsEventCards.fancyCardId]} - ${cardReference[LoriCoolCardsEventCards.title]}")
                                                if (!it.second.any { it.haveWeAlreadySeenThisCardBefore })
                                                    append(" **[NOVO!]**")
                                                appendLine()
                                            }
                                            val stickersNotShown = groupedCards.drop(25)
                                            if (stickersNotShown.isNotEmpty()) {
                                                append("* ${context.i18nContext.get(I18nKeysData.Commands.Command.Loricoolcards.Open.AndXMoreStickers(stickersNotShown.sumOf { it.second.size }))}")
                                                appendLine()
                                            }
                                        }

                                        this.components += TextDisplay(description)

                                        this.components += row(
                                            if (result.alreadyStickedCardsCount != lastStickerOfThePacks.totalAlbumCompletionCount) {
                                                // If the number is different, then it means that we have new stickers to be sticked!
                                                loritta.interactivityManager
                                                    .buttonForUser(
                                                        context.user,
                                                        context.alwaysEphemeral,
                                                        stickStickersButton
                                                    ) {
                                                        loriCoolCardsCommand.stickStickers.stickStickers(it)
                                                    }
                                            } else {
                                                // If not, just disable the button
                                                stickStickersButton.asDisabled()
                                            }
                                        )

                                        this.components += Separator(isDivider = true, spacing = Separator.Spacing.SMALL)

                                        this.components += TextDisplay(
                                            buildString {
                                                appendLine("**Progresso do Álbum:** ${lastStickerOfThePacks.totalAlbumCompletionCount}/${result.albumCardsCount} figurinhas (+${lastStickerOfThePacks.totalAlbumCompletionCount - result.totalAlbumCompletionCountBeforeBuying})")
                                                appendLine("**Progresso do Álbum:** ${result.alreadyStickedCardsCount}/${result.albumCardsCount} figurinhas coladas")
                                                appendLine(context.i18nContext.get(I18nKeysData.Commands.Command.Loricoolcards.Open.TradeTip(loritta.commandMentions.loriCoolCardsTrade, loritta.commandMentions.loriCoolCardsGive)))
                                            }
                                        )

                                        this.components += TextDisplay("-# ${context.i18nContext.get(I18nKeysData.Commands.Command.Loricoolcards.Open.YouOpenedXStickerPacks(result.boosterPacksCount))}")
                                    }
                                }
                            }
                        )
                    }
                }

                context.reply(false) {
                    apply(getCurrentStickerAndCreateMessage())
                }

                context.giveAchievementAndNotify(AchievementType.NEW_ITEM_SMELL, true)
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

    sealed class OpenBoosterPacksResult {
        data object EventUnavailable : OpenBoosterPacksResult()
        data object YouDontHaveAnyPendingBoosterPacks : OpenBoosterPacksResult()
        class Success(
            val template: StickerAlbumTemplate,
            val boosterPacksCount: Int,
            val albumCardsCount: Int,
            val totalAlbumCompletionCountBeforeBuying: Long,
            val cards: List<CardResult>,
            val alreadyStickedCardsCount: Long
        ) : OpenBoosterPacksResult() {
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