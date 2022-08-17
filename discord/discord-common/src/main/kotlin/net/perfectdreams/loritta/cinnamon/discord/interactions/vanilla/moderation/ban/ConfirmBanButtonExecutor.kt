package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.ban

import dev.kord.core.entity.User
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.GuildComponentContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.AdminUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds

class ConfirmBanButtonExecutor(loritta: LorittaCinnamon) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.CONFIRM_BAN_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        if (context !is GuildComponentContext)
            return

        val data = context.decodeDataFromComponentOrFromDatabaseAndRequireUserToMatch<ConfirmBanData>()

        // TODO: Apply the ban
        context.sendEphemeralMessage {
            content = "TODO: do stuff - ${data.users}"
        }

        AdminUtils.banUsers(loritta, data)
    }
}