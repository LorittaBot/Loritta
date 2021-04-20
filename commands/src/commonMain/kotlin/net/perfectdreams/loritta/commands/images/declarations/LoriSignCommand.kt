package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.LoriSignExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object LoriSignCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.lorisign"

    override fun declaration() = command(listOf("lorisign", "lorittasign", "loriplaca", "lorittaplaca"), CommandCategory.IMAGES) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = LoriSignExecutor
    }
}