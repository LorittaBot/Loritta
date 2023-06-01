package net.perfectdreams.loritta.morenitta.interactions.commands.options

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

fun interface AutocompleteExecutor<T> {
    fun execute(event: CommandAutoCompleteInteractionEvent): Map<String, T>
}