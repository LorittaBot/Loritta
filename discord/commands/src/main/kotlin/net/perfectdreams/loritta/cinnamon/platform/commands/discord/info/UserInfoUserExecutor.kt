package net.perfectdreams.loritta.cinnamon.platform.commands.discord.info

import io.ktor.client.*
import net.perfectdreams.discordinteraktions.common.entities.InteractionMember
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonUserCommandExecutor

class UserInfoUserExecutor(loritta: LorittaCinnamon, override val http: HttpClient) : CinnamonUserCommandExecutor(loritta), UserInfoExecutor {
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