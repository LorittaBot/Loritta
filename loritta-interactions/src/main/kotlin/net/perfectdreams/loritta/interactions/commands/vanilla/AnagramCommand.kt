package net.perfectdreams.loritta.interactions.commands.vanilla

import com.google.common.math.BigIntegerMath
import net.perfectdreams.discordinteraktions.commands.SlashCommand
import net.perfectdreams.discordinteraktions.commands.get
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.required

class AnagramCommand : SlashCommand(this) {
    companion object : SlashCommandDeclaration(
        name = "anagram",
        description = "faz uns anagramas bem massa"
    ) {
        override val options = Options

        object Options : SlashCommandDeclaration.Options() {
            val currentWord = string("text", "Text to be anagram stuff idk")
                .required()
                .register()
        }
    }

    override suspend fun executes(context: SlashCommandContext) {
        val currentWord = options.currentWord.get(context)

        var shuffledChars = currentWord.toCharArray().toList()

        while (shuffledChars.size != 1 && shuffledChars.joinToString("") == currentWord && currentWord.groupBy { it }.size >= 2)
            shuffledChars = shuffledChars.shuffled()

        val shuffledWord = shuffledChars.joinToString(separator = "")

        var exp = 1.toBigInteger()
        currentWord.groupingBy { it }.eachCount().forEach { (_, value) ->
            exp = exp.multiply(BigIntegerMath.factorial(value))
        }

        val max = BigIntegerMath.factorial(currentWord.length).divide(exp)

        context.sendMessage {
            content = "Anagrama: $shuffledWord\nMeu deus existem $max anagramas diferentes"
        }
    }
}