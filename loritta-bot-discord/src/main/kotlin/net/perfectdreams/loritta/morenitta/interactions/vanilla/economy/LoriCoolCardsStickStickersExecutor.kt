package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesignsPayments
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEventCards
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsFinishedAlbumUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsUserOwnedCards
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.loricoolcards.CardRarity
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.math.Easings
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import net.perfectdreams.loritta.morenitta.loricoolcards.StickerAlbumTemplate
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.serializable.StoredLoriCoolCardsFinishedAlbumSonhosTransaction
import org.jetbrains.exposed.sql.*
import java.awt.Color
import java.time.Instant

class LoriCoolCardsStickStickersExecutor(val loritta: LorittaBot, private val loriCoolCardsCommand: LoriCoolCardsCommand) : LorittaSlashCommandExecutor() {
    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        stickStickers(context)
    }

    // Here because we want to access this via buttons too
    suspend fun stickStickers(context: UnleashedContext) {
        if (SonhosUtils.checkIfEconomyIsDisabled(context))
            return

        val now = Instant.now()

        val result = loritta.transaction {
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
                LoriCoolCardsUserOwnedCards.sticked eq false and (LoriCoolCardsUserOwnedCards.card notInList alreadyStickedCardIds) and (LoriCoolCardsUserOwnedCards.user eq context.user.idLong)
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
                suspend fun doStuff(currentActiveContext: UnleashedContext, target: suspend (InlineMessage<*>.() -> (Unit)) -> (Unit)) {
                    // TODO: This causes issues if the interaction fails, because the sticker is removed from the list before it is sticked
                    // This should ONLY BE TRIGGERED on exceptional cases, because we do disable the button when there isn't any more stickers
                    val cardsToBeSticked = (0 until 5).mapNotNull { cards.removeFirstOrNull() }
               
                    if (cardsToBeSticked.isEmpty()) {
                        currentActiveContext.reply(true) {
                            styled(
                                "Você já colocou todas as suas figurinhas!"
                            )
                        }
                        return
                    }

                    // Generate bulk sticker being sticked in album GIF
                    val albumPasteGif = loritta.loriCoolCardsManager.generateStickerBeingStickedInAlbumGIF(
                        template,
                        alreadyStickedCards,
                        cardsToBeSticked
                    )

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
                    val stickResult = loritta.transaction {
                        // This is hard because we need to check if a sticker with the same card ID has been sticked (the user may have initiated the stickering in another command)
                        val eventId = cardsToBeSticked.first()[LoriCoolCardsUserOwnedCards.event]

                        val alreadyStickedCards = LoriCoolCardsUserOwnedCards.select {
                            LoriCoolCardsUserOwnedCards.sticked eq true and (LoriCoolCardsUserOwnedCards.event eq eventId) and (LoriCoolCardsUserOwnedCards.user eq context.user.idLong)
                        }.toList()

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

                        // TODO: If hasStickedAllAlbumCards = true, give the rewards!
                        if (hasStickedAllAlbumCards) {
                            val completionId = LoriCoolCardsFinishedAlbumUsers.insertAndGetId {
                                it[LoriCoolCardsFinishedAlbumUsers.user] = context.user.idLong
                                it[LoriCoolCardsFinishedAlbumUsers.event] = eventId
                                it[LoriCoolCardsFinishedAlbumUsers.finishedAt] = Instant.now()
                            }

                            val howManyCompletionsWeAreInRightNow = LoriCoolCardsFinishedAlbumUsers.select {
                                LoriCoolCardsFinishedAlbumUsers.user eq context.user.idLong
                            }.count()

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
                                6L to "loriCoolCardsStickerReceivedMythic",
                                7L to "loriCoolCardsStickerReceivedPlainCommon",
                                8L to "loriCoolCardsStickerReceivedPlainUncommon",
                                9L to "loriCoolCardsStickerReceivedPlainRare",
                                10L to "loriCoolCardsStickerReceivedPlainEpic",
                                11L to "loriCoolCardsStickerReceivedPlainLegendary",
                                12L to "loriCoolCardsStickerReceivedPlainMythic"
                            )

                            val profileDesignInternalNameToBeGiven = profileDesignInternalNamesToBeGiven[howManyCompletionsWeAreInRightNow]

                            if (profileDesignInternalNameToBeGiven != null) {
                                ProfileDesignsPayments.insert {
                                    it[ProfileDesignsPayments.userId] = context.user.idLong
                                    it[cost] = 0
                                    it[profile] = "loriCoolCardsStickerReceivedCommon"
                                    it[boughtAt] = System.currentTimeMillis()
                                }
                            }
                        }

                        return@transaction StickedStickerResult.Success(hasStickedAllAlbumCards)
                    }

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

                                val nextStickerButton = UnleashedButton.of(
                                    ButtonStyle.PRIMARY,
                                    "Colar Mais Figurinhas",
                                    Emotes.LoriCoolSticker
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
                                    if (cards.isNotEmpty()) {
                                        actionRow(
                                            loritta.interactivityManager.buttonForUser(
                                                context.user,
                                                nextStickerButton
                                            ) {
                                                // Same thing we did for the previous button
                                                it.invalidateComponentCallback()

                                                val editJob2 = it.event.editMessage(
                                                    MessageEdit {
                                                        actionRow(
                                                            nextStickerButton
                                                                .withLabel("Procurando as Próximas Figurinhas...")
                                                                .withEmoji(Emotes.LoriDerp.toJDA())
                                                                .asDisabled(),
                                                            buyStickerPackButton.asDisabled(),
                                                        )
                                                    }
                                                ).submit()

                                                val hook2 = it.event.hook

                                                doStuff(it) {
                                                    editJob2.await()
                                                    hook2.editOriginal(
                                                        MessageEdit {
                                                            it.invoke(this)
                                                        }
                                                    ).await()
                                                }
                                            },
                                            loritta.interactivityManager.buttonForUser(
                                                context.user,
                                                buyStickerPackButton,
                                                buyStickerPackButtonCallback
                                            )
                                        )
                                    } else {
                                        actionRow(
                                            nextStickerButton.asDisabled(),
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
                }

                context.deferChannelMessage(false) // Defer response
                doStuff(context) {
                    context.reply(false) {
                        it.invoke(this)
                    }
                }
            }
        }
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