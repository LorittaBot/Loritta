package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.discordinteraktions.common.entities.InteractionMember
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments

sealed class ApplicationCommandExecutor

abstract class SlashCommandExecutor : ApplicationCommandExecutor() {
    abstract suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments)
}

abstract class UserCommandExecutor : ApplicationCommandExecutor() {
    abstract suspend fun execute(context: ApplicationCommandContext, targetUser: User, targetMember: InteractionMember?)
}