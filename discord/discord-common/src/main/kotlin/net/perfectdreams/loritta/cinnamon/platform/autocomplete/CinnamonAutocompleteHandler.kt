package net.perfectdreams.loritta.cinnamon.platform.autocomplete

import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.autocomplete.AutocompleteContext
import net.perfectdreams.discordinteraktions.common.autocomplete.AutocompleteHandler
import net.perfectdreams.discordinteraktions.common.autocomplete.FocusedCommandOption
import net.perfectdreams.discordinteraktions.common.autocomplete.GuildAutocompleteContext
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutorWrapper
import net.perfectdreams.loritta.cinnamon.platform.utils.metrics.InteractionsMetrics

abstract class CinnamonAutocompleteHandler<T>(val loritta: LorittaCinnamon) : AutocompleteHandler<T> {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    abstract suspend fun handle(context: net.perfectdreams.loritta.cinnamon.platform.autocomplete.AutocompleteContext, focusedOption: FocusedCommandOption): Map<String, T>

    override suspend fun handle(context: AutocompleteContext, focusedOption: FocusedCommandOption): Map<String, T> {
        // TODO: Fix this
        // val rootDeclarationClazzName = executorDeclaration::class.simpleName
        // val executorClazzName = executor::class.simpleName

        // logger.info { "(${context.sender.id.value}) $executor" }

        val timer = InteractionsMetrics.EXECUTED_AUTOCOMPLETE_LATENCY_COUNT
            .labels("Unknown", "Unknown") // TODO: Fix this
            .startTimer()

        val result = try {
            val guildId = (context as? GuildAutocompleteContext)?.guildId

            val serverConfig = if (guildId != null) {
                // TODO: Fix this workaround, while this does work, it isn't that good
                loritta.services.serverConfigs.getServerConfigRoot(guildId.value)?.data ?: CommandExecutorWrapper.NonGuildServerConfigRoot
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

        // TODO: Fix this
        val commandLatency = timer.observeDuration()
        // logger.info { "(${context.sender.id.value}) $executor - OK! Took ${commandLatency * 1000}ms" }

        // Weird hack
        return result
    }
}