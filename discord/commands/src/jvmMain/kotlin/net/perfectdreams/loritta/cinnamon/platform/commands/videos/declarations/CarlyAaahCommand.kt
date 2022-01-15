package net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.CarlyAaahExecutor

object CarlyAaahCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Carlyaaah

    override fun declaration() = slashCommand(listOf("carlyaaah"), CommandCategory.VIDEOS, I18N_PREFIX.Description) {
        executor = CarlyAaahExecutor
    }
}