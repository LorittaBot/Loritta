package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.avatar

import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.entities.InteractionMember
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonUserCommandExecutor

class UserAvatarUserExecutor(loritta: LorittaCinnamon) : CinnamonUserCommandExecutor(loritta), UserAvatarExecutor {
    override suspend fun execute(
        context: ApplicationCommandContext,
        targetUser: User,
        targetMember: InteractionMember?
    ) {
        handleAvatarCommand(context, applicationId, targetUser, targetMember, true)
    }
}