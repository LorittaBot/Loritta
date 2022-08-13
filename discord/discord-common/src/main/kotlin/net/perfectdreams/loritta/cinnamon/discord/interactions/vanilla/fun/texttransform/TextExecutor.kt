package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.texttransform

import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordInviteUtils

abstract class TextExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    suspend fun sendPublicOrEphemeralReplyIfTheMessageHasInvite(
        context: ApplicationCommandContext,
        content: String,
        prefix: String
    ) {
        if (DiscordInviteUtils.hasInvite(content))
            context.sendEphemeralReply(
                content,
                prefix
            )
        else
            context.sendReply(
                content,
                prefix
            )
    }
}