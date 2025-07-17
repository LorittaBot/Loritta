package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

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

class LoriCoolCardsMissingStickersInventoryExecutor(val loritta: LorittaBot, private val loriCoolCardsCommand: LoriCoolCardsCommand) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Loricoolcards.Missing
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
            }.firstOrNull() ?: return@transaction MissingStickersResult.EventUnavailable

            val eventStickers = LoriCoolCardsEventCards.selectAll().where {
                LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id]
            }.toList()

            val stickersThatYouHaveInYourInventory = LoriCoolCardsUserOwnedCards
                .select(LoriCoolCardsUserOwnedCards.card, stickerCountColumn)
                .where {
                    LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id] and (LoriCoolCardsUserOwnedCards.user eq userThatWillBeLookedUp.idLong)
                }
                .groupBy(LoriCoolCardsUserOwnedCards.card)
                .toList()
                .map { it[LoriCoolCardsUserOwnedCards.card].value }

            MissingStickersResult.Success(
                eventStickers,
                stickersThatYouHaveInYourInventory
            )
        }

        when (result) {
            MissingStickersResult.EventUnavailable -> {
                context.reply(false) {
                    styled(
                        "Nenhum evento de figurinhas ativo"
                    )
                }
            }
            is MissingStickersResult.Success -> {
                // Similar to the duplicate stickers code, but it is inverted
                val stickersThatTheUserDoesNotHave = result.eventStickers.map { it[LoriCoolCardsEventCards.id].value } - result.stickersThatYouHaveInYourInventory.toSet()
                val yourStickersMissing = mutableListOf<ResultRow>()

                for (stickerId in stickersThatTheUserDoesNotHave) {
                    val stickerInfo = result.eventStickers.first { it[LoriCoolCardsEventCards.id].value == stickerId }
                    yourStickersMissing.add(stickerInfo)
                }

                context.reply(false) {
                    embed {
                        if (userThatWillBeLookedUp == context.user) {
                            title = "${Emotes.LoriLurk} ${context.i18nContext.get(I18N_PREFIX.YourMissingStickers(yourStickersMissing.size))}"
                        } else {
                            title = "${Emotes.LoriLurk} ${context.i18nContext.get(I18N_PREFIX.UserMissingStickers(userThatWillBeLookedUp.name, yourStickersMissing.size))}"
                        }

                        description = buildString {
                            append(yourStickersMissing.sortedBy { it[LoriCoolCardsEventCards.fancyCardId] }.joinToString { it[LoriCoolCardsEventCards.fancyCardId] })
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
                }
            }
        }
    }

    sealed class MissingStickersResult {
        data object EventUnavailable : MissingStickersResult()
        class Success(
            val eventStickers: List<ResultRow>,
            val stickersThatYouHaveInYourInventory: List<Long>
        ) : MissingStickersResult()
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?>? {
        TODO("Not yet implemented")
    }
}