package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.texttransform

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.cleanUpForOutput
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations.TextTransformCommand
import net.perfectdreams.loritta.common.utils.text.VaporwaveUtils
import net.perfectdreams.loritta.morenitta.LorittaBot

class TextVaporQualityExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val text = string("text", TextTransformCommand.I18N_PREFIX.Vaporquality.Description)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val text = cleanUpForOutput(context, args[options.text])

        val vaporquality = VaporwaveUtils.vaporwave(text.uppercase().toCharArray().joinToString(" "))
        context.sendReply(
            vaporquality,
            "✍"
        )
    }
}