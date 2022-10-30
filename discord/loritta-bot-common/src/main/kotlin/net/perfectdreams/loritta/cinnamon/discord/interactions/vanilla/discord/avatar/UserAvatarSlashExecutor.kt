package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.avatar

import dev.kord.core.entity.Member
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations.UserCommand
import net.perfectdreams.loritta.morenitta.LorittaBot

class UserAvatarSlashExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta), UserAvatarExecutor {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user = optionalUser("user", UserCommand.I18N_PREFIX.Avatar.Options.User)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val user = args[options.user] ?: context.user

        val member = if (user == context.user && context is GuildApplicationCommandContext)
            context.member
        else
            user as? Member

        handleAvatarCommand(context, applicationId, user, member, false)
    }
}