package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext

class StartMatchmakingButtonClickExecutor(
    val loritta: LorittaCinnamon
) : ButtonClickWithDataExecutor {
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