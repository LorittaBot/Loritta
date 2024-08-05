package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.messages.InlineMessage
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.BoughtStocks
import net.perfectdreams.loritta.cinnamon.pudding.tables.TickerPrices
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import org.jetbrains.exposed.sql.count

class SonhosAtmExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    companion object {
        val SONHOS_I18N_PREFIX = I18nKeysData.Commands.Command.Sonhosatm
    }

    inner class Options : ApplicationCommandOptions() {
        val user = optionalUser("user", SONHOS_I18N_PREFIX.Options.User)

        val informationType = optionalString("information_type", SONHOS_I18N_PREFIX.Options.InformationType.Text) {
            choice(SONHOS_I18N_PREFIX.Options.InformationType.Choice.Normal, InformationType.NORMAL.name)
            choice(SONHOS_I18N_PREFIX.Options.InformationType.Choice.Extended, InformationType.EXTENDED.name)
        }
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.deferChannelMessage(false) // Defer because this sometimes takes too long

        val user = args[options.user]?.user ?: context.user
        val informationType = args[options.informationType]?.let { InformationType.valueOf(it) } ?: InformationType.NORMAL

        val profile = context.loritta.pudding.users.getUserProfile(net.perfectdreams.loritta.serializable.UserId(user.idLong))
        val userSonhos = profile?.money ?: 0L
        val isSelf = context.user.id == user.id

        // Needs to be in here because MessageBuilder is not suspendable!
        val sonhosRankPosition = if (userSonhos != 0L && profile != null) // Only show the ranking position if the user has any sonhos, this avoids querying the db with useless stuff
            profile.getRankPositionInSonhosRanking()
        else
            null

        var extendedSonhosInfo: ExtendedSonhosInfo? = null

        if (informationType == InformationType.EXTENDED) {
            loritta.transaction {
                val tickerFieldCount = BoughtStocks.ticker.count()
                var totalBoughtStocks = 0L

                val boughtStocks = BoughtStocks.select(BoughtStocks.ticker, tickerFieldCount)
                    .where {
                        BoughtStocks.user eq user.idLong
                    }
                    .groupBy(BoughtStocks.ticker)
                    .associate { it[BoughtStocks.ticker].value to it[tickerFieldCount] }

                val boughtTickers = boughtStocks.map { it.key }

                val tickerPrices = TickerPrices.select(TickerPrices.ticker, TickerPrices.value)
                    .where {
                        TickerPrices.ticker inList boughtTickers
                    }
                    .toList()
                    .associate { it[TickerPrices.ticker].value to it[TickerPrices.value] }

                for ((tickerId, tickerPrice) in tickerPrices) {
                    totalBoughtStocks += boughtStocks[tickerId]!! * tickerPrice
                }

                extendedSonhosInfo = ExtendedSonhosInfo(
                    totalBoughtStocks
                )
            }
        }

        fun InlineMessage<*>.addExtendedSonhosInfoEmbed(extendedSonhosInfo: ExtendedSonhosInfo) {
            val totalSonhos = userSonhos + extendedSonhosInfo.boughtStocks

            embed {
                title = "${Emotes.Sonhos3} ${context.i18nContext.get(SONHOS_I18N_PREFIX.SonhosSummary)}"

                field(
                    "${Emotes.LoriCard} ${context.i18nContext.get(SONHOS_I18N_PREFIX.SonhosInTheWallet)}",
                    context.i18nContext.get(SONHOS_I18N_PREFIX.SonhosField(userSonhos)),
                    false
                )

                field(
                    "${Emotes.LoriStonks} ${context.i18nContext.get(SONHOS_I18N_PREFIX.BoughtStocks(loritta.commandMentions.brokerPortfolio))}",
                    context.i18nContext.get(SONHOS_I18N_PREFIX.SonhosField(extendedSonhosInfo.boughtStocks)),
                    false
                )

                field(
                    "${SonhosUtils.getSonhosEmojiOfQuantity(totalSonhos)} ${context.i18nContext.get(SONHOS_I18N_PREFIX.TotalSonhos)}",
                    context.i18nContext.get(SONHOS_I18N_PREFIX.SonhosField(totalSonhos)),
                    false
                )

                color = LorittaColors.LorittaAqua.rgb
            }

        }
        if (isSelf) {
            context.reply(false) {
                styled(
                    context.i18nContext.get(
                        SONHOS_I18N_PREFIX.YouHaveSonhos(
                            SonhosUtils.getSonhosEmojiOfQuantity(userSonhos),
                            userSonhos,
                            if (sonhosRankPosition != null) {
                                SONHOS_I18N_PREFIX.YourCurrentRankPosition(
                                    sonhosRankPosition,
                                    loritta.commandMentions.sonhosRank
                                )
                            } else {
                                ""
                            }
                        )
                    ),
                    Emotes.LoriRich
                )

                val extendedSonhosInfo = extendedSonhosInfo
                if (extendedSonhosInfo != null)
                    addExtendedSonhosInfoEmbed(extendedSonhosInfo)
            }

            if (context is ApplicationCommandContext) {
                SonhosUtils.sendEphemeralMessageIfUserHaventGotDailyRewardToday(
                    context.loritta,
                    context,
                    net.perfectdreams.loritta.serializable.UserId(user.idLong)
                )
            }
        } else {
            context.reply(false) {
                styled(
                    context.i18nContext.get(
                        SONHOS_I18N_PREFIX.UserHasSonhos(
                            user.asMention, // We don't want to notify the user!
                            SonhosUtils.getSonhosEmojiOfQuantity(userSonhos),
                            userSonhos,
                            if (sonhosRankPosition != null) {
                                SONHOS_I18N_PREFIX.UserCurrentRankPosition(
                                    user.asMention, // Again, we don't want to notify the user!
                                    sonhosRankPosition,
                                    loritta.commandMentions.sonhosRank
                                )
                            } else {
                                ""
                            }
                        )
                    ),
                    Emotes.LoriRich
                )

                val extendedSonhosInfo = extendedSonhosInfo
                if (extendedSonhosInfo != null)
                    addExtendedSonhosInfoEmbed(extendedSonhosInfo)
            }
        }
    }

    enum class InformationType {
        NORMAL,
        EXTENDED
    }

    data class ExtendedSonhosInfo(
        val boughtStocks: Long
    )

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?> {
        return mapOf(
            options.user to context.getUserAndMember(0)
        )
    }
}