package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.FaustaoExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object FaustaoCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.faustao"

    override fun declaration() = command(listOf("faustão", "faustao"), CommandCategory.FUN) {
        description = LocaleKeyData("${LOCALE_PREFIX}.description")
        executor = FaustaoExecutor
    }
}