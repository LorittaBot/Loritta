package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.avatar

import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonUserCommandExecutor

class UserAvatarUserExecutor(loritta: LorittaBot) : CinnamonUserCommandExecutor(loritta), UserAvatarExecutor {
    override suspend fun execute(
        context: ApplicationCommandContext,
        targetUser: User,
        targetMember: Member?
    ) {
        handleAvatarCommand(context, applicationId, targetUser, targetMember, true)
    }
}