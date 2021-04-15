package net.perfectdreams.loritta.common.commands.declarations

import net.perfectdreams.loritta.common.locale.LocaleKeyData

fun command(labels: List<String>, block: CommandDeclarationBuilder.() -> (Unit)): CommandDeclarationBuilder {
    return CommandDeclarationBuilder(labels).apply(block)
}

class CommandDeclarationBuilder(val labels: List<String>) {
    var description: LocaleKeyData? = null
    var executor: CommandExecutorDeclaration? = null
    val subcommands = mutableListOf<CommandDeclarationBuilder>()
    val subcommandGroups = mutableListOf<CommandDeclarationBuilder>()

    fun subcommand(labels: List<String>, block: CommandDeclarationBuilder.() -> (Unit)) {
        subcommands += CommandDeclarationBuilder(labels).apply(block)
    }

    fun subcommandGroup(labels: List<String>, block: CommandDeclarationBuilder.() -> (Unit)) {
        subcommandGroups += CommandDeclarationBuilder(labels).apply(block)
    }
}