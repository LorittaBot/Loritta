package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`

import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.CancelledCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.styled

class CancelledExecutor() : CommandExecutor() {
    companion object : CommandExecutorDeclaration(CancelledExecutor::class) {
        object Options : CommandOptions() {
            val user = user("user", CancelledCommand.I18N_PREFIX.Options.User)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val user = args[options.user]

        context.sendMessage {
            styled(
                content = context.i18nContext.get(
                    CancelledCommand.I18N_PREFIX.WasCancelled(
                        mentionUser(user, false),
                        context.i18nContext.get(CancelledCommand.I18N_PREFIX.Reasons)
                            .random()
                    )
                ),
                prefix = Emotes.loriHmpf.toString()
            )
        }
    }
}