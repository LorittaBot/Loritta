package net.perfectdreams.loritta.cinnamon.platform.commands.discord

import net.perfectdreams.discordinteraktions.common.entities.InteractionMember
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.commands.UserCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.UserCommandExecutorDeclaration

class ViewAvatarExecutor : UserCommandExecutor() {
    companion object : UserCommandExecutorDeclaration()

    override suspend fun execute(
        context: net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext,
        targetUser: User,
        targetMember: InteractionMember?
    ) {
        context.sendMessage {
            content = "test"
        }
    }
}