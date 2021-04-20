package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.BriggsCoverExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object BriggsCoverCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.briggscover"

    override fun declaration() = command(listOf("briggscover", "coverbriggs", "capabriggs", "briggscapa"), CommandCategory.IMAGES) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = BriggsCoverExecutor
    }
}