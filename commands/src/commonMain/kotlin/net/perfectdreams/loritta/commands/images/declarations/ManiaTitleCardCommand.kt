package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.ManiaTitleCardExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object ManiaTitleCardCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.maniatitlecard"

    override fun declaration() = command(listOf("maniatitlecard"), CommandCategory.IMAGES) {
        description = LocaleKeyData("${LOCALE_PREFIX}.description")
        executor = ManiaTitleCardExecutor
    }
}