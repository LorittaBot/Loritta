package net.perfectdreams.loritta.commands.vanilla.`fun`

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.commands.declarations.CommandOption
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.commands.vanilla.`fun`.declarations.ChooseCommandDeclaration
import net.perfectdreams.loritta.utils.Emotes

class ChooseCommand(val m: LorittaBot) : LorittaCommand<CommandContext>(ChooseCommandDeclaration) {
    companion object {
        const val LOCALE_PREFIX = "commands.command.choose"
    }

    override suspend fun executes(context: CommandContext) {
        val choices = with(ChooseCommandDeclaration.options) {
            choiceOptions.mapNotNull { context.optionsManager.getNullableString(it as CommandOption<String?>) }
        }

        // Hora de escolher algo aleatório!
        val chosen = choices.random()
        context.reply(
            LorittaReply(
                message = context.locale["commands.command.choose.result", chosen],
                prefix = Emotes.LORI_HM
            )
        )
    }
}