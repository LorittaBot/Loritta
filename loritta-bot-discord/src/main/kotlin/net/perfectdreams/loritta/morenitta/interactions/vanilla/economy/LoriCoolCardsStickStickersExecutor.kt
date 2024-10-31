package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesignsPayments
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.*
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.loricoolcards.CardRarity
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.math.Easings
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
import net.perfectdreams.loritta.serializable.StoredLoriCoolCardsFinishedAlbumSonhosTransaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.rank
import org.jetbrains.exposed.sql.vendors.ForUpdateOption
import java.awt.Color
import java.time.Instant
import kotlin.time.measureTimedValue

class LoriCoolCardsStickStickersExecutor(val loritta: LorittaBot, private val loriCoolCardsCommand: LoriCoolCardsCommand) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Loricoolcards.Stick
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        stickStickers(context)
    }

    // Here because we want to access this via buttons too
    suspend fun stickStickers(context: UnleashedContext) {
        if (SonhosUtils.checkIfEconomyIsDisabled(context))
            return

        val now = Instant.now()

        logger.info { "User ${context.user.idLong} wants to stick stickers (initial)! Let's get the info about the event and other cool stuff..." }

        val (result, time) = measureTimedValue {
            loritta.transaction {
                val event = LoriCoolCardsEvents.select {
                    LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
                }.firstOrNull() ?: return@transaction StickStickersResult.EventUnavailable

                val totalEventCards = LoriCoolCardsEventCards.select {
                    LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id]
                }.count()

                // First we need to check all cards that we have already sticked
                // We will inner join because we need that info when generating the album
                val alreadyStickedCards = LoriCoolCardsUserOwnedCards.innerJoin(LoriCoolCardsEventCards).select {
                    LoriCoolCardsUserOwnedCards.sticked eq true and (LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id]) and (LoriCoolCardsUserOwnedCards.user eq context.user.idLong)
                }.toList()

                val alreadyStickedCardIds = alreadyStickedCards.map {
                    it[LoriCoolCardsUserOwnedCards.card].value
                }

                // Now we get the cards that aren't already sticked but can be sticked
                // We will also inner join because we need that info when generating the album
                val cardsThatCanBeSticked = LoriCoolCardsUserOwnedCards.innerJoin(LoriCoolCardsEventCards).select {
                    LoriCoolCardsUserOwnedCards.sticked eq false and (LoriCoolCardsUserOwnedCards.card notInList alreadyStickedCardIds) and (LoriCoolCardsUserOwnedCards.user eq context.user.idLong) and (LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id])
                }.orderBy(LoriCoolCardsEventCards.fancyCardId, SortOrder.ASC) // Not really needed but this keeps a consistent order
                    .toList()

                if (cardsThatCanBeSticked.isEmpty())
                    return@transaction StickStickersResult.NoCardsToBeSticked

                // And finally here's the thing, we don't want any duplicates!!!
                val cardIdsAlreadyInTheList = mutableListOf<Long>()
                val cardsThatCanBeStickedUnique = mutableListOf<ResultRow>()
                for (card in cardsThatCanBeSticked) {
                    val cardId = card[LoriCoolCardsUserOwnedCards.card].value
                    if (cardId in cardIdsAlreadyInTheList)
                        continue
                    cardIdsAlreadyInTheList.add(cardId)
                    cardsThatCanBeStickedUnique.add(card)
                }

                // The stickers will be sticked when the user clicks to stick the sticker
                return@transaction StickStickersResult.Success(Json.decodeFromString(event[LoriCoolCardsEvents.template]), totalEventCards, cardsThatCanBeStickedUnique, alreadyStickedCards)
            }
        }

        logger.info { "Got user ${context.user.idLong} stick stickers (initial)'s information! - Took $time" }

        when (result) {
            StickStickersResult.EventUnavailable -> {
                context.reply(true) {
                    styled(
                        "Nenhum evento de figurinhas ativo"
                    )
                }
            }
            StickStickersResult.NoCardsToBeSticked -> {
                context.reply(true) {
                    styled(
                        "Você não tem figurinhas a serem coladas!"
                    )
                }
            }
            is StickStickersResult.Success -> {
                val template = result.template
                val cards = result.cardsThatCanBeSticked.toMutableList()
                val cardsToBeStickedOriginalCount = cards.size
                // Mutable, we will insert newly sticked cards here
                val alreadyStickedCards = result.alreadyStickedCards.toMutableList()

                // We stick stickers in bulk because sticking each sticker manually, while cool when you have at most 20 stickers, gets very tiresome when
                // you have 500+ stickers
                suspend fun doStuff(
                    currentActiveContext: UnleashedContext,
                    howManyStickersToStick: Int,
                    target: suspend (InlineMessage<*>.() -> (Unit)) -> (Unit)
                ) {
                    // TODO: This causes issues if the interaction fails, because the sticker is removed from the list before it is sticked
                    // This should ONLY BE TRIGGERED on exceptional cases, because we do disable the button when there isn't any more stickers
                    val cardsToBeSticked = (0 until howManyStickersToStick).mapNotNull { cards.removeFirstOrNull() }

                    if (cardsToBeSticked.isEmpty()) {
                        currentActiveContext.reply(true) {
                            styled(
                                "Você já colocou todas as suas figurinhas!"
                            )
                        }
                        return
                    }

                    val buyStickerPackButton = UnleashedButton.of(
                        ButtonStyle.SECONDARY,
                        "Comprar Pacote de Figurinhas",
                        Emotes.LoriCard
                    )

                    val buyStickerPackButtonCallback: suspend (ComponentContext) -> (Unit) = {
                        // Same thing we did for the previous button
                        it.invalidateComponentCallback()

                        it.deferChannelMessage(false)

                        loriCoolCardsCommand.buyStickers.buyStickers(it)
                    }

                    // Stick the sticker
                    logger.info { "User ${context.user.idLong} is now sticking stickers! Getting information from the database..." }

                    val (stickResult, time) = measureTimedValue {
                        loritta.transaction {
                            // This is hard because we need to check if a sticker with the same card ID has been sticked (the user may have initiated the stickering in another command)
                            val eventId = cardsToBeSticked.first()[LoriCoolCardsUserOwnedCards.event]

                            val alreadyStickedCards = LoriCoolCardsUserOwnedCards.selectAll()
                                .where {
                                    (LoriCoolCardsUserOwnedCards.event eq eventId) and (LoriCoolCardsUserOwnedCards.user eq context.user.idLong)
                                }
                                // THIS IS REQUIRED TO AVOID THE FOLLOWING BUG:
                                // When you have multiple duplicate stickers and you spam the "stick stickers" command, this may cause PostgreSQL to select different stickers on the "cardsToBeSticked" list
                                // And this causes an issue where both stickers are sticked, even tho that SHOULD NOT happen at all!
                                // To avoid this, we tell PostgreSQL to lock ALL stickers rows for updates
                                // This is also why we filter by sticked stickers manually later instead of the query, because we want to request ALL the sticker rows to be locked to avoid any issues,
                                // not just sticked stickers
                                .forUpdate(ForUpdateOption.PostgreSQL.ForUpdate())
                                .toList()
                                .filter { it[LoriCoolCardsUserOwnedCards.sticked] }

                            val alreadyStickedCardIds = alreadyStickedCards.map {
                                it[LoriCoolCardsUserOwnedCards.card].value
                            }

                            // Bail out!
                            for (cardToBeSticked in cardsToBeSticked) {
                                if (alreadyStickedCardIds.contains(cardToBeSticked[LoriCoolCardsUserOwnedCards.card].value))
                                    return@transaction StickedStickerResult.StickerAlreadySticked
                            }

                            val cardsToBeStickedIds = cardsToBeSticked.map { it[LoriCoolCardsUserOwnedCards.id] }

                            LoriCoolCardsUserOwnedCards.update({
                                LoriCoolCardsUserOwnedCards.id inList cardsToBeStickedIds and (LoriCoolCardsUserOwnedCards.sticked eq false)
                            }) {
                                it[LoriCoolCardsUserOwnedCards.sticked] = true
                                it[LoriCoolCardsUserOwnedCards.stickedAt] = Instant.now()
                            }

                            // Check if we have already sticked all stickers of this album
                            // We could get the total cards amount from the result, but what if we add new cards after the fact?
                            // (Probably won't ever happen, but still...)
                            val totalEventCards = LoriCoolCardsEventCards.select {
                                LoriCoolCardsEventCards.event eq eventId
                            }.count()

                            // "Plus cardsToBeSticked size" because we have sticked a new sticker (but we did the query before updating)
                            val alreadyStickedCardIdsCountPlusOne = alreadyStickedCardIds.size + cardsToBeSticked.size

                            val hasStickedAllAlbumCards = alreadyStickedCardIdsCountPlusOne == totalEventCards.toInt()

                            if (hasStickedAllAlbumCards) {
                                val completionId = LoriCoolCardsFinishedAlbumUsers.insertAndGetId {
                                    it[LoriCoolCardsFinishedAlbumUsers.user] = context.user.idLong
                                    it[LoriCoolCardsFinishedAlbumUsers.event] = eventId
                                    it[LoriCoolCardsFinishedAlbumUsers.finishedAt] = Instant.now()
                                }

                                val howManyCompletionsWeAreInRightNow = LoriCoolCardsFinishedAlbumUsers.select {
                                    LoriCoolCardsFinishedAlbumUsers.user eq context.user.idLong
                                }.count()

                                // Have we finished the album? If yes, in what position are we in?
                                val rankOverField = rank().over().orderBy(LoriCoolCardsFinishedAlbumUsers.finishedAt, SortOrder.ASC)
                                val albumRank = LoriCoolCardsFinishedAlbumUsers.select(
                                    LoriCoolCardsFinishedAlbumUsers.user,
                                    LoriCoolCardsFinishedAlbumUsers.finishedAt,
                                    rankOverField
                                ).where {
                                    // We cannot filter by user here, if we do an "eq userToBeViewed.idLong" here, the rank position will always be 1 (or null, if the user hasn't completed the album)
                                    // So we filter it after the fact
                                    LoriCoolCardsFinishedAlbumUsers.event eq eventId
                                }.first { it[LoriCoolCardsFinishedAlbumUsers.user] == context.user.idLong }[rankOverField]
                                // ^ This should NEVER be not present considering that we have inserted the user before

                                // Process rewards
                                Profiles.update({ Profiles.id eq context.user.idLong }) {
                                    with(SqlExpressionBuilder) {
                                        it.update(Profiles.money, Profiles.money + result.template.sonhosReward)
                                    }
                                }

                                // Cinnamon transactions log
                                SimpleSonhosTransactionsLogUtils.insert(
                                    context.user.idLong,
                                    now,
                                    TransactionType.LORI_COOL_CARDS,
                                    result.template.sonhosReward,
                                    StoredLoriCoolCardsFinishedAlbumSonhosTransaction(
                                        eventId.value,
                                        completionId.value
                                    )
                                )

                                // Give the sticker profile
                                val profileDesignInternalNamesToBeGiven = mapOf(
                                    1L to "loriCoolCardsStickerReceivedCommon",
                                    2L to "loriCoolCardsStickerReceivedUncommon",
                                    3L to "loriCoolCardsStickerReceivedRare",
                                    4L to "loriCoolCardsStickerReceivedEpic",
                                    5L to "loriCoolCardsStickerReceivedLegendary",
                                    6L to "loriCoolCardsStickerReceivedMythic"
                                )

                                val profileDesignInternalNameToBeGiven = profileDesignInternalNamesToBeGiven[howManyCompletionsWeAreInRightNow]

                                if (profileDesignInternalNameToBeGiven != null) {
                                    ProfileDesignsPayments.insert {
                                        it[ProfileDesignsPayments.userId] = context.user.idLong
                                        it[cost] = 0
                                        it[profile] = profileDesignInternalNameToBeGiven
                                        it[boughtAt] = System.currentTimeMillis()
                                    }
                                }

                                // If we finished the album quickly, we can pay out a special design to the user
                                if (albumRank >= 100) {
                                    LoriCoolCardsQuickestUserTracks.insert {
                                        it[LoriCoolCardsQuickestUserTracks.userId] = context.user.idLong
                                        it[LoriCoolCardsQuickestUserTracks.finished] = completionId
                                        it[LoriCoolCardsQuickestUserTracks.type] = 1
                                    }

                                    val quickCount = LoriCoolCardsQuickestUserTracks.selectAll()
                                        .where {
                                            LoriCoolCardsQuickestUserTracks.userId eq context.user.idLong and (LoriCoolCardsQuickestUserTracks.type eq 1)
                                        }
                                        .count()

                                    val specialProfileDesignInternalNamesToBeGiven = mapOf(
                                        1L to "loriCoolCardsStickerReceivedPlainCommon",
                                        2L to "loriCoolCardsStickerReceivedPlainUncommon",
                                        3L to "loriCoolCardsStickerReceivedPlainRare",
                                        4L to "loriCoolCardsStickerReceivedPlainEpic",
                                        5L to "loriCoolCardsStickerReceivedPlainLegendary",
                                        6L to "loriCoolCardsStickerReceivedPlainMythic"
                                    )

                                    val specialProfileDesignInternalNameToBeGiven = specialProfileDesignInternalNamesToBeGiven[quickCount]

                                    if (specialProfileDesignInternalNameToBeGiven != null) {
                                        ProfileDesignsPayments.insert {
                                            it[ProfileDesignsPayments.userId] = context.user.idLong
                                            it[cost] = 0
                                            it[profile] = specialProfileDesignInternalNameToBeGiven
                                            it[boughtAt] = System.currentTimeMillis()
                                        }
                                    }
                                }
                            }

                            return@transaction StickedStickerResult.Success(hasStickedAllAlbumCards)
                        }
                    }

                    logger.info { "User ${context.user.idLong} sticked stickers! - Took $time" }

                    when (stickResult) {
                        StickedStickerResult.StickerAlreadySticked -> {
                            target.invoke {
                                styled(
                                    "Figurinhas já coladas"
                                )
                            }
                            return
                        }
                        is StickedStickerResult.Success -> {
                            // Generate bulk sticker being sticked in album GIF
                            logger.info { "Generating GIF for user ${context.user.idLong}'s sticking stickers... Stickers to be sticked: ${cardsToBeSticked.size}" }

                            val (albumPasteGif, time) = measureTimedValue {
                                loritta.loriCoolCardsManager.generateStickerBeingStickedInAlbumGIF(
                                    template,
                                    alreadyStickedCards,
                                    cardsToBeSticked
                                )
                            }

                            logger.info { "GIF for user ${context.user.idLong}'s sticking stickers was successfully generated! Stickers to be sticked: ${cardsToBeSticked.size} - Took $time" }

                            alreadyStickedCards.addAll(cardsToBeSticked)

                            target.invoke {
                                embed {
                                    title = "${Emotes.LoriCoolSticker} Colando Figurinhas (${cardsToBeStickedOriginalCount - cards.size}/${cardsToBeStickedOriginalCount})"
                                    files += FileUpload.fromData(albumPasteGif, "album-stick.gif")
                                        .setDescription("Animação mostrando figurinhas sendo coladas no Álbum de Figurinhas de ${context.user.name}")

                                    image = "attachment://album-stick.gif"
                                    // Make the embed color be based on how near you are from the goal
                                    val eventPercentage = (alreadyStickedCards.size / result.totalEventCards.toDouble())
                                    color = Color(Easings.easeLinear(0, LorittaColors.LorittaAqua.red, eventPercentage), Easings.easeLinear(0, LorittaColors.LorittaAqua.green, eventPercentage), Easings.easeLinear(0, LorittaColors.LorittaAqua.blue, eventPercentage)).rgb
                                    footer("Progresso do Álbum: ${alreadyStickedCards.size}/${result.totalEventCards} figurinhas coladas")
                                }

                                val next5StickersButton = UnleashedButton.of(
                                    ButtonStyle.PRIMARY,
                                    if (cards.size == 0 || cards.size >= 5)
                                        context.i18nContext.get(I18N_PREFIX.StickXStickers(5))
                                    else
                                        context.i18nContext.get(I18N_PREFIX.StickXStickers(cards.size)),
                                    Emotes.StickerRarityCommon
                                )

                                val next10StickersButton = UnleashedButton.of(
                                    ButtonStyle.PRIMARY,
                                    context.i18nContext.get(I18N_PREFIX.StickXStickers(10)),
                                    Emotes.StickerRarityUncommon
                                )

                                val next15StickersButton = UnleashedButton.of(
                                    ButtonStyle.PRIMARY,
                                    context.i18nContext.get(I18N_PREFIX.StickXStickers(15)),
                                    Emotes.LoriCoolSticker
                                )

                                val next20StickersButton = UnleashedButton.of(
                                    ButtonStyle.PRIMARY,
                                    context.i18nContext.get(I18N_PREFIX.StickXStickers(20)),
                                    Emotes.StickerRarityEpic
                                )

                                val next25StickersButton = UnleashedButton.of(
                                    ButtonStyle.PRIMARY,
                                    context.i18nContext.get(I18N_PREFIX.StickXStickers(25)),
                                    Emotes.StickerRarityLegendary
                                )

                                if (stickResult.hasStickedAllAlbumCards) {
                                    actionRow(
                                        loritta.interactivityManager.buttonForUser(
                                            context.user,
                                            ButtonStyle.PRIMARY,
                                            "Fim!",
                                            {
                                                loriEmoji = Emotes.LoriYay
                                            }
                                        ) {
                                            // Same thing we did for the previous button
                                            it.invalidateComponentCallback()

                                            // TODO: Improve this message
                                            it.event.editMessage(
                                                MessageEdit {
                                                    embed {
                                                        description = "Parabéns, você completou o álbum de figurinhas!\n\nComo recompensa, você ganhou ${result.template.sonhosReward} sonhos, um design de perfil e uma insígnia para você equipar!\n\nQue tal ajudar as outras pessoas que querem completar o álbum dando as suas figurinhas repetidas para elas?\n\nObrigada por participar das Figurittas da Loritta! :3"
                                                        image = "https://stuff.loritta.website/loricoolcards/figurittas-da-loritta-logo.png"
                                                        color = CardRarity.RARE.color.rgb
                                                    }
                                                }
                                            ).setReplace(true).await()
                                        }
                                    )
                                } else {
                                    fun createNextStickersCallback(
                                        buttonThatInvokedTheCallback: Button,
                                        howManyStickersToStick: Int
                                    ): suspend (ComponentContext) -> (Unit) = {
                                        // Same thing we did for the previous button
                                        it.invalidateComponentCallback()

                                        val editJob2 = it.event.editMessage(
                                            MessageEdit {
                                                actionRow(
                                                    // TODO: I think this could be better
                                                    if (buttonThatInvokedTheCallback == next5StickersButton) {
                                                        next5StickersButton
                                                            .withLabel("Procurando as Próximas Figurinhas...")
                                                            .withEmoji(Emotes.LoriDerp.toJDA())
                                                            .asDisabled()
                                                    } else {
                                                        next5StickersButton
                                                            .asDisabled()
                                                    },
                                                    if (buttonThatInvokedTheCallback == next10StickersButton) {
                                                        next10StickersButton
                                                            .withLabel("Procurando as Próximas Figurinhas...")
                                                            .withEmoji(Emotes.LoriDerp.toJDA())
                                                            .asDisabled()
                                                    } else {
                                                        next10StickersButton
                                                            .asDisabled()
                                                    },
                                                    if (buttonThatInvokedTheCallback == next15StickersButton) {
                                                        next15StickersButton
                                                            .withLabel("Procurando as Próximas Figurinhas...")
                                                            .withEmoji(Emotes.LoriDerp.toJDA())
                                                            .asDisabled()
                                                    } else {
                                                        next15StickersButton
                                                            .asDisabled()
                                                    },
                                                    if (buttonThatInvokedTheCallback == next20StickersButton) {
                                                        next20StickersButton
                                                            .withLabel("Procurando as Próximas Figurinhas...")
                                                            .withEmoji(Emotes.LoriDerp.toJDA())
                                                            .asDisabled()
                                                    } else {
                                                        next20StickersButton
                                                            .asDisabled()
                                                    },
                                                    if (buttonThatInvokedTheCallback == next25StickersButton) {
                                                        next25StickersButton
                                                            .withLabel("Procurando as Próximas Figurinhas...")
                                                            .withEmoji(Emotes.LoriDerp.toJDA())
                                                            .asDisabled()
                                                    } else {
                                                        next25StickersButton
                                                            .asDisabled()
                                                    }
                                                )

                                                actionRow(
                                                    buyStickerPackButton.asDisabled()
                                                )
                                            }
                                        ).submit()

                                        val hook2 = it.event.hook

                                        doStuff(it, howManyStickersToStick) {
                                            editJob2.await()
                                            hook2.editOriginal(
                                                MessageEdit {
                                                    it.invoke(this)
                                                }
                                            ).await()
                                        }
                                    }

                                    actionRow(
                                        if (cards.size > 0) {
                                            loritta.interactivityManager.buttonForUser(
                                                context.user,
                                                next5StickersButton,
                                                createNextStickersCallback(next5StickersButton, 5)
                                            )
                                        } else next5StickersButton.asDisabled(),
                                        if (cards.size >= 10) {
                                            loritta.interactivityManager.buttonForUser(
                                                context.user,
                                                next10StickersButton,
                                                createNextStickersCallback(next10StickersButton, 10)
                                            )
                                        } else next10StickersButton.asDisabled(),
                                        if (cards.size >= 15) {
                                            loritta.interactivityManager.buttonForUser(
                                                context.user,
                                                next15StickersButton,
                                                createNextStickersCallback(next15StickersButton, 15)
                                            )
                                        } else next15StickersButton.asDisabled(),
                                        if (cards.size >= 20) {
                                            loritta.interactivityManager.buttonForUser(
                                                context.user,
                                                next20StickersButton,
                                                createNextStickersCallback(next20StickersButton, 20)
                                            )
                                        } else next20StickersButton.asDisabled(),
                                        if (cards.size >= 25) {
                                            loritta.interactivityManager.buttonForUser(
                                                context.user,
                                                next25StickersButton,
                                                createNextStickersCallback(next25StickersButton, 25)
                                            )
                                        } else next25StickersButton.asDisabled()
                                    )

                                    actionRow(
                                        loritta.interactivityManager.buttonForUser(
                                            context.user,
                                            buyStickerPackButton,
                                            buyStickerPackButtonCallback
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                context.deferChannelMessage(false) // Defer response
                doStuff(context, 5) {
                    context.reply(false) {
                        it.invoke(this)
                    }
                }
            }
        }
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?>? {
        return mapOf()
    }

    sealed class StickStickersResult {
        data object EventUnavailable : StickStickersResult()
        data object NoCardsToBeSticked : StickStickersResult()
        class Success(
            val template: StickerAlbumTemplate,
            val totalEventCards: Long,
            val cardsThatCanBeSticked: List<ResultRow>,
            val alreadyStickedCards: List<ResultRow>
        ) : StickStickersResult()
    }

    sealed class StickedStickerResult {
        data object StickerAlreadySticked : StickedStickerResult()
        class Success(
            val hasStickedAllAlbumCards: Boolean
        ) : StickedStickerResult()
    }
}