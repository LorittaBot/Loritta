package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.sonhosrank

import dev.kord.core.entity.User
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ButtonExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.CinnamonButtonExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.GuildComponentContext
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds

class ChangeSonhosRankPageButtonExecutor(loritta: LorittaCinnamon) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.CHANGE_SONHOS_RANK_PAGE_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        val data = context.decodeDataFromComponentOrFromDatabaseAndRequireUserToMatch<ChangeSonhosRankPageData>()

        context.deferUpdateMessage()

        val message = if (data.rankType == SonhosRankType.LOCAL) {
            if (context !is GuildComponentContext)
                error("Trying to look up a local sonhos rank outside of a guild!")

            SonhosRankExecutor.createMessageLocal(loritta, context, loritta.kord.getGuild(context.guildId)!!, data.page)
        } else {
            SonhosRankExecutor.createMessageGlobal(loritta, context, data.page)
        }

        context.updateMessage {
            message()
        }
    }
}