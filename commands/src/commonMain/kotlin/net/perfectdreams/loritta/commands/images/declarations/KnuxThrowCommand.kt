package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.KnuxThrowExecutor
import net.perfectdreams.loritta.commands.images.LoriSignExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object KnuxThrowCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.knuxthrow"

    override fun declaration() = command(listOf("knuxthrow", "knucklesthrow", "throwknux", "throwknuckles", "knucklesjogar", "knuxjogar", "jogarknuckles", "jogarknux")) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = KnuxThrowExecutor
    }
}