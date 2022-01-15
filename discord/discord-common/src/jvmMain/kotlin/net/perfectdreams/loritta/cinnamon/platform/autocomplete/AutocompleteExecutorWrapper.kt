package net.perfectdreams.loritta.cinnamon.platform.autocomplete

import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.autocomplete.FocusedCommandOption
import net.perfectdreams.loritta.cinnamon.platform.utils.metrics.Prometheus

sealed class AutocompleteExecutorWrapperBase<T>(
    private val executorDeclaration: AutocompleteExecutorDeclaration<T>,
    private val executor: AutocompleteExecutor<T>
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun onAutocompleteBase(focusedOption: FocusedCommandOption): Map<String, T> {
        val rootDeclarationClazzName = executorDeclaration::class.simpleName
        val executorClazzName = executor::class.simpleName

        // TODO: Add the user that triggered the autocomplete
        logger.info { "$executor" }

        val timer = Prometheus.EXECUTED_SELECT_MENU_LATENCY_COUNT
            .labels(rootDeclarationClazzName, executorClazzName)
            .startTimer()

        val result = try {
            executor.onAutocomplete(
                focusedOption
            )
        } catch (e: Throwable) {
            logger.warn(e) { "Something went wrong while executing this executor!" } // TODO: Better logs
            throw e
        }

        val commandLatency = timer.observeDuration()
        // TODO: Add the user that triggered the autocomplete
        logger.info { "$executor - OK! Took ${commandLatency * 1000}ms" }

        // Weird hack
        return result
    }
}

class StringAutocompleteExecutorWrapper(
    private val executorDeclaration: StringAutocompleteExecutorDeclaration,
    executor: StringAutocompleteExecutor
) : AutocompleteExecutorWrapperBase<String>(executorDeclaration, executor), net.perfectdreams.discordinteraktions.common.autocomplete.StringAutocompleteExecutor {
    override suspend fun onAutocomplete(focusedOption: FocusedCommandOption) = onAutocompleteBase(focusedOption)

    override fun signature() = executorDeclaration::class
}

class IntegerAutocompleteExecutorWrapper(
    private val executorDeclaration: IntegerAutocompleteExecutorDeclaration,
    executor: IntegerAutocompleteExecutor
) : AutocompleteExecutorWrapperBase<Long>(executorDeclaration, executor), net.perfectdreams.discordinteraktions.common.autocomplete.IntegerAutocompleteExecutor {
    override suspend fun onAutocomplete(focusedOption: FocusedCommandOption) = onAutocompleteBase(focusedOption)

    override fun signature() = executorDeclaration::class
}

class NumberAutocompleteExecutorWrapper(
    private val executorDeclaration: NumberAutocompleteExecutorDeclaration,
    executor: NumberAutocompleteExecutor
) : AutocompleteExecutorWrapperBase<Double>(executorDeclaration, executor), net.perfectdreams.discordinteraktions.common.autocomplete.NumberAutocompleteExecutor {
    override suspend fun onAutocomplete(focusedOption: FocusedCommandOption) = onAutocompleteBase(focusedOption)

    override fun signature() = executorDeclaration::class
}