package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEventCards
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsUserOwnedCards
import net.perfectdreams.loritta.common.utils.LorittaColors
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
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class LoriCoolCardsCompareStickersExecutor(val loritta: LorittaBot, private val loriCoolCardsCommand: LoriCoolCardsCommand) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Loricoolcards.Compare
    }

    inner class Options : ApplicationCommandOptions() {
        val user = user("user", I18N_PREFIX.Options.User.Text)
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        // How compare should work?
        // It should show ONLY stickers that
        // - You don't have sticked
        // - You don't have them in your inventory
        // - Your friend has them

        context.deferChannelMessage(false)

        val userToBeComparedTo = args[options.user].user

        // We expect that this is already deferred by the caller
        val now = Instant.now()

        // Load the current active event
        val result = loritta.transaction {
            // First we will get the active cards event to get the album template
            val event = LoriCoolCardsEvents.selectAll().where {
                LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
            }.firstOrNull() ?: return@transaction CompareStickersResult.EventUnavailable

            val player1Stickers = getStickers(
                event[LoriCoolCardsEvents.id].value,
                context.user
            )

            val player2Stickers = getStickers(
                event[LoriCoolCardsEvents.id].value,
                userToBeComparedTo
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

            CompareStickersResult.Success(
                eventStickers,
                player1Stickers,
                player2Stickers
            )
        }

        when (result) {
            CompareStickersResult.EventUnavailable -> {
                context.reply(false) {
                    styled(
                        "Nenhum evento de figurinhas ativo"
                    )
                }
            }
            is CompareStickersResult.Success -> {
                val yourStickersMissing = getMissingStickers(result.eventStickers, result.player1Stickers, result.player2Stickers)
                val friendStickersMissing = getMissingStickers(result.eventStickers, result.player2Stickers, result.player1Stickers)

                context.reply(false) {
                    embed {
                        title = "${Emotes.LoriLurk} Comparando Figurinhas"

                        description = buildString {
                            append("${Emotes.LoriHanglooseRight} ${context.i18nContext.get(I18N_PREFIX.WhatStickersYouNeedThatTheOtherUserHas(userToBeComparedTo = userToBeComparedTo.asMention, yourStickersMissing.size, if (yourStickersMissing.isEmpty()) context.i18nContext.get(I18N_PREFIX.NoStickersToBeCompared) else yourStickersMissing.sortedBy { it[LoriCoolCardsEventCards.fancyCardId] }.joinToString { it[LoriCoolCardsEventCards.fancyCardId] }))}")
                            append("\n\n")
                            append("${Emotes.PantufaHanglooseRight} ${context.i18nContext.get(I18N_PREFIX.WhatStickersYouHaveThatTheOtherUserNeeds(friend = userToBeComparedTo.asMention, friendStickersMissing.size, if (friendStickersMissing.isEmpty()) context.i18nContext.get(I18N_PREFIX.NoStickersToBeCompared) else friendStickersMissing.sortedBy { it[LoriCoolCardsEventCards.fancyCardId] }.joinToString { it[LoriCoolCardsEventCards.fancyCardId] }))}")
                        }

                        color = LorittaColors.LorittaAqua.rgb
                    }

                    // This is useful for phone users to copy the sticker list
                    val stickersThatYouNeedButton = UnleashedButton.of(
                        ButtonStyle.PRIMARY,
                        context.i18nContext.get(I18N_PREFIX.GetFirstListOfStickers),
                        Emotes.LoriHanglooseRight
                    )

                    val stickersThatOtherFriendButton = UnleashedButton.of(
                        ButtonStyle.PRIMARY,
                        context.i18nContext.get(I18N_PREFIX.GetSecondListOfStickers),
                        Emotes.PantufaHanglooseRight
                    )

                    actionRow(
                        if (yourStickersMissing.isEmpty())
                            stickersThatYouNeedButton.asDisabled()
                        else
                            loritta.interactivityManager.button(
                                context.alwaysEphemeral,
                                stickersThatYouNeedButton
                            ) { context ->
                                context.reply(true) {
                                    content = yourStickersMissing.sortedBy { it[LoriCoolCardsEventCards.fancyCardId] }.joinToString { it[LoriCoolCardsEventCards.fancyCardId] }
                                }
                            }
                    )

                    actionRow(
                        if (friendStickersMissing.isEmpty())
                            stickersThatOtherFriendButton.asDisabled()
                        else
                            loritta.interactivityManager.button(
                                context.alwaysEphemeral,
                                stickersThatOtherFriendButton
                            ) { context ->
                                context.reply(true) {
                                    content = friendStickersMissing.sortedBy { it[LoriCoolCardsEventCards.fancyCardId] }.joinToString { it[LoriCoolCardsEventCards.fancyCardId] }
                                }
                            }
                    )
                }
            }
        }
    }

    private fun getStickers(
        eventId: Long,
        user: User,
    ): UserStickers {
        // This gets ALL the sticked stickers
        val stickerIdsThatYouHaveSticked = LoriCoolCardsUserOwnedCards
            .select(LoriCoolCardsUserOwnedCards.card, LoriCoolCardsUserOwnedCards.card.count())
            .where {
                LoriCoolCardsUserOwnedCards.event eq eventId and (LoriCoolCardsUserOwnedCards.user eq user.idLong) and (LoriCoolCardsUserOwnedCards.sticked eq true)
            }
            .groupBy(LoriCoolCardsUserOwnedCards.card)
            .toList()
            .map { it[LoriCoolCardsUserOwnedCards.card].value }

        // This gets ALL the non-sticked stickers, this is used to know if we can give them to our friend
        val stickersIdsThatYouHaveInYourInventory = LoriCoolCardsUserOwnedCards
            .select(LoriCoolCardsUserOwnedCards.card, LoriCoolCardsUserOwnedCards.card.count())
            .where {
                LoriCoolCardsUserOwnedCards.event eq eventId and (LoriCoolCardsUserOwnedCards.user eq user.idLong) and (LoriCoolCardsUserOwnedCards.sticked eq false)
            }
            .groupBy(LoriCoolCardsUserOwnedCards.card)
            .toList()
            .map { it[LoriCoolCardsUserOwnedCards.card].value }
            .toSet()

        val stickerIdsThatYouHave = mutableSetOf<Long>()
        stickerIdsThatYouHave.addAll(stickerIdsThatYouHaveSticked)
        stickerIdsThatYouHave.addAll(stickersIdsThatYouHaveInYourInventory)

        return UserStickers(
            stickerIdsThatYouHave,
            stickersIdsThatYouHaveInYourInventory
        )
    }

    private fun getMissingStickers(
        eventStickers: List<ResultRow>,
        whoNeedsTheStickers: UserStickers,
        whoHasTheStickers: UserStickers
    ): List<ResultRow>  {
        val yourStickersMissing = mutableListOf<ResultRow>()

        // What stickers you need that the other user has?
        for (stickerId in whoHasTheStickers.stickerIdsThatWeHaveInOurInventory) {
            val doWeHaveIt = whoNeedsTheStickers.stickerIdsThatWeHave.contains(stickerId)

            if (!doWeHaveIt) {
                // We don't have it!
                val stickerInfo = eventStickers.first { it[LoriCoolCardsEventCards.id].value == stickerId }
                yourStickersMissing.add(stickerInfo)
            }
        }

        return yourStickersMissing
    }

    data class UserStickers(
        val stickerIdsThatWeHave: Set<Long>,
        val stickerIdsThatWeHaveInOurInventory: Set<Long>,
    )

    sealed class CompareStickersResult {
        data object EventUnavailable : CompareStickersResult()
        class Success(
            val eventStickers: List<ResultRow>,
            val player1Stickers: UserStickers,
            val player2Stickers: UserStickers
        ) : CompareStickersResult()
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?>? {
        TODO("Not yet implemented")
    }
}