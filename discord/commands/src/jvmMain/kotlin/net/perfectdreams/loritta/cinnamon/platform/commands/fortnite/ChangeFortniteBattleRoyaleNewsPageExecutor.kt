package net.perfectdreams.loritta.cinnamon.platform.commands.fortnite

import net.perfectdreams.discordinteraktions.api.entities.User
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.selects.SelectMenuExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.selects.SelectMenuWithDataExecutor
import net.perfectdreams.neotilted.client.NeoTiltedClient

class ChangeFortniteBattleRoyaleNewsPageExecutor(
    val neoTiltedClient: NeoTiltedClient
) : SelectMenuWithDataExecutor {
    companion object : SelectMenuExecutorDeclaration(ChangeFortniteBattleRoyaleNewsPageExecutor::class, ComponentExecutorIds.CHANGE_FORTNITE_BR_NEWS_PAGE_EXECUTOR)

    override suspend fun onSelect(user: User, context: ComponentContext, data: String, values: List<String>) {
        // We will already defer to avoid issues
        // Also because we want to edit the message with a file... later!
        context.deferUpdateMessage()

        val (_, hash) = context.decodeViaComponentDataUtilsAndRequireUserToMatch<ChangeFortniteBattleRoyaleNewsPageData>(data)

        val r = neoTiltedClient.getFortniteBattleRoyaleNewsByHash(hash)

        val selectedPage = values.first().toInt()

        context.updateMessage(FortniteNewsExecutor.createMessage(user, r, selectedPage))
    }
}