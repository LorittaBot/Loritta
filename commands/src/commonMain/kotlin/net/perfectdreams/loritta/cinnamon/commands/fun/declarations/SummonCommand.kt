package net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations

import net.perfectdreams.loritta.cinnamon.commands.`fun`.BemBoladaExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.FaustaoExecutor
import net.perfectdreams.loritta.cinnamon.commands.`fun`.TioDoPaveExecutor
import net.perfectdreams.loritta.cinnamon.common.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.common.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

object SummonCommand : CommandDeclaration {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Summon

    override fun declaration() = command(listOf("summon"), CommandCategory.FUN, TodoFixThisData) {
        subcommand(listOf("tiodopavê"), I18N_PREFIX.Tiodopave.Description) {
            executor = TioDoPaveExecutor
        }

        subcommand(listOf("faustão"), I18N_PREFIX.Faustao.Description) {
            executor = FaustaoExecutor
        }

        subcommand(listOf("kenji"), I18N_PREFIX.Kenji.Description) {
            executor = BemBoladaExecutor
        }
    }
}