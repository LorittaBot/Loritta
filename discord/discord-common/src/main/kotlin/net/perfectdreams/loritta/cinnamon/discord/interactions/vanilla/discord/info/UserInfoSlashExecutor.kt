package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.info

import io.ktor.client.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations.UserCommand
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions

class UserInfoSlashExecutor(loritta: LorittaCinnamon, override val http: HttpClient) : CinnamonSlashCommandExecutor(loritta), UserInfoExecutor {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user = optionalUser("user", UserCommand.I18N_PREFIX.Info.Options.User.Text)
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

        handleUserExecutor(
            context,
            user,
            member,
            false
        )
    }
}