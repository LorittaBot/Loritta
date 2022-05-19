package net.perfectdreams.loritta.cinnamon.platform.commands.discord.avatar

import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.entities.InteractionMember
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.UserCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.UserCommandExecutorDeclaration

class UserAvatarUserExecutor(val lorittaId: Snowflake) : UserCommandExecutor(), UserAvatarExecutor {
    companion object : UserCommandExecutorDeclaration()

    override suspend fun execute(
        context: ApplicationCommandContext,
        targetUser: User,
        targetMember: InteractionMember?
    ) {
        handleAvatarCommand(context, lorittaId, targetUser, targetMember)
    }
}