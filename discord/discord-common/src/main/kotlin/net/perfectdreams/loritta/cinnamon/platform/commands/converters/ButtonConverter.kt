package net.perfectdreams.loritta.cinnamon.platform.commands.converters

import net.perfectdreams.discordinteraktions.common.commands.CommandManager
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandRegistry
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.buttons.ButtonClickWithDataExecutorWrapper

class ButtonConverter(
    private val loritta: LorittaCinnamon,
    private val cinnamonCommandRegistry: CommandRegistry,
    private val interaKTionsManager: CommandManager
) {
    private val buttonsDeclarations by cinnamonCommandRegistry::buttonsDeclarations
    private val buttonsExecutors by cinnamonCommandRegistry::buttonsExecutors

    fun convertButtonsToInteraKTions() {
        for (declaration in buttonsDeclarations) {
            val executor = buttonsExecutors.firstOrNull { declaration.parent == it::class }
                ?: throw UnsupportedOperationException("The button click executor wasn't found! Did you register the button click executor?")

            val interaKTionsExecutor = ButtonClickWithDataExecutorWrapper(
                loritta,
                declaration,
                executor as ButtonClickWithDataExecutor
            )

            val interaKTionsExecutorDeclaration = object : net.perfectdreams.discordinteraktions.common.components.ButtonClickExecutorDeclaration(
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