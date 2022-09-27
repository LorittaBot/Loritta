package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.notifications

import dev.kord.core.entity.User
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext

class ChangeNotificationsPageButtonClickExecutor(
    loritta: LorittaBot
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.CHANGE_NOTIFICATIONS_PAGE_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        context.deferUpdateMessage()

        val decoded = context.decodeDataFromComponentAndRequireUserToMatch<NotificationsListData>()

        // TODO: Implement this
        /* val builtMessage = TransactionsExecutor.createMessage(
            loritta,
            context.i18nContext,
            decoded
        )

        context.updateMessage {
            builtMessage()
        } */
    }
}