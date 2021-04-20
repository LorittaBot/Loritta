package net.perfectdreams.loritta.common.commands.declarations

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.LocaleKeyData

fun command(labels: List<String>, category: CommandCategory, block: CommandDeclarationBuilder.() -> (Unit)): CommandDeclarationBuilder {
    return CommandDeclarationBuilder(labels, category).apply(block)
}

class CommandDeclarationBuilder(val labels: List<String>, val category: CommandCategory) {
    var description: LocaleKeyData? = null
    var executor: CommandExecutorDeclaration? = null
    val subcommands = mutableListOf<CommandDeclarationBuilder>()
    val subcommandGroups = mutableListOf<CommandDeclarationBuilder>()

    fun subcommand(labels: List<String>, block: CommandDeclarationBuilder.() -> (Unit)) {
        subcommands += CommandDeclarationBuilder(labels, category).apply(block)
    }

    fun subcommandGroup(labels: List<String>, block: CommandDeclarationBuilder.() -> (Unit)) {
        subcommandGroups += CommandDeclarationBuilder(labels, category).apply(block)
    }
}