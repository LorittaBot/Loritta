package net.perfectdreams.loritta.commands.`fun`

import net.perfectdreams.loritta.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import kotlin.random.Random

class TextVemDeZapExecutor(val emotes: Emotes, val random: Random) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(TextVemDeZapExecutor::class) {
        object Options : CommandOptions() {
            val mood = string("mood", TodoFixThisData)
                .choice("happy", TodoFixThisData)
                .choice("angry", TodoFixThisData)
                .choice("sassy", TodoFixThisData)
                .choice("sad", TodoFixThisData)
                .choice("sick", TodoFixThisData)
                .register()

            val level = integer("level", TodoFixThisData)
                .choice(0, TodoFixThisData)
                .choice(1, TodoFixThisData)
                .choice(2, TodoFixThisData)
                .choice(3, TodoFixThisData)
                .choice(4, TodoFixThisData)
                .register()

            val text = string("text", TodoFixThisData)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val mood = ZapZapMood.valueOf(args[options.mood].toUpperCase())
        val level = args[options.level]
        val split = args[options.text].split(" ")

        var output = ""

        for (word in split) {
            val lowerCaseWord = word.toLowerCase()
            output += "$word "
            var addedEmoji = false

            for ((match, emojis) in TextTransformDeclaration.fullMatch) {
                if (lowerCaseWord == match) {
                    output += "${emojis.random()} "
                    addedEmoji = true
                }
            }

            for ((match, emojis) in TextTransformDeclaration.partialMatchAny) {
                if (lowerCaseWord.contains(match, true)) {
                    output += "${emojis.random()} "
                    addedEmoji = true
                }
            }

            for ((match, emojis) in TextTransformDeclaration.partialMatchPrefix) {
                if (lowerCaseWord.startsWith(match, true)) {
                    output += "${emojis.random()} "
                    addedEmoji = true
                }
            }

            if (!addedEmoji) { // Se nós ainda não adicionamos nenhum emoji na palavra...
                // Para fazer um aleatório baseado no nível... quanto maior o nível = mais chance de aparecer emojos
                val upperBound = (5 - level) + 3
                val randomInteger = random.nextInt(upperBound)

                if (randomInteger == 0) {
                    val moodEmojis = when (mood) {
                        ZapZapMood.HAPPY -> TextTransformDeclaration.happyEmojis
                        ZapZapMood.ANGRY -> TextTransformDeclaration.angryEmojis
                        ZapZapMood.SASSY -> TextTransformDeclaration.sassyEmojis
                        ZapZapMood.SAD -> TextTransformDeclaration.sadEmojis
                        ZapZapMood.SICK -> TextTransformDeclaration.sickEmojis
                    }

                    // E quanto maior o nível, maiores as chances de aparecer mais emojis do lado da palavra
                    val addEmojis = random.nextInt(1, level + 2)

                    for (i in 0 until addEmojis) {
                        output += "${moodEmojis.random()} "
                    }
                }
            }
        }

        context.sendMessage(output)
    }

    enum class ZapZapMood {
        HAPPY,
        ANGRY,
        SASSY,
        SAD,
        SICK
    }
}