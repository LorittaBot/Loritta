package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.interactions.components.asDisabled
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEventCards
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsSeenCards
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsUserOwnedCards
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.time.Instant

class LoriCoolCardsGiveStickersExecutor(val loritta: LorittaBot, private val loriCoolCardsCommand: LoriCoolCardsCommand) : LorittaSlashCommandExecutor() {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Loricoolcards.Give
    }

    inner class Options : ApplicationCommandOptions() {
        val user = user("user", I18N_PREFIX.Options.User.Text)
        // We did support autocomplete before, but now that we support multi sticker giving, it was getting very cumbersome to support autocomplete for lists
        val stickerIds = string("stickers", I18N_PREFIX.Options.Stickers.Text) {
            autocomplete {
                val now = Instant.now()
                val focusedOptionValue = it.event.focusedOption.value

                // If it contains a comma, then it is a list
                if (focusedOptionValue.contains(",")) {
                    return@autocomplete mapOf(
                        it.i18nContext.get(I18N_PREFIX.StickerList(focusedOptionValue))
                            .shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length) to focusedOptionValue
                    )
                }

                // We also let searchingByCardId = true if empty to make the autocomplete results be sorted from 0001 -> ... by default
                val searchingByCardId = focusedOptionValue.startsWith("#") || focusedOptionValue.isEmpty() || focusedOptionValue.toIntOrNull() != null

                return@autocomplete loritta.transaction {
                    val event = LoriCoolCardsEvents.select {
                        LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
                    }.firstOrNull() ?: return@transaction mapOf()

                    val countField = LoriCoolCardsUserOwnedCards.card.count()

                    val cardsThatTheUserHas = LoriCoolCardsUserOwnedCards.slice(LoriCoolCardsUserOwnedCards.card, countField).select {
                        LoriCoolCardsUserOwnedCards.user eq it.event.user.idLong and (LoriCoolCardsUserOwnedCards.sticked eq false)
                    }.groupBy(LoriCoolCardsUserOwnedCards.card)
                        .having {
                            countField greaterEq 1
                        }
                        .toList()
                        .map { it[LoriCoolCardsUserOwnedCards.card] to it[countField] }
                        .toMap()

                    if (searchingByCardId) {
                        var searchQuery = focusedOptionValue
                        if (searchQuery.toIntOrNull() != null) {
                            searchQuery = "#${searchQuery.toInt().toString().padStart(4, '0')}"
                        }

                        val cardEventCardsMatchingQuery = LoriCoolCardsEventCards.select {
                            LoriCoolCardsEventCards.fancyCardId.like(
                                "${searchQuery.replace("%", "")}%"
                            ) and (LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id]) and (LoriCoolCardsEventCards.id inList cardsThatTheUserHas.keys)
                        }.limit(25).orderBy(LoriCoolCardsEventCards.fancyCardId, SortOrder.ASC).toList()

                        val cardIds = cardEventCardsMatchingQuery.map { it[LoriCoolCardsEventCards.id] }

                        val seenCards = LoriCoolCardsSeenCards.select {
                            (LoriCoolCardsSeenCards.user eq it.event.user.idLong) and (LoriCoolCardsSeenCards.card inList cardIds)
                        }.map { it[LoriCoolCardsSeenCards.card].value }

                        val results = mutableMapOf<String, String>()
                        for (card in cardEventCardsMatchingQuery) {
                            if (card[LoriCoolCardsEventCards.id].value in seenCards) {
                                results["${card[LoriCoolCardsEventCards.fancyCardId]} - ${card[LoriCoolCardsEventCards.title]} (${cardsThatTheUserHas[card[LoriCoolCardsEventCards.id]]} figurinhas)"] =
                                    card[LoriCoolCardsEventCards.fancyCardId]
                            } else {
                                results["${card[LoriCoolCardsEventCards.fancyCardId]} - ???"] =
                                    card[LoriCoolCardsEventCards.fancyCardId]
                            }
                        }
                        results
                    } else {
                        val cardEventCardsMatchingQuery = LoriCoolCardsEventCards.select {
                            LoriCoolCardsEventCards.title.like(
                                "${
                                    focusedOptionValue.replace(
                                        "%",
                                        ""
                                    )
                                }%"
                            ) and (LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id]) and (LoriCoolCardsEventCards.id inList cardsThatTheUserHas.keys)
                        }.limit(25).orderBy(LoriCoolCardsEventCards.title, SortOrder.ASC).toList()

                        val cardIds = cardEventCardsMatchingQuery.map { it[LoriCoolCardsEventCards.id] }

                        val seenCards = LoriCoolCardsSeenCards.select {
                            (LoriCoolCardsSeenCards.user eq it.event.user.idLong) and (LoriCoolCardsSeenCards.card inList cardIds)
                        }.map { it[LoriCoolCardsSeenCards.card].value }

                        val results = mutableMapOf<String, String>()
                        for (card in cardEventCardsMatchingQuery) {
                            if (card[LoriCoolCardsEventCards.id].value in seenCards) {
                                results["${card[LoriCoolCardsEventCards.fancyCardId]} - ${card[LoriCoolCardsEventCards.title]} (${cardsThatTheUserHas[card[LoriCoolCardsEventCards.id]]} figurinhas)"] = card[LoriCoolCardsEventCards.fancyCardId]
                            } else {
                                results["${card[LoriCoolCardsEventCards.fancyCardId]} - ???"] = card[LoriCoolCardsEventCards.fancyCardId]
                            }
                        }
                        results
                    }
                }
            }
        }
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        val userThatWillReceiveTheSticker = args[options.user].user
        val stickerFancyIdsList = args[options.stickerIds]
            .split(",")
            .map { it.trim() }

        if (context.user == userThatWillReceiveTheSticker) {
            context.reply(true) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.YouCantGiveStickersToYourself)
                )
            }
            return
        }

        // Quick validation of the sticker IDs
        for (stickerFancyId in stickerFancyIdsList) {
            if (stickerFancyId.removePrefix("#").toIntOrNull() == null) {
                // Invalid sticker fancy ID!
                context.reply(true) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.InvalidStickerId)
                    )
                }
                return
            }
        }

        if (stickerFancyIdsList.size > 100) {
            // Too many stickers!
            context.reply(true) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.TooManyStickers)
                )
            }
            return
        }

        context.deferChannelMessage(false)

        val now = Instant.now()

        val result = loritta.transaction {
            val event = LoriCoolCardsEvents.select {
                LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
            }.firstOrNull() ?: return@transaction GiveStickerResult.EventUnavailable

            val stickersToBeGiven = LoriCoolCardsEventCards.select {
                LoriCoolCardsEventCards.fancyCardId inList stickerFancyIdsList and (LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id])
            }.toList()

            if (stickersToBeGiven.size != stickerFancyIdsList.size)
                return@transaction GiveStickerResult.UnknownCard

            val stickersIdsToBeGiven = stickersToBeGiven.map {
                it[LoriCoolCardsEventCards.id].value
            }

            val ownedStickersMatchingTheIds = LoriCoolCardsUserOwnedCards.innerJoin(LoriCoolCardsEventCards).select {
                LoriCoolCardsUserOwnedCards.card inList stickersIdsToBeGiven and (LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id]) and (LoriCoolCardsUserOwnedCards.sticked eq false) and (LoriCoolCardsUserOwnedCards.user eq context.user.idLong)
            }.orderBy(LoriCoolCardsUserOwnedCards.receivedAt, SortOrder.DESC)
                .toList()

            val stickerIdsToBeGivenMappedToSticker = mutableMapOf<Long, ResultRow>()
            val missingStickers = mutableListOf<ResultRow>()

            for (stickerId in stickersIdsToBeGiven) {
                val stickerData = ownedStickersMatchingTheIds.firstOrNull { it[LoriCoolCardsEventCards.id].value == stickerId }
                if (stickerData == null) {
                    missingStickers.add(stickersToBeGiven.first { it[LoriCoolCardsEventCards.id].value == stickerId })
                } else {
                    stickerIdsToBeGivenMappedToSticker[stickerId] = stickerData
                }
            }

            if (missingStickers.isNotEmpty())
                return@transaction GiveStickerResult.NotEnoughCards(missingStickers)

            return@transaction GiveStickerResult.Success(stickerIdsToBeGivenMappedToSticker.map { it.value })
        }

        when (result) {
            GiveStickerResult.EventUnavailable -> {
                context.reply(false) {
                    styled(
                        "Nenhum evento de figurinhas ativo"
                    )
                }
            }
            GiveStickerResult.UnknownCard -> {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.TryingToGiveAnUnknownSticker)
                    )
                }
            }
            is GiveStickerResult.NotEnoughCards -> {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.YouDontHaveEnoughStickers(result.stickersMissing.joinToString { "`${it[LoriCoolCardsEventCards.fancyCardId]}`" },))
                    )
                }
            }
            is GiveStickerResult.Success -> {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(
                            I18N_PREFIX.YouAreGoingToTransfer(
                                result.givenStickers.joinToString { "`${it[LoriCoolCardsEventCards.fancyCardId]}`" },
                                result.givenStickers.size,
                                userThatWillReceiveTheSticker.asMention
                            )
                        ),
                        Emotes.LoriCoolSticker
                    )
                    styled(
                        context.i18nContext.get(I18N_PREFIX.ConfirmTheTransaction(userThatWillReceiveTheSticker.asMention)),
                        Emotes.LoriZap
                    )

                    actionRow(
                        loritta.interactivityManager.buttonForUser(
                            userThatWillReceiveTheSticker,
                            ButtonStyle.PRIMARY,
                            context.i18nContext.get(I18N_PREFIX.AcceptTransfer),
                            {
                                loriEmoji = Emotes.Handshake
                            }
                        ) {
                            it.invalidateComponentCallback()

                            it.deferAndEditOriginal(
                                MessageEditBuilder.fromMessage(it.event.message)
                                    .setComponents(it.event.message.components.asDisabled())
                                    .build()
                            )

                            // Repeat the checks
                            val finalResult = loritta.transaction {
                                val event = LoriCoolCardsEvents.select {
                                    LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
                                }.firstOrNull() ?: return@transaction GiveStickerAcceptedTransactionResult.EventUnavailable

                                val stickersToBeGiven = LoriCoolCardsEventCards.select {
                                    LoriCoolCardsEventCards.fancyCardId inList stickerFancyIdsList and (LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id])
                                }.toList()

                                if (stickersToBeGiven.size != stickerFancyIdsList.size)
                                    return@transaction GiveStickerResult.UnknownCard

                                val stickersIdsToBeGiven = stickersToBeGiven.map {
                                    it[LoriCoolCardsEventCards.id].value
                                }

                                val ownedStickersMatchingTheIds = LoriCoolCardsUserOwnedCards.innerJoin(LoriCoolCardsEventCards).select {
                                    LoriCoolCardsUserOwnedCards.card inList stickersIdsToBeGiven and (LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id]) and (LoriCoolCardsUserOwnedCards.sticked eq false) and (LoriCoolCardsUserOwnedCards.user eq context.user.idLong)
                                }.orderBy(LoriCoolCardsUserOwnedCards.receivedAt, SortOrder.DESC)
                                    .toList()

                                val stickerIdsToBeGivenMappedToSticker = mutableMapOf<Long, ResultRow>()
                                val missingStickers = mutableListOf<ResultRow>()

                                for (stickerId in stickersIdsToBeGiven) {
                                    val stickerData = ownedStickersMatchingTheIds.firstOrNull { it[LoriCoolCardsEventCards.id].value == stickerId }
                                    if (stickerData == null) {
                                        missingStickers.add(stickersToBeGiven.first { it[LoriCoolCardsEventCards.id].value == stickerId })
                                    } else {
                                        stickerIdsToBeGivenMappedToSticker[stickerId] = stickerData
                                    }
                                }

                                if (missingStickers.isNotEmpty())
                                    return@transaction GiveStickerResult.NotEnoughCards(missingStickers)

                                val stickerIdsToBeGivenMappedToOwnedStickerId = stickerIdsToBeGivenMappedToSticker.map { it.value[LoriCoolCardsUserOwnedCards.id].value }
                                val stickerIdsToBeGivenMappedToEventStickerId = stickerIdsToBeGivenMappedToSticker.map { it.value[LoriCoolCardsEventCards.id].value }

                                // Transfer
                                // Delete the old card
                                LoriCoolCardsUserOwnedCards.deleteWhere {
                                    LoriCoolCardsUserOwnedCards.id inList stickerIdsToBeGivenMappedToOwnedStickerId
                                }

                                // Insert the new ones WITH BATCH INSERT BECAUSE WE ARE BUILT LIKE THAT!!!
                                LoriCoolCardsUserOwnedCards.batchInsert(stickerIdsToBeGivenMappedToEventStickerId, shouldReturnGeneratedValues = false) {
                                    this[LoriCoolCardsUserOwnedCards.card] = it
                                    this[LoriCoolCardsUserOwnedCards.user] = userThatWillReceiveTheSticker.idLong
                                    this[LoriCoolCardsUserOwnedCards.event] = event[LoriCoolCardsEvents.id]
                                    this[LoriCoolCardsUserOwnedCards.receivedAt] = now
                                    this[LoriCoolCardsUserOwnedCards.sticked] = false
                                }

                                // Now that we selected the cards, we will mark them as seen + owned
                                // OPTIMIZATION: Get all seen stickers beforehand, this way we don't need to do an individual select for each sticker
                                val stickersThatWeHaveAlreadySeenBeforeBasedOnTheSelectedStickers = LoriCoolCardsSeenCards.slice(LoriCoolCardsSeenCards.card).select {
                                    LoriCoolCardsSeenCards.card inList stickerIdsToBeGivenMappedToEventStickerId and (LoriCoolCardsSeenCards.user eq userThatWillReceiveTheSticker.idLong)
                                }.map { it[LoriCoolCardsSeenCards.card].value }

                                val stickersIdsThatWeHaveNotSeenBefore = mutableListOf<Long>()
                                for (eventStickerId in stickerIdsToBeGivenMappedToEventStickerId) {
                                    if (eventStickerId !in stickersThatWeHaveAlreadySeenBeforeBasedOnTheSelectedStickers)
                                        stickersIdsThatWeHaveNotSeenBefore.add(eventStickerId)
                                }

                                if (stickersIdsThatWeHaveNotSeenBefore.isNotEmpty()) {
                                    // "Seen cards" just mean that the card won't be unknown (???) when the user looks it up, even if they give the card away
                                    LoriCoolCardsSeenCards.batchInsert(stickersIdsThatWeHaveNotSeenBefore, shouldReturnGeneratedValues = false) {
                                        this[LoriCoolCardsSeenCards.card] = it
                                        this[LoriCoolCardsSeenCards.user] = userThatWillReceiveTheSticker.idLong
                                        this[LoriCoolCardsSeenCards.seenAt] = now
                                    }
                                }

                                return@transaction GiveStickerAcceptedTransactionResult.Success(stickersToBeGiven)
                            }

                            when (finalResult) {
                                GiveStickerAcceptedTransactionResult.EventUnavailable -> {
                                    context.reply(false) {
                                        styled(
                                            "Nenhum evento de figurinhas ativo"
                                        )
                                    }
                                }
                                GiveStickerAcceptedTransactionResult.UnknownCard -> {
                                    context.reply(false) {
                                        styled(
                                            context.i18nContext.get(I18N_PREFIX.TryingToGiveAnUnknownSticker)
                                        )
                                    }
                                }
                                is GiveStickerAcceptedTransactionResult.NotEnoughCards -> {
                                    context.reply(false) {
                                        styled(
                                            context.i18nContext.get(I18N_PREFIX.YouDontHaveEnoughStickers(finalResult.stickersMissing.joinToString { "`${it[LoriCoolCardsEventCards.fancyCardId]}`" }))
                                        )
                                    }
                                }
                                is GiveStickerAcceptedTransactionResult.Success -> {
                                    it.reply(false) {
                                        styled(
                                            context.i18nContext.get(
                                                I18N_PREFIX.SuccessfullyTransferred(
                                                    userThatWillReceiveTheSticker.asMention,
                                                    result.givenStickers.joinToString { "`${it[LoriCoolCardsEventCards.fancyCardId]}`" },
                                                    result.givenStickers.size
                                                )
                                            ),
                                            Emotes.LoriCoolSticker
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    sealed class GiveStickerResult {
        data object EventUnavailable : GiveStickerResult()
        data object UnknownCard : GiveStickerResult()
        data class NotEnoughCards(val stickersMissing: List<ResultRow>) : GiveStickerResult()
        data class Success(val givenStickers: List<ResultRow>) : GiveStickerResult()
    }

    sealed class GiveStickerAcceptedTransactionResult {
        data object EventUnavailable : GiveStickerAcceptedTransactionResult()
        data object UnknownCard : GiveStickerAcceptedTransactionResult()
        data class NotEnoughCards(val stickersMissing: List<ResultRow>) : GiveStickerAcceptedTransactionResult()
        data class Success(val givenStickers: List<ResultRow>) : GiveStickerAcceptedTransactionResult()
    }
}