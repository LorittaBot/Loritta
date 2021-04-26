package net.perfectdreams.loritta.commands.economy

import net.perfectdreams.loritta.commands.`fun`.declarations.CancelledCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.entities.UserProfile
import net.perfectdreams.loritta.common.locale.LocaleKeyData

class SonhosExecutor(val emotes: Emotes) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(SonhosExecutor::class) {
        object Options : CommandOptions() {
            val user = optionalUser("user", LocaleKeyData("${CancelledCommand.LOCALE_PREFIX}.selectUser"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val user = args[options.user] ?: context.user

        val profile = context.loritta.services.profiles.id(user.id).retrieve()
        val userSonhos = profile?.money ?: 0L
        val isSelf = context.user.id == user.id

        context.sendMessage {
            if (isSelf) {
                styled(
                    context.locale[
                            "commands.command.sonhos.youHaveSonhos",
                            userSonhos,
                            context.locale["commands.command.sonhos.sonhos.${if (userSonhos == 1L) "one" else "multiple"}"],
                            "" // TODO: Add sonhos ranking
                    ],
                    emotes.loriRich
                )
            } else {
                styled(
                    context.locale[
                            "commands.command.sonhos.userHasSonhos",
                            user.asMention,
                            userSonhos,
                            context.locale["commands.command.sonhos.sonhos.${if (userSonhos == 1L) "one" else "multiple"}"],
                            "" // TODO: Add sonhos ranking
                    ],
                    emotes.loriRich
                )
            }
        }
    }
}