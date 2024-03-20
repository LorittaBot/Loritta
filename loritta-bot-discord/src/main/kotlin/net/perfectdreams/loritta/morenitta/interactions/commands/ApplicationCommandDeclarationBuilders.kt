package net.perfectdreams.loritta.morenitta.interactions.commands

import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.i18nhelper.core.keydata.ListI18nData
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.commands.CommandCategory

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
    var examples: ListI18nData? = null
    var executor: LorittaSlashCommandExecutor? = null
    var defaultMemberPermissions: DefaultMemberPermissions? = null
    var isGuildOnly = false
    var enableLegacyMessageSupport = false
    var alternativeLegacyLabels = mutableListOf<String>()
    var alternativeLegacyAbsoluteCommandPaths = mutableListOf<String>()
    val subcommands = mutableListOf<SlashCommandDeclarationBuilder>()
    val subcommandGroups = mutableListOf<SlashCommandGroupDeclarationBuilder>()
    var integrationTypes = listOf(Command.IntegrationType.GUILD_INSTALL)
    var interactionContexts = listOf(Command.InteractionContextType.GUILD, Command.InteractionContextType.BOT_DM, Command.InteractionContextType.PRIVATE_CHANNEL)

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

    fun build(): SlashCommandDeclaration {
        return SlashCommandDeclaration(
            name,
            description,
            category,
            examples,
            defaultMemberPermissions,
            isGuildOnly,
            enableLegacyMessageSupport,
            alternativeLegacyLabels,
            alternativeLegacyAbsoluteCommandPaths,
            integrationTypes,
            interactionContexts,
            executor,
            subcommands.map { it.build() },
            subcommandGroups.map { it.build() }
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
    var alternativeLegacyLabels = mutableListOf<String>()

    fun subcommand(name: StringI18nData, description: StringI18nData, block: SlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands += SlashCommandDeclarationBuilder(
            name,
            description,
            category
        ).apply(block)
    }

    fun build(): SlashCommandGroupDeclaration {
        return SlashCommandGroupDeclaration(
            name,
            description,
            category,
            alternativeLegacyLabels,
            subcommands.map { it.build() }
        )
    }
}

// ===[ USER COMMANDS ]===
fun userCommand(name: StringI18nData, category: CommandCategory, executor: LorittaUserCommandExecutor, block: UserCommandDeclarationBuilder.() -> (Unit) = {}) = UserCommandDeclarationBuilder(name, category, executor)
    .apply(block)

@InteraKTionsUnleashedDsl
class UserCommandDeclarationBuilder(
    val name: StringI18nData,
    val category: CommandCategory,
    val executor: LorittaUserCommandExecutor
) {
    var defaultMemberPermissions: DefaultMemberPermissions? = null
    var isGuildOnly = false
    var integrationTypes = listOf(Command.IntegrationType.GUILD_INSTALL)
    var interactionContexts = listOf(Command.InteractionContextType.GUILD, Command.InteractionContextType.BOT_DM, Command.InteractionContextType.PRIVATE_CHANNEL)

    fun build(): UserCommandDeclaration {
        return UserCommandDeclaration(
            name,
            category,
            defaultMemberPermissions,
            isGuildOnly,
            integrationTypes,
            interactionContexts,
            executor
        )
    }
}

// ===[ MESSAGE COMMANDS ]===
fun messageCommand(name: StringI18nData, category: CommandCategory, executor: LorittaMessageCommandExecutor, block: MessageCommandDeclarationBuilder.() -> (Unit) = {}) = MessageCommandDeclarationBuilder(name, category, executor)
    .apply(block)

@InteraKTionsUnleashedDsl
class MessageCommandDeclarationBuilder(
    val name: StringI18nData,
    val category: CommandCategory,
    val executor: LorittaMessageCommandExecutor
) {
    var defaultMemberPermissions: DefaultMemberPermissions? = null
    var isGuildOnly = false
    var integrationTypes = listOf(Command.IntegrationType.GUILD_INSTALL)
    var interactionContexts = listOf(Command.InteractionContextType.GUILD, Command.InteractionContextType.BOT_DM, Command.InteractionContextType.PRIVATE_CHANNEL)

    fun build(): MessageCommandDeclaration {
        return MessageCommandDeclaration(
            name,
            category,
            defaultMemberPermissions,
            isGuildOnly,
            integrationTypes,
            interactionContexts,
            executor
        )
    }
}