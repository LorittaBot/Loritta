package net.perfectdreams.loritta.commands.videos.declarations

import net.perfectdreams.loritta.commands.videos.CarlyAaahExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.i18n.I18nKeysData

object CarlyAaahCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Carlyaaah

    override fun declaration() = command(listOf("carlyaaah"), CommandCategory.VIDEOS, I18N_PREFIX.Description) {
        executor = CarlyAaahExecutor
    }
}