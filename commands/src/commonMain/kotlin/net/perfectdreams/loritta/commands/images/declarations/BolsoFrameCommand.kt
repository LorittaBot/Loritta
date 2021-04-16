package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.BolsoFrameExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object BolsoFrameCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.bolsoframe"

    override fun declaration() = command(listOf("bolsoframe", "bolsonaroframe", "bolsoquadro", "bolsonaroquadro")) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = BolsoFrameExecutor
    }
}