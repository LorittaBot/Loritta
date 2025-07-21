package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.InlineEmbed
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.utils.LoadingEmojis
import net.perfectdreams.loritta.cinnamon.discord.utils.NumberUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils.appendUserHaventGotDailyTodayOrUpsellSonhosBundles
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.services.BovespaBrokerService
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenAndStripCodeBackticks
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedButton
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import net.perfectdreams.loritta.serializable.BrokerTickerInformation
import net.perfectdreams.loritta.serializable.UserId
import java.awt.Color
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil

class BrokerCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Broker

        fun InlineMessage<*>.brokerEmbed(context: UnleashedContext, block: InlineEmbed.() -> Unit) {
            embed {
                author("Loritta's Home Broker")
                // TODO: Move this to an object
                color = Color(23, 62, 163).rgb
                thumbnail = "${context.loritta.config.loritta.website.url}assets/img/loritta_stonks.png"
                footer(context.i18nContext.get(I18N_PREFIX.FooterDataInfo))
                apply(block)
            }
        }

        fun getEmojiStatusForTicker(brokerTickerInformation: BrokerTickerInformation) = if (!LorittaBovespaBrokerUtils.checkIfTickerIsActive(brokerTickerInformation.status))
            Emotes.DoNotDisturb
        else if (LorittaBovespaBrokerUtils.checkIfTickerDataIsStale(brokerTickerInformation.lastUpdatedAt))
            Emotes.Idle
        else Emotes.Online

        private const val TICKERS_PER_PAGE = 10
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY, UUID.fromString("65b54675-e0bb-43ec-948a-d6c73e57aaed")) {
        enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        val infoExecutor = BrokerInfoExecutor()
        executor = infoExecutor

        subcommand(I18N_PREFIX.Info.Label, I18N_PREFIX.Info.Description, UUID.fromString("d9d1daa7-9a58-4d3c-bba2-f251d40c2657")) {
            executor = infoExecutor
        }

        subcommand(I18N_PREFIX.Portfolio.Label, I18N_PREFIX.Portfolio.Description, UUID.fromString("05805c8e-e431-4900-b222-4c590c339da5")) {
            executor = BrokerPortfolioExecutor()
        }

        subcommand(I18N_PREFIX.Stock.Label, I18N_PREFIX.Stock.Description, UUID.fromString("1804a409-f0f0-489f-95ff-47363463a453")) {
            executor = BrokerStockInfoExecutor()
        }

        subcommand(I18N_PREFIX.Buy.Label, I18N_PREFIX.Buy.Description, UUID.fromString("bc4903dc-8734-4092-b82d-15489529d989")) {
            executor = BrokerBuyStockExecutor()
        }

        subcommand(I18N_PREFIX.Sell.Label, I18N_PREFIX.Sell.Description, UUID.fromString("fd9d1aa4-1c6a-4075-9f01-edd0bcae4e58")) {
            executor = BrokerSellStockExecutor()
        }
    }

    inner class BrokerInfoExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false) // Defer because this sometimes takes too long

            context.reply(false) {
                brokerEmbed(context) {
                    title = "${Emotes.LoriStonks} ${context.i18nContext.get(I18N_PREFIX.Info.Embed.Title)}"
                    description = context.i18nContext.get(
                        I18N_PREFIX.Info.Embed.Explanation(
                            loriSob = Emotes.LoriSob,
                            tickerOutOfMarket = Emotes.DoNotDisturb,
                            openTime = LorittaBovespaBrokerUtils.TIME_OPEN_DISCORD_TIMESTAMP,
                            closingTime = LorittaBovespaBrokerUtils.TIME_CLOSING_DISCORD_TIMESTAMP,
                            brokerBuyCommandMention = loritta.commandMentions.brokerBuy,
                            brokerSellCommandMention = loritta.commandMentions.brokerSell,
                            brokerPortfolioCommandMention = loritta.commandMentions.brokerPortfolio,
                        )
                    ).joinToString("\n")
                }

                actionRow(selectCompanyCategoryMenu(context, null))
            }
        }

        override suspend fun convertToInteractionsArguments(context: LegacyMessageCommandContext, args: List<String>): Map<OptionReference<*>, Any?> = LorittaLegacyMessageCommandExecutor.NO_ARGS

        private fun selectCompanyCategoryMenu(context: UnleashedContext, selectedCategory: LorittaBovespaBrokerUtils.CompanyCategory?): StringSelectMenu {
            return loritta.interactivityManager.stringSelectMenuForUser(
                context.user,
                context.alwaysEphemeral,
                {
                    for (category in LorittaBovespaBrokerUtils.CompanyCategory.values()) {
                        addOption(
                            context.i18nContext.get(category.i18nName),
                            category.name,
                            Emoji.fromFormatted(category.emoji.asMention)
                        )
                    }

                    if (selectedCategory != null)
                        setDefaultValues(selectedCategory.name)
                }
            ) { context, values ->
                context.deferAndEditOriginal {
                    val categories = values.map { LorittaBovespaBrokerUtils.CompanyCategory.valueOf(it) }
                    val stockInformations = context.loritta.pudding.bovespaBroker.getAllTickers()

                    brokerEmbed(context) {
                        title = "${Emotes.LoriStonks} ${context.i18nContext.get(I18N_PREFIX.Info.Embed.Title)}"
                        description = context.i18nContext.get(
                            I18N_PREFIX.Info.Embed.Explanation(
                                loriSob = Emotes.LoriSob,
                                tickerOutOfMarket = Emotes.DoNotDisturb,
                                openTime = LorittaBovespaBrokerUtils.TIME_OPEN_DISCORD_TIMESTAMP,
                                closingTime = LorittaBovespaBrokerUtils.TIME_CLOSING_DISCORD_TIMESTAMP,
                                brokerBuyCommandMention = loritta.commandMentions.brokerBuy,
                                brokerSellCommandMention = loritta.commandMentions.brokerSell,
                                brokerPortfolioCommandMention = loritta.commandMentions.brokerPortfolio,
                            )
                        ).joinToString("\n")

                        for (stockInformation in stockInformations.sortedBy(BrokerTickerInformation::ticker)) {
                            val stockData =
                                LorittaBovespaBrokerUtils.trackedTickerCodes.first { it.ticker == stockInformation.ticker }
                            if (stockData.category !in categories)
                                continue

                            val tickerId = stockInformation.ticker
                            val tickerName = stockData.name
                            val currentPrice = LorittaBovespaBrokerUtils.convertReaisToSonhos(stockInformation.value)
                            val buyingPrice = LorittaBovespaBrokerUtils.convertToBuyingPrice(currentPrice) // Buying price
                            val sellingPrice = LorittaBovespaBrokerUtils.convertToSellingPrice(currentPrice) // Selling price
                            val changePercentage = stockInformation.dailyPriceVariation

                            val fieldTitle =
                                "`$tickerId` ($tickerName) | ${"%.2f".format(changePercentage)}%"
                            val emojiStatus =
                                getEmojiStatusForTicker(stockInformation)

                            if (!LorittaBovespaBrokerUtils.checkIfTickerIsActive(stockInformation.status)) {
                                field {
                                    name = "$emojiStatus $fieldTitle"
                                    value = context.i18nContext.get(
                                        I18N_PREFIX.Info.Embed.PriceBeforeMarketClose(
                                            currentPrice
                                        )
                                    )
                                    inline = true
                                }
                            } else if (LorittaBovespaBrokerUtils.checkIfTickerDataIsStale(
                                    stockInformation.lastUpdatedAt
                                )
                            ) {
                                field {
                                    name = "$emojiStatus $fieldTitle"
                                    value =
                                        """${
                                            context.i18nContext.get(
                                                I18N_PREFIX.Info.Embed.BuyPrice(
                                                    buyingPrice
                                                )
                                            )
                                        }
                                |${context.i18nContext.get(I18N_PREFIX.Info.Embed.SellPrice(sellingPrice))}
                            """.trimMargin()
                                    inline = true
                                }
                            } else {
                                field {
                                    name = "$emojiStatus $fieldTitle"
                                    value =
                                        """${
                                            context.i18nContext.get(
                                                I18N_PREFIX.Info.Embed.BuyPrice(
                                                    buyingPrice
                                                )
                                            )
                                        }
                                |${context.i18nContext.get(I18N_PREFIX.Info.Embed.SellPrice(sellingPrice))}
                            """.trimMargin()
                                    inline = true
                                }
                            }
                        }
                    }

                    actionRow(selectCompanyCategoryMenu(context, categories.first()))
                }
            }
        }
    }

    inner class BrokerPortfolioExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val page = optionalLong("page", TodoFixThisData)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false) // Defer because this sometimes takes too long

            val page = args[options.page] ?: 1
            val pageZeroIndexed = (page - 1).coerceIn(0L..99L)

            createMessage(
                context,
                loritta,
                context.i18nContext,
                pageZeroIndexed.toInt()
            ) {
                context.reply(false) {
                    it.invoke(this)
                }
            }
        }

        suspend fun createMessage(
            context: UnleashedContext,
            loritta: LorittaBot,
            i18nContext: I18nContext,
            pageZeroIndexed: Int,
            targetEdit: suspend (InlineMessage<*>.() -> (Unit)) -> (Unit)
        ) {
            val stockInformations = context.loritta.pudding.bovespaBroker.getAllTickers()
            val userStockAssets = context.loritta.pudding.bovespaBroker.getUserBoughtStocks(context.user.idLong)

            if (userStockAssets.isEmpty())
                context.fail(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Portfolio.YouDontHaveAnyShares(loritta.commandMentions.brokerInfo, loritta.commandMentions.brokerBuy)),
                        Emotes.LoriSob
                    )
                }

            val totalPagesZeroIndexed = ceil(userStockAssets.size / TICKERS_PER_PAGE.toDouble()).toInt() - 1
            val userStockAssetsForThisPage = userStockAssets
                .drop(pageZeroIndexed * TICKERS_PER_PAGE)
                .take(TICKERS_PER_PAGE)

            if (userStockAssetsForThisPage.isEmpty())
                context.fail(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Portfolio.YouDontHaveAnySharesInThatPage),
                        Emotes.LoriSob
                    )
                }

            targetEdit.invoke {
                brokerEmbed(context) {
                    title = "${Emotes.LoriStonks} ${context.i18nContext.get(I18N_PREFIX.Portfolio.Title)}"

                    val totalStockCount = userStockAssets.sumOf { it.count }
                    val totalStockSum = userStockAssets.sumOf { it.sum }
                    val totalGainsIfSoldEverythingNow = userStockAssets.sumOf { stockAsset ->
                        val tickerInformation = stockInformations.first { it.ticker == stockAsset.ticker }

                        LorittaBovespaBrokerUtils.convertToSellingPrice(
                            LorittaBovespaBrokerUtils.convertReaisToSonhos(
                                tickerInformation.value
                            )
                        ) * stockAsset.count
                    }
                    val diff = totalGainsIfSoldEverythingNow - totalStockSum
                    val totalProfitPercentage =
                        ((totalGainsIfSoldEverythingNow - totalStockSum.toDouble()) / totalStockSum)

                    description = context.i18nContext.get(
                        I18N_PREFIX.Portfolio.YouHaveSharesInYourPortfolio(
                            totalStockCount,
                            totalStockSum,
                            totalGainsIfSoldEverythingNow,
                            diff.let { if (it > 0) "+$it" else it.toString() },
                            totalProfitPercentage
                        )
                    )

                    for (stockAsset in userStockAssetsForThisPage.sortedByDescending {
                        // Sort the portfolio by the stock's profit percentage
                        val stockTicker = it.ticker
                        val stockCount = it.count
                        val stockSum = it.sum
                        val tickerInformation = stockInformations.first { it.ticker == stockTicker }

                        val totalGainsIfSoldNow = LorittaBovespaBrokerUtils.convertToSellingPrice(
                            LorittaBovespaBrokerUtils.convertReaisToSonhos(
                                tickerInformation.value
                            )
                        ) * stockCount

                        ((totalGainsIfSoldNow - stockSum.toDouble()) / stockSum)
                    }) {
                        val (tickerId, stockCount, stockSum, stockAverage) = stockAsset
                        val tickerName =
                            LorittaBovespaBrokerUtils.trackedTickerCodes.first { it.ticker == tickerId }.name
                        val tickerInformation = stockInformations.first { it.ticker == stockAsset.ticker }
                        val currentPrice = LorittaBovespaBrokerUtils.convertReaisToSonhos(tickerInformation.value)
                        val buyingPrice = LorittaBovespaBrokerUtils.convertToBuyingPrice(currentPrice) // Buying price
                        val sellingPrice =
                            LorittaBovespaBrokerUtils.convertToSellingPrice(currentPrice) // Selling price
                        val emojiStatus = getEmojiStatusForTicker(tickerInformation)

                        val totalGainsIfSoldNow = LorittaBovespaBrokerUtils.convertToSellingPrice(
                            LorittaBovespaBrokerUtils.convertReaisToSonhos(
                                tickerInformation.value
                            )
                        ) * stockCount

                        val diff = totalGainsIfSoldNow - stockSum
                        val emojiProfit = when {
                            diff > 0 -> "\uD83D\uDD3C"
                            0 > diff -> "\uD83D\uDD3D"
                            else -> "⏹️"
                        }

                        val changePercentage = tickerInformation.dailyPriceVariation

                        // https://percentage-change-calculator.com/
                        val profitPercentage = ((totalGainsIfSoldNow - stockSum.toDouble()) / stockSum)

                        val youHaveSharesInThisTickerMessage = context.i18nContext.get(
                            I18N_PREFIX.Portfolio.YouHaveSharesInThisTicker(
                                stockCount,
                                stockSum,
                                totalGainsIfSoldNow,
                                diff.let { if (it > 0) "+$it" else it.toString() },
                                profitPercentage
                            )
                        )

                        if (!LorittaBovespaBrokerUtils.checkIfTickerIsActive(tickerInformation.status)) {
                            field(
                                "$emojiStatus$emojiProfit `${tickerId}` ($tickerName) | ${"%.2f".format(changePercentage)}%",
                                """${context.i18nContext.get(I18N_PREFIX.Info.Embed.PriceBeforeMarketClose(currentPrice))}
                                |$youHaveSharesInThisTickerMessage
                            """.trimMargin(),
                                true
                            )
                        } else {
                            field(
                                "$emojiStatus$emojiProfit `${tickerId}` ($tickerName) | ${"%.2f".format(changePercentage)}%",
                                """${context.i18nContext.get(I18N_PREFIX.Info.Embed.BuyPrice(buyingPrice))}
                                |${context.i18nContext.get(I18N_PREFIX.Info.Embed.SellPrice(sellingPrice))}
                                |$youHaveSharesInThisTickerMessage""".trimMargin(),
                                true
                            )
                        }
                    }
                }

                val leftButton = UnleashedButton.of(
                    ButtonStyle.PRIMARY,
                    emoji = Emotes.ChevronLeft
                )

                val rightButton = UnleashedButton.of(
                    ButtonStyle.PRIMARY,
                    emoji = Emotes.ChevronRight
                )

                // ==[ PAGES BUTTONS ]===
                actionRow(
                    if (pageZeroIndexed != 0) {
                        loritta.interactivityManager.buttonForUser(
                            context.user.idLong,
                            context.alwaysEphemeral,
                            leftButton
                        ) {
                            it.invalidateComponentCallback()

                            val editJob = it.event.editMessage(
                                MessageEdit {
                                    actionRow(
                                        leftButton
                                            .withEmoji(LoadingEmojis.random().toJDA())
                                            .asDisabled(),
                                        rightButton.asDisabled()
                                    )
                                }
                            ).submit()

                            val hook = it.event.hook

                            createMessage(it, loritta, i18nContext, pageZeroIndexed - 1) {
                                editJob.await()

                                hook.editOriginal(
                                    MessageEdit {
                                        it.invoke(this)
                                    }
                                ).await()
                            }
                        }
                    } else leftButton.asDisabled(),

                    if (pageZeroIndexed != totalPagesZeroIndexed) {
                        loritta.interactivityManager.buttonForUser(
                            context.user.idLong,
                            context.alwaysEphemeral,
                            rightButton
                        ) {
                            it.invalidateComponentCallback()

                            val editJob = it.event.editMessage(
                                MessageEdit {
                                    actionRow(
                                        leftButton.asDisabled(),
                                        rightButton
                                            .withEmoji(LoadingEmojis.random().toJDA())
                                            .asDisabled()
                                    )
                                }
                            ).submit()

                            val hook = it.event.hook

                            createMessage(it, loritta, i18nContext, pageZeroIndexed + 1) {
                                editJob.await()

                                hook.editOriginal(
                                    MessageEdit {
                                        it.invoke(this)
                                    }
                                ).await()
                            }
                        }
                    } else rightButton.asDisabled()
                )
            }
        }

        override suspend fun convertToInteractionsArguments(context: LegacyMessageCommandContext, args: List<String>): Map<OptionReference<*>, Any?> = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }

    inner class BrokerStockInfoExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val ticker = string("ticker", I18N_PREFIX.Stock.Options.Ticker.Text) {
                LorittaBovespaBrokerUtils.trackedTickerCodes.map { Pair(it.ticker, it.name) }.forEach { (tickerId, tickerTitle) ->
                    choice("$tickerTitle ($tickerId)", tickerId.lowercase())
                }
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true) // Defer because this sometimes takes too long

            val tickerId = args[options.ticker].uppercase()

            // This should *never* happen because the values are validated on Discord side BUT who knows
            if (tickerId !in LorittaBovespaBrokerUtils.validStocksCodes)
                context.fail(true, context.i18nContext.get(I18N_PREFIX.ThatIsNotAnValidStockTicker(loritta.commandMentions.brokerInfo)))

            val stockInformation = context.loritta.pudding.bovespaBroker.getTicker(tickerId)

            val stockAsset = context.loritta.pudding.bovespaBroker.getUserBoughtStocks(context.user.id.toLong())
                .firstOrNull { it.ticker == tickerId }

            context.reply(true) {
                brokerEmbed(context) {
                    title = "${Emotes.LoriStonks} ${context.i18nContext.get(I18N_PREFIX.Stock.Embed.Title)}"

                    // This is just like the "/broker portfolio" command
                    // There is two alternatives however: If the user has stock, the output will be the same as the "/broker portfolio" command
                    // If not, it will be just the buy/sell price
                    val tickerInformation = stockInformation
                    val tickerName = LorittaBovespaBrokerUtils.trackedTickerCodes.first { it.ticker == tickerInformation.ticker }.name
                    val currentPrice = LorittaBovespaBrokerUtils.convertReaisToSonhos(tickerInformation.value)
                    val buyingPrice = LorittaBovespaBrokerUtils.convertToBuyingPrice(currentPrice) // Buying price
                    val sellingPrice = LorittaBovespaBrokerUtils.convertToSellingPrice(currentPrice) // Selling price
                    val changePercentage = tickerInformation.dailyPriceVariation
                    val emojiStatus = getEmojiStatusForTicker(tickerInformation)

                    if (stockAsset != null) {
                        val (tickerId, stockCount, stockSum, stockAverage) = stockAsset

                        val totalGainsIfSoldNow = LorittaBovespaBrokerUtils.convertToSellingPrice(
                            LorittaBovespaBrokerUtils.convertReaisToSonhos(
                                tickerInformation.value
                            )
                        ) * stockCount

                        val diff = totalGainsIfSoldNow - stockSum
                        val emojiProfit = when {
                            diff > 0 -> "\uD83D\uDD3C"
                            0 > diff -> "\uD83D\uDD3D"
                            else -> "⏹️"
                        }

                        // https://percentage-change-calculator.com/
                        val profitPercentage = ((totalGainsIfSoldNow - stockSum.toDouble()) / stockSum)

                        val youHaveSharesInThisTickerMessage = context.i18nContext.get(
                            I18N_PREFIX.Portfolio.YouHaveSharesInThisTicker(
                                stockCount,
                                stockSum,
                                totalGainsIfSoldNow,
                                diff.let { if (it > 0) "+$it" else it.toString() },
                                profitPercentage
                            )
                        )

                        if (!LorittaBovespaBrokerUtils.checkIfTickerIsActive(tickerInformation.status)) {
                            field(
                                "$emojiStatus$emojiProfit `${tickerId}` ($tickerName) | ${"%.2f".format(changePercentage)}%",
                                """${context.i18nContext.get(I18N_PREFIX.Info.Embed.PriceBeforeMarketClose(currentPrice))}
                                |$youHaveSharesInThisTickerMessage
                            """.trimMargin()
                            )
                        } else {
                            field(
                                "$emojiStatus$emojiProfit `${tickerId}` ($tickerName) | ${"%.2f".format(changePercentage)}%",
                                """${context.i18nContext.get(I18N_PREFIX.Info.Embed.BuyPrice(buyingPrice))}
                                |${context.i18nContext.get(I18N_PREFIX.Info.Embed.SellPrice(sellingPrice))}
                                |$youHaveSharesInThisTickerMessage""".trimMargin()
                            )
                        }
                    } else {
                        if (!LorittaBovespaBrokerUtils.checkIfTickerIsActive(tickerInformation.status)) {
                            field(
                                "$emojiStatus `${tickerId}` ($tickerName) | ${"%.2f".format(changePercentage)}%",
                                context.i18nContext.get(I18N_PREFIX.Info.Embed.PriceBeforeMarketClose(currentPrice))
                                    .trimMargin()
                            )
                        } else {
                            field(
                                "$emojiStatus `${tickerId}` ($tickerName) | ${"%.2f".format(changePercentage)}%",
                                """${context.i18nContext.get(I18N_PREFIX.Info.Embed.BuyPrice(buyingPrice))}
                                |${context.i18nContext.get(I18N_PREFIX.Info.Embed.SellPrice(sellingPrice))}""".trimMargin()
                            )
                        }
                    }
                }
            }
        }

        override suspend fun convertToInteractionsArguments(context: LegacyMessageCommandContext, args: List<String>): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            return mapOf(options.ticker to args[0])
        }
    }

    inner class BrokerBuyStockExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val ticker = string("ticker", I18N_PREFIX.Stock.Options.Ticker.Text) {
                autocomplete {
                    val focusedOptionValue = it.event.focusedOption.value

                    val results = LorittaBovespaBrokerUtils.trackedTickerCodes.filter {
                        it.ticker.startsWith(focusedOptionValue, true)
                    }

                    return@autocomplete results.map {
                        "${it.name} (${it.ticker})" to it.ticker.uppercase()
                    }.take(DiscordResourceLimits.Command.Options.ChoicesCount).toMap()
                }
            }

            val quantity = optionalString("quantity", I18N_PREFIX.Buy.Options.Quantity.Text) {
                autocomplete {
                    val currentInput = it.event.focusedOption.value

                    val ticker = it.event.getOption("ticker")?.asString?.uppercase() ?: return@autocomplete mapOf()
                    // Not a valid ticker, bye!
                    if (ticker !in LorittaBovespaBrokerUtils.validStocksCodes)
                        return@autocomplete mapOf()

                    val tickerInfo = loritta.pudding.bovespaBroker.getTicker(ticker)

                    val quantity = NumberUtils.convertShortenedNumberToLong(it.i18nContext, currentInput) ?: return@autocomplete mapOf(
                        it.i18nContext.get(
                            I18nKeysData.Commands.InvalidNumber(currentInput)
                        ).shortenAndStripCodeBackticks(DiscordResourceLimits.Command.Options.Description.Length) to "invalid_number"
                    )

                    return@autocomplete mapOf(
                        it.i18nContext.get(
                            I18N_PREFIX.SharesCountWithPrice(
                                quantity,
                                quantity * tickerInfo.value
                            )
                        ).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length) to quantity.toString()
                    )
                }
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            if (SonhosUtils.checkIfEconomyIsDisabled(context))
                return

            context.deferChannelMessage(true) // Defer because this sometimes takes too long

            val tickerId = args[options.ticker].uppercase()
            val quantityAsString = args[options.quantity] ?: "1"

            // This should *never* happen because the values are validated on Discord side BUT who knows
            if (tickerId !in LorittaBovespaBrokerUtils.validStocksCodes)
                context.fail(true, context.i18nContext.get(I18N_PREFIX.ThatIsNotAnValidStockTicker(loritta.commandMentions.brokerInfo)))

            val quantity = NumberUtils.convertShortenedNumberToLong(context.i18nContext, quantityAsString) ?: context.fail(
                true,
                context.i18nContext.get(
                    I18nKeysData.Commands.InvalidNumber(quantityAsString)
                )
            )

            val (_, boughtQuantity, value) = try {
                context.loritta.pudding.bovespaBroker.buyStockShares(
                    context.user.idLong,
                    tickerId,
                    quantity
                )
            } catch (e: BovespaBrokerService.TransactionActionWithLessThanOneShareException) {
                context.fail(
                    true,
                    context.i18nContext.get(
                        when (quantity) {
                            0L -> I18N_PREFIX.Buy.TryingToBuyZeroShares
                            else -> I18N_PREFIX.Buy.TryingToBuyLessThanZeroShares
                        }
                    )
                )
            } catch (e: BovespaBrokerService.StaleTickerDataException) {
                context.fail(true, context.i18nContext.get(I18N_PREFIX.StaleTickerData))
            } catch (e: BovespaBrokerService.OutOfSessionException) {
                context.fail(
                    true,
                    context.i18nContext.get(
                        I18N_PREFIX.StockMarketClosed(
                            LorittaBovespaBrokerUtils.TIME_OPEN_DISCORD_TIMESTAMP,
                            LorittaBovespaBrokerUtils.TIME_CLOSING_DISCORD_TIMESTAMP
                        )
                    )
                )
            } catch (e: BovespaBrokerService.NotEnoughSonhosException) {
                context.reply(true) {
                    styled(
                        context.i18nContext.get(SonhosUtils.insufficientSonhos(e.userSonhos, e.howMuch)),
                        Emotes.LoriSob
                    )

                    appendUserHaventGotDailyTodayOrUpsellSonhosBundles(
                        context.loritta,
                        context.i18nContext,
                        UserId(context.user.idLong),
                        "lori-broker",
                        "buy-shares-not-enough-sonhos"
                    )
                }
                return
            } catch (e: BovespaBrokerService.TooManySharesException) {
                context.fail(
                    true,
                    context.i18nContext.get(
                        I18N_PREFIX.Buy.TooManyShares(
                            LorittaBovespaBrokerUtils.MAX_STOCK_SHARES_PER_USER
                        )
                    )
                )
            }

            context.reply(true) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.Buy.SuccessfullyBought(
                            sharesCount = boughtQuantity,
                            ticker = tickerId,
                            price = value,
                            brokerPortfolioCommandMention = loritta.commandMentions.brokerPortfolio
                        )
                    ),
                    Emotes.LoriRich
                )
            }
        }

        override suspend fun convertToInteractionsArguments(context: LegacyMessageCommandContext, args: List<String>): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            return mapOf(options.ticker to args[0])
        }
    }

    inner class BrokerSellStockExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val ticker = string("ticker", I18N_PREFIX.Stock.Options.Ticker.Text) {
                autocomplete {
                    val focusedOptionValue = it.event.focusedOption.value

                    val userBoughtStocks = loritta.pudding.bovespaBroker.getUserBoughtStocks(it.event.user.idLong)
                        .map { it.ticker }
                        .toSet()

                    val results = LorittaBovespaBrokerUtils.trackedTickerCodes.filter {
                        it.ticker in userBoughtStocks && it.ticker.startsWith(focusedOptionValue, true)
                    }

                    return@autocomplete results.map {
                        "${it.name} (${it.ticker})" to it.ticker
                    }.take(DiscordResourceLimits.Command.Options.ChoicesCount).toMap()
                }
            }

            val quantity = optionalString("quantity", I18N_PREFIX.Buy.Options.Quantity.Text) {
                autocomplete {
                    val currentInput = it.event.focusedOption.value

                    val ticker = it.event.getOption("ticker")?.asString?.uppercase() ?: return@autocomplete mapOf()
                    // Not a valid ticker, bye!
                    if (ticker !in LorittaBovespaBrokerUtils.validStocksCodes)
                        return@autocomplete mapOf()

                    val tickerInfo = loritta.pudding.bovespaBroker.getTicker(ticker)

                    val quantity = NumberUtils.convertShortenedNumberToLong(it.i18nContext, currentInput) ?: return@autocomplete mapOf(
                        it.i18nContext.get(
                            I18nKeysData.Commands.InvalidNumber(currentInput)
                        ).shortenAndStripCodeBackticks(DiscordResourceLimits.Command.Options.Description.Length) to "invalid_number"
                    )

                    return@autocomplete mapOf(
                        it.i18nContext.get(
                            I18N_PREFIX.SharesCountWithPrice(
                                quantity,
                                quantity * tickerInfo.value
                            )
                        ).shortenWithEllipsis(DiscordResourceLimits.Command.Options.Description.Length) to quantity.toString()
                    )
                }
            }
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            if (SonhosUtils.checkIfEconomyIsDisabled(context))
                return

            context.deferChannelMessage(true) // Defer because this sometimes takes too long

            val tickerId = args[options.ticker].uppercase()
            val quantityAsString = args[options.quantity] ?: "1"

            // This should *never* happen because the values are validated on Discord side BUT who knows
            if (tickerId !in LorittaBovespaBrokerUtils.validStocksCodes)
                context.fail(true, context.i18nContext.get(I18N_PREFIX.ThatIsNotAnValidStockTicker(loritta.commandMentions.brokerInfo)))

            val quantity = if (quantityAsString == "all") {
                context.loritta.pudding.bovespaBroker.getUserBoughtStocks(context.user.idLong)
                    .firstOrNull { it.ticker == tickerId }
                    ?.count ?: context.fail(
                    true,
                    context.i18nContext.get(
                        I18N_PREFIX.Sell.YouDontHaveAnySharesInThatTicker(
                            tickerId
                        )
                    )
                )
            } else {
                NumberUtils.convertShortenedNumberToLong(context.i18nContext, quantityAsString) ?: context.fail(
                    true,
                    context.i18nContext.get(
                        I18nKeysData.Commands.InvalidNumber(quantityAsString)
                    )
                )
            }

            val (_, soldQuantity, earnings, profit) = try {
                context.loritta.pudding.bovespaBroker.sellStockShares(
                    context.user.idLong,
                    tickerId,
                    quantity
                )
            } catch (e: BovespaBrokerService.TransactionActionWithLessThanOneShareException) {
                context.fail(
                    true,
                    context.i18nContext.get(
                        when (quantity) {
                            0L -> I18N_PREFIX.Sell.TryingToSellZeroShares
                            else -> I18N_PREFIX.Sell.TryingToSellLessThanZeroShares
                        }
                    )
                )
            } catch (e: BovespaBrokerService.StaleTickerDataException) {
                context.fail(true, context.i18nContext.get(I18N_PREFIX.StaleTickerData))
            } catch (e: BovespaBrokerService.OutOfSessionException) {
                context.fail(
                    true,
                    context.i18nContext.get(
                        I18N_PREFIX.StockMarketClosed(
                            LorittaBovespaBrokerUtils.TIME_OPEN_DISCORD_TIMESTAMP,
                            LorittaBovespaBrokerUtils.TIME_CLOSING_DISCORD_TIMESTAMP
                        )
                    )
                )
            } catch (e: BovespaBrokerService.NotEnoughSharesException) {
                context.fail(
                    true,
                    context.i18nContext.get(
                        I18N_PREFIX.Sell.YouDontHaveEnoughStocks(
                            e.currentBoughtSharesCount,
                            tickerId
                        )
                    )
                )
            }

            val isNeutralProfit = profit == 0L
            val isPositiveProfit = profit > 0L
            val isNegativeProfit = !isNeutralProfit && !isPositiveProfit

            context.reply(true) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.Sell.SuccessfullySold(
                            soldQuantity,
                            tickerId,
                            when {
                                isNeutralProfit -> {
                                    context.i18nContext.get(
                                        I18N_PREFIX.Sell.SuccessfullySoldNeutral
                                    )
                                }

                                isPositiveProfit -> {
                                    context.i18nContext.get(
                                        I18N_PREFIX.Sell.SuccessfullySoldProfit(
                                            abs(earnings),
                                            abs(profit)
                                        )
                                    )
                                }

                                else -> {
                                    context.i18nContext.get(
                                        I18N_PREFIX.Sell.SuccessfullySoldLoss(
                                            abs(earnings),
                                            abs(profit),
                                            (loritta.commandMentions.brokerPortfolio)
                                        )
                                    )
                                }
                            }
                        )
                    ),
                    when {
                        profit == 0L -> Emotes.LoriShrug
                        profit > 0L -> Emotes.LoriRich
                        else -> Emotes.LoriSob
                    }
                )
            }

            if (isPositiveProfit)
                context.giveAchievementAndNotify(AchievementType.STONKS, ephemeral = true)
            if (isNegativeProfit)
                context.giveAchievementAndNotify(AchievementType.NOT_STONKS, ephemeral = true)
        }

        override suspend fun convertToInteractionsArguments(context: LegacyMessageCommandContext, args: List<String>): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            return mapOf(options.ticker to args[0])
        }
    }
}