package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaBovespaBrokerUtils.BrokerSonhosTransactionsEntryAction.BOUGHT_SHARES
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaBovespaBrokerUtils.BrokerSonhosTransactionsEntryAction.SOLD_SHARES
import net.perfectdreams.loritta.cinnamon.common.utils.TransactionType
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.pudding.data.BrokerSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UnknownSonhosTransaction
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import kotlin.math.ceil
import kotlin.time.ExperimentalTime

class TransactionsExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(TransactionsExecutor::class) {
        /* object Options : CommandOptions() {
            val user = optionalUser("user", CancelledCommand.I18N_PREFIX.Options.User)
                .register()
        }

        override val options = Options */

        const val TRANSACTIONS_PER_PAGE = 10

        @OptIn(ExperimentalTime::class)
        suspend fun createMessage(
            loritta: LorittaCinnamon,
            data: ChangeTransactionPageData
        ): MessageBuilder.() -> (Unit) {
            val transactions = loritta.services.sonhos.getUserTransactions(
                data.viewingTransactionsOfUserId,
                data.transactionTypeFilter,
                TRANSACTIONS_PER_PAGE,
                (data.page * TRANSACTIONS_PER_PAGE)
            )

            val totalTransactions = loritta.services.sonhos.getUserTotalTransactions(
                data.viewingTransactionsOfUserId,
                data.transactionTypeFilter
            )

            return {
                embed {
                    title = "Suas Transações"
                    color = Color(26, 160, 254)

                    description = buildString {
                        for (transaction in transactions) {
                            append("[<t:${transaction.timestamp.epochSeconds}:f> | <t:${transaction.timestamp.epochSeconds}:R>]")
                            append(" ")
                            when (transaction) {
                                is BrokerSonhosTransaction -> {
                                    when (transaction.action) {
                                        BOUGHT_SHARES -> append("\uD83D\uDCB8 Comprou ${transaction.stockQuantity} ações de `${transaction.ticker}` por ${transaction.sonhos} sonhos")
                                        SOLD_SHARES -> append("\uD83D\uDCB8 Vendeu ${transaction.stockQuantity} ações de `${transaction.ticker}` por ${transaction.sonhos} sonhos")
                                    }
                                }
                                is UnknownSonhosTransaction -> {
                                    append("${Emotes.LoriShrug} Transação Desconhecida (Bug?)")
                                }
                            }
                            append("\n")
                        }
                    }

                    footer("Total de transações: $totalTransactions")
                }

                val totalPages = ceil((totalTransactions / TRANSACTIONS_PER_PAGE.toDouble())).toLong()
                val addLeftButton = data.page != 0L
                val addRightButton = (data.page + 1) != totalPages

                actionRow {
                    if (addLeftButton) {
                        interactiveButton(
                            ButtonStyle.Secondary,
                            ChangeTransactionPageButtonClickExecutor,
                            ComponentDataUtils.encode(
                                data.copy(
                                    page = data.page - 1
                                )
                            )
                        ) {
                            label = "<"
                        }
                    } else {
                        interactiveButton(
                            ButtonStyle.Secondary,
                            ChangeTransactionPageButtonClickExecutor,
                            ComponentDataUtils.encode(
                                data.copy(
                                    page = data.page - 1
                                )
                            )
                        ) {
                            label = "<"
                            disabled = true
                        }
                    }

                    if (addRightButton) {
                        interactiveButton(
                            ButtonStyle.Secondary,
                            ChangeTransactionPageButtonClickExecutor,
                            ComponentDataUtils.encode(
                                data.copy(
                                    page = data.page + 1
                                )
                            )
                        ) {
                            label = ">"
                        }
                    } else {
                        interactiveButton(
                            ButtonStyle.Secondary,
                            ChangeTransactionPageButtonClickExecutor,
                            ComponentDataUtils.encode(
                                data.copy(
                                    page = data.page + 1
                                )
                            )
                        ) {
                            label = ">"
                            disabled = true
                        }
                    }
                }

                actionRow {
                    selectMenu("aaaa") {
                        val transactionTypes = TransactionType.values()
                        this.allowedValues = 1..(25.coerceAtMost(transactionTypes.size))

                        for (transactionType in transactionTypes) {
                            option(transactionType.name, transactionType.name) {
                                default = true
                            }
                        }
                    }
                }
            }
        }
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessage() // Defer because this sometimes takes too long

        val userId = UserId(context.user.id.value)

        val builtMessage = createMessage(
            context.loritta,
            ChangeTransactionPageData(
                context.user.id,
                userId,
                0,
                TransactionType.values().toList()
            )
        )

        context.sendMessage {
            apply(builtMessage)
        }
    }
}