package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.BoughtStocks
import net.perfectdreams.loritta.cinnamon.pudding.tables.TickerPrices
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

        val extendedInfo = optionalBoolean("extended", SONHOS_I18N_PREFIX.Options.Extended.Text)
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.deferChannelMessage(false) // Defer because this sometimes takes too long

        val user = args[options.user]?.user ?: context.user
        val showExtendedInfo = args[options.extendedInfo] ?: false

        val profile = context.loritta.pudding.users.getUserProfile(net.perfectdreams.loritta.serializable.UserId(user.idLong))
        val userSonhos = profile?.money ?: 0L
        val isSelf = context.user.id == user.id

        // Needs to be in here because MessageBuilder is not suspendable!
        val sonhosRankPosition = if (userSonhos != 0L && profile != null) // Only show the ranking position if the user has any sonhos, this avoids querying the db with useless stuff
            profile.getRankPositionInSonhosRanking()
        else
            null

        var extendedSonhosInfo: ExtendedSonhosInfo? = null

        if (showExtendedInfo) {
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
                if (extendedSonhosInfo != null) {
                    styled(
                        context.i18nContext.get(
                            SONHOS_I18N_PREFIX.SonhosInTheWallet(
                                SonhosUtils.getSonhosEmojiOfQuantity(userSonhos),
                                userSonhos
                            )
                        ),
                        Emotes.LoriCard
                    )

                    styled(
                        context.i18nContext.get(
                            SONHOS_I18N_PREFIX.BoughtStocks(
                                loritta.commandMentions.brokerInfo,
                                SonhosUtils.getSonhosEmojiOfQuantity(extendedSonhosInfo.boughtStocks),
                                extendedSonhosInfo.boughtStocks
                            )
                        ),
                        Emotes.LoriStonks
                    )

                    val totalSonhos = userSonhos + extendedSonhosInfo.boughtStocks
                    styled(
                        context.i18nContext.get(
                            SONHOS_I18N_PREFIX.TotalSonhos(
                                SonhosUtils.getSonhosEmojiOfQuantity(totalSonhos),
                                totalSonhos
                            )
                        ),
                        Emotes.Sonhos2
                    )
                }
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
                if (extendedSonhosInfo != null) {
                    styled(
                        context.i18nContext.get(
                            SONHOS_I18N_PREFIX.SonhosInTheWallet(
                                SonhosUtils.getSonhosEmojiOfQuantity(userSonhos),
                                userSonhos
                            )
                        ),
                        Emotes.LoriCard
                    )

                    styled(
                        context.i18nContext.get(
                            SONHOS_I18N_PREFIX.BoughtStocks(
                                loritta.commandMentions.brokerInfo,
                                SonhosUtils.getSonhosEmojiOfQuantity(extendedSonhosInfo.boughtStocks),
                                extendedSonhosInfo.boughtStocks
                            )
                        ),
                        Emotes.LoriStonks
                    )

                    val totalSonhos = userSonhos + extendedSonhosInfo.boughtStocks
                    styled(
                        context.i18nContext.get(
                            SONHOS_I18N_PREFIX.TotalSonhos(
                                SonhosUtils.getSonhosEmojiOfQuantity(totalSonhos),
                                totalSonhos
                            )
                        ),
                        Emotes.Sonhos2
                    )
                }
            }
        }
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