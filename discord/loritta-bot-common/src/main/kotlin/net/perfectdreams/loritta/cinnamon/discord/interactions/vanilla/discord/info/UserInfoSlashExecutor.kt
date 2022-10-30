package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.info

import dev.kord.core.entity.Member
import io.ktor.client.*
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations.UserCommand
import net.perfectdreams.loritta.morenitta.LorittaBot

class UserInfoSlashExecutor(loritta: LorittaBot, override val http: HttpClient) : CinnamonSlashCommandExecutor(loritta),
    UserInfoExecutor {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user = optionalUser("user", UserCommand.I18N_PREFIX.Info.Options.User.Text)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val user = args[options.user] ?: context.user

        val member = if (user == context.user && context is GuildApplicationCommandContext)
            context.member
        else
            user as? Member

        handleUserExecutor(
            context,
            user,
            member,
            false
        )
    }
}