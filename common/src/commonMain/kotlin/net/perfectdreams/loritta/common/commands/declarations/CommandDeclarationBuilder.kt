package net.perfectdreams.loritta.common.commands.declarations

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import kotlin.reflect.KClass

fun command(parent: KClass<*>, labels: List<String>, category: CommandCategory, block: CommandDeclarationBuilder.() -> (Unit)): CommandDeclarationBuilder {
    return CommandDeclarationBuilder(parent, labels, category).apply(block)
}

class CommandDeclarationBuilder(val parent: KClass<*>, val labels: List<String>, val category: CommandCategory) {
    var description: LocaleKeyData? = null
    var executor: CommandExecutorDeclaration? = null
    var allowedInPrivateChannel: Boolean = true
    val subcommands = mutableListOf<CommandDeclarationBuilder>()
    val subcommandGroups = mutableListOf<CommandDeclarationBuilder>()
    val cooldown: Int? = null

    fun subcommand(labels: List<String>, block: CommandDeclarationBuilder.() -> (Unit)) {
        subcommands += CommandDeclarationBuilder(parent, labels, category).apply(block)
    }

    fun subcommandGroup(labels: List<String>, block: CommandDeclarationBuilder.() -> (Unit)) {
        subcommandGroups += CommandDeclarationBuilder(parent, labels, category).apply(block)
    }
}