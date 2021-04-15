package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.ArtExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object ArtCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.art"

    override fun declaration() = command(listOf("art", "arte")) {
        description = LocaleKeyData("${LOCALE_PREFIX}.description")
        executor = ArtExecutor
    }
}