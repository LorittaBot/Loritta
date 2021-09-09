package net.perfectdreams.loritta.cinnamon.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.commands.images.LoriSignExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object LoriSignCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Lorisign

    override fun declaration() = command(listOf("lorisign", "lorittasign", "loriplaca", "lorittaplaca"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = LoriSignExecutor
    }
}