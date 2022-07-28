package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.info

import io.ktor.client.*
import net.perfectdreams.discordinteraktions.common.entities.InteractionMember
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonUserCommandExecutor

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