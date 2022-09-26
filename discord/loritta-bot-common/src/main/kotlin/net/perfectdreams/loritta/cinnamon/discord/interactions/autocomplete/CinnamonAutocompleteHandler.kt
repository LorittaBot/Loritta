package net.perfectdreams.loritta.cinnamon.discord.interactions.autocomplete

import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.autocomplete.AutocompleteContext
import net.perfectdreams.discordinteraktions.common.autocomplete.AutocompleteHandler
import net.perfectdreams.discordinteraktions.common.autocomplete.FocusedCommandOption
import net.perfectdreams.discordinteraktions.common.autocomplete.GuildAutocompleteContext
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandExecutorWrapper
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.InteractionsMetrics

abstract class CinnamonAutocompleteHandler<T>(val loritta: LorittaCinnamon) : AutocompleteHandler<T> {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    abstract suspend fun handle(context: net.perfectdreams.loritta.cinnamon.discord.interactions.autocomplete.AutocompleteContext, focusedOption: FocusedCommandOption): Map<String, T>

    override suspend fun handle(context: AutocompleteContext, focusedOption: FocusedCommandOption): Map<String, T> {
        logger.info { "(${context.sender.id.value}) $this" }

        val timer = InteractionsMetrics.EXECUTED_AUTOCOMPLETE_LATENCY_COUNT
            .labels(this::class.simpleName ?: "UnknownHandler") // TODO: Fix this
            .startTimer()

        val result = try {
            val guildId = (context as? GuildAutocompleteContext)?.guildId

            val serverConfig = if (guildId != null) {
                // TODO: Fix this workaround, while this does work, it isn't that good
                loritta.pudding.serverConfigs.getServerConfigRoot(guildId.value)?.data ?: CommandExecutorWrapper.NonGuildServerConfigRoot
            } else {
                // TODO: Should this class *really* be named "ServerConfig"? After all, it isn't always used for guilds
                CommandExecutorWrapper.NonGuildServerConfigRoot
            }

            val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

            val cinnamonContext = AutocompleteContext(
                loritta,
                i18nContext,
                context.sender,
                context
            )

            handle(cinnamonContext, focusedOption)
        } catch (e: Throwable) {
            logger.warn(e) { "Something went wrong while executing this executor!" } // TODO: Better logs
            throw e
        }

        val commandLatency = timer.observeDuration()
        logger.info { "(${context.sender.id.value}) $this - OK! Took ${commandLatency * 1000}ms" }

        // Weird hack
        return result
    }
}