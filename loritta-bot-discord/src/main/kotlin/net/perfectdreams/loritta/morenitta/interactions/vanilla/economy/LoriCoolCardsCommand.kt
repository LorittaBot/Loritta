package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.interactions.components.asDisabled
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEventCards
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsSeenCards
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsUserOwnedCards
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.loricoolcards.StickerAlbumTemplate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.awt.Color
import java.time.Instant

class LoriCoolCardsCommand(private val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Loricoolcards
    }

    val stickStickers = LoriCoolCardsStickStickersExecutor(loritta, this)
    val buyStickers = LoriCoolCardsBuyStickersExecutor(loritta, this)
    val viewAlbum = LoriCoolCardsViewAlbumExecutor(loritta, this)

    override fun command() = slashCommand(I18N_PREFIX.Label, TodoFixThisData, CommandCategory.ECONOMY) {
        this.integrationTypes = listOf(Command.IntegrationType.GUILD_INSTALL, Command.IntegrationType.USER_INSTALL)

        subcommand(I18N_PREFIX.Buy.Label, I18N_PREFIX.Buy.Description) {
            // Buy
            executor = buyStickers
        }

        subcommand(I18N_PREFIX.View.Label, I18N_PREFIX.View.Description) {
            // View Figurinhas Stats
            executor = LoriCoolCardsViewExecutor()
        }

        subcommand(I18N_PREFIX.Stick.Label, I18N_PREFIX.Stick.Description) {
            // Stick stickers
            executor = stickStickers
        }

        subcommand(I18N_PREFIX.Album.Label, I18N_PREFIX.Album.Description) {
            // View album
            executor = viewAlbum
        }

        subcommand(I18N_PREFIX.Give.Label, I18N_PREFIX.Give.Description) {
            // Give stickers
            executor = LoriCoolCardsGiveStickersExecutor()
        }

        subcommand(I18N_PREFIX.Stats.Label, I18N_PREFIX.Stats.Description) {
            // Event stats
            executor = LoriCoolCardsStatsExecutor(loritta, this@LoriCoolCardsCommand)
        }

        subcommand(I18N_PREFIX.Compare.Label, I18N_PREFIX.Compare.Description) {
            // Compare stickers
            executor = LoriCoolCardsCompareStickersExecutor(loritta, this@LoriCoolCardsCommand)
        }
    }

    inner class LoriCoolCardsViewExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val cardId = string("card_id", TodoFixThisData) {
                autocomplete {
                    val now = Instant.now()
                    val focusedOptionValue = it.event.focusedOption.value

                    // We also let searchingByCardId = true if empty to make the autocomplete results be sorted from 0001 -> ... by default
                    val searchingByCardId = focusedOptionValue.startsWith("#") || focusedOptionValue.isEmpty() || focusedOptionValue.toIntOrNull() != null

                    return@autocomplete loritta.transaction {
                        val event = LoriCoolCardsEvents.select {
                            LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
                        }.firstOrNull() ?: return@transaction mapOf()

                        if (searchingByCardId) {
                            var searchQuery = focusedOptionValue
                            if (searchQuery.toIntOrNull() != null) {
                                searchQuery = "#${searchQuery.toInt().toString().padStart(4, '0')}"
                            }

                            val cardEventCardsMatchingQuery = LoriCoolCardsEventCards.select {
                                LoriCoolCardsEventCards.fancyCardId.like(
                                    "${searchQuery.replace("%", "")}%"
                                ) and (LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id])
                            }.limit(25).orderBy(LoriCoolCardsEventCards.fancyCardId, SortOrder.ASC).toList()

                            val cardIds = cardEventCardsMatchingQuery.map { it[LoriCoolCardsEventCards.id] }

                            val seenCards = LoriCoolCardsSeenCards.select {
                                (LoriCoolCardsSeenCards.user eq it.event.user.idLong) and (LoriCoolCardsSeenCards.card inList cardIds)
                            }.map { it[LoriCoolCardsSeenCards.card].value }

                            val results = mutableMapOf<String, String>()
                            for (card in cardEventCardsMatchingQuery) {
                                if (card[LoriCoolCardsEventCards.id].value in seenCards) {
                                    results["${card[LoriCoolCardsEventCards.fancyCardId]} - ${card[LoriCoolCardsEventCards.title]}"] =
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
                                ) and (LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id])
                            }.limit(25).orderBy(LoriCoolCardsEventCards.title, SortOrder.ASC).toList()

                            val cardIds = cardEventCardsMatchingQuery.map { it[LoriCoolCardsEventCards.id] }

                            val seenCards = LoriCoolCardsSeenCards.select {
                                (LoriCoolCardsSeenCards.user eq it.event.user.idLong) and (LoriCoolCardsSeenCards.card inList cardIds)
                            }.map { it[LoriCoolCardsSeenCards.card].value }

                            val results = mutableMapOf<String, String>()
                            for (card in cardEventCardsMatchingQuery) {
                                if (card[LoriCoolCardsEventCards.id].value in seenCards) {
                                    results["${card[LoriCoolCardsEventCards.fancyCardId]} - ${card[LoriCoolCardsEventCards.title]}"] =
                                        card[LoriCoolCardsEventCards.fancyCardId]
                                } else {
                                    results["${card[LoriCoolCardsEventCards.fancyCardId]} - ???"] =
                                        card[LoriCoolCardsEventCards.fancyCardId]
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
            val now = Instant.now()
            val fancyCardId = args[options.cardId]

            val result = loritta.transaction {
                val event = LoriCoolCardsEvents.select {
                    LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
                }.firstOrNull() ?: return@transaction GetCardInfoResult.EventUnavailable

                val cardEventCard = LoriCoolCardsEventCards.select {
                    LoriCoolCardsEventCards.fancyCardId eq fancyCardId and (LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id])
                }.limit(1).firstOrNull() ?: return@transaction GetCardInfoResult.UnknownCard

                val isSticked = LoriCoolCardsUserOwnedCards.select {
                    LoriCoolCardsUserOwnedCards.card eq cardEventCard[LoriCoolCardsEventCards.id] and (LoriCoolCardsUserOwnedCards.sticked eq true) and (LoriCoolCardsUserOwnedCards.user eq context.user.idLong)
                }.count() != 0L

                val isSeen = LoriCoolCardsSeenCards.select {
                    LoriCoolCardsSeenCards.card eq cardEventCard[LoriCoolCardsEventCards.id] and (LoriCoolCardsSeenCards.user eq context.user.idLong)
                }.count() != 0L

                val cardsOfThisTypeInCirculation = LoriCoolCardsUserOwnedCards.select {
                    LoriCoolCardsUserOwnedCards.card eq cardEventCard[LoriCoolCardsEventCards.id] and (LoriCoolCardsUserOwnedCards.sticked eq false)
                }.count()

                val cardsOfThisTypeSticked = LoriCoolCardsUserOwnedCards.select {
                    LoriCoolCardsUserOwnedCards.card eq cardEventCard[LoriCoolCardsEventCards.id] and (LoriCoolCardsUserOwnedCards.sticked eq true)
                }.count()

                val cardsOfThisTypeOwnedByTheCurrentUser = LoriCoolCardsUserOwnedCards.select {
                    LoriCoolCardsUserOwnedCards.card eq cardEventCard[LoriCoolCardsEventCards.id] and (LoriCoolCardsUserOwnedCards.user eq context.user.idLong) and (LoriCoolCardsUserOwnedCards.sticked eq false)
                }.count()

                return@transaction GetCardInfoResult.Success(
                    Json.decodeFromString(event[LoriCoolCardsEvents.template]),
                    cardEventCard,
                    isSeen,
                    isSticked,
                    cardsOfThisTypeOwnedByTheCurrentUser,
                    cardsOfThisTypeInCirculation,
                    cardsOfThisTypeSticked
                )
            }

            when (result) {
                GetCardInfoResult.EventUnavailable -> {
                    context.reply(true) {
                        styled(
                            "Nenhum evento de figurinhas ativo"
                        )
                    }
                }
                GetCardInfoResult.UnknownCard -> {
                    context.reply(false) {
                        styled(
                            "Figurinha não existe!"
                        )
                    }
                    return
                }
                is GetCardInfoResult.Success -> {
                    val albumStickerPage = result.template.getAlbumPageThatHasSticker(result.card[LoriCoolCardsEventCards.fancyCardId])!!

                    context.reply(false) {
                        embed {
                            val title = result.card[LoriCoolCardsEventCards.title]
                            val cardId = result.card[LoriCoolCardsEventCards.fancyCardId]
                            val cardFrontImageUrl = result.card[LoriCoolCardsEventCards.cardFrontImageUrl]
                            val cardReceivedImageUrl = result.card[LoriCoolCardsEventCards.cardReceivedImageUrl]

                            this.title = buildString {
                                if (result.isSeen) {
                                    append(result.card[LoriCoolCardsEventCards.rarity].emoji)
                                } else {
                                    append(Emotes.StickerRarityUnknown)
                                }
                                append(" ")
                                append(cardId)
                                append(" - ")
                                if (result.isSeen) {
                                    append(title)
                                } else {
                                    append("???")
                                }
                            }

                            this.description = buildString {
                                if (result.isSticked) {
                                    appendLine("# ${context.i18nContext.get(I18N_PREFIX.View.YouHaveThisStickerInYourAlbum)}")
                                } else {
                                    appendLine("# ${context.i18nContext.get(I18N_PREFIX.View.YouDontHaveThisStickerInYourAlbum)}")
                                }
                                appendLine(context.i18nContext.get(I18N_PREFIX.View.StickersInYourInventory(result.cardsOfThisTypeOwnedByTheCurrentUserNotSticked)))
                                appendLine(context.i18nContext.get(I18N_PREFIX.View.StickersInCirculation(result.cardsInCirculation)))
                                appendLine(context.i18nContext.get(I18N_PREFIX.View.StickersStickedInAlbums(result.cardsInAlbums)))
                                appendLine(context.i18nContext.get(I18N_PREFIX.View.StickersTotal(result.cardsInCirculation + result.cardsInAlbums)))
                                appendLine(context.i18nContext.get(I18N_PREFIX.View.StickerPage(albumStickerPage)))
                            }

                            if (result.isSeen) {
                                this.color = result.card[LoriCoolCardsEventCards.rarity].color.rgb
                            } else {
                                this.color = Color(47, 47, 47).rgb
                            }

                            val frontFacingStickerUrl = UnleashedButton.of(
                                ButtonStyle.LINK,
                                context.i18nContext.get(I18N_PREFIX.View.StickerFront)
                            )

                            val animatedStickerUrl = UnleashedButton.of(
                                ButtonStyle.LINK,
                                context.i18nContext.get(I18N_PREFIX.View.StickerAnimated)
                            )

                            if (result.isSeen) {
                                this.image = cardReceivedImageUrl

                                actionRow(
                                    frontFacingStickerUrl.withUrl(cardFrontImageUrl),
                                    animatedStickerUrl.withUrl(cardReceivedImageUrl)
                                )
                            } else {
                                this.image = result.template.unknownStickerImageUrl

                                actionRow(
                                    frontFacingStickerUrl.asDisabled(),
                                    animatedStickerUrl.asDisabled()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    inner class LoriCoolCardsGiveStickersExecutor : LorittaSlashCommandExecutor() {
        inner class Options : ApplicationCommandOptions() {
            val user = user("user", TodoFixThisData)
            val cardId = string("card_id", TodoFixThisData) {
                autocomplete {
                    val now = Instant.now()
                    val focusedOptionValue = it.event.focusedOption.value

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
            val user = args[options.user]
            val fancyCardId = args[options.cardId]

            if (context.user == user.user) {
                context.reply(true) {
                    styled(
                        "Você não pode dar figurinhas para você mesmo!"
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

                val cardEventCard = LoriCoolCardsEventCards.select {
                    LoriCoolCardsEventCards.fancyCardId eq fancyCardId and (LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id])
                }.limit(1).firstOrNull() ?: return@transaction GiveStickerResult.UnknownCard

                val ownedCard = LoriCoolCardsUserOwnedCards.innerJoin(LoriCoolCardsEventCards).select {
                    LoriCoolCardsUserOwnedCards.card eq cardEventCard[LoriCoolCardsEventCards.id] and (LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id]) and (LoriCoolCardsUserOwnedCards.sticked eq false) and (LoriCoolCardsUserOwnedCards.user eq context.user.idLong)
                }.orderBy(LoriCoolCardsUserOwnedCards.receivedAt, SortOrder.DESC)
                    .limit(1)
                    .firstOrNull()

                if (ownedCard == null)
                    return@transaction GiveStickerResult.NotEnoughCards

                return@transaction GiveStickerResult.Success(ownedCard)
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
                            "Figurinha não existe"
                        )
                    }
                }
                GiveStickerResult.NotEnoughCards -> {
                    context.reply(false) {
                        styled(
                            "Você não tem figurinhas suficientes!"
                        )
                    }
                }
                is GiveStickerResult.Success -> {
                    context.reply(false) {
                        styled(
                            "Você está prestes a transferir **`${result.givenCard[LoriCoolCardsEventCards.fancyCardId]} - ${result.givenCard[LoriCoolCardsEventCards.title]}`** para ${user.user.asMention}!",
                            Emotes.LoriCoolSticker
                        )
                        styled(
                            "Para confirmar a transação, ${user.user.asMention} deve aceitar a transação",
                            Emotes.LoriZap
                        )

                        actionRow(
                            loritta.interactivityManager.buttonForUser(
                                user.user,
                                ButtonStyle.PRIMARY,
                                "Aceitar Transferência",
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

                                    val cardEventCard = LoriCoolCardsEventCards.select {
                                        LoriCoolCardsEventCards.fancyCardId eq fancyCardId and (LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id])
                                    }.limit(1).firstOrNull() ?: return@transaction GiveStickerAcceptedTransactionResult.UnknownCard

                                    val ownedCard = LoriCoolCardsUserOwnedCards.innerJoin(LoriCoolCardsEventCards).select {
                                        LoriCoolCardsUserOwnedCards.card eq cardEventCard[LoriCoolCardsEventCards.id] and (LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id]) and (LoriCoolCardsUserOwnedCards.sticked eq false) and (LoriCoolCardsUserOwnedCards.user eq context.user.idLong)
                                    }.orderBy(LoriCoolCardsUserOwnedCards.receivedAt, SortOrder.DESC)
                                        .limit(1)
                                        .firstOrNull()

                                    if (ownedCard == null)
                                        return@transaction GiveStickerAcceptedTransactionResult.NotEnoughCards

                                    // Transfer
                                    // Delete the old card
                                    LoriCoolCardsUserOwnedCards.deleteWhere {
                                        LoriCoolCardsUserOwnedCards.id eq ownedCard[LoriCoolCardsUserOwnedCards.id]
                                    }

                                    // Insert the new one
                                    LoriCoolCardsUserOwnedCards.insert {
                                        it[LoriCoolCardsUserOwnedCards.card] = ownedCard[LoriCoolCardsUserOwnedCards.card]
                                        it[LoriCoolCardsUserOwnedCards.user] = args[options.user].user.idLong
                                        it[LoriCoolCardsUserOwnedCards.event] = event[LoriCoolCardsEvents.id]
                                        it[LoriCoolCardsUserOwnedCards.receivedAt] = now
                                        it[LoriCoolCardsUserOwnedCards.sticked] = false
                                    }

                                    // Have we already seen this card before?
                                    val haveWeAlreadySeenThisCardBefore = LoriCoolCardsSeenCards.select {
                                        LoriCoolCardsSeenCards.card eq ownedCard[LoriCoolCardsUserOwnedCards.card] and (LoriCoolCardsSeenCards.user eq context.user.idLong)
                                    }.count() != 0L

                                    // "Seen cards" just mean that the card won't be unknown (???) when the user looks it up, even if they give the card away
                                    if (!haveWeAlreadySeenThisCardBefore) {
                                        LoriCoolCardsSeenCards.insert {
                                            it[LoriCoolCardsSeenCards.card] = ownedCard[LoriCoolCardsUserOwnedCards.card]
                                            it[LoriCoolCardsSeenCards.user] = context.user.idLong
                                            it[LoriCoolCardsSeenCards.seenAt] = now
                                        }
                                    }

                                    return@transaction GiveStickerAcceptedTransactionResult.Success(ownedCard)
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
                                                "Figurinha não existe"
                                            )
                                        }
                                    }
                                    GiveStickerAcceptedTransactionResult.NotEnoughCards -> {
                                        context.reply(false) {
                                            styled(
                                                "Você não tem figurinhas suficientes!"
                                            )
                                        }
                                    }
                                    is GiveStickerAcceptedTransactionResult.Success -> {
                                        it.reply(false) {
                                            styled(
                                                "Transferência realizada com sucesso! ${user.user.asMention} recebeu **`${result.givenCard[LoriCoolCardsEventCards.fancyCardId]} - ${result.givenCard[LoriCoolCardsEventCards.title]}`**!",
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
    }

    sealed class GetCardInfoResult {
        data object EventUnavailable : GetCardInfoResult()
        data object UnknownCard : GetCardInfoResult()
        class Success(
            val template: StickerAlbumTemplate,
            val card: ResultRow,
            val isSeen: Boolean,
            val isSticked: Boolean,
            val cardsOfThisTypeOwnedByTheCurrentUserNotSticked: Long,
            val cardsInCirculation: Long,
            val cardsInAlbums: Long
        ) : GetCardInfoResult()
    }

    sealed class GiveStickerResult {
        data object EventUnavailable : GiveStickerResult()
        data object UnknownCard : GiveStickerResult()
        data object NotEnoughCards : GiveStickerResult()
        data class Success(val givenCard: ResultRow) : GiveStickerResult()
    }

    sealed class GiveStickerAcceptedTransactionResult {
        data object EventUnavailable : GiveStickerAcceptedTransactionResult()
        data object UnknownCard : GiveStickerAcceptedTransactionResult()
        data object NotEnoughCards : GiveStickerAcceptedTransactionResult()
        data class Success(val givenCard: ResultRow) : GiveStickerAcceptedTransactionResult()
    }
}