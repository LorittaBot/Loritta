package net.perfectdreams.loritta.cinnamon.platform.commands.utils

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.i18nhelper.core.keys.StringI18nKey
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.MoneyCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.styled

class MoneyExecutor(val ecbManager: ECBManager) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(MoneyExecutor::class) {
        object Options : CommandOptions() {
            val from = string("from", MoneyCommand.I18N_PREFIX.Options.From)
                .also {
                    for (currencyId in MoneyCommand.currencyIds) {
                        it.choice(
                            currencyId,
                            StringI18nData(
                                StringI18nKey("commands.command.money.currencies.${currencyId.lowercase()}"),
                                emptyMap()
                            )
                        )
                    }
                }
                .register()

            val to = string("to", MoneyCommand.I18N_PREFIX.Options.To)
                .also {
                    for (currencyId in MoneyCommand.currencyIds) {
                        it.choice(
                            currencyId,
                            StringI18nData(
                                StringI18nKey("commands.command.money.currencies.${currencyId.lowercase()}"),
                                emptyMap()
                            )
                        )
                    }
                }
                .register()

            val quantity = optionalNumber("quantity", MoneyCommand.I18N_PREFIX.Options.Quantity)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val from = args[options.from]
        val to = args[options.to]
        val multiply = args[options.quantity] ?: 1.0
        val exchangeRates = ecbManager.getOrUpdateExchangeRates().await()

        val value: Double

        if (from == to) { // :rolling_eyes:
            value = 1.0
        } else {
            // Para calcular, devemos lembrar que a base é em EUR
            // Então, para converter, primeiro devemos converter a currency para EUR e depois para o target
            // Primeiro iremos verificar se existe no exchange rate
            // Por exemplo, se a gente colocar BRL, o "valueInEuros" será 5.5956
            //
            // I don't think that those "InvalidCurrency" code paths will be ever be called in a platform that supports arguments.
            // (Example: Discord Slash Commands)
            val euroValueInCurrency = exchangeRates[from] ?: context.failEphemerally {
                styled(
                    prefix = Emotes.error,
                    content = context.i18nContext.get(
                        MoneyCommand.I18N_PREFIX.InvalidCurrency(
                            currency = from
                        )
                    )
                )

                styled(
                    content = context.i18nContext.get(
                        MoneyCommand.I18N_PREFIX.CurrencyList(
                            list = exchangeRates.keys.joinToString(transform = { "`$it`" })
                        )
                    )
                )
            }

            val valueInEuro = 1 / euroValueInCurrency

            val endValueInEuros = exchangeRates[to] ?: context.failEphemerally {
                styled(
                    prefix = Emotes.error,
                    content = context.i18nContext.get(
                        MoneyCommand.I18N_PREFIX.InvalidCurrency(
                            currency = from
                        )
                    )
                )

                styled(
                    content = context.i18nContext.get(
                        MoneyCommand.I18N_PREFIX.CurrencyList(
                            list = exchangeRates.keys.joinToString(transform = { "`$it`" })
                        )
                    )
                )
            }

            value = endValueInEuros * valueInEuro
        }

        context.sendReply(
            prefix = "\uD83D\uDCB5",
            content = context.i18nContext.get(
                MoneyCommand.I18N_PREFIX.Result(
                    multiply,
                    from,
                    to,
                    value * multiply
                )
            )
        )
    }
}