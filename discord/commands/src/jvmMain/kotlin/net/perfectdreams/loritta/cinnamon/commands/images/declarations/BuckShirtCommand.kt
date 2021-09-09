package net.perfectdreams.loritta.cinnamon.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.commands.images.BuckShirtExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object BuckShirtCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Buckshirt

    override fun declaration() = command(listOf("buckshirt", "buckcamisa"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = BuckShirtExecutor
    }
}