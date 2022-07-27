package net.perfectdreams.loritta.cinnamon.platform.commands.discord.avatar

import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations.UserCommand
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.*
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions

class UserAvatarSlashExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta), UserAvatarExecutor {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user = optionalUser("user", UserCommand.I18N_PREFIX.Avatar.Options.User)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val user = args[options.user] ?: context.user

        // TODO: Fix this workaround, it would be nice if Discord InteraKTions provided a "UserAndMember" object to us
        // (Or maybe expose it correctly?)
        val member = if (user == context.user && context is GuildApplicationCommandContext)
            context.member
        else
            context.interaKTionsContext.interactionData.resolved?.members?.get(user.id)

        handleAvatarCommand(context, applicationId, user, member, false)
    }
}