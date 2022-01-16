package net.perfectdreams.loritta.cinnamon.platform.components

import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentId

sealed class ComponentExecutorDeclaration(
    /**
     * The "parent" is Any to avoid issues with anonymous classes
     *
     * When using anonymous classes, you can use another type to match declarations
     */
    val parent: Any,

    /**
     * The executor's ID, this is stored in the button, to be able to figure out what executor should be used
     *
     * All button executors should be unique!
     */
    val id: ComponentId
)

open class ButtonClickExecutorDeclaration(
    parent: Any,
    id: ComponentId
) : ComponentExecutorDeclaration(parent, id)

open class SelectMenuExecutorDeclaration(
    parent: Any,
    id: ComponentId
) : ComponentExecutorDeclaration(parent, id)