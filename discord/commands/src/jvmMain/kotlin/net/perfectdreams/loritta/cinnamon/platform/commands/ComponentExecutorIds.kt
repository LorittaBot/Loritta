package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.discordinteraktions.common.components.ComponentExecutorDeclaration.Companion.ID_REGEX

object ComponentExecutorIds {
    private val ID_REGEX = Regex("[A-z0-9]+")
    private val registeredComponents = mutableSetOf<ComponentId>()

    // All component executors can have any character from A-z0-9
    // This means that we can have 1185921 executors, which should be enough :)
    val CHANGE_CATEGORY_MENU_EXECUTOR = register("0000")
    val PORTRAIT_SELECT_MENU_EXECUTOR = register("0001")
    val CHANGE_TOBY_CHARACTER_MENU_EXECUTOR = register("0002")
    val CHANGE_DIALOG_BOX_TYPE_BUTTON_EXECUTOR = register("0003")
    val CHANGE_UNIVERSE_SELECT_MENU_EXECUTOR = register("0004")
    val CONFIRM_DIALOG_BOX_BUTTON_EXECUTOR = register("0005")
    val CHANGE_COLOR_PORTRAIT_TYPE_BUTTON_EXECUTOR = register("0006")
    val CHANGE_TRANSACTION_PAGE_BUTTON_EXECUTOR = register("0007")
    val CHANGE_TRANSACTION_FILTER_SELECT_MENU_EXECUTOR = register("0008")
    val START_MATCHMAKING_BUTTON_EXECUTOR = register("0009")

    /**
     * Verifies if the [id] matches our constraints
     *
     * * The ID must match the [ID_REGEX]
     * * The ID must have four characters
     *
     * @return the id
     */
    fun register(id: String): ComponentId {
        require(ID_REGEX.matches(id)) { "ID must respect the $ID_REGEX regular expression!" }
        require(id.length == 4) { "ID must have four characters!" }
        val componentId = ComponentId(id)
        require(componentId !in registeredComponents) { "There is already an component with ID $id!" }
        registeredComponents.add(componentId)
        return componentId
    }
}