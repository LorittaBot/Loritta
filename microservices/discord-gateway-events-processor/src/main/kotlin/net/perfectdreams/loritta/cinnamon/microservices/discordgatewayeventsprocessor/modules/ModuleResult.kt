package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

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