package net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.social.MarryExecutor

object MarryCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Marry

    override fun declaration() = command(listOf("marry"), CommandCategory.SOCIAL, I18N_PREFIX.Description) {
        executor = MarryExecutor
    }
}