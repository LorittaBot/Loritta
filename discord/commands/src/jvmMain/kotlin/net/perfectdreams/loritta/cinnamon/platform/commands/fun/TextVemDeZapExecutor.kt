package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`

import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.TextTransformDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import kotlin.random.Random

class TextVemDeZapExecutor(val random: Random) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(TextVemDeZapExecutor::class) {
        object Options : CommandOptions() {
            val mood = string("mood", TextTransformDeclaration.VEMDEZAP_I18N_PREFIX.Options.Mood.Text)
                .choice("happy", TextTransformDeclaration.VEMDEZAP_I18N_PREFIX.Options.Mood.Choice.Happy)
                .choice("angry", TextTransformDeclaration.VEMDEZAP_I18N_PREFIX.Options.Mood.Choice.Angry)
                .choice("sassy", TextTransformDeclaration.VEMDEZAP_I18N_PREFIX.Options.Mood.Choice.Sassy)
                .choice("sad", TextTransformDeclaration.VEMDEZAP_I18N_PREFIX.Options.Mood.Choice.Sad)
                .choice("sick", TextTransformDeclaration.VEMDEZAP_I18N_PREFIX.Options.Mood.Choice.Sick)
                .register()

            val level = integer("level", TextTransformDeclaration.VEMDEZAP_I18N_PREFIX.Options.Level.Text)
                .choice(0, TextTransformDeclaration.VEMDEZAP_I18N_PREFIX.Options.Level.Choice.Level1)
                .choice(1, TextTransformDeclaration.VEMDEZAP_I18N_PREFIX.Options.Level.Choice.Level2)
                .choice(2, TextTransformDeclaration.VEMDEZAP_I18N_PREFIX.Options.Level.Choice.Level3)
                .choice(3, TextTransformDeclaration.VEMDEZAP_I18N_PREFIX.Options.Level.Choice.Level4)
                .choice(4, TextTransformDeclaration.VEMDEZAP_I18N_PREFIX.Options.Level.Choice.Level5)
                .register()

            val text = string("text", TextTransformDeclaration.VEMDEZAP_I18N_PREFIX.Options.Text)
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

        // TODO: Fix Escape Mentions
        context.sendReply(
            output,
            "✍"
        )
    }

    enum class ZapZapMood {
        HAPPY,
        ANGRY,
        SASSY,
        SAD,
        SICK
    }
}