package net.perfectdreams.loritta.cinnamon.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.commands.images.SustoExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object SustoCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Fright

    override fun declaration() = command(listOf("scared", "fright", "susto"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = SustoExecutor
    }
}