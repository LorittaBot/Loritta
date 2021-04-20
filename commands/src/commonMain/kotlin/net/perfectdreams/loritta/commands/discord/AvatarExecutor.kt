package net.perfectdreams.loritta.commands.discord

import net.perfectdreams.loritta.commands.discord.declarations.AvatarCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.embed.embed

class AvatarExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(AvatarExecutor::class) {
        object Options : CommandOptions() {
            val user = user("user", LocaleKeyData("TODO_FIX_THIS"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val user = args[options.user]

        // TODO: Easter eggs when looking up the avatar of specific users
        context.sendMessage {
            embed = embed {
                body {
                    title = "\uD83D\uDDBC ${user.name}"
                    description = "**${context.locale["${AvatarCommand.LOCALE_PREFIX}.clickHere", "${user.avatar.url}?size=2048"]}**"
                }

                images {
                    image = "${user.avatar.url}?size=2048"
                }
            }.build()
        }
    }
}