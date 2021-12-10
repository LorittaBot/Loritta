package net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.social.RepExecutor

object RepCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Rep

    override fun declaration() = command(listOf("rep"), CommandCategory.SOCIAL, I18N_PREFIX.Description) {
        executor = RepExecutor
    }
}