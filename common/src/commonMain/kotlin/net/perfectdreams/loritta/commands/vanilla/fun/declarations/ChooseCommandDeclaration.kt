package net.perfectdreams.loritta.commands.vanilla.`fun`.declarations

import net.perfectdreams.loritta.api.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.api.commands.declarations.CommandOption
import net.perfectdreams.loritta.api.commands.declarations.required
import net.perfectdreams.loritta.commands.vanilla.`fun`.ChooseCommand
import net.perfectdreams.loritta.utils.locale.LocaleKeyData

object ChooseCommandDeclaration : CommandDeclaration(
    name = "choose",
    description = LocaleKeyData("${ChooseCommand.LOCALE_PREFIX}.description")
) {
    override val options = Options

    object Options : CommandDeclaration.Options() {
        // Look, originally we were going to create a gigantic list with all the possible choices, but this takes waaaay too long
        // So we are going to improvise:tm:
        const val MAX_OPTIONS = 25
        val choiceOptions = mutableListOf<CommandOption<out String?>>()

        init {
            repeat(MAX_OPTIONS) {
                // TODO: Fix locale
                val option = string("choice${it + 1}", LocaleKeyData("idk"))
                    .let { option ->
                        // Set the first two options as required
                        if (it in 0..1)
                            option.required()
                        else
                            option
                    }
                    .register()

                choiceOptions.add(option)
            }
        }
    }
}