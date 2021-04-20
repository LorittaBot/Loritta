package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.AttackOnHeartExecutor
import net.perfectdreams.loritta.commands.images.CarlyAaahExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object AttackOnHeartCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.attackonheart"

    override fun declaration() = command(listOf("attackonheart"), CommandCategory.IMAGES) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = AttackOnHeartExecutor
    }
}