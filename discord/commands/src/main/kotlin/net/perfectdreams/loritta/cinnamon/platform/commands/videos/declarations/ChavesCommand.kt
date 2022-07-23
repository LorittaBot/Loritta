package net.perfectdreams.loritta.cinnamon.platform.commands.videos.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.ChavesCocieloExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.videos.ChavesOpeningExecutor

object ChavesCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Chaves

    override fun declaration() = slashCommand(listOf("chaves"), CommandCategory.VIDEOS, I18N_PREFIX.Description) {
        subcommand(listOf("opening"), I18N_PREFIX.Opening.Description) {
            executor = ChavesOpeningExecutor
        }

        subcommand(listOf("cocielo"), I18N_PREFIX.Cocielo.Description) {
            executor = ChavesCocieloExecutor
        }
    }
}