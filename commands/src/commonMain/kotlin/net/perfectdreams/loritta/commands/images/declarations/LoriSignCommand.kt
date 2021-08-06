package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.LoriSignExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object LoriSignCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.lorisign"

    override fun declaration() = command(listOf("lorisign", "lorittasign", "loriplaca", "lorittaplaca"), CommandCategory.IMAGES, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = LoriSignExecutor
    }
}