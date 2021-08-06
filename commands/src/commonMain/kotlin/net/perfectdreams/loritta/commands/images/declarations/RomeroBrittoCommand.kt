package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.RomeroBrittoExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object RomeroBrittoCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.romerobritto"

    override fun declaration() = command(listOf("romerobritto", "pintura", "painting"), CommandCategory.IMAGES, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = RomeroBrittoExecutor
    }
}