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

class MorseExecutor(val emotes: Emotes): CommandExecutor() {
    companion object: CommandExecutorDeclaration(MorseExecutor::class) {
        object Options: CommandOptions() {
            val textArgument = string("text", LocaleKeyData("TODO_FIX_THIS"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val text = args[options.textArgument]
        val toMorse = MorseUtils.toMorse(text)
        val fromMorse = MorseUtils.fromMorse(text)

        if (toMorse.isBlank()) {
            context.sendReply {
                prefix = emotes.error.asMention
                content = context.locale["${MorseCommand.LOCALE_PREFIX}.fail"]
            }
            return
        }

        context.sendEmbed {
            body {
                title = buildString {
                    if (fromMorse.isNotBlank()) {
                        append(emotes.handPointRight.asMention)
                        append(emotes.radio.asMention)
                        append(" ")
                        append(context.locale["${MorseCommand.LOCALE_PREFIX}.toFrom"])
                    } else {
                        append(emotes.handPointLeft.asMention)
                        append(emotes.radio.asMention)
                        append(" ")
                        append(context.locale["${MorseCommand.LOCALE_PREFIX}.fromTo"])
                    }
                }
                description = buildString {
                    append("*beep* *boop*")
                    append("\n")
                    append("```")
                    append(fromMorse.ifBlank { toMorse })
                    append("```")
                }
                color = -6706507
            }
        }
    }
}