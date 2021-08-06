package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.ManiaTitleCardExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object ManiaTitleCardCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.maniatitlecard"

    override fun declaration() = command(listOf("maniatitlecard"), CommandCategory.IMAGES, LocaleKeyData("${LOCALE_PREFIX}.description").toI18nHelper()) {
        executor = ManiaTitleCardExecutor
    }
}