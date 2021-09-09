package net.perfectdreams.loritta.cinnamon.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.commands.images.TrumpExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object TrumpCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Trump

    override fun declaration() = command(listOf("trump"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = TrumpExecutor
    }
}