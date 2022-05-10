package net.perfectdreams.loritta.cinnamon.platform.components

import net.perfectdreams.discordinteraktions.platforms.kord.commands.CommandDeclarationUtils
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentId

sealed class ComponentExecutorDeclaration(
    /**
     * The [parent] is Any? to avoid issues with anonymous classes
     *
     * When using anonymous classes, you can use another type to match declarations
     *
     * If [parent] is null when the class is initialized, the declaration will try to find the parent by using Reflection!
     */
    var parent: Any? = null,

    /**
     * The executor's ID, this is stored in the button, to be able to figure out what executor should be used
     *
     * All button executors should be unique!
     */
    val id: ComponentId
) {
    init {
        if (parent == null)
            parent = CommandDeclarationUtils.getParentClass(this)
    }
}

open class ButtonClickExecutorDeclaration(
    parent: Any? = null,
    id: ComponentId
) : ComponentExecutorDeclaration(parent, id) {
    constructor(id: ComponentId) : this(null, id)
}

open class SelectMenuExecutorDeclaration(
    parent: Any? = null,
    id: ComponentId
) : ComponentExecutorDeclaration(parent, id) {
    constructor(id: ComponentId) : this(null, id)
}