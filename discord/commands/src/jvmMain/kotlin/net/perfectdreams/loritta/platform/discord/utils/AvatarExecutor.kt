package net.perfectdreams.loritta.platform.discord.utils

import net.perfectdreams.loritta.platform.discord.utils.declarations.AvatarCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.embed.LorittaColor
import net.perfectdreams.loritta.common.utils.embed.embed

class AvatarExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(AvatarExecutor::class) {
        object Options : CommandOptions() {
            val user = optionalUser("user", LocaleKeyData("TODO_FIX_THIS"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val user = args[Options.user] ?: context.user

        // TODO: Easter eggs when looking up the avatar of specific users
        context.sendMessage {
            embed = embed {
                body {
                    title = "\uD83D\uDDBC ${user.name}"
                    description = "**${context.locale["${AvatarCommand.LOCALE_PREFIX}.clickHere", "${user.avatar.url}?size=2048"]}**"
                    color = LorittaColor.DISCORD_BLURPLE
                }

                images {
                    image = "${user.avatar.url}?size=2048"
                }
            }.build()
        }
    }
}