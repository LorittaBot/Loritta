package net.perfectdreams.loritta.commands.vanilla.administration

import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.commands.getTextChannel

class UnlockCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta, listOf("unlock", "destrancar"), CommandCategory.ADMIN) {

    override fun command(): Command<CommandContext> = create {
        localizedDescription("commands.moderation.unlock.description")

        userRequiredPermissions = listOf(Permission.MANAGE_SERVER)
        botRequiredPermissions = listOf(Permission.MANAGE_CHANNEL, Permission.MANAGE_PERMISSIONS)

        executesDiscord {
            val channel = getTextChannel(args.getOrNull(0), executedIfNull = true)!! // Já que o comando não será executado via DM, podemos assumir que textChannel nunca será nulo

            val publicRole = guild.publicRole
            val override = channel.getPermissionOverride(publicRole)

            if (override != null) {
                if (Permission.MESSAGE_WRITE in override.denied) {
                    override.manager
                            .grant(Permission.MESSAGE_WRITE)
                            .queue()
                }
            } else { // Bem, na verdade não seria totalmente necessário este else, mas vamos supor que o cara usou o "+unlock" com o chat destravado sem ter travado antes :rolling_eyes:
                channel.createPermissionOverride(publicRole)
                        .setAllow(Permission.MESSAGE_WRITE)
                        .queue()
            }

            reply(
                    locale["commands.moderation.unlock.allowed", serverConfig.commandPrefix],
                    "\uD83C\uDF89"
            )
        }
    }

}