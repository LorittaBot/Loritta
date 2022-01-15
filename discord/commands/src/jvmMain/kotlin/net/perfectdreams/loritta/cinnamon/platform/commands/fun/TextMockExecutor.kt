package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`

import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import kotlin.random.Random

class TextMockExecutor() : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(TextMockExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val text = string("text", TextTransformDeclaration.I18N_PREFIX.Mock.Description)
                .register()
        }

        override val options = Options
    }

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