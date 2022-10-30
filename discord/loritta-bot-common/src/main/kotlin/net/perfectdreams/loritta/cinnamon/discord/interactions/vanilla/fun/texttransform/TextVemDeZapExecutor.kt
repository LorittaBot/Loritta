package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.texttransform

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.cleanUpForOutput
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations.TextTransformCommand
import net.perfectdreams.loritta.morenitta.LorittaBot

class TextVemDeZapExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val mood = string("mood", TextTransformCommand.VEMDEZAP_I18N_PREFIX.Options.Mood.Text) {
            choice(TextTransformCommand.VEMDEZAP_I18N_PREFIX.Options.Mood.Choice.Happy, "happy")
            choice(TextTransformCommand.VEMDEZAP_I18N_PREFIX.Options.Mood.Choice.Angry, "angry")
            choice(TextTransformCommand.VEMDEZAP_I18N_PREFIX.Options.Mood.Choice.Sassy, "sassy")
            choice(TextTransformCommand.VEMDEZAP_I18N_PREFIX.Options.Mood.Choice.Sad, "sad")
            choice(TextTransformCommand.VEMDEZAP_I18N_PREFIX.Options.Mood.Choice.Sick, "sick")
        }

        val level = integer("level", TextTransformCommand.VEMDEZAP_I18N_PREFIX.Options.Level.Text) {
            choice(TextTransformCommand.VEMDEZAP_I18N_PREFIX.Options.Level.Choice.Level1, 0)
            choice(TextTransformCommand.VEMDEZAP_I18N_PREFIX.Options.Level.Choice.Level2, 1)
            choice(TextTransformCommand.VEMDEZAP_I18N_PREFIX.Options.Level.Choice.Level3, 2)
            choice(TextTransformCommand.VEMDEZAP_I18N_PREFIX.Options.Level.Choice.Level4, 3)
            choice(TextTransformCommand.VEMDEZAP_I18N_PREFIX.Options.Level.Choice.Level5, 4)
        }

        val text = string("text", TextTransformCommand.VEMDEZAP_I18N_PREFIX.Options.Text)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val mood = ZapZapMood.valueOf(args[options.mood].toUpperCase())
        val level = args[options.level]
        val split = cleanUpForOutput(context, args[options.text]).split(" ")

        var output = ""

        for (word in split) {
            val lowerCaseWord = word.toLowerCase()
            output += "$word "
            var addedEmoji = false

            for ((match, emojis) in TextTransformCommand.fullMatch) {
                if (lowerCaseWord == match) {
                    output += "${emojis.random()} "
                    addedEmoji = true
                }
            }

            for ((match, emojis) in TextTransformCommand.partialMatchAny) {
                if (lowerCaseWord.contains(match, true)) {
                    output += "${emojis.random()} "
                    addedEmoji = true
                }
            }

            for ((match, emojis) in TextTransformCommand.partialMatchPrefix) {
                if (lowerCaseWord.startsWith(match, true)) {
                    output += "${emojis.random()} "
                    addedEmoji = true
                }
            }

            if (!addedEmoji) { // Se nós ainda não adicionamos nenhum emoji na palavra...
                // Para fazer um aleatório baseado no nível... quanto maior o nível = mais chance de aparecer emojos
                val upperBound = (5 - level) + 3
                val randomInteger = loritta.random.nextLong(upperBound)

                if (randomInteger == 0L) {
                    val moodEmojis = when (mood) {
                        ZapZapMood.HAPPY -> TextTransformCommand.happyEmojis
                        ZapZapMood.ANGRY -> TextTransformCommand.angryEmojis
                        ZapZapMood.SASSY -> TextTransformCommand.sassyEmojis
                        ZapZapMood.SAD -> TextTransformCommand.sadEmojis
                        ZapZapMood.SICK -> TextTransformCommand.sickEmojis
                    }

                    // E quanto maior o nível, maiores as chances de aparecer mais emojis do lado da palavra
                    val addEmojis = loritta.random.nextLong(1, level + 2)

                    for (i in 0 until addEmojis) {
                        output += "${moodEmojis.random()} "
                    }
                }
            }
        }

        context.sendReply(output, "✍")
    }

    enum class ZapZapMood {
        HAPPY,
        ANGRY,
        SASSY,
        SAD,
        SICK
    }
}