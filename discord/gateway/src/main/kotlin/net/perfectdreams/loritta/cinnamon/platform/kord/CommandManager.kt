package net.perfectdreams.loritta.cinnamon.platform.kord

import net.perfectdreams.loritta.cinnamon.common.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandDeclarationBuilder

class CommandManager {
    val declarations = mutableListOf<CommandDeclarationBuilder>()
    val executors = mutableListOf<CommandExecutor>()

    fun register(declaration: CommandDeclaration, vararg executors: CommandExecutor) {
        declarations.add(declaration.declaration())
        this.executors.addAll(executors)
    }
}