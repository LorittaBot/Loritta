package net.perfectdreams.loritta.morenitta.interactions.commands.autocomplete

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

fun interface AutocompleteExecutor<T> {
    suspend fun execute(
        context: AutocompleteContext
    ): Map<String, T>
}