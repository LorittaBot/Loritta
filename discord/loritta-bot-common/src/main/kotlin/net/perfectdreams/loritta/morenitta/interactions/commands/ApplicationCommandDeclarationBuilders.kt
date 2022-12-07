package net.perfectdreams.loritta.morenitta.interactions.commands

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.LanguageManager

// ===[ SLASH COMMANDS ]===
fun slashCommand(name: StringI18nData, description: StringI18nData, category: CommandCategory, block: SlashCommandDeclarationBuilder.() -> (Unit)) = SlashCommandDeclarationBuilder(
    name,
    description,
    category
).apply(block)

@InteraKTionsUnleashedDsl
class SlashCommandDeclarationBuilder(
    val name: StringI18nData,
    val description: StringI18nData,
    val category: CommandCategory
) {
    var executor: LorittaSlashCommandExecutor? = null
    val subcommands = mutableListOf<SlashCommandDeclarationBuilder>()
    val subcommandGroups = mutableListOf<SlashCommandGroupDeclarationBuilder>()

    fun subcommand(name: StringI18nData, description: StringI18nData, block: SlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands.add(
            SlashCommandDeclarationBuilder(
                name,
                description,
                category
            ).apply(block)
        )
    }

    fun subcommandGroup(name: StringI18nData, description: StringI18nData, block: SlashCommandGroupDeclarationBuilder.() -> (Unit)) {
        subcommandGroups.add(
            SlashCommandGroupDeclarationBuilder(
                name,
                description,
                category
            ).apply(block)
        )
    }

    fun build(languageManager: LanguageManager): SlashCommandDeclaration {
        return SlashCommandDeclaration(
            name,
            description,
            category,
            executor,
            subcommands.map { it.build(languageManager) },
            subcommandGroups.map { it.build(languageManager) }
        )
    }
}

@InteraKTionsUnleashedDsl
class SlashCommandGroupDeclarationBuilder(
    val name: StringI18nData,
    val description: StringI18nData,
    val category: CommandCategory
) {
    // Groups can't have executors!
    val subcommands = mutableListOf<SlashCommandDeclarationBuilder>()

    fun subcommand(name: StringI18nData, description: StringI18nData, block: SlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands += SlashCommandDeclarationBuilder(
            name,
            description,
            category
        ).apply(block)
    }

    fun build(languageManager: LanguageManager): SlashCommandGroupDeclaration {
        return SlashCommandGroupDeclaration(
            name,
            description,
            category,
            subcommands.map { it.build(languageManager) }
        )
    }
}