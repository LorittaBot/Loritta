package net.perfectdreams.loritta.morenitta.interactions.commands

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.i18nhelper.core.keydata.ListI18nData
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.commands.CommandCategory
import java.util.*

// ===[ SLASH COMMANDS ]===
fun slashCommand(name: StringI18nData, description: StringI18nData, category: CommandCategory, uniqueId: UUID, block: SlashCommandDeclarationBuilder.() -> (Unit)) = SlashCommandDeclarationBuilder(
    name,
    description,
    category,
    uniqueId
).apply(block)

@InteraKTionsUnleashedDsl
class SlashCommandDeclarationBuilder(
    val name: StringI18nData,
    val description: StringI18nData,
    val category: CommandCategory,
    val uniqueId: UUID
) {
    var examples: ListI18nData? = null
    var executor: LorittaSlashCommandExecutor? = null
    var botPermissions: Set<Permission>? = null
    var defaultMemberPermissions: DefaultMemberPermissions? = null
    var enableLegacyMessageSupport = false
    var allowUsageEvenIfLorittaBanned = false
    var alternativeLegacyLabels = mutableListOf<String>()
    var alternativeLegacyAbsoluteCommandPaths = mutableListOf<String>()
    val subcommands = mutableListOf<SlashCommandDeclarationBuilder>()
    val subcommandGroups = mutableListOf<SlashCommandGroupDeclarationBuilder>()
    var integrationTypes = listOf(IntegrationType.GUILD_INSTALL)
    var interactionContexts = listOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL)

    fun subcommand(name: StringI18nData, description: StringI18nData, uniqueId: UUID, block: SlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands.add(
            SlashCommandDeclarationBuilder(
                name,
                description,
                category,
                uniqueId
            ).apply {
                this.integrationTypes = this@SlashCommandDeclarationBuilder.integrationTypes
                this.interactionContexts = this@SlashCommandDeclarationBuilder.interactionContexts
            }.apply(block)
        )
    }

    fun subcommandGroup(name: StringI18nData, description: StringI18nData, block: SlashCommandGroupDeclarationBuilder.() -> (Unit)) {
        subcommandGroups.add(
            SlashCommandGroupDeclarationBuilder(
                name,
                description,
                category,
                integrationTypes,
                interactionContexts
            ).apply(block)
        )
    }

    fun build(): SlashCommandDeclaration {
        return SlashCommandDeclaration(
            name,
            description,
            category,
            uniqueId,
            examples,
            botPermissions ?: emptySet(),
            defaultMemberPermissions,
            enableLegacyMessageSupport,
            alternativeLegacyLabels,
            alternativeLegacyAbsoluteCommandPaths,
            allowUsageEvenIfLorittaBanned,
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
    val category: CommandCategory,
    private val integrationTypes: List<IntegrationType>,
    private val interactionContexts: List<InteractionContextType>
) {
    // Groups can't have executors!
    val subcommands = mutableListOf<SlashCommandDeclarationBuilder>()
    var alternativeLegacyLabels = mutableListOf<String>()

    fun subcommand(name: StringI18nData, description: StringI18nData, uniqueId: UUID, block: SlashCommandDeclarationBuilder.() -> (Unit)) {
        subcommands += SlashCommandDeclarationBuilder(
            name,
            description,
            category,
            uniqueId
        ).apply {
            this.integrationTypes = this@SlashCommandGroupDeclarationBuilder.integrationTypes
            this.interactionContexts = this@SlashCommandGroupDeclarationBuilder.interactionContexts
        }.apply(block)
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
fun userCommand(name: StringI18nData, category: CommandCategory, uniqueId: UUID, executor: LorittaUserCommandExecutor, block: UserCommandDeclarationBuilder.() -> (Unit) = {}) = UserCommandDeclarationBuilder(name, category, uniqueId, executor)
    .apply(block)

@InteraKTionsUnleashedDsl
class UserCommandDeclarationBuilder(
    val name: StringI18nData,
    val category: CommandCategory,
    val uniqueId: UUID,
    val executor: LorittaUserCommandExecutor,
) {
    var defaultMemberPermissions: DefaultMemberPermissions? = null
    var integrationTypes = listOf(IntegrationType.GUILD_INSTALL)
    var interactionContexts = listOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL)
    var allowUsageEvenIfLorittaBanned = false

    fun build(): UserCommandDeclaration {
        return UserCommandDeclaration(
            name,
            category,
            uniqueId,
            defaultMemberPermissions,
            allowUsageEvenIfLorittaBanned,
            integrationTypes,
            interactionContexts,
            executor
        )
    }
}

// ===[ MESSAGE COMMANDS ]===
fun messageCommand(name: StringI18nData, category: CommandCategory, uniqueId: UUID, executor: LorittaMessageCommandExecutor, block: MessageCommandDeclarationBuilder.() -> (Unit) = {}) = MessageCommandDeclarationBuilder(name, category, uniqueId, executor)
    .apply(block)

@InteraKTionsUnleashedDsl
class MessageCommandDeclarationBuilder(
    val name: StringI18nData,
    val category: CommandCategory,
    val uniqueId: UUID,
    val executor: LorittaMessageCommandExecutor
) {
    var defaultMemberPermissions: DefaultMemberPermissions? = null
    var integrationTypes = listOf(IntegrationType.GUILD_INSTALL)
    var interactionContexts = listOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL)
    var allowUsageEvenIfLorittaBanned = false
    
    fun build(): MessageCommandDeclaration {
        return MessageCommandDeclaration(
            name,
            category,
            uniqueId,
            defaultMemberPermissions,
            allowUsageEvenIfLorittaBanned,
            integrationTypes,
            interactionContexts,
            executor
        )
    }
}