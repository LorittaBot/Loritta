package net.perfectdreams.loritta.commands.`fun`

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
            val user = user("user", LocaleKeyData("TODO_FIX_THIS"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val user = args[options.user]

        context.sendReply {
            content = context.locale["commands.command.cancelled.wasCancelled", user.asMention,  context.locale.getList("commands.command.cancelled.reasons").random()]
            prefix = emotes.loriHmpf.toString()
        }
    }
}