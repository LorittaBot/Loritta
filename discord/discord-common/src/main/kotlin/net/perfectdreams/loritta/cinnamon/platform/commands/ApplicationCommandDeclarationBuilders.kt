package net.perfectdreams.loritta.cinnamon.platform.commands

import dev.kord.common.entity.Permissions
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
    // Only root commands can have permissions and dmPermission
    var defaultMemberPermissions: Permissions? = null
    var dmPermission: Boolean? = null

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
        defaultMemberPermissions,
        dmPermission,
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
fun userCommand(name: StringI18nData, executor: UserCommandExecutorDeclaration): UserCommandDeclaration {
    return UserCommandDeclarationBuilder(name, executor).build()
}

class UserCommandDeclarationBuilder(val name: StringI18nData, val executor: UserCommandExecutorDeclaration) {
    fun build(): UserCommandDeclaration {
        return UserCommandDeclaration(
            name,
            executor
        )
    }
}