package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import net.perfectdreams.discordinteraktions.api.entities.User
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.buttons.ButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.buttons.ButtonClickExecutorDeclaration

class StartMatchmakingButtonClickExecutor(
    val loritta: LorittaCinnamon
) : ButtonClickExecutor {
    companion object : ButtonClickExecutorDeclaration(
        StartMatchmakingButtonClickExecutor::class,
        ComponentExecutorIds.START_MATCHMAKING_BUTTON_EXECUTOR
    )

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        context.deferUpdateMessage()

        val decoded = context.decodeViaComponentDataUtilsAndRequireUserToMatch<CoinflipGlobalStartMatchmakingData>(data)

        CoinflipBetGlobalExecutor.addToMatchmakingQueue(
            context,
            decoded.quantity
        )
    }
}