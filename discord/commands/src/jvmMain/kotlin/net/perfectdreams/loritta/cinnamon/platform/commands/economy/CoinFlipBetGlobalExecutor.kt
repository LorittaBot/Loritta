package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Clock
import net.perfectdreams.discordinteraktions.common.BarebonesInteractionContext
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.allowedMentions
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.InteractionContext
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.BetCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.components.loriEmoji
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
            if (quantity !in QUANTITIES) {
                context.sendEphemeralMessage {
                    content = "Quantidade inválida!"
                }
                return
            }

            val results = context.loritta.services.bets.addToCoinFlipBetGlobalMatchmakingQueue(
                UserId(context.user.id.value),
                context.interaKTionsContext.discordInteraction.token,
                quantity,
            )

            for (result in results) {
                when (result) {
                    is BetsService.AddedToQueueResult -> context.sendEphemeralMessage {
                        content = "Você foi adicionado na fila de matchmaking! Caso nenhum match seja encontrado em até 5 minutos, você sairá automaticamente da fila! Você pode sair da fila a qualquer momento usando `/bet coinflip global` antes de encontrar uma partida! lets go :3"
                    }
                    is BetsService.AlreadyInQueueResult -> context.sendEphemeralMessage {
                        content = "Você já está na fila do coinflip safade!!"
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
                                UserId(context.user.id.value),
                                result,
                                quantity,
                                winnerCachedUserInfo,
                                loserCachedUserInfo,
                                if (isSelfUserTheWinner)
                                    winnerBetStats
                                else
                                    loserBetStats
                            )
                        )

                        val otherUserMessage = createCoinFlipResultMessage(
                            result.otherUser,
                            result,
                            quantity,
                            winnerCachedUserInfo,
                            loserCachedUserInfo,
                            if (!isSelfUserTheWinner)
                                winnerBetStats
                            else
                                loserBetStats
                        )

                        val otherUserContext = BarebonesInteractionContext(
                            context.loritta.rest,
                            context.interaKTionsContext.discordInteraction.applicationId, // Should be always the same app ID
                            result.userInteractionToken
                        )

                        otherUserContext.sendEphemeralMessage(otherUserMessage)
                    }
                    is BetsService.AnotherUserRemovedFromMatchmakingQueue -> {
                        val otherUserContext = BarebonesInteractionContext(
                            context.loritta.rest,
                            context.interaKTionsContext.discordInteraction.applicationId, // Should be always the same app ID
                            result.userInteractionToken
                        )

                        otherUserContext.sendEphemeralMessage {
                            allowedMentions {
                                users.add(Snowflake(result.user.value))
                            }

                            content = "Você saiu da fila de matchmaking pois você não possui sonhos suficientes para realizar a sua aposta..."
                        }
                    }

                    is BetsService.YouDontHaveEnoughSonhosToBetResult -> {
                        context.sendEphemeralMessage {
                            content = "Você não tem sonhos suficientes para apostar!"
                        }
                    }
                }
            }
        }

        private fun createCoinFlipResultMessage(
            selfUser: UserId,
            result: BetsService.CoinFlipResult,
            quantity: Long,
            winnerCachedUserInfo: CachedUserInfo?,
            loserCachedUserInfo: CachedUserInfo?,
            selfStats: BetsService.UserCoinFlipBetGlobalStats,
        ): MessageBuilder.() -> (Unit) = {
            allowedMentions {
                users.add(Snowflake(result.winner.value))
                users.add(Snowflake(result.loser.value))
            }

            val isSelfUserTheWinner = result.winner == selfUser

            styled(
                if (result.isTails)
                    "**Coroa!**"
                else
                    "**Cara!**",
                if (result.isTails)
                    Emotes.CoinTails
                else
                    Emotes.CoinHeads
            )

            if (isSelfUserTheWinner) {
                styled(
                    "Parabéns <@${selfUser.value}>, você ganhou $quantity sonhos! Financiado por `${loserCachedUserInfo?.name}#${loserCachedUserInfo?.discriminator}` (`${loserCachedUserInfo?.id?.value}`) rsrs",
                    Emotes.LoriRich
                )
            } else {
                styled(
                    "Que pena <@${selfUser.value}>, você perdeu $quantity sonhos para `${winnerCachedUserInfo?.name}#${winnerCachedUserInfo?.discriminator}` (`${winnerCachedUserInfo?.id?.value}`)",
                    Emotes.LoriSob
                )
            }

            styled("Nas últimas 24 horas, você ganhou ${selfStats.winCount} partidas e perdeu ${selfStats.lostCount} partidas. Total de sonhos: ${selfStats.winSum - selfStats.lostSum}")

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
                    label = "vamo de novo pois o pai tá rico kk"

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
        ) ?: context.failEphemerally("Quantidade inválida!")

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