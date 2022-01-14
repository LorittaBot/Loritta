package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.allowedMentions
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.allowedMentions
import net.perfectdreams.discordinteraktions.platforms.kord.context.manager.HttpRequestManager
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.InteractionContext
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.services.BetsService

class CoinflipBetGlobalExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(CoinflipBetGlobalExecutor::class) {
        suspend fun addToMatchmakingQueue(
            context: InteractionContext,
            quantity: Long
        ) {
            // TODO: Remove this hack! Maybe expose the token somewhere?
            val httpRequestManager = context.interaKTionsContext.bridge.manager as HttpRequestManager

            val result = context.loritta.services.bets.addToMatchmakingQueue(
                UserId(context.user.id.value),
                httpRequestManager.interactionToken,
                quantity, // TODO: Add proper quantities
            )

            when (result) {
                is BetsService.AddedToQueueResult -> context.sendEphemeralMessage {
                    content = "Você foi adicionado na fila de matchmaking! lets go :3"
                }
                is BetsService.AlreadyInQueueResult -> context.sendEphemeralMessage {
                    content = "Você já está na fila do coinflip safade!!"
                }
                is BetsService.CoinflipResult -> {
                    context.sendEphemeralMessage {
                        allowedMentions {
                            users.add(Snowflake(result.winner.value))
                            users.add(Snowflake(result.loser.value))
                        }

                        content = "Fim!\nVencedor: <@${result.winner.value}>\nPerdedor: <@${result.loser.value}>"

                        actionRow {
                            interactiveButton(
                                ButtonStyle.Primary,
                                StartMatchmakingButtonClickExecutor,
                                ComponentDataUtils.encode(
                                    CoinflipGlobalStartMatchmakingData(
                                        context.user.id,
                                        quantity
                                    )
                                )
                            ) {
                                label = "vamo de novo pois o pai tá rico kk"

                                loriEmoji = Emotes.LoriRich
                            }
                        }
                    }

                    context.loritta.rest.interaction.createFollowupMessage(
                        httpRequestManager.applicationId, // Should be always the application ID right?
                        result.userInteractionToken,
                        true
                    ) {
                        allowedMentions {
                            users.add(Snowflake(result.winner.value))
                            users.add(Snowflake(result.loser.value))
                        }

                        content = "Fim!\nVencedor: <@${result.winner.value}>\nPerdedor: <@${result.loser.value}>"


                        actionRow {
                            interactiveButton(
                                ButtonStyle.Primary,
                                StartMatchmakingButtonClickExecutor,
                                ComponentDataUtils.encode(
                                    CoinflipGlobalStartMatchmakingData(
                                        Snowflake(result.otherUser.value),
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
            }
        }
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessageEphemerally() // Defer because this sometimes takes too long

        addToMatchmakingQueue(context, 0) // TODO: Quantity
    }
}