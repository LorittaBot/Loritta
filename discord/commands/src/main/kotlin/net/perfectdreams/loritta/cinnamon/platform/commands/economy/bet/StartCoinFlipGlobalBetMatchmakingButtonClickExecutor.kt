package net.perfectdreams.loritta.cinnamon.platform.commands.economy.bet

import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext

class StartCoinFlipGlobalBetMatchmakingButtonClickExecutor(
    loritta: LorittaCinnamon
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.START_MATCHMAKING_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        context.deferUpdateMessage()

        val decoded = context.decodeDataFromComponentAndRequireUserToMatch<CoinFlipBetGlobalStartMatchmakingData>()

        CoinFlipBetGlobalExecutor.addToMatchmakingQueue(
            context,
            decoded.quantity
        )
    }
}