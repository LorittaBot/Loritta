package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
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
import org.jetbrains.exposed.sql.select
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
            val event = LoriCoolCardsEvents.select {
                LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
            }.firstOrNull() ?: return@transaction DuplicateStickersResult.EventUnavailable

            val eventStickers = LoriCoolCardsEventCards.select {
                LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id]
            }.toList()

            val stickersThatYouHaveStickedIds = LoriCoolCardsUserOwnedCards
                .slice(LoriCoolCardsUserOwnedCards.card, LoriCoolCardsUserOwnedCards.card.count())
                .select {
                    LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id] and (LoriCoolCardsUserOwnedCards.user eq userThatWillBeLookedUp.idLong) and (LoriCoolCardsUserOwnedCards.sticked eq true)
                }
                .groupBy(LoriCoolCardsUserOwnedCards.card)
                .toList()
                .map { it[LoriCoolCardsUserOwnedCards.card].value }

            val stickersThatYouHaveInYourInventoryExcludingStickersThatArentStickedYetIds = LoriCoolCardsUserOwnedCards
                .slice(LoriCoolCardsUserOwnedCards.card, stickerCountColumn)
                .select {
                    LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id] and (LoriCoolCardsUserOwnedCards.user eq userThatWillBeLookedUp.idLong) and (LoriCoolCardsUserOwnedCards.sticked eq false) and (LoriCoolCardsUserOwnedCards.card inList stickersThatYouHaveStickedIds)
                }
                .groupBy(LoriCoolCardsUserOwnedCards.card)
                .toList()
                .map { it[LoriCoolCardsUserOwnedCards.card].value to it[stickerCountColumn] }

            DuplicateStickersResult.Success(
                eventStickers,
                stickersThatYouHaveStickedIds,
                stickersThatYouHaveInYourInventoryExcludingStickersThatArentStickedYetIds
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
                val yourStickersDuplicated = mutableListOf<Pair<ResultRow, Long>>()

                for ((stickerId, stickerCount) in result.stickersThatYouHaveInYourInventoryExcludingStickersThatArentStickedYet) {
                    val stickerInfo = result.eventStickers.first { it[LoriCoolCardsEventCards.id].value == stickerId }
                    yourStickersDuplicated.add(Pair(stickerInfo, stickerCount))
                }

                context.reply(false) {
                    embed {
                        if (userThatWillBeLookedUp == context.user) {
                            title = "${Emotes.LoriLurk} ${context.i18nContext.get(I18N_PREFIX.YourDuplicateStickers)}"
                        } else {
                            title = "${Emotes.LoriLurk} ${context.i18nContext.get(I18N_PREFIX.UserDuplicateStickers(userThatWillBeLookedUp.name))}"
                        }

                        description = buildString {
                            append(yourStickersDuplicated.sortedBy { it.first[LoriCoolCardsEventCards.fancyCardId] }.joinToString { "${it.first[LoriCoolCardsEventCards.fancyCardId]} (${it.second}x)" })
                        }

                        color = LorittaColors.LorittaAqua.rgb
                    }

                    // This is useful for phone users to copy the sticker list
                    val stickersThatYouNeedButton = UnleashedButton.of(
                        ButtonStyle.PRIMARY,
                        context.i18nContext.get(I18N_PREFIX.GetListOfStickers),
                        Emotes.LoriHanglooseRight
                    )

                    actionRow(
                        if (yourStickersDuplicated.isEmpty())
                            stickersThatYouNeedButton.asDisabled()
                        else
                            loritta.interactivityManager.button(
                                stickersThatYouNeedButton
                            ) { context ->
                                context.reply(true) {
                                    content = yourStickersDuplicated.sortedBy { it.first[LoriCoolCardsEventCards.fancyCardId] }.joinToString { it.first[LoriCoolCardsEventCards.fancyCardId] }
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
            val eventStickers: List<ResultRow>,
            val stickersThatYouHaveSticked: List<Long>,
            val stickersThatYouHaveInYourInventoryExcludingStickersThatArentStickedYet: List<Pair<Long, Long>>
        ) : DuplicateStickersResult()
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?>? {
        TODO("Not yet implemented")
    }
}