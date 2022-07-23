package net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.GigaChadExecutor

object GigaChadCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Gigachad

    override fun declaration() = slashCommand(listOf("gigachad"), CommandCategory.VIDEOS, I18N_PREFIX.Description) {
        executor = GigaChadExecutor
    }
}