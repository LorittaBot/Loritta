package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.bet.coinflipglobal

import dev.kord.core.entity.User
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext

class StartCoinFlipGlobalBetMatchmakingButtonClickExecutor(
    loritta: LorittaBot
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