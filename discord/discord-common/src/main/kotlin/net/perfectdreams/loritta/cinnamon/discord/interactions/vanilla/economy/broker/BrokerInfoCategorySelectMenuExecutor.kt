package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.broker

import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.BarebonesSingleUserComponentData
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.broker.BrokerExecutorUtils.brokerBaseEmbed
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.BrokerCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.data.BrokerTickerInformation
import net.perfectdreams.loritta.cinnamon.utils.LorittaBovespaBrokerUtils

class BrokerInfoCategorySelectMenuExecutor(loritta: LorittaCinnamon) : CinnamonSelectMenuExecutor(loritta) {
    companion object : SelectMenuExecutorDeclaration(ComponentExecutorIds.CHANGE_TICKER_CATEGORY_MENU_EXECUTOR)

    override suspend fun onSelect(user: User, context: ComponentContext, values: List<String>) {
        // We only want to validate it
        context.decodeDataFromComponentAndRequireUserToMatch<BarebonesSingleUserComponentData>()

        context.updateMessageSetLoadingState(updateMessageContent = false)

        val categories = values.map { LorittaBovespaBrokerUtils.CompanyCategory.valueOf(it) }
        val stockInformations = context.loritta.services.bovespaBroker.getAllTickers()

        context.updateMessage {
            brokerBaseEmbed(context) {
                title = "${Emotes.LoriStonks} ${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.Title)}"
                description = context.i18nContext.get(
                    BrokerCommand.I18N_PREFIX.Info.Embed.Explanation(
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
                    val stockData = LorittaBovespaBrokerUtils.trackedTickerCodes.first { it.ticker == stockInformation.ticker }
                    if (stockData.category !in categories)
                        continue

                    val tickerId = stockInformation.ticker
                    val tickerName = stockData.name
                    val currentPrice = LorittaBovespaBrokerUtils.convertReaisToSonhos(stockInformation.value)
                    val buyingPrice = LorittaBovespaBrokerUtils.convertToBuyingPrice(currentPrice) // Buying price
                    val sellingPrice = LorittaBovespaBrokerUtils.convertToSellingPrice(currentPrice) // Selling price
                    val changePercentage = stockInformation.dailyPriceVariation

                    val fieldTitle = "`$tickerId` ($tickerName) | ${"%.2f".format(changePercentage)}%"
                    val emojiStatus = BrokerExecutorUtils.getEmojiStatusForTicker(stockInformation)

                    if (!LorittaBovespaBrokerUtils.checkIfTickerIsActive(stockInformation.status)) {
                        field {
                            name = "$emojiStatus $fieldTitle"
                            value = context.i18nContext.get(
                                BrokerCommand.I18N_PREFIX.Info.Embed.PriceBeforeMarketClose(currentPrice)
                            )
                            inline = true
                        }
                    } else if (LorittaBovespaBrokerUtils.checkIfTickerDataIsStale(stockInformation.lastUpdatedAt)) {
                        field {
                            name = "$emojiStatus $fieldTitle"
                            value =
                                """${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.BuyPrice(buyingPrice))}
                                |${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.SellPrice(sellingPrice))}
                            """.trimMargin()
                            inline = true
                        }
                    } else {
                        field {
                            name = "$emojiStatus $fieldTitle"
                            value =
                                """${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.BuyPrice(buyingPrice))}
                                |${context.i18nContext.get(BrokerCommand.I18N_PREFIX.Info.Embed.SellPrice(sellingPrice))}
                            """.trimMargin()
                            inline = true
                        }
                    }
                }
            }

            actionRow {
                selectMenu(
                    BrokerInfoCategorySelectMenuExecutor,
                    ComponentDataUtils.encode(BarebonesSingleUserComponentData(context.user.id))
                ) {
                    for (category in LorittaBovespaBrokerUtils.CompanyCategory.values()) {
                        option(context.i18nContext.get(category.i18nName), category.name) {
                            if (category in categories)
                                default = true

                            loriEmoji = category.emoji
                        }
                    }
                }
            }
        }
    }
}