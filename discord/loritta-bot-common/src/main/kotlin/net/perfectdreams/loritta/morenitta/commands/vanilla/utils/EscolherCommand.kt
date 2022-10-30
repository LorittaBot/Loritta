package net.perfectdreams.loritta.morenitta.commands.vanilla.utils

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils

class EscolherCommand(loritta: LorittaBot) : AbstractCommand(
    loritta,
    "choose",
    listOf("escolher"),
    category = net.perfectdreams.loritta.common.commands.CommandCategory.UTILS
) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.choose.description")
    override fun getExamplesKey() = LocaleKeyData("commands.command.choose.examples")

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
        OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "choose")

        if (context.args.isNotEmpty()) {
            val joined = context.args.joinToString(separator = " ") // Vamos juntar tudo em uma string
            val split = joined.split(",").map { it.trim() } // E vamos separar!

            // Hora de escolher algo aleatório!
            val chosen = split[LorittaBot.RANDOM.nextInt(split.size)]
            context.reply(
                LorittaReply(
                    message = context.locale["commands.command.choose.result", chosen],
                    prefix = Emotes.LORI_HM
                )
            )
        } else {
            context.explain()
        }
    }
}