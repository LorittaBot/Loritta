package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.discordinteraktions.common.components.ComponentDeclaration
import net.perfectdreams.discordinteraktions.common.components.ComponentDeclaration.Companion.ID_REGEX

object ComponentExecutorIds {
    private val ID_REGEX = Regex("[A-z0-9]+")
    private val registeredComponents = mutableSetOf<ComponentId>()

    // All component executors can have any character from A-z0-9
    // This means that we can have 1185921 executors, which should be enough :)
    val CHANGE_CATEGORY_MENU_EXECUTOR = register("0000")

    /**
     * Verifies if the [id] matches our constraints
     *
     * * The ID must match the [ID_REGEX]
     * * The ID must have four characters
     *
     * @return the id
     */
    fun register(id: String): ComponentId {
        require(ID_REGEX.matches(id)) { "ID must respect the ${ComponentDeclaration.ID_REGEX} regular expression!" }
        require(id.length == 4) { "ID must have four characters!" }
        val componentId = ComponentId(id)
        require(componentId !in registeredComponents) { "There is already an component with ID $id!" }
        registeredComponents.add(componentId)
        return componentId
    }
}