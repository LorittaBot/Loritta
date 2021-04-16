package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.PetPetExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object PetPetCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.petpet"

    override fun declaration() = command(listOf("petpet")) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = PetPetExecutor
    }
}