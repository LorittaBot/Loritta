package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.KnuxThrowExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object KnuxThrowCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.knuxthrow"

    override fun declaration() = command(listOf("knuxthrow", "knucklesthrow", "throwknux", "throwknuckles", "knucklesjogar", "knuxjogar", "jogarknuckles", "jogarknux"), CommandCategory.IMAGES, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = KnuxThrowExecutor
    }
}