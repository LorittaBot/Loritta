package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.CanellaDvdExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object CanellaDvdCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.canelladvd"

    override fun declaration() = command(listOf("canelladvd", "matheuscanelladvd", "canellacover", "matheuscanelladvd"), CommandCategory.IMAGES) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = CanellaDvdExecutor
    }
}