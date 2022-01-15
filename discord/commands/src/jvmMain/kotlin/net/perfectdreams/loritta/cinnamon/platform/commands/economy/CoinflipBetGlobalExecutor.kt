package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kord.rest.builder.message.create.allowedMentions
import kotlinx.datetime.Clock
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.allowedMentions
import net.perfectdreams.discordinteraktions.common.builder.message.create.InteractionOrFollowupMessageCreateBuilder
import net.perfectdreams.discordinteraktions.platforms.kord.context.manager.HttpRequestManager
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.platform.InteractionContext
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.services.BetsService
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

class CoinflipBetGlobalExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(CoinflipBetGlobalExecutor::class) {
        object Options : CommandOptions() {
            val quantity = integer("quantity", TodoFixThisData)
                .autocomplete(SonhosQuantityCoinFlipBetGlobalAutocompleteExecutor)
                .register()
        }

        override val options = Options

        val QUANTITIES = listOf(
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

        @OptIn(ExperimentalTime::class)
        suspend fun addToMatchmakingQueue(
            context: InteractionContext,
            quantity: Long
        ) {
            // TODO: Remove this hack! Maybe expose the token somewhere?
            val httpRequestManager = context.interaKTionsContext.bridge.manager as HttpRequestManager

            val results = context.loritta.services.bets.addToCoinFlipBetGlobalMatchmakingQueue(
                UserId(context.user.id.value),
                httpRequestManager.interactionToken,
                quantity, // TODO: Add proper quantities
            )

            for (result in results) {
                when (result) {
                    is BetsService.AddedToQueueResult -> context.sendEphemeralMessage {
                        content = "Você foi adicionado na fila de matchmaking! lets go :3"
                    }
                    is BetsService.AlreadyInQueueResult -> context.sendEphemeralMessage {
                        content = "Você já está na fila do coinflip safade!!"
                    }
                    is BetsService.CoinflipResult -> {
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

                        // TODO: Add a converter on Discord InteraKTions to convert those messages
                        val b = createCoinFlipResultMessage(
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

                        val builder = InteractionOrFollowupMessageCreateBuilder(true).apply(b)

                        context.loritta.rest.interaction.createFollowupMessage(
                            httpRequestManager.applicationId, // Should be always the application ID right?
                            result.userInteractionToken,
                            FollowupMessageCreateBuilder(true).apply {
                                this.content = builder.content
                                this.tts = builder.tts
                                this.allowedMentions = builder.allowedMentions
                                builder.components?.let { this.components.addAll(it) }
                                builder.embeds?.let { this.embeds.addAll(it) }
                                builder.files?.let { this.files.addAll(it) }
                            }.toRequest()
                        )
                    }
                    is BetsService.AnotherUserRemovedFromMatchmakingQueue -> {
                        context.loritta.rest.interaction.createFollowupMessage(
                            httpRequestManager.applicationId, // Should be always the application ID right?
                            result.userInteractionToken,
                            true
                        ) {
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
            result: BetsService.CoinflipResult,
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

            styled(
                "Nas últimas 24 horas, você ganhou ${selfStats.winCount} partidas e perdeu ${selfStats.lostCount} partidas. Total de sonhos: ${selfStats.winSum - selfStats.lostSum}"
            )
            actionRow {
                interactiveButton(
                    ButtonStyle.Primary,
                    StartMatchmakingButtonClickExecutor,
                    ComponentDataUtils.encode(
                        CoinflipGlobalStartMatchmakingData(
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

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessageEphemerally() // Defer because this sometimes takes too long

        addToMatchmakingQueue(context, 1) // TODO: Quantity
    }
}