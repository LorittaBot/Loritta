package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import kotlin.reflect.KClass

fun slashCommand(
    parent: KClass<*>,
    labels: List<String>,
    category: CommandCategory,
    description: StringI18nData,
    block: SlashCommandDeclarationBuilder.() -> (Unit)
): SlashCommandDeclarationBuilder {
    return SlashCommandDeclarationBuilder(parent, labels, category, description).apply(block)
}

class SlashCommandDeclarationBuilder(
    val parent: KClass<*>,
    val labels: List<String>,
    val category: CommandCategory,
    val description: StringI18nData
) {
    var executor: SlashCommandExecutorDeclaration? = null
    var allowedInPrivateChannel: Boolean = true
    val subcommands = mutableListOf<SlashCommandDeclarationBuilder>()
    val subcommandGroups = mutableListOf<SlashCommandDeclarationBuilder>()

    fun subcommand(labels: List<String>, description: StringI18nData, block: SlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands += SlashCommandDeclarationBuilder(parent, labels, category, description).apply(block)
    }

    fun subcommandGroup(labels: List<String>, description: StringI18nData, block: SlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommandGroups += SlashCommandDeclarationBuilder(parent, labels, category, description).apply(block)
    }
}