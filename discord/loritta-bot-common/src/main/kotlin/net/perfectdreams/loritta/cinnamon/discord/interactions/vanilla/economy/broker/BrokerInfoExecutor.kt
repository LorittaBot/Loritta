package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.broker

import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.broker.BrokerExecutorUtils.brokerBaseEmbed
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.BrokerCommand
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.BarebonesSingleUserComponentData
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.selectMenu
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.pudding.data.BrokerTickerInformation

class BrokerInfoExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer because this sometimes takes too long

        val stockInformations = context.loritta.pudding.bovespaBroker.getAllTickers()

        context.sendMessage {
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
            }

            actionRow {
                selectMenu(
                    BrokerInfoCategorySelectMenuExecutor,
                    ComponentDataUtils.encode(BarebonesSingleUserComponentData(context.user.id))
                ) {
                    for (category in LorittaBovespaBrokerUtils.CompanyCategory.values()) {
                        option(context.i18nContext.get(category.i18nName), category.name) {
                            loriEmoji = category.emoji
                        }
                    }
                }
            }
        }
    }
}