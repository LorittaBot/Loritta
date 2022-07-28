package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations

import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.BomDiaECiaExecutor

class BomDiaECiaCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Cancelled

    override fun declaration() = slashCommand("40028922", CommandCategory.FUN, TodoFixThisData) {
        executor = { BomDiaECiaExecutor(it) }
    }
}