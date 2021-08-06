package net.perfectdreams.loritta.common.commands.declarations

import net.perfectdreams.i18nhelper.core.keydata.StringTranslationData
import net.perfectdreams.loritta.common.commands.CommandCategory
import kotlin.reflect.KClass

fun command(
    parent: KClass<*>,
    labels: List<String>,
    category: CommandCategory,
    description: StringTranslationData,
    block: CommandDeclarationBuilder.() -> (Unit)
): CommandDeclarationBuilder {
    return CommandDeclarationBuilder(parent, labels, category, description).apply(block)
}

class CommandDeclarationBuilder(
    val parent: KClass<*>,
    val labels: List<String>,
    val category: CommandCategory,
    val description: StringTranslationData
) {
    var executor: CommandExecutorDeclaration? = null
    var allowedInPrivateChannel: Boolean = true
    val subcommands = mutableListOf<CommandDeclarationBuilder>()
    val subcommandGroups = mutableListOf<CommandDeclarationBuilder>()

    fun subcommand(labels: List<String>, description: StringTranslationData, block: CommandDeclarationBuilder.() -> (Unit)) {
        subcommands += CommandDeclarationBuilder(parent, labels, category, description).apply(block)
    }

    fun subcommandGroup(labels: List<String>, description: StringTranslationData, block: CommandDeclarationBuilder.() -> (Unit)) {
        subcommandGroups += CommandDeclarationBuilder(parent, labels, category, description).apply(block)
    }
}