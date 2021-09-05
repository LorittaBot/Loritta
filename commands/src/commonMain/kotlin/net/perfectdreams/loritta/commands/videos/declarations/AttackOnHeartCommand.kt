package net.perfectdreams.loritta.commands.videos.declarations

import net.perfectdreams.loritta.commands.videos.AttackOnHeartExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object AttackOnHeartCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Attackonheart

    override fun declaration() = command(listOf("attackonheart"), CommandCategory.VIDEOS, I18N_PREFIX.Description) {
        executor = AttackOnHeartExecutor
    }
}