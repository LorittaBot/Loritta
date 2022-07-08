package net.perfectdreams.loritta.cinnamon.platform.modals

import net.perfectdreams.discordinteraktions.platforms.kord.commands.CommandDeclarationUtils
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentId
import net.perfectdreams.loritta.cinnamon.platform.modals.components.ModalComponents

open class ModalSubmitExecutorDeclaration(
    /**
     * The [parent] is Any? to avoid issues with anonymous classes
     *
     * When using anonymous classes, you can use another type to match declarations
     *
     * If [parent] is null when the class is initialized, the declaration will try to find the parent by using Reflection!
     */
    var parent: Any? = null,

    /**
     * The executor's ID, this is stored in the modal, to be able to figure out what executor should be used
     *
     * All modal executors should be unique!
     */
    val id: ComponentId
) {
    constructor(id: ComponentId) : this(null, id)

    init {
        if (parent == null)
            parent = CommandDeclarationUtils.getParentClass(this)
    }

    open val options: ModalComponents = object: ModalComponents() {}
}