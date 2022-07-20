package net.perfectdreams.loritta.cinnamon.platform.autocomplete

import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.autocomplete.FocusedCommandOption
import net.perfectdreams.discordinteraktions.common.autocomplete.GuildAutocompleteContext
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutorWrapper
import net.perfectdreams.loritta.cinnamon.platform.utils.metrics.InteractionsMetrics

sealed class AutocompleteExecutorWrapperBase<T>(
    private val loritta: LorittaCinnamon,
    private val executorDeclaration: AutocompleteExecutorDeclaration<T>,
    private val executor: AutocompleteExecutor<T>
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun onAutocompleteBase(context: net.perfectdreams.discordinteraktions.common.autocomplete.AutocompleteContext, focusedOption: FocusedCommandOption): Map<String, T> {
        val rootDeclarationClazzName = executorDeclaration::class.simpleName
        val executorClazzName = executor::class.simpleName

        logger.info { "(${context.sender.id.value}) $executor" }

        val timer = InteractionsMetrics.EXECUTED_AUTOCOMPLETE_LATENCY_COUNT
            .labels(rootDeclarationClazzName, executorClazzName)
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

            val cinnamonContext = net.perfectdreams.loritta.cinnamon.platform.autocomplete.AutocompleteContext(
                loritta,
                i18nContext,
                context.sender,
                context
            )

            executor.onAutocomplete(
                cinnamonContext,
                focusedOption
            )
        } catch (e: Throwable) {
            logger.warn(e) { "Something went wrong while executing this executor!" } // TODO: Better logs
            throw e
        }

        val commandLatency = timer.observeDuration()
        logger.info { "(${context.sender.id.value}) $executor - OK! Took ${commandLatency * 1000}ms" }

        // Weird hack
        return result
    }
}

class StringAutocompleteExecutorWrapper(
    loritta: LorittaCinnamon,
    private val executorDeclaration: StringAutocompleteExecutorDeclaration,
    executor: StringAutocompleteExecutor
) : AutocompleteExecutorWrapperBase<String>(loritta, executorDeclaration, executor), net.perfectdreams.discordinteraktions.common.autocomplete.StringAutocompleteExecutor {
    override suspend fun onAutocomplete(context: net.perfectdreams.discordinteraktions.common.autocomplete.AutocompleteContext, focusedOption: FocusedCommandOption) = onAutocompleteBase(context, focusedOption)

    override fun signature() = executorDeclaration::class
}

class IntegerAutocompleteExecutorWrapper(
    loritta: LorittaCinnamon,
    private val executorDeclaration: IntegerAutocompleteExecutorDeclaration,
    executor: IntegerAutocompleteExecutor
) : AutocompleteExecutorWrapperBase<Long>(loritta, executorDeclaration, executor), net.perfectdreams.discordinteraktions.common.autocomplete.IntegerAutocompleteExecutor {
    override suspend fun onAutocomplete(context: net.perfectdreams.discordinteraktions.common.autocomplete.AutocompleteContext, focusedOption: FocusedCommandOption) = onAutocompleteBase(context, focusedOption)

    override fun signature() = executorDeclaration::class
}

class NumberAutocompleteExecutorWrapper(
    loritta: LorittaCinnamon,
    private val executorDeclaration: NumberAutocompleteExecutorDeclaration,
    executor: NumberAutocompleteExecutor
) : AutocompleteExecutorWrapperBase<Double>(loritta, executorDeclaration, executor), net.perfectdreams.discordinteraktions.common.autocomplete.NumberAutocompleteExecutor {
    override suspend fun onAutocomplete(context: net.perfectdreams.discordinteraktions.common.autocomplete.AutocompleteContext, focusedOption: FocusedCommandOption) = onAutocompleteBase(context, focusedOption)

    override fun signature() = executorDeclaration::class
}