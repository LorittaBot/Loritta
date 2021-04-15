package net.perfectdreams.loritta.commands.`fun`

import net.perfectdreams.loritta.commands.`fun`.declarations.VemDeZapCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import kotlin.random.Random

class VemDeZapExecutor(val emotes: Emotes, val random: Random) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(VemDeZapExecutor::class) {
        object Options : CommandOptions() {
            val mood = string("mood", LocaleKeyData("commands.command.vemdezap.whatIsTheMood"))
                .choice("happy", LocaleKeyData("commands.command.vemdezap.moodHappy"))
                .choice("angry", LocaleKeyData("commands.command.vemdezap.moodAngry"))
                .choice("sassy", LocaleKeyData("commands.command.vemdezap.moodSassy"))
                .choice("sad", LocaleKeyData("commands.command.vemdezap.moodSad"))
                .choice("sick", LocaleKeyData("commands.command.vemdezap.moodSick"))
                .register()

            val level = integer("level", LocaleKeyData("commands.command.vemdezap.whatIsTheLevel"))
                .choice(0, LocaleKeyData("commands.command.vemdezap.level1"))
                .choice(1, LocaleKeyData("commands.command.vemdezap.level2"))
                .choice(2, LocaleKeyData("commands.command.vemdezap.level3"))
                .choice(3, LocaleKeyData("commands.command.vemdezap.level4"))
                .choice(4, LocaleKeyData("commands.command.vemdezap.level5"))
                .register()

            val text = string("text", LocaleKeyData("TODO_FIX_THIS"))
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

            for ((match, emojis) in VemDeZapCommand.fullMatch) {
                if (lowerCaseWord == match) {
                    output += "${emojis.random()} "
                    addedEmoji = true
                }
            }

            for ((match, emojis) in VemDeZapCommand.partialMatchAny) {
                if (lowerCaseWord.contains(match, true)) {
                    output += "${emojis.random()} "
                    addedEmoji = true
                }
            }

            for ((match, emojis) in VemDeZapCommand.partialMatchPrefix) {
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
                        ZapZapMood.HAPPY -> VemDeZapCommand.happyEmojis
                        ZapZapMood.ANGRY -> VemDeZapCommand.angryEmojis
                        ZapZapMood.SASSY -> VemDeZapCommand.sassyEmojis
                        ZapZapMood.SAD -> VemDeZapCommand.sadEmojis
                        ZapZapMood.SICK -> VemDeZapCommand.sickEmojis
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