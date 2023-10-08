package net.perfectdreams.loritta.cinnamon.discord.gateway.modules

sealed class ModuleResult {
    /**
     * Continues processing the event to subsequent modules.
     */
    object Continue : ModuleResult()

    /**
     * Stops processing the event to subsequent modules.
     */
    object Cancel : ModuleResult()
}