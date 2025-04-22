package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import net.dv8tion.jda.api.components.button.ButtonStyle
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
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
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class LoriCoolCardsDuplicateStickersInventoryExecutor(val loritta: LorittaBot, private val loriCoolCardsCommand: LoriCoolCardsCommand) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Loricoolcards.Duplicates
    }

    inner class Options : ApplicationCommandOptions() {
        val user = optionalUser("user", I18N_PREFIX.Options.User.Text)
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.deferChannelMessage(false)

        val userThatWillBeLookedUp = args[options.user]?.user ?: context.user

        // We expect that this is already deferred by the caller
        val now = Instant.now()

        val stickerCountColumn = LoriCoolCardsUserOwnedCards.card.count()

        // Load the current active event
        val result = loritta.transaction {
            // First we will get the active cards event to get the album template
            val event = LoriCoolCardsEvents.selectAll().where {
                LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
            }.firstOrNull() ?: return@transaction DuplicateStickersResult.EventUnavailable

            val eventStickers = LoriCoolCardsEventCards.selectAll().where {
                LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id]
            }.toList()

            // First we get the stickers that we have sticked
            val stickersThatYouHaveStickedIds = LoriCoolCardsUserOwnedCards
                .select(LoriCoolCardsUserOwnedCards.card, LoriCoolCardsUserOwnedCards.card.count())
                .where {
                    LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id] and (LoriCoolCardsUserOwnedCards.user eq userThatWillBeLookedUp.idLong) and (LoriCoolCardsUserOwnedCards.sticked eq true)
                }
                .groupBy(LoriCoolCardsUserOwnedCards.card)
                .toList()
                .map { it[LoriCoolCardsUserOwnedCards.card].value }

            // Then we get the stickers that aren't sticked, with their counts
            val stickersThatYouHaveInYourInventoryWithTheirCountsIds = LoriCoolCardsUserOwnedCards
                .select(LoriCoolCardsUserOwnedCards.card, stickerCountColumn)
                .where {
                    LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id] and (LoriCoolCardsUserOwnedCards.user eq userThatWillBeLookedUp.idLong) and (LoriCoolCardsUserOwnedCards.sticked eq false)
                }
                .groupBy(LoriCoolCardsUserOwnedCards.card)
                .toList()
                .map { it[LoriCoolCardsUserOwnedCards.card].value to it[stickerCountColumn] }

            // So, here's how to figure out if a sticker is a duplicate:
            // If we have it sticked + have one of them in our inventory
            // If we DON'T have them sticked + we have TWO of them in our inventory (this result will be -1)
            val stickerIdsToCount = mutableMapOf<Long, Long>()
            for ((stickerId, inventoryCount) in stickersThatYouHaveInYourInventoryWithTheirCountsIds) {
                val isSticked = stickersThatYouHaveStickedIds.contains(stickerId)

                if (isSticked)
                    stickerIdsToCount[stickerId] = inventoryCount
                else if (inventoryCount >= 2)
                    stickerIdsToCount[stickerId] = inventoryCount - 1
            }

            DuplicateStickersResult.Success(
                stickerIdsToCount.map { (stickerId, stickerCount) ->
                    DuplicateSticker(
                        eventStickers.first { it[LoriCoolCardsEventCards.id].value == stickerId }[LoriCoolCardsEventCards.fancyCardId],
                        stickerCount
                    )
                }
            )
        }

        when (result) {
            DuplicateStickersResult.EventUnavailable -> {
                context.reply(false) {
                    styled(
                        "Nenhum evento de figurinhas ativo"
                    )
                }
            }
            is DuplicateStickersResult.Success -> {
                context.reply(false) {
                    embed {
                        if (userThatWillBeLookedUp == context.user) {
                            title = "${Emotes.LoriLurk} ${context.i18nContext.get(I18N_PREFIX.YourDuplicateStickers)}"
                        } else {
                            title = "${Emotes.LoriLurk} ${context.i18nContext.get(I18N_PREFIX.UserDuplicateStickers(userThatWillBeLookedUp.name))}"
                        }

                        var isFirst = true
                        var descriptionHasOverflown = false

                        description = buildString {
                            for (duplicateSticker in result.duplicateStickers.sortedBy { it.fancyStickerId }) {
                                var str = if (isFirst)
                                    ""
                                else ", "
                                str += "${duplicateSticker.count}x ${duplicateSticker.fancyStickerId}"
                                isFirst = false
                                if (str.length + this.length > DiscordResourceLimits.Embed.Description) {
                                    descriptionHasOverflown = true
                                    return@buildString
                                }

                                append(str)
                            }
                        }

                        color = LorittaColors.LorittaAqua.rgb

                        if (descriptionHasOverflown) {
                            footer(context.i18nContext.get(I18N_PREFIX.YouHaveSoManyStickersThatItDoesntFitAllInHere))
                        }
                    }

                    // This is useful for phone users to copy the sticker list
                    val stickersThatYouNeedButton = UnleashedButton.of(
                        ButtonStyle.PRIMARY,
                        context.i18nContext.get(I18N_PREFIX.GetListOfStickers),
                        Emotes.LoriHanglooseRight
                    )

                    actionRow(
                        if (result.duplicateStickers.isEmpty())
                            stickersThatYouNeedButton.asDisabled()
                        else
                            loritta.interactivityManager.button(
                                context.alwaysEphemeral,
                                stickersThatYouNeedButton
                            ) { context ->
                                context.reply(true) {
                                    content = result.duplicateStickers.sortedBy { it.fancyStickerId }.joinToString { it.fancyStickerId }
                                }
                            }
                    )
                }
            }
        }
    }

    sealed class DuplicateStickersResult {
        data object EventUnavailable : DuplicateStickersResult()
        class Success(
            val duplicateStickers: List<DuplicateSticker>
        ) : DuplicateStickersResult()
    }

    data class DuplicateSticker(
        val fancyStickerId: String,
        val count: Long
    )

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?>? {
        TODO("Not yet implemented")
    }
}