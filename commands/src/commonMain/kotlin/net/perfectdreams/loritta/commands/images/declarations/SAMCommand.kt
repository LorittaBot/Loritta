package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.SAMExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object SAMCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.sam"

    override fun declaration() = command(listOf("sam"), CommandCategory.IMAGES, LocaleKeyData("commands.command.sam.description").toI18nHelper()) {
        executor = SAMExecutor
    }
}