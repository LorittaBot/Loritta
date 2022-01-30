package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordCommandContext
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * Utilites to ask users to use Loritta's slash commands instead of using old commands
 */
object OutdatedCommandUtils {
    private val ZONE_UTC = ZoneOffset.UTC
    private val MESSAGE_INTENT_DEADLINE = ZonedDateTime.of(2022, 4, 1, 0, 0, 0, 0, ZONE_UTC)
    private val OUTDATED_COMMAND_WARNING_DEADLINE = MESSAGE_INTENT_DEADLINE.minusMonths(1L)

    suspend fun sendOutdatedCommandMessage(context: CommandContext, locale: BaseLocale, slashCommandName: String) {
        if (shouldSendOutdatedCommandWarning())
            context.sendMessage(buildEmbed(locale, slashCommandName))
    }

    suspend fun sendOutdatedCommandMessage(context: net.perfectdreams.loritta.api.commands.CommandContext, locale: BaseLocale, slashCommandName: String) {
        context as DiscordCommandContext
        if (shouldSendOutdatedCommandWarning())
            context.sendMessage(buildEmbed(locale, slashCommandName))
    }

    /**
     * This checks if the outdated command warning should be sent or not, based on the amount of days between now and the day where the
     */
    private fun shouldSendOutdatedCommandWarning(): Boolean {
        val now = Instant.now()
        val dayDiff = now.until(OUTDATED_COMMAND_WARNING_DEADLINE, ChronoUnit.DAYS)
        return Loritta.RANDOM.nextLong(0, dayDiff.coerceAtLeast(1L)) == 0L
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