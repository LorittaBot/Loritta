package net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.images.GetOverHereExecutor

object GetOverHereCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Getoverhere

    override fun declaration() = slashCommand(listOf("getoverhere"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        executor = GetOverHereExecutor
    }
}