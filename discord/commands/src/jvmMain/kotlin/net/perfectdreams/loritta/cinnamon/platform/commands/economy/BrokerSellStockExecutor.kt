package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.pudding.services.BovespaBrokerService

class BrokerSellStockExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(BrokerSellStockExecutor::class) {
        object Options : CommandOptions() {
            val ticker = string("ticker", TodoFixThisData)
                .register()

            val quantity = integer("quantity", TodoFixThisData)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessageEphemerally()

        val tickerId = args[Options.ticker]
        val quantity = args[Options.quantity]

        // This should *never* happen because the values are validated on Discord side BUT who knows
        // TODO: Improve this
        if (tickerId !in BrokerInfo.validStocksCodes)
            context.failEphemerally("That is not a valid stock ticker!")

        try {
            context.loritta.services.bovespaBroker.sellStockAsset(
                context.user.id.value.toLong(),
                tickerId,
                quantity
            )
        } catch (e: BovespaBrokerService.NotEnoughStocksException) {
            context.failEphemerally("Você não tem ações suficientes para vender!")
        }

        context.sendEphemeralMessage {
            content = "Prontinho meu chapa tá vendido"
        }
    }
}