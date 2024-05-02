package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEventCards
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEvents
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsUserOwnedCards
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.select
import java.time.Instant

class LoriCoolCardsCompareStickersExecutor(val loritta: LorittaBot, private val loriCoolCardsCommand: LoriCoolCardsCommand) : LorittaSlashCommandExecutor() {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Loricoolcards.Compare
    }

    inner class Options : ApplicationCommandOptions() {
        val user = user("user", I18N_PREFIX.Options.User.Text)
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.deferChannelMessage(false)

        val userToBeComparedTo = args[options.user]

        // We expect that this is already deferred by the caller
        val now = Instant.now()

        // Load the current active event
        val result = loritta.transaction {
            // First we will get the active cards event to get the album template
            val event = LoriCoolCardsEvents.select {
                LoriCoolCardsEvents.endsAt greaterEq now and (LoriCoolCardsEvents.startsAt lessEq now)
            }.firstOrNull() ?: return@transaction CompareStickersResult.EventUnavailable

            val eventStickers = LoriCoolCardsEventCards.select {
                LoriCoolCardsEventCards.event eq event[LoriCoolCardsEvents.id]
            }.toList()

            val stickersThatYouHaveStickedIds = LoriCoolCardsUserOwnedCards
                .slice(LoriCoolCardsUserOwnedCards.card, LoriCoolCardsUserOwnedCards.card.count())
                .select {
                    LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id] and (LoriCoolCardsUserOwnedCards.user eq context.user.idLong) and (LoriCoolCardsUserOwnedCards.sticked eq true)
                }
                .groupBy(LoriCoolCardsUserOwnedCards.card)
                .toList()
                .map { it[LoriCoolCardsUserOwnedCards.card].value }

            val stickersThatYourFriendHasStickedIds = LoriCoolCardsUserOwnedCards
                .innerJoin(LoriCoolCardsEventCards)
                .slice(LoriCoolCardsUserOwnedCards.card, LoriCoolCardsUserOwnedCards.card.count())
                .select {
                    LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id] and (LoriCoolCardsUserOwnedCards.user eq userToBeComparedTo.user.idLong) and (LoriCoolCardsUserOwnedCards.sticked eq true)
                }
                .groupBy(LoriCoolCardsUserOwnedCards.card)
                .toList()
                .map { it[LoriCoolCardsUserOwnedCards.card].value }

            val stickersThatYouHaveInYourInventoryIds = LoriCoolCardsUserOwnedCards
                .slice(LoriCoolCardsUserOwnedCards.card, LoriCoolCardsUserOwnedCards.card.count())
                .select {
                    LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id] and (LoriCoolCardsUserOwnedCards.user eq context.user.idLong) and (LoriCoolCardsUserOwnedCards.sticked eq false)
                }
                .groupBy(LoriCoolCardsUserOwnedCards.card)
                .toList()
                .map { it[LoriCoolCardsUserOwnedCards.card].value }

            val stickersThatYourFriendHasInTheirInventoryIds = LoriCoolCardsUserOwnedCards
                .innerJoin(LoriCoolCardsEventCards)
                .slice(LoriCoolCardsUserOwnedCards.card, LoriCoolCardsUserOwnedCards.card.count())
                .select {
                    LoriCoolCardsUserOwnedCards.event eq event[LoriCoolCardsEvents.id] and (LoriCoolCardsUserOwnedCards.user eq userToBeComparedTo.user.idLong) and (LoriCoolCardsUserOwnedCards.sticked eq false)
                }
                .groupBy(LoriCoolCardsUserOwnedCards.card)
                .toList()
                .map { it[LoriCoolCardsUserOwnedCards.card].value }

            CompareStickersResult.Success(
                eventStickers,
                stickersThatYouHaveStickedIds,
                stickersThatYourFriendHasStickedIds,
                stickersThatYouHaveInYourInventoryIds,
                stickersThatYourFriendHasInTheirInventoryIds
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
                context.reply(false) {
                    embed {
                        title = "${Emotes.LoriLurk} Comparando Figurinhas"

                        val yourStickersMissing = mutableListOf<ResultRow>()

                        for (stickerId in result.stickersThatYourFriendInTheirInventory) {
                            val doWeHaveIt = result.stickersThatYouHaveSticked.contains(stickerId) || result.stickersThatYouHaveInYourInventory.contains(stickerId)

                            if (!doWeHaveIt) {
                                // We don't have it!
                                val stickerInfo = result.eventStickers.first { it[LoriCoolCardsEventCards.id].value == stickerId }
                                yourStickersMissing.add(stickerInfo)
                            }
                        }

                        val friendStickersMissing = mutableListOf<ResultRow>()

                        for (stickerId in result.stickersThatYouHaveInYourInventory) {
                            val doWeHaveIt = result.stickersThatYourFriendHasSticked.contains(stickerId) || result.stickersThatYourFriendInTheirInventory.contains(stickerId)

                            if (!doWeHaveIt) {
                                // We don't have it!
                                val stickerInfo = result.eventStickers.first { it[LoriCoolCardsEventCards.id].value == stickerId }
                                friendStickersMissing.add(stickerInfo)
                            }
                        }

                        /* val yourStickersMissingLines = buildString {
                            for (stickerInfo in yourStickersMissing.sortedBy { it[LoriCoolCardsEventCards.fancyCardId] }) {
                                appendLine("${stickerInfo[LoriCoolCardsEventCards.fancyCardId]} - ${stickerInfo[LoriCoolCardsEventCards.title].replace("_", "\\_")}")
                            }
                        }.lines().toMutableList()

                        val friendStickersMissingLines = buildString {
                            for (stickerInfo in friendStickersMissing.sortedBy { it[LoriCoolCardsEventCards.fancyCardId] }) {
                                appendLine("${stickerInfo[LoriCoolCardsEventCards.fancyCardId]} - ${stickerInfo[LoriCoolCardsEventCards.title].replace("_", "\\_")}")
                            }
                        }.lines().toMutableList()

                        field {
                            name = "Figurinhas que você precisa e que ${userToBeComparedTo.user.name} tem"
                            value = buildString {
                                while (true) {
                                    val newLine = yourStickersMissingLines.firstOrNull() ?: break
                                    if (this.length + newLine.length + 1 > 1024) { // Plus one for the line break
                                        break
                                    }
                                    yourStickersMissingLines.removeAt(0)
                                    appendLine(newLine)
                                }
                            }
                            inline = true
                        }

                        field {
                            name = "Figurinhas que ${userToBeComparedTo.user.name} precisa e você tem"
                            value = buildString {
                                while (true) {
                                    val newLine = friendStickersMissingLines.firstOrNull() ?: break
                                    if (this.length + newLine.length + 1 > 1024) { // Plus one for the line break
                                        break
                                    }
                                    friendStickersMissingLines.removeAt(0)
                                    appendLine(newLine)
                                }
                            }
                            inline = true
                        }

                        if (yourStickersMissingLines.isNotEmpty() || friendStickersMissingLines.isNotEmpty()) {
                            field {
                                name = ""
                                value = ""
                                inline = false // this needs to be false to cause a line break
                            }

                            field {
                                name = "Figurinhas que ${userToBeComparedTo.user.name} precisa e você tem (continuação)"
                                value = buildString {
                                    while (true) {
                                        val newLine = friendStickersMissingLines.firstOrNull() ?: break
                                        if (this.length + newLine.length + 1 > 1024) { // Plus one for the line break
                                            break
                                        }
                                        friendStickersMissingLines.removeAt(0)
                                        appendLine(newLine)
                                    }
                                }
                                inline = true
                            }

                            field {
                                name = "Figurinhas que você precisa e que ${userToBeComparedTo.user.name} tem (continuação)"
                                value = buildString {
                                    while (true) {
                                        val newLine = yourStickersMissingLines.firstOrNull() ?: break
                                        if (this.length + newLine.length + 1 > 1024) { // Plus one for the line break
                                            break
                                        }
                                        yourStickersMissingLines.removeAt(0)
                                        appendLine(newLine)
                                    }
                                }
                                inline = true
                            }
                        } */

                        description = "${Emotes.LoriHanglooseRight} **Figurinhas que você precisa e que ${userToBeComparedTo.user.asMention} tem:** ${yourStickersMissing.sortedBy { it[LoriCoolCardsEventCards.fancyCardId] }.joinToString { it[LoriCoolCardsEventCards.fancyCardId] }}\n\n${Emotes.PantufaHanglooseRight} **Figurinhas que ${userToBeComparedTo.user.asMention} precisa e você tem:** ${friendStickersMissing.sortedBy { it[LoriCoolCardsEventCards.fancyCardId] }.joinToString { it[LoriCoolCardsEventCards.fancyCardId] }}"
                        color = LorittaColors.LorittaAqua.rgb
                    }
                }
            }
        }
    }

    sealed class CompareStickersResult {
        data object EventUnavailable : CompareStickersResult()
        class Success(
            val eventStickers: List<ResultRow>,
            val stickersThatYouHaveSticked: List<Long>,
            val stickersThatYourFriendHasSticked: List<Long>,
            val stickersThatYouHaveInYourInventory: List<Long>,
            val stickersThatYourFriendInTheirInventory: List<Long>
        ) : CompareStickersResult()
    }
}