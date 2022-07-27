package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations

import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.BomDiaECiaExecutor

class BomDiaECiaCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Cancelled

    override fun declaration() = slashCommand("40028922", CommandCategory.FUN, TodoFixThisData) {
        executor = { BomDiaECiaExecutor(it) }
    }
}