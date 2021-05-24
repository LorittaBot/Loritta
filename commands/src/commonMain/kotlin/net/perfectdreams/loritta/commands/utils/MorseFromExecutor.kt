package net.perfectdreams.loritta.commands.utils

import net.perfectdreams.loritta.commands.utils.declarations.MorseCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.text.MorseUtils

class MorseFromExecutor(val emotes: Emotes): CommandExecutor() {
    companion object : CommandExecutorDeclaration(MorseFromExecutor::class) {
        object Options : CommandOptions() {
            val textArgument = string("text", LocaleKeyData("${MorseCommand.LOCALE_PREFIX}.options.fromMorse"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val text = args[options.textArgument]
        val fromMorse = MorseUtils.fromMorse(text)

        if (fromMorse.isBlank())
            context.fail(
                prefix = emotes.error.asMention,
                content = context.locale["${MorseCommand.LOCALE_PREFIX}.fail"]
            ) { isEphemeral = true }

        context.sendReply(
            content = "`$fromMorse`",
            prefix = emotes.radio.toString(),
            mentionSenderHint = true
        )
    }
}