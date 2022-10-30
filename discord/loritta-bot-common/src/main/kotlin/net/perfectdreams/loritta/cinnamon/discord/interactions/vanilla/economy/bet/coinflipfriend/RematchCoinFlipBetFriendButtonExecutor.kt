package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.bet.coinflipfriend

import dev.kord.core.entity.User
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.morenitta.LorittaBot

class RematchCoinFlipBetFriendButtonExecutor(
    loritta: LorittaBot
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.REMATCH_COIN_FLIP_BET_FRIEND_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        val rematchData = context.decodeDataFromComponentOrFromDatabase<RematchCoinFlipBetFriendData>()

        context.deferUpdateMessage()

        // Let's rematch!
        loritta.coinFlipBetUtils.createBet(
            context,
            rematchData.quantity,
            if (context.user.id == rematchData.sourceId) rematchData.userId else rematchData.sourceId,
            rematchData.combo + 1
        )
    }
}