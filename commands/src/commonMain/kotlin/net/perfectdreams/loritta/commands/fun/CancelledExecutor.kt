package net.perfectdreams.loritta.commands.`fun`

import net.perfectdreams.loritta.commands.`fun`.declarations.CancelledCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleKeyData

class CancelledExecutor(val emotes: Emotes) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(CancelledExecutor::class) {
        object Options : CommandOptions() {
            val user = user("user", LocaleKeyData("${CancelledCommand.LOCALE_PREFIX}.selectUser"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val user = args[options.user]

        context.sendMessage {
            styled(
                context.locale["commands.command.cancelled.wasCancelled", mentionUser(user, false), context.locale.getList("commands.command.cancelled.reasons").random()],
                emotes.loriHmpf.toString()
            )
        }
    }
}