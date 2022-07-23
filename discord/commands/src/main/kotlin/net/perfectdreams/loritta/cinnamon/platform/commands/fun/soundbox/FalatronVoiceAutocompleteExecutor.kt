package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.soundbox

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import net.perfectdreams.discordinteraktions.common.autocomplete.FocusedCommandOption
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.AutocompleteContext
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.StringAutocompleteExecutor
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.StringAutocompleteExecutorDeclaration

class FalatronVoiceAutocompleteExecutor(val falatronModelsManager: FalatronModelsManager) : StringAutocompleteExecutor {
    companion object : StringAutocompleteExecutorDeclaration()

    override suspend fun onAutocomplete(
        context: AutocompleteContext,
        focusedOption: FocusedCommandOption
    ): Map<String, String> {
        // Wait until we have a non-empty model list
        val models = falatronModelsManager.models.filter { it.isNotEmpty() }
            .first()

        // Then we filter only the models that starts with the user input!
        return models
            .sortedBy { it.name }
            .filter {
                it.name.startsWith(focusedOption.value, true)
            }.take(25).associate {
                "${it.name} (${it.category})" to it.name
            }
    }
}