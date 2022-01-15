package net.perfectdreams.loritta.cinnamon.platform.commands.options

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.platform.autocomplete.AutocompleteExecutorDeclaration

open class CommandOptionBuilder<T>(
    // We need to store the command option type due to type erasure
    val type: CommandOptionType,
    val name: String,
    val description: StringI18nData,
    val choices: MutableList<CommandChoice<T>>,
    var autocompleteExecutorDeclaration: AutocompleteExecutorDeclaration<T>?
) {
    fun choice(value: T, name: StringI18nData): CommandOptionBuilder<T> {
        if (this.autocompleteExecutorDeclaration != null)
            error("You can't use pre-defined choices with an autocomplete executor set!")

        choices.add(
            LocalizedCommandChoice(
                type,
                name,
                value
            )
        )
        return this
    }

    fun choice(value: T, name: String): CommandOptionBuilder<T> {
        if (this.autocompleteExecutorDeclaration != null)
            error("You can't use pre-defined choices with an autocomplete executor set!")

        choices.add(
            RawCommandChoice(
                type,
                name,
                value
            )
        )
        return this
    }

    fun autocomplete(autocompleteExecutorDeclaration: AutocompleteExecutorDeclaration<T>): CommandOptionBuilder<T> {
        if (this.choices.isNotEmpty())
            error("You can't use an autocomplete executor with pre-defined choices set!")

        this.autocompleteExecutorDeclaration = autocompleteExecutorDeclaration

        return this
    }
}