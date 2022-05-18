package net.perfectdreams.loritta.cinnamon.platform.commands

import dev.kord.common.Locale
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

fun slashCommand(
    labels: List<String>,
    description: StringI18nData,
    category: CommandCategory,
    block: SlashCommandDeclarationBuilder.() -> (Unit)
): SlashCommandDeclaration {
    return SlashCommandDeclarationBuilder(labels, description, category).apply(block).build()
}

class SlashCommandDeclarationBuilder(val labels: List<String>, val description: StringI18nData, val category: CommandCategory) {
    var executor: SlashCommandExecutorDeclaration? = null
    val subcommands = mutableListOf<SlashCommandDeclaration>()
    val subcommandGroups = mutableListOf<SlashCommandGroupDeclaration>()

    fun subcommand(labels: List<String>, description: StringI18nData, block: SlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands += SlashCommandDeclarationBuilder(labels, description, category).apply(block).build()
    }

    fun subcommandGroup(labels: List<String>, description: StringI18nData, block: SlashCommandGroupDeclarationBuilder.() -> (Unit)) {
        subcommandGroups += SlashCommandGroupDeclarationBuilder(labels, description, category).apply(block).build()
    }

    fun build(): SlashCommandDeclaration = SlashCommandDeclaration(
        labels.first(),
        description,
        category,
        executor,
        subcommands,
        subcommandGroups
    )
}

class SlashCommandGroupDeclarationBuilder(val labels: List<String>, val description: StringI18nData, val category: CommandCategory) {
    val subcommands = mutableListOf<SlashCommandDeclaration>()

    fun subcommand(labels: List<String>, description: StringI18nData, block: SlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands += SlashCommandDeclarationBuilder(labels, description, category).apply(block).build()
    }

    fun build(): SlashCommandGroupDeclaration = SlashCommandGroupDeclaration(
        labels.first(),
        description,
        subcommands
    )
}

// ===[ USER COMMANDS ]===
fun userCommand(name: String, executor: UserCommandExecutorDeclaration): UserCommandDeclaration {
    return UserCommandDeclarationBuilder(name, executor).build()
}

class UserCommandDeclarationBuilder(val name: String, val executor: UserCommandExecutorDeclaration) {
    var nameLocalizations: Map<Locale, String>? = null

    fun build(): UserCommandDeclaration {
        return UserCommandDeclaration(
            name,
            executor
        )
    }
}