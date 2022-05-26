package net.perfectdreams.loritta.cinnamon.platform.commands.discord.info

import io.ktor.client.*
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments

class UserInfoSlashExecutor(override val http: HttpClient) : SlashCommandExecutor(), UserInfoExecutor {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val user = optionalUser("user", TodoFixThisData)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val user = args[Options.user] ?: context.user

        // TODO: Fix this workaround, it would be nice if Discord InteraKTions provided a "UserAndMember" object to us
        // (Or maybe expose it correctly?)
        val member = if (user == context.user && context is GuildApplicationCommandContext)
            context.member
        else
            context.interaKTionsContext.data.resolved?.members?.get(user.id)

        handleUserExecutor(
            context,
            user,
            member,
            false
        )
    }
}