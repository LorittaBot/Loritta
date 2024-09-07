package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.i18nhelper.core.keys.StringI18nKey
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.ecb.ECBManager
import java.util.*

class MoneyCommand(val loritta: LorittaBot, val ecbManager: ECBManager) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Money

        // Remember, Discord Slash Commands have a limit of 25 options per command!
        // The overflown options are taken out before being registered
        val currencyIds = listOf(
            "EUR",
            "USD",
            "BRL",
            "JPY",
            "BGN",
            "CZK",
            "DKK",
            "GBP",
            "HUF",
            "PLN",
            "RON",
            "SEK",
            "CHF",
            "ISK",
            "NOK",
            "HRK",
            "RUB",
            "TRY",
            "AUD",
            "CAD",
            "CNY",
            "HKD",
            "IDR",
            // "shekel" triggers Discord's bad words check, for some reason
            // https://canary.discord.com/channels/613425648685547541/916395737141620797/1022169074089861130
            // "ILS",
            "INR",
            "KRW",
            "MXN",
            "MYR",
            "NZD",
            "PHP",
            "SGD",
            "THB",
            "ZAR"
        )
    }

    override fun command()  = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.UTILS, UUID.fromString("01d57fc8-b86e-4ffc-9177-47a578fe73c2")) {
        enableLegacyMessageSupport = true

        alternativeLegacyLabels.apply {
            add("dinheiro")
            add("grana")
        }

        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        examples = I18N_PREFIX.Examples

        executor = MoneyExecutor()
    }


    inner class MoneyExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val from = string("from", I18N_PREFIX.Options.From) {
                for (currencyId in currencyIds.take(25)) {
                    choice(
                        StringI18nData(
                            StringI18nKey("commands.command.money.currencies.${currencyId.lowercase()}"),
                            emptyMap()
                        ),
                        currencyId,
                    )
                }
            }

            val to = string("to", I18N_PREFIX.Options.To) {
                for (currencyId in currencyIds.take(25)) {
                    choice(
                        StringI18nData(
                            StringI18nKey("commands.command.money.currencies.${currencyId.lowercase()}"),
                            emptyMap()
                        ),
                        currencyId,
                    )
                }
            }

            val quantity = optionalDouble("quantity", I18N_PREFIX.Options.Quantity)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
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
                val euroValueInCurrency = exchangeRates[from] ?: context.fail(true) {
                    styled(
                        prefix = Emotes.Error,
                        content = context.i18nContext.get(
                            I18N_PREFIX.InvalidCurrency(
                                currency = from
                            )
                        )
                    )

                    styled(
                        content = context.i18nContext.get(
                            I18N_PREFIX.CurrencyList(
                                list = exchangeRates.keys.joinToString(transform = { "`$it`" })
                            )
                        )
                    )
                }

                val valueInEuro = 1 / euroValueInCurrency

                val endValueInEuros = exchangeRates[to] ?: context.fail(true) {
                    styled(
                        prefix = Emotes.Error,
                        content = context.i18nContext.get(
                            I18N_PREFIX.InvalidCurrency(
                                currency = from
                            )
                        )
                    )

                    styled(
                        content = context.i18nContext.get(
                            I18N_PREFIX.CurrencyList(
                                list = exchangeRates.keys.joinToString(transform = { "`$it`" })
                            )
                        )
                    )
                }

                value = endValueInEuros * valueInEuro
            }

            context.reply(false) {
                styled(
                    prefix = "\uD83D\uDCB5",
                    content = context.i18nContext.get(
                        I18N_PREFIX.Result(
                            multiply,
                            from,
                            to,
                            value * multiply
                        )
                    )
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val arg0 = context.args.getOrNull(0)
            val arg1 = context.args.getOrNull(1)
            val arg2 = context.args.getOrNull(2)

            if (arg0 == null || arg1 == null) {
                context.explain()
                return null
            }

            return mapOf(
                options.from to arg0.uppercase(),
                options.to to arg1.uppercase(),
                options.quantity to arg2?.toDouble()
            )
        }
    }
}