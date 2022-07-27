package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.texttransform

import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.TextTransformCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import kotlin.random.Random

class TextMockExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {

        inner class Options : LocalizedApplicationCommandOptions(loritta) {
            val text = string("text", TextTransformCommand.I18N_PREFIX.Mock.Description)
                
        }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val text = args[options.text]

        // Create a random based on the text's hash code
        val random = Random(text.hashCode())

        val mockedText = text.mapIndexed { index, c -> if (random.nextBoolean()) c.uppercaseChar() else c.lowercaseChar() }
            .joinToString("")

        context.sendReply(
            content = mockedText,
            prefix = "✍"
        )
    }
}