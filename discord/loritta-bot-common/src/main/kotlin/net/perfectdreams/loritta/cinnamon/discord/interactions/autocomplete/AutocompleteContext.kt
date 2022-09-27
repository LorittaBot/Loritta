package net.perfectdreams.loritta.cinnamon.discord.interactions.autocomplete

import dev.kord.common.entity.CommandArgument
import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.autocomplete.AutocompleteContext
import net.perfectdreams.discordinteraktions.common.commands.options.OptionReference
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot

// This doesn't inherit from InteractionContext because we can't send messages on an autocomplete request
open class AutocompleteContext(
    val loritta: LorittaBot,
    val i18nContext: I18nContext,
    val sender: User,
    val interaKTionsContext: AutocompleteContext
) {
    /**
     * Gets the filled argument from the [arguments] map, matching it via the [CommandOption].
     *
     * The reason the return value is [T?] is that a user can skip previous options and go straight to the autocompletable option, which
     * in that case, the option will be null because it wasn't filled by the user.
     *
     * **You should not use this to match the focused option!** Due to the way Discord works, a [CommandArgument.AutoCompleteArgument] is a String, so
     * it would be impossible to return the data in the proper type to you. If you do this, an error will be thrown.
     *
     * @param option the command option
     */
    fun <T> getArgument(option: OptionReference<T>): T? = interaKTionsContext.getArgument(option)
}