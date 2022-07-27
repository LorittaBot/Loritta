package net.perfectdreams.loritta.cinnamon.platform.commands

import dev.kord.common.Locale
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

open class LorittaCommandDeclaration(
    name: String,
    description: String,
    descriptionLocalizations: Map<Locale, String>? = null,
    val descriptionI18n: StringI18nData,
    val category: CommandCategory,
    executor: SlashCommandExecutor? = null,
    defaultMemberPermissions: Permissions?,
    dmPermission: Boolean?,
    subcommands: List<SlashCommandDeclaration>,
    subcommandGroups: List<SlashCommandGroupDeclaration>
) : SlashCommandDeclaration(
    name,
    emptyMap(),
    description,
    descriptionLocalizations,
    executor,
    defaultMemberPermissions,
    dmPermission,
    subcommands,
    subcommandGroups
)

open class LorittaCommandGroupDeclaration(
    name: String,
    description: String,
    descriptionLocalizations: Map<Locale, String>? = null,
    val descriptionI18n: StringI18nData,
    val category: CommandCategory,
    subcommands: List<SlashCommandDeclaration>
) : SlashCommandGroupDeclaration(
    name,
    emptyMap(),
    description,
    descriptionLocalizations,
    subcommands
)