package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations

import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.BemBoladaExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.FaustaoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.TioDoPaveExecutor

class SummonCommand(loritta: LorittaCinnamon) : CinnamonSlashCommandDeclarationWrapper(loritta) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Summon
    }

    override fun declaration() = slashCommand("summon", CommandCategory.FUN, TodoFixThisData) {
        subcommand("tiodopavê", I18N_PREFIX.Tiodopave.Description) {
            executor = TioDoPaveExecutor(loritta)
        }

        subcommand("faustão", I18N_PREFIX.Faustao.Description) {
            executor = FaustaoExecutor(loritta)
        }

        subcommand("kenji", I18N_PREFIX.Kenji.Description) {
            executor = BemBoladaExecutor(loritta)
        }
    }
}