package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.transactions

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.xprank.ChangeXpRankPageData
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.utils.LoadingEmojis
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.utils.TransactionType

class ChangeTransactionPageButtonClickExecutor(
    loritta: LorittaCinnamon
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.CHANGE_TRANSACTION_PAGE_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        val decoded = context.decodeDataFromComponentAndRequireUserToMatch<ChangeTransactionPageData>()

        // Loading Section
        val loadingEmoji = LoadingEmojis.random()
        context.updateMessage {
            styled(
                context.i18nContext.get(I18nKeysData.Website.Dashboard.Loading),
                loadingEmoji
            )

            if (decoded.button == ChangeTransactionPageData.Button.GO_TO_THE_LAST_PAGE) {
                actionRow {
                    disabledButton(ButtonStyle.Primary) {
                        label = context.i18nContext.get(SonhosCommand.TRANSACTIONS_I18N_PREFIX.UnknownPage.GoToTheLastPage)
                        loriEmoji = Emotes.LoriSob
                    }
                }
            } else {
                actionRow {
                    disabledButton(ButtonStyle.Primary) {
                        loriEmoji = if (decoded.button == ChangeTransactionPageData.Button.LEFT_ARROW) loadingEmoji else Emotes.ChevronLeft
                    }

                    disabledButton(ButtonStyle.Primary) {
                        loriEmoji = if (decoded.button == ChangeTransactionPageData.Button.RIGHT_ARROW) loadingEmoji else Emotes.ChevronRight
                    }
                }

                actionRow {
                    selectMenu(
                        ChangeTransactionFilterSelectMenuExecutor,
                        ComponentDataUtils.encode(
                            ChangeTransactionFilterData(
                                decoded.userId,
                                decoded.viewingTransactionsOfUserId,
                                decoded.page,
                                decoded.transactionTypeFilter
                            )
                        )
                    ) {
                        val transactionTypes = TransactionType.values()
                        this.allowedValues = 1..(25.coerceAtMost(transactionTypes.size))

                        for (transactionType in transactionTypes) {
                            option(context.i18nContext.get(transactionType.title), transactionType.name) {
                                description = context.i18nContext.get(transactionType.description)
                                loriEmoji = transactionType.emote
                                default = transactionType in  decoded.transactionTypeFilter
                            }
                        }
                    }
                }
            }
        }

        val builtMessage = TransactionsExecutor.createMessage(
            loritta,
            context.i18nContext,
            decoded.userId,
            decoded.viewingTransactionsOfUserId,
            decoded.page,
            decoded.transactionTypeFilter
        )

        context.updateMessage {
            builtMessage()
        }
    }
}