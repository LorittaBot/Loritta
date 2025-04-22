package net.perfectdreams.loritta.morenitta.interactions

import mu.KLogger
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.perfectdreams.loritta.morenitta.interactions.CommandUtils.logMessageEvent
import net.perfectdreams.loritta.morenitta.interactions.CommandUtils.logMessageEventComplete

object CommandUtils {
    /**
     * Logs the [event] to the provided [logger], this is useful to log executed commands to the [logger]
     *
     * @param event  the message event
     * @param logger the logger
     * @see logMessageEventComplete
     */
    fun logMessageEvent(event: SlashCommandInteractionEvent, logger: KLogger) {
        logger.info("${event.user.name} (${event.user.idLong}): ${event.fullCommandName} (${event.options.toList().joinToString { "${it.name}=${it.asString}" }})")
    }

    /**
     * Logs the [event] to the provided [logger], this is useful to log executed commands to the [logger] after they finished its execution
     *
     * @param event          the message event
     * @param logger         the logger
     * @see logMessageEvent
     */
    fun logMessageEventComplete(event: SlashCommandInteractionEvent, logger: KLogger) {
        logger.info("${event.user.name} (${event.user.idLong}): ${event.fullCommandName} (${event.options.toList().joinToString { "${it.name}=${it.asString}" }}) - OK!")
    }
}