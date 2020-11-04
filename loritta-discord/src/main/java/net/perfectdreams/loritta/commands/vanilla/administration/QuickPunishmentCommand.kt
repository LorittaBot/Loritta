package net.perfectdreams.loritta.commands.vanilla.administration

import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase

class QuickPunishmentCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta, listOf("quickpunishment"), CommandCategory.ADMIN) {

    override fun command(): Command<CommandContext> = create {
        localizedDescription("commands.moderation.quickpunishment.description")

        executesDiscord {
            val userData = serverConfig.getUserData(user.idLong)

            if (userData.quickPunishment) {
                reply(
                        message = locale["commands.moderation.quickpunishment.disabled"]
                )
            } else {
                reply(
                        message = locale["commands.moderation.quickpunishment.enabled"]
                )
            }

            loritta.newSuspendedTransaction {
                userData.quickPunishment = !userData.quickPunishment
            }
        }
    }
}