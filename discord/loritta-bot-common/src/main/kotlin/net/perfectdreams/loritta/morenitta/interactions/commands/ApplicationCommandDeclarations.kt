package net.perfectdreams.loritta.morenitta.interactions.commands

import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.commands.CommandCategory

data class SlashCommandDeclaration(
    val name: StringI18nData,
    val description: StringI18nData,
    val category: CommandCategory,
    val defaultMemberPermissions: DefaultMemberPermissions?,
    var isGuildOnly: Boolean,
    val executor: LorittaSlashCommandExecutor?,
    val subcommands: List<SlashCommandDeclaration>,
    val subcommandGroups: List<SlashCommandGroupDeclaration>
)

data class SlashCommandGroupDeclaration(
    val name: StringI18nData,
    val description: StringI18nData,
    val category: CommandCategory,
    val subcommands: List<SlashCommandDeclaration>
)