package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.footer
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
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
import kotlin.time.ExperimentalTime

class TransactionsExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(TransactionsExecutor::class) {
        /* object Options : CommandOptions() {
            val user = optionalUser("user", CancelledCommand.I18N_PREFIX.Options.User)
                .register()
        }

        override val options = Options */

        @OptIn(ExperimentalTime::class)
        suspend fun createMessage(
            loritta: LorittaCinnamon,
            data: ChangeTransactionPageData
        ): MessageBuilder.() -> (Unit) {
            val transactions = loritta.services.sonhos.getUserTransactions(
                data.viewingTransactionsOfUserId,
                10,
                data.page
            )

            val totalTransactions = loritta.services.sonhos.getUserTotalTransactions(data.viewingTransactionsOfUserId)

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
                                    append("\uD83D\uDCB8 Comprou ${transaction.stockQuantity} ações de `${transaction.ticker}` por ${transaction.sonhos} sonhos")
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

                actionRow {
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
                0
            )
        )

        context.sendMessage {
            apply(builtMessage)
        }
    }
}