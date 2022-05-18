package net.perfectdreams.loritta.cinnamon.platform.commands.converters

import net.perfectdreams.discordinteraktions.common.commands.CommandManager
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandRegistry
import net.perfectdreams.loritta.cinnamon.platform.components.SelectMenuWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.selects.SelectMenuWithDataExecutorWrapper

class SelectMenuConverter(
    private val loritta: LorittaCinnamon,
    private val cinnamonCommandRegistry: CommandRegistry,
    private val interaKTionsManager: CommandManager
) {
    private val selectMenusDeclarations by cinnamonCommandRegistry::selectMenusDeclarations
    private val selectMenusExecutors by cinnamonCommandRegistry::selectMenusExecutors

    fun convertSelectMenusToInteraKTions() {
        for (declaration in selectMenusDeclarations) {
            val executor = selectMenusExecutors.firstOrNull { declaration.parent == it::class }
                ?: throw UnsupportedOperationException("The select menu executor wasn't found! Did you register the select menu executor?")

            if (executor is SelectMenuWithDataExecutor) {
                val interaKTionsExecutor = SelectMenuWithDataExecutorWrapper(
                    loritta,
                    declaration,
                    executor
                )

                val interaKTionsExecutorDeclaration = object : net.perfectdreams.discordinteraktions.common.components.SelectMenuExecutorDeclaration(
                    declaration::class,
                    declaration.id.value
                ) {}

                interaKTionsManager.register(
                    interaKTionsExecutorDeclaration,
                    interaKTionsExecutor
                )
            }
        }
    }
}