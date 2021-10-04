package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordCommandContext

/**
 * Utilites to ask users to use Loritta's slash commands instead of using old commands
 */
object OutdatedCommandUtils {
    suspend fun sendOutdatedCommandMessage(context: CommandContext, locale: BaseLocale, slashCommandName: String) {
        context.sendMessage(buildEmbed(locale, slashCommandName))
    }

    suspend fun sendOutdatedCommandMessage(context: net.perfectdreams.loritta.api.commands.CommandContext, locale: BaseLocale, slashCommandName: String) {
        context as DiscordCommandContext

        context.sendMessage(buildEmbed(locale, slashCommandName))
    }

    private fun buildEmbed(locale: BaseLocale, slashCommandName: String) = EmbedBuilder()
        .setTitle("${locale["commands.outdatedCommand.title"]} ${Emotes.LORI_TEMMIE}")
        .setDescription(
            locale.getList(
                "commands.outdatedCommand.description",
                slashCommandName,
                loritta.discordInstanceConfig.discord.addBotUrl,
                "${loritta.instanceConfig.loritta.website.url}support",
                locale["commands.outdatedCommand.discordUrl"],
                Emotes.LORI_OWO,
                Emotes.LORI_NICE,
                Emotes.LORI_PRAY,
                Emotes.LORI_SMILE,
                Emotes.LORI_COFFEE
            ).joinToString("\n")
        )
        .setImage(locale["commands.outdatedCommand.slashCommandsTutorial"])
        .setColor(Constants.ROBLOX_RED) // heh
        .build()
}