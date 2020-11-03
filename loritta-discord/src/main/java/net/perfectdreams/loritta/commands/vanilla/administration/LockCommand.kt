package net.perfectdreams.loritta.commands.vanilla.administration

import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.commands.getTextChannel

class LockCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta, listOf("lock", "trancar"), CommandCategory.ADMIN) {

    override fun command(): Command<CommandContext> = create {
        localizedDescription("commands.moderation.lock.description")

        userRequiredPermissions = listOf(Permission.MANAGE_SERVER)
        botRequiredPermissions = listOf(Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS)

        executesDiscord {
            val channel = getTextChannel(args.getOrNull(0), executedIfNull = true)!!

            val publicRole = guild.publicRole
            val override = channel.getPermissionOverride(publicRole)

            if (override != null) {
                if (Permission.MESSAGE_WRITE !in override.denied) {
                    override.manager
                            .deny(Permission.MESSAGE_WRITE)
                            .queue()
                }
            } else {
                channel.createPermissionOverride(publicRole)
                        .setDeny(Permission.MESSAGE_WRITE)
                        .queue()
            }

            reply(
                    locale["commands.moderation.lock.denied", serverConfig.commandPrefix],
                    "\uD83C\uDF89"
            )
        }
    }

}