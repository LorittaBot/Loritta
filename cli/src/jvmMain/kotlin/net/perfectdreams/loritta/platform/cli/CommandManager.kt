package net.perfectdreams.loritta.platform.cli

import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.commands.vanilla.declarations.PingCommandDeclaration

class CommandManager {
    val declarations = mutableListOf<CommandDeclarationBuilder>()
    val executors = mutableListOf<CommandExecutor>()

    fun register(declaration: PingCommandDeclaration, vararg executors: CommandExecutor) {
        declarations.add(declaration.declaration())
        this.executors.addAll(executors)
    }
}