package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Clock
import net.perfectdreams.discordinteraktions.common.BarebonesInteractionContext
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.allowedMentions
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.GACampaigns
import net.perfectdreams.loritta.cinnamon.platform.InteractionContext
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.BetCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.platform.utils.AchievementUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.NumberUtils
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.services.BetsService
import kotlin.time.Duration.Companion.hours

class CoinFlipBetGlobalExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(CoinFlipBetGlobalExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val quantity = string("quantity", BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Text)
                .autocomplete(CoinFlipBetGlobalSonhosQuantityAutocompleteExecutor)
                .register()
        }

        override val options = Options

        val QUANTITIES = listOf<Long>(
            0,
            100,
            1_000,
            2_500,
            5_000,
            10_000,
            25_000,
            50_000,
            100_000,
            250_000,
            500_000,
            1_000_000
        )

        suspend fun addToMatchmakingQueue(
            context: InteractionContext,
            quantity: Long
        ) {
            // Required because autocomplete is only validated in the client side
            if (quantity !in QUANTITIES)
                context.failEphemerally(
                    context.i18nContext.get(
                        BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.InvalidQuantity(
                            QUANTITIES.joinToString(", ")
                        )
                    ),
                    Emotes.Error
                )

            val results = context.loritta.services.bets.addToCoinFlipBetGlobalMatchmakingQueue(
                UserId(context.user.id.value),
                context.interaKTionsContext.discordInteraction.token,
                quantity,
            )

            for (result in results) {
                when (result) {
                    is BetsService.AddedToQueueResult -> context.sendEphemeralMessage {
                        styled(
                            context.i18nContext.get(
                                BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.AddedToMatchmakingQueue
                            ),
                            Emotes.LoriRich
                        )
                    }
                    is BetsService.AlreadyInQueueResult -> context.sendEphemeralMessage {
                        styled(
                            context.i18nContext.get(
                                BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.YouAreAlreadyInTheMatchmakingQueue
                            ),
                            Emotes.LoriRage
                        )
                    }
                    is BetsService.CoinFlipResult -> {
                        val winnerCachedUserInfo = context.loritta.getCachedUserInfo(result.winner)
                        val loserCachedUserInfo = context.loritta.getCachedUserInfo(result.loser)
                        val now24HoursAgo = Clock.System.now()
                            .minus(24.hours)
                        val winnerBetStats = context.loritta.services.bets.getCoinFlipBetGlobalUserBetsStats(
                            result.winner,
                            now24HoursAgo
                        )
                        val loserBetStats = context.loritta.services.bets.getCoinFlipBetGlobalUserBetsStats(
                            result.loser,
                            now24HoursAgo
                        )

                        val isSelfUserTheWinner = result.winner == UserId(context.user.id.value)

                        context.sendEphemeralMessage(
                            createCoinFlipResultMessage(
                                context.loritta,
                                context.i18nContext,
                                UserId(context.user.id.value),
                                result,
                                quantity,
                                winnerCachedUserInfo,
                                loserCachedUserInfo,
                                if (isSelfUserTheWinner)
                                    winnerBetStats
                                else
                                    loserBetStats,
                                if (isSelfUserTheWinner)
                                    context.loritta.services.bets.getCoinFlipBetGlobalUserWinningStreakStats(result.winner)
                                else
                                    context.loritta.services.bets.getCoinFlipBetGlobalUserLosingStreakStats(result.loser)
                            )
                        )

                        val otherUserMessage = createCoinFlipResultMessage(
                            context.loritta,
                            context.i18nContext,
                            result.otherUser,
                            result,
                            quantity,
                            winnerCachedUserInfo,
                            loserCachedUserInfo,
                            if (!isSelfUserTheWinner)
                                winnerBetStats
                            else
                                loserBetStats,
                            if (!isSelfUserTheWinner)
                                context.loritta.services.bets.getCoinFlipBetGlobalUserWinningStreakStats(result.winner)
                            else
                                context.loritta.services.bets.getCoinFlipBetGlobalUserLosingStreakStats(result.loser)
                        )

                        val otherUserContext = BarebonesInteractionContext(
                            context.loritta.rest,
                            context.interaKTionsContext.discordInteraction.applicationId, // Should be always the same app ID
                            result.userInteractionToken
                        )

                        otherUserContext.sendEphemeralMessage(otherUserMessage)
                    }
                    is BetsService.AnotherUserRemovedFromMatchmakingQueueResult -> {
                        val otherUserContext = BarebonesInteractionContext(
                            context.loritta.rest,
                            context.interaKTionsContext.discordInteraction.applicationId, // Should be always the same app ID
                            result.userInteractionToken
                        )

                        otherUserContext.sendEphemeralMessage {
                            allowedMentions {
                                users.add(Snowflake(result.user.value))
                            }

                            styled(
                                "${mentionUser(Snowflake(result.user.value))} ${context.i18nContext.get(
                                    BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.LeftMatchmakingQueueDueToNotEnoughSonhos
                                )}",
                                Emotes.LoriSob
                            )
                            styled(
                                context.i18nContext.get(
                                    GACampaigns.sonhosBundlesUpsellDiscordMessage(
                                        context.loritta.config.website,
                                        "bet-coinflip-global",
                                        "removed-from-mm"
                                    )
                                ),
                                Emotes.CreditCard
                            )
                        }
                    }

                    is BetsService.YouDontHaveEnoughSonhosToBetResult -> {
                        context.sendEphemeralMessage {
                            styled(
                                "${mentionUser(context.user.id)} ${context.i18nContext.get(
                                    BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.NotEnoughSonhosToBet
                                )}",
                                Emotes.LoriSob
                            )

                            styled(
                                context.i18nContext.get(
                                    GACampaigns.sonhosBundlesUpsellDiscordMessage(
                                        context.loritta.config.website,
                                        "bet-coinflip-global",
                                        "mm-check"
                                    )
                                ),
                                Emotes.CreditCard
                            )
                        }
                    }
                    is BetsService.OtherUserAchievementResult -> {
                        val otherUserContext = BarebonesInteractionContext(
                            context.loritta.rest,
                            context.interaKTionsContext.discordInteraction.applicationId, // Should be always the same app ID
                            result.userInteractionToken
                        )

                        AchievementUtils.giveAchievementToUser(
                            context.loritta,
                            net.perfectdreams.loritta.cinnamon.platform.BarebonesInteractionContext(otherUserContext),
                            context.i18nContext,
                            result.user,
                            result.achievementType
                        )
                    }
                    is BetsService.SelfUserAchievementResult -> {
                        context.giveAchievement(result.achievementType)
                    }
                }
            }
        }

        private fun createCoinFlipResultMessage(
            loritta: LorittaCinnamon,
            i18nContext: I18nContext,
            selfUser: UserId,
            result: BetsService.CoinFlipResult,
            quantity: Long,
            winnerCachedUserInfo: CachedUserInfo?,
            loserCachedUserInfo: CachedUserInfo?,
            selfStats: BetsService.UserCoinFlipBetGlobalStats,
            selfStreak: Int
        ): MessageBuilder.() -> (Unit) = {
            val isSelfUserTheWinner = result.winner == selfUser
            val isJustForFun = quantity == 0L

            styled(
                if (result.isTails)
                    "**${i18nContext.get(CoinFlipCommand.I18N_PREFIX.Tails)}!**"
                else
                    "**${i18nContext.get(CoinFlipCommand.I18N_PREFIX.Heads)}!**",
                if (result.isTails)
                    Emotes.CoinTails
                else
                    Emotes.CoinHeads
            )

            if (isJustForFun) {
                if (isSelfUserTheWinner) {
                    styled(
                        i18nContext.get(
                            BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.CongratulationsJustForFun(
                                user = mentionUser(Snowflake(selfUser.value)),
                                loserTag = "${loserCachedUserInfo?.name}#${loserCachedUserInfo?.discriminator}",
                                loserId = loserCachedUserInfo?.id?.value.toString()
                            )
                        ),
                        Emotes.LoriRich
                    )
                } else {
                    styled(
                        i18nContext.get(
                            BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.LostJustForFun(
                                user = mentionUser(Snowflake(selfUser.value)),
                                winnerTag = "${winnerCachedUserInfo?.name}#${winnerCachedUserInfo?.discriminator}",
                                winnerId = winnerCachedUserInfo?.id?.value.toString()
                            )
                        ),
                        Emotes.LoriSob
                    )
                }
            } else if (result.tax != null && result.taxPercentage != null) {
                // Taxed
                if (isSelfUserTheWinner) {
                    styled(
                        i18nContext.get(
                            BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.CongratulationsTaxed(
                                user = mentionUser(Snowflake(selfUser.value)),
                                sonhosCount = result.quantityAfterTax,
                                sonhosCountWithoutTax = result.quantity,
                                loserTag = "${loserCachedUserInfo?.name}#${loserCachedUserInfo?.discriminator}",
                                loserId = loserCachedUserInfo?.id?.value.toString()
                            )
                        ),
                        Emotes.LoriRich
                    )
                } else {
                    styled(
                        i18nContext.get(
                            BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.LostTaxed(
                                user = mentionUser(Snowflake(selfUser.value)),
                                sonhosCount = result.quantityAfterTax,
                                sonhosCountWithoutTax = result.quantity,
                                winnerTag = "${winnerCachedUserInfo?.name}#${winnerCachedUserInfo?.discriminator}",
                                winnerId = winnerCachedUserInfo?.id?.value.toString()
                            )
                        ),
                        Emotes.LoriSob
                    )
                }
            } else {
                if (isSelfUserTheWinner) {
                    styled(
                        i18nContext.get(
                            BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.Congratulations(
                                user = mentionUser(Snowflake(selfUser.value)),
                                sonhosCount = result.quantityAfterTax,
                                loserTag = "${loserCachedUserInfo?.name}#${loserCachedUserInfo?.discriminator}",
                                loserId = loserCachedUserInfo?.id?.value.toString()
                            )
                        ),
                        Emotes.LoriRich
                    )

                    // Upsell if the user does not have premium but the loser has
                    if (result.winner !in result.premiumUsers && result.loser in result.premiumUsers) {
                        styled(
                            i18nContext.get(
                                BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.DontWantTaxAnymorePremiumPlanUpsellOtherUserHasPremium(
                                    GACampaigns.premiumUpsellDiscordMessageUrl(
                                        loritta.config.website,
                                        "bet-coinflip-global",
                                        "victory-against-premium-users"
                                    )
                                )
                            ),
                            Emotes.CreditCard
                        )
                    }
                } else {
                    styled(
                        i18nContext.get(
                            BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.Lost(
                                user = mentionUser(Snowflake(selfUser.value)),
                                sonhosCount = result.quantityAfterTax,
                                winnerTag = "${winnerCachedUserInfo?.name}#${winnerCachedUserInfo?.discriminator}",
                                winnerId = winnerCachedUserInfo?.id?.value.toString()
                            )
                        ),
                        Emotes.LoriSob
                    )
                }
            }

            styled(
                i18nContext.get(
                    BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.RecentBetsStats(
                        selfStats.winCount,
                        selfStats.lostCount,
                        selfStats.winSum - selfStats.lostSum
                    )
                ),
                Emotes.LoriReading
            )

            // If the user won, then the selfStreak is their winning streak
            // (After all, if they won... the losing streak would be 0)
            if (isSelfUserTheWinner) {
                styled(
                    i18nContext.get(BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.YouHaveConsecutiveWins(selfStreak)),
                    Emotes.LoriWow
                )
            } else {
                styled(
                    i18nContext.get(BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.YouHaveConsecutiveLosses(selfStreak)),
                    Emotes.LoriHmpf
                )
            }

            actionRow {
                interactiveButton(
                    ButtonStyle.Primary,
                    StartCoinFlipGlobalBetMatchmakingButtonClickExecutor,
                    ComponentDataUtils.encode(
                        CoinFlipBetGlobalStartMatchmakingData(
                            Snowflake(selfUser.value),
                            quantity
                        )
                    )
                ) {
                    label = if (isJustForFun) {
                        i18nContext.get(BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.JoinMatchmakingQueueJustForFunButton)
                    } else {
                        i18nContext.get(BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.JoinMatchmakingQueueButton(quantity))
                    }

                    loriEmoji = Emotes.LoriRich
                }
            }
        }
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally() // Defer because this sometimes takes too long

        val quantityAsString = args[Options.quantity]

        val isRemoveFromQueueRequest = quantityAsString.startsWith("q")

        val quantity = NumberUtils.convertShortenedNumberToLong(
            context.i18nContext,
            quantityAsString
                .removePrefix("q")
        ) ?: context.failEphemerally(
            context.i18nContext.get(
                BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.InvalidQuantity(
                    QUANTITIES.joinToString(", ")
                )
            ),
            Emotes.Error
        )

        if (isRemoveFromQueueRequest) {
            val leftQueue = context.loritta.services.bets.removeFromCoinFlipBetGlobalMatchmakingQueue(
                UserId(context.user.id.value),
                quantity
            )

            if (leftQueue) {
                context.sendEphemeralMessage {
                    styled(
                        context.i18nContext.get(BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.QuittedMatchmakingQueue),
                        Emotes.LoriSmile
                    )
                }
            } else {
                context.sendEphemeralMessage {
                    styled(
                        context.i18nContext.get(BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.YouArentInTheMatchmakingQueueToLeaveIt),
                        Emotes.Error
                    )
                }
            }
        } else {
            addToMatchmakingQueue(context, quantity)
        }
    }
}