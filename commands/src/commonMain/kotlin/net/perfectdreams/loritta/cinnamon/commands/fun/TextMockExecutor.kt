package net.perfectdreams.loritta.cinnamon.commands.`fun`

import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.cinnamon.common.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.common.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.common.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import kotlin.random.Random

class TextMockExecutor(val emotes: Emotes) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(TextMockExecutor::class) {
        object Options : CommandOptions() {
            val text = string("text", TextTransformDeclaration.I18N_PREFIX.Mock.Description)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
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