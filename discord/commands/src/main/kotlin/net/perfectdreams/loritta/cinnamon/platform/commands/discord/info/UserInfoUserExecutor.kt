package net.perfectdreams.loritta.cinnamon.platform.commands.discord.info

import io.ktor.client.*
import net.perfectdreams.discordinteraktions.common.entities.InteractionMember
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.UserCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.UserCommandExecutorDeclaration

class UserInfoUserExecutor(override val http: HttpClient) : UserCommandExecutor(), UserInfoExecutor {
    companion object : UserCommandExecutorDeclaration()

    override suspend fun execute(
        context: ApplicationCommandContext,
        targetUser: User,
        targetMember: InteractionMember?
    ) {
        handleUserExecutor(
            context,
            targetUser,
            targetMember,
            true
        )
    }
}