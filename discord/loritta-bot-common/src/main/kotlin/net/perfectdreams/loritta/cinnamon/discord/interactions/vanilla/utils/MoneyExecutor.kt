package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.i18nhelper.core.keys.StringI18nKey
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.MoneyCommand
import net.perfectdreams.loritta.morenitta.utils.ecb.ECBManager

class MoneyExecutor(loritta: LorittaBot, val ecbManager: ECBManager) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val from = string("from", MoneyCommand.I18N_PREFIX.Options.From) {
            for (currencyId in MoneyCommand.currencyIds.take(25)) {
                choice(
                    StringI18nData(
                        StringI18nKey("commands.command.money.currencies.${currencyId.lowercase()}"),
                        emptyMap()
                    ),
                    currencyId,
                )
            }
        }


        val to = string("to", MoneyCommand.I18N_PREFIX.Options.To) {
            for (currencyId in MoneyCommand.currencyIds.take(25)) {
                choice(
                    StringI18nData(
                        StringI18nKey("commands.command.money.currencies.${currencyId.lowercase()}"),
                        emptyMap()
                    ),
                    currencyId,
                )
            }
        }


        val quantity = optionalNumber("quantity", MoneyCommand.I18N_PREFIX.Options.Quantity)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
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
                    prefix = Emotes.Error,
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
                    prefix = Emotes.Error,
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