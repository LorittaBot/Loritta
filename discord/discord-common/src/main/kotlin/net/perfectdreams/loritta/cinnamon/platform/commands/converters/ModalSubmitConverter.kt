package net.perfectdreams.loritta.cinnamon.platform.commands.converters

import net.perfectdreams.discordinteraktions.common.commands.CommandManager
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandRegistry
import net.perfectdreams.loritta.cinnamon.platform.modals.ModalSubmitWithDataExecutorWrapper
import net.perfectdreams.loritta.cinnamon.platform.modals.components.ModalComponentsWrapper

class ModalSubmitConverter(
    private val loritta: LorittaCinnamon,
    private val cinnamonCommandRegistry: CommandRegistry,
    private val interaKTionsManager: CommandManager
) {
    private val modalSubmitDeclarations by cinnamonCommandRegistry::modalSubmitDeclarations
    private val modalSubmitExecutors by cinnamonCommandRegistry::modalSubmitExecutors

    fun convertModalSubmitToInteraKTions() {
        for (declaration in modalSubmitDeclarations) {
            val executor = modalSubmitExecutors.firstOrNull { declaration.parent == it::class }
                ?: throw UnsupportedOperationException("The modal submit executor wasn't found! Did you register the modal submit executor?")

            // We use a class reference because we need to have a consistent signature, because we also use it on the SlashCommandOptionsWrapper class
            val interaKTionsExecutor = ModalSubmitWithDataExecutorWrapper(loritta, declaration, executor)

            val interaKTionsExecutorDeclaration = object : net.perfectdreams.discordinteraktions.common.modals.ModalSubmitExecutorDeclaration(
                declaration::class,
                declaration.id
            ) {
                override val options = ModalComponentsWrapper(declaration)
            }

            interaKTionsManager.register(
                interaKTionsExecutorDeclaration,
                interaKTionsExecutor
            )
        }
    }
}