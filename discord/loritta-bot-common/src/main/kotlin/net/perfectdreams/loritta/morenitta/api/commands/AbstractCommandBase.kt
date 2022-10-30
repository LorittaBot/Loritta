package net.perfectdreams.loritta.morenitta.api.commands

import net.perfectdreams.loritta.common.commands.CommandCategory

/**
 * AbstractCommandBase
 */
abstract class AbstractCommandBase<ContextType : CommandContext, CommandBuilderType : CommandBuilder<ContextType>>(
    val labels: List<String>,
    val category: net.perfectdreams.loritta.common.commands.CommandCategory
) {
    abstract fun command(): Command<ContextType>
    abstract fun create(builder: CommandBuilderType.() -> (Unit)): Command<ContextType>
}