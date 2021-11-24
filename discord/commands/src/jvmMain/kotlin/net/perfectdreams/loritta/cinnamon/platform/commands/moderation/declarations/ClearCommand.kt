package net.perfectdreams.loritta.cinnamon.platform.commands.moderation.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.moderation.ClearExecutor

object ClearCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Clear

    override fun declaration() = command(listOf("clear"), CommandCategory.MODERATION, I18N_PREFIX.Description) {
        executor = ClearExecutor
    }
}