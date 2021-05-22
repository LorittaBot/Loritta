package net.perfectdreams.loritta.api.commands

import net.perfectdreams.loritta.common.commands.CommandCategory

/**
 * AbstractCommandBase
 */
abstract class AbstractCommandBase<ContextType: CommandContext, CommandBuilderType: CommandBuilder<ContextType>>(
        val labels: List<String>,
        val category: CommandCategory
) {
    abstract fun command(): Command<ContextType>
    abstract fun create(builder: CommandBuilderType.() -> (Unit)): Command<ContextType>
}