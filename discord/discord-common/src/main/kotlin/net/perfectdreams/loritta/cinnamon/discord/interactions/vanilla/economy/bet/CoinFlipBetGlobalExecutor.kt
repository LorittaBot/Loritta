package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.bet

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Clock
import net.perfectdreams.discordinteraktions.common.BarebonesInteractionContext
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.allowedMentions
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.utils.GACampaigns
import net.perfectdreams.loritta.cinnamon.discord.interactions.InteractionContext
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.BetCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.discord.utils.AchievementUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.NumberUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils.userHaventGotDailyTodayOrUpsellSonhosBundles
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.services.BetsService
import kotlin.time.Duration.Companion.hours

class CoinFlipBetGlobalExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    companion object {
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
                context.loritta.languageManager.getIdByI18nContext(context.i18nContext),
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
                                    result.winnerStreakCount
                                else
                                    result.loserStreakCount
                            )
                        )

                        val otherUserMessage = createCoinFlipResultMessage(
                            context.loritta,
                            context.loritta.languageManager.getI18nContextById(result.otherUserLanguage),
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
                                result.winnerStreakCount
                            else
                                result.loserStreakCount
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

                        val otherUserI18nContext = context.loritta.languageManager.getI18nContextById(result.language)

                        otherUserContext.sendEphemeralMessage {
                            allowedMentions {
                                users.add(Snowflake(result.user.value))
                            }

                            styled(
                                "${mentionUser(Snowflake(result.user.value))} ${
                                    otherUserI18nContext.get(
                                        BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.LeftMatchmakingQueueDueToNotEnoughSonhos
                                    )
                                }",
                                Emotes.LoriSob
                            )

                            userHaventGotDailyTodayOrUpsellSonhosBundles(
                                context.loritta,
                                context.i18nContext,
                                UserId(context.user.id.value),
                                "bet-coinflip-global",
                                "removed-from-mm"
                            )
                        }
                    }

                    is BetsService.YouDontHaveEnoughSonhosToBetResult -> {
                        context.sendEphemeralMessage {
                            styled(
                                "${mentionUser(context.user.id)} ${
                                    context.i18nContext.get(
                                        BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.NotEnoughSonhosToBet
                                    )
                                }",
                                Emotes.LoriSob
                            )

                            userHaventGotDailyTodayOrUpsellSonhosBundles(
                                context.loritta,
                                context.i18nContext,
                                UserId(context.user.id.value),
                                "bet-coinflip-global",
                                "mm-check"
                            )
                        }
                    }
                    is BetsService.OtherUserAchievementResult -> {
                        val otherUserContext = BarebonesInteractionContext(
                            context.loritta.rest,
                            context.interaKTionsContext.discordInteraction.applicationId, // Should be always the same app ID
                            result.userInteractionToken
                        )

                        val otherUserI18nContext = context.loritta.languageManager.getI18nContextById(result.language)

                        AchievementUtils.giveAchievementToUserAndNotifyThem(
                            context.loritta,
                            net.perfectdreams.loritta.cinnamon.discord.interactions.BarebonesInteractionContext(
                                otherUserContext
                            ),
                            otherUserI18nContext,
                            result.user,
                            result.achievementType
                        )
                    }
                    is BetsService.SelfUserAchievementResult -> {
                        context.giveAchievementAndNotify(result.achievementType)
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
                                        loritta.config.loritta.website,
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
                        selfStats.winCount + selfStats.lostCount,
                        selfStats.winCount,
                        selfStats.lostCount,
                        selfStats.winSum - selfStats.lostSum
                    )
                ),
                Emotes.LoriReading
            )

            // If the user won, then the selfStreak is their winning streak
            // (After all, if they won... the losing streak would be 0)
            // emojis = the three stages of happiness/grief idk i never watched it
            if (isSelfUserTheWinner) {
                styled(
                    i18nContext.get(BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.YouHaveConsecutiveWins(selfStreak)),
                    when {
                        selfStreak >= 10 -> Emotes.LoriHappy
                        selfStreak >= 5 -> Emotes.LoriUwU
                        else -> Emotes.LoriWow
                    }
                )
            } else {
                styled(
                    i18nContext.get(BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.YouHaveConsecutiveLosses(selfStreak)),
                    when {
                        selfStreak >= 10 -> Emotes.LoriSob
                        selfStreak >= 5 -> Emotes.LoriRage
                        else -> Emotes.LoriHmpf
                    }
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

    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val quantity = string("quantity", BetCommand.COINFLIP_GLOBAL_I18N_PREFIX.Options.Quantity.Text) {
            autocomplete(CoinFlipBetGlobalSonhosQuantityAutocompleteExecutor(loritta))
        }
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally() // Defer because this sometimes takes too long

        val quantityAsString = args[options.quantity]

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