package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.xprank

import dev.kord.core.entity.User
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.GuildComponentContext
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.morenitta.LorittaBot

class ChangeXpRankPageButtonExecutor(loritta: LorittaBot) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.CHANGE_XP_RANK_PAGE_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        if (context !is GuildComponentContext)
            return

        val data = context.decodeDataFromComponentOrFromDatabaseAndRequireUserToMatch<ChangeXpRankPageData>()

        // Loading Section
        context.updateMessageSetLoadingState()

        // TODO: Cache this somewhere
        val guild = loritta.kord.getGuild(context.guildId)!!

        val message = XpRankExecutor.createMessage(loritta, context, guild, data.page)

        context.updateMessage {
            message()
        }
    }
}