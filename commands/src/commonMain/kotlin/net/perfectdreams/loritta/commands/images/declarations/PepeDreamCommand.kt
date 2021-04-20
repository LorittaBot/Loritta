package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.PepeDreamExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object PepeDreamCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.pepedream"

    override fun declaration() = command(listOf("pepedream", "sonhopepe", "pepesonho"), CommandCategory.IMAGES) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = PepeDreamExecutor
    }
}