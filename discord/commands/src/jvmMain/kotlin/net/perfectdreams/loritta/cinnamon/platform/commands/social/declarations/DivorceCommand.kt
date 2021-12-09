package net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.social.DivorceExecutor

object DivorceCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Divorce

    override fun declaration() = command(listOf("divorce"), CommandCategory.SOCIAL, I18N_PREFIX.Description) {
        executor = DivorceExecutor
    }
}