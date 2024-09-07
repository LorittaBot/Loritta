package net.perfectdreams.loritta.morenitta.interactions.commands

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.i18nhelper.core.keydata.ListI18nData
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.commands.CommandCategory
import java.util.*

sealed class ExecutableApplicationCommandDeclaration {
    abstract val name: StringI18nData
    abstract val category: CommandCategory
    abstract val uniqueId: UUID
    abstract val integrationTypes: List<IntegrationType>
    abstract val interactionContexts: List<InteractionContextType>
}

data class SlashCommandDeclaration(
    override val name: StringI18nData,
    val description: StringI18nData,
    override val category: CommandCategory,
    override val uniqueId: UUID,
    val examples: ListI18nData?,
    val defaultMemberPermissions: DefaultMemberPermissions?,
    var isGuildOnly: Boolean,
    var enableLegacyMessageSupport: Boolean,
    var alternativeLegacyLabels: List<String>,
    var alternativeLegacyAbsoluteCommandPaths: List<String>,
    override val integrationTypes: List<IntegrationType>,
    override val interactionContexts: List<InteractionContextType>,
    val executor: LorittaSlashCommandExecutor?,
    val subcommands: List<SlashCommandDeclaration>,
    val subcommandGroups: List<SlashCommandGroupDeclaration>
) : ExecutableApplicationCommandDeclaration()

data class SlashCommandGroupDeclaration(
    val name: StringI18nData,
    val description: StringI18nData,
    val category: CommandCategory,
    var alternativeLegacyLabels: List<String>,
    val subcommands: List<SlashCommandDeclaration>
)

data class UserCommandDeclaration(
    override val name: StringI18nData,
    override val category: CommandCategory,
    override val uniqueId: UUID,
    val defaultMemberPermissions: DefaultMemberPermissions?,
    var isGuildOnly: Boolean,
    override val integrationTypes: List<IntegrationType>,
    override val interactionContexts: List<InteractionContextType>,
    val executor: LorittaUserCommandExecutor?
) : ExecutableApplicationCommandDeclaration()

data class MessageCommandDeclaration(
    override val name: StringI18nData,
    override val category: CommandCategory,
    override val uniqueId: UUID,
    val defaultMemberPermissions: DefaultMemberPermissions?,
    var isGuildOnly: Boolean,
    override val integrationTypes: List<IntegrationType>,
    override val interactionContexts: List<InteractionContextType>,
    val executor: LorittaMessageCommandExecutor?
) : ExecutableApplicationCommandDeclaration()