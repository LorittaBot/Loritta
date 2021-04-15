package net.perfectdreams.loritta.commands.misc

import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.entities.LorittaEmbed
import net.perfectdreams.loritta.common.locale.LocaleKeyData

class PingAyayaExecutor(val emotes: Emotes) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(PingAyayaExecutor::class) {
        object Options : CommandOptions() {
            val ayayaCount = integer("ayaya_count", LocaleKeyData("commands.command.ping.ayaya.count"))
                .register()

            val boldAyayaCount = optionalInteger("bold_ayaya_count", LocaleKeyData("commands.command.ping.ayaya.count"))
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val ayayaCount = args[options.ayayaCount]
        val boldAyayaCount = args[options.boldAyayaCount] ?: 1

        context.sendMessage("", LorittaEmbed("Ayaya!", "${emotes.chinoAyaya} ${(0 until ayayaCount).map { "ayaya" }.joinToString(" ")}! **${(0 until boldAyayaCount).map { "ayaya" }.joinToString(" ")}!**", image = "https://media.tenor.com/images/e36cab14cbe800ad261f77df5a68a66d/tenor.gif"))
    }
}