package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper

import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.HungerGamesExecutor

object HungerGamesCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Hungergames

    override fun declaration() = slashCommand(listOf("hungergames"), CommandCategory.FUN, I18N_PREFIX.Description) {
        dmPermission = false

        executor = HungerGamesExecutor
    }
}