package net.perfectdreams.loritta.morenitta.interactions.commands

import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.i18nhelper.core.keydata.ListI18nData
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.commands.CommandCategory

data class SlashCommandDeclaration(
    val name: StringI18nData,
    val description: StringI18nData,
    val category: CommandCategory,
    val examples: ListI18nData?,
    val defaultMemberPermissions: DefaultMemberPermissions?,
    var isGuildOnly: Boolean,
    var enableLegacyMessageSupport: Boolean,
    var alternativeLegacyLabels: List<String>,
    var alternativeLegacyAbsoluteCommandPaths: List<String>,
    val integrationTypes: List<Command.IntegrationType>,
    val interactionContexts: List<Command.InteractionContextType>,
    val executor: LorittaSlashCommandExecutor?,
    val subcommands: List<SlashCommandDeclaration>,
    val subcommandGroups: List<SlashCommandGroupDeclaration>
)

data class SlashCommandGroupDeclaration(
    val name: StringI18nData,
    val description: StringI18nData,
    val category: CommandCategory,
    var alternativeLegacyLabels: List<String>,
    val subcommands: List<SlashCommandDeclaration>
)

data class UserCommandDeclaration(
    val name: StringI18nData,
    val category: CommandCategory,
    val defaultMemberPermissions: DefaultMemberPermissions?,
    var isGuildOnly: Boolean,
    val integrationTypes: List<Command.IntegrationType>,
    val interactionContexts: List<Command.InteractionContextType>,
    val executor: LorittaUserCommandExecutor?
)

data class MessageCommandDeclaration(
    val name: StringI18nData,
    val category: CommandCategory,
    val defaultMemberPermissions: DefaultMemberPermissions?,
    var isGuildOnly: Boolean,
    val integrationTypes: List<Command.IntegrationType>,
    val interactionContexts: List<Command.InteractionContextType>,
    val executor: LorittaMessageCommandExecutor?
)