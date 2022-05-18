package net.perfectdreams.loritta.cinnamon.platform.commands.converters

import net.perfectdreams.discordinteraktions.common.commands.CommandManager
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.IntegerAutocompleteExecutor
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.IntegerAutocompleteExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.IntegerAutocompleteExecutorWrapper
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.NumberAutocompleteExecutor
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.NumberAutocompleteExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.NumberAutocompleteExecutorWrapper
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.StringAutocompleteExecutor
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.StringAutocompleteExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.StringAutocompleteExecutorWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandRegistry

class AutocompleteConverter(
    private val loritta: LorittaCinnamon,
    private val cinnamonCommandRegistry: CommandRegistry,
    private val interaKTionsManager: CommandManager
) {
    private val autocompleteDeclarations by cinnamonCommandRegistry::autocompleteDeclarations
    private val autocompleteExecutors by cinnamonCommandRegistry::autocompleteExecutors

    fun convertAutocompleteToInteraKTions() {
        for (declaration in autocompleteDeclarations) {
            val executor = autocompleteExecutors.firstOrNull { declaration.parent == it::class }
                ?: throw UnsupportedOperationException("The autocomplete executor wasn't found! Did you register the autocomplete executor?")

            // We use a class reference because we need to have a consistent signature, because we also use it on the SlashCommandOptionsWrapper class
            when (executor) {
                is StringAutocompleteExecutor -> {
                    val interaKTionsExecutor = StringAutocompleteExecutorWrapper(
                        loritta,
                        declaration as StringAutocompleteExecutorDeclaration,
                        executor
                    )

                    val interaKTionsExecutorDeclaration = object : net.perfectdreams.discordinteraktions.common.autocomplete.StringAutocompleteExecutorDeclaration(
                        declaration::class
                    ) {}

                    interaKTionsManager.register(
                        interaKTionsExecutorDeclaration,
                        interaKTionsExecutor
                    )
                }

                is IntegerAutocompleteExecutor -> {
                    val interaKTionsExecutor = IntegerAutocompleteExecutorWrapper(
                        loritta,
                        declaration as IntegerAutocompleteExecutorDeclaration,
                        executor
                    )

                    val interaKTionsExecutorDeclaration = object : net.perfectdreams.discordinteraktions.common.autocomplete.IntegerAutocompleteExecutorDeclaration(
                        declaration::class
                    ) {}

                    interaKTionsManager.register(
                        interaKTionsExecutorDeclaration,
                        interaKTionsExecutor
                    )
                }
                is NumberAutocompleteExecutor -> {
                    val interaKTionsExecutor = NumberAutocompleteExecutorWrapper(
                        loritta,
                        declaration as NumberAutocompleteExecutorDeclaration,
                        executor
                    )

                    val interaKTionsExecutorDeclaration = object : net.perfectdreams.discordinteraktions.common.autocomplete.NumberAutocompleteExecutorDeclaration(
                        declaration::class
                    ) {}

                    interaKTionsManager.register(
                        interaKTionsExecutorDeclaration,
                        interaKTionsExecutor
                    )
                }
            }
        }
    }
}