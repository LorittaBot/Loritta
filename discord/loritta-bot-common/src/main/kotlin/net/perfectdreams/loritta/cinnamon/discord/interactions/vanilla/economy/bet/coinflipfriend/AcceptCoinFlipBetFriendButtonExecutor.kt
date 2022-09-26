package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.bet.coinflipfriend

import dev.kord.core.entity.User
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.ComponentContextHighLevelEditableMessage
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.utils.StoredGenericInteractionData

class AcceptCoinFlipBetFriendButtonExecutor(
    loritta: LorittaCinnamon
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.ACCEPT_COIN_FLIP_BET_FRIEND_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        val decodedGenericInteractionData = context.decodeDataFromComponent<StoredGenericInteractionData>()

        context.deferUpdateMessage()

        loritta.coinFlipBetUtils.acceptBet(
            context,
            user.id,
            ComponentContextHighLevelEditableMessage(context),
            decodedGenericInteractionData
        )
    }
}