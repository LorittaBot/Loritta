package net.perfectdreams.loritta.cinnamon.platform.components

import net.perfectdreams.discordinteraktions.common.entities.User

interface ComponentExecutor {
    /**
     * Used by the [net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandExecutorDeclaration] to match declarations to executors.
     *
     * By default the class of the executor is used, but this may cause issues when using anonymous classes!
     *
     * To avoid this issue, you can replace the signature with another unique identifier
     */
    open fun signature(): Any = this::class
}

// ===[ BUTTON CLICK ]===
sealed interface ButtonClickBaseExecutor : ComponentExecutor

interface ButtonClickExecutor : ButtonClickBaseExecutor {
    suspend fun onClick(user: User, context: ComponentContext)
}

interface ButtonClickWithDataExecutor : ButtonClickBaseExecutor {
    suspend fun onClick(user: User, context: ComponentContext, data: String)
}

// ===[ SELECT MENUS ]===
sealed interface SelectMenuBaseExecutor : ComponentExecutor

interface SelectMenuExecutor : SelectMenuBaseExecutor {
    suspend fun onSelect(user: User, context: ComponentContext, values: List<String>)
}

interface SelectMenuWithDataExecutor : SelectMenuBaseExecutor {
    suspend fun onSelect(user: User, context: ComponentContext, data: String, values: List<String>)
}