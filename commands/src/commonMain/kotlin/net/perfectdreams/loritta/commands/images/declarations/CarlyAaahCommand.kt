package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.CarlyAaahExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object CarlyAaahCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.carlyaaah"

    override fun declaration() = command(listOf("carlyaaah"), CommandCategory.IMAGES) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = CarlyAaahExecutor
    }
}