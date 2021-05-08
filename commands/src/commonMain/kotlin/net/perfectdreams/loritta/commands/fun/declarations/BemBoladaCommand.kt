package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.BemBoladaExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object BemBoladaCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.bembolada"

    override fun declaration() = command(listOf("bembolada"), CommandCategory.FUN) {
        description = LocaleKeyData("${LOCALE_PREFIX}.description")
        executor = BemBoladaExecutor
    }
}