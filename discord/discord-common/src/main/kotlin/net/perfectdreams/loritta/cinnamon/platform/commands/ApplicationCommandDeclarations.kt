package net.perfectdreams.loritta.cinnamon.platform.commands

import dev.kord.common.Locale
import dev.kord.common.entity.Permissions
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

class CinnamonSlashCommandDeclaration(
    val declarationWrapper: CinnamonSlashCommandDeclarationWrapper,
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

class CinnamonSlashCommandGroupDeclaration(
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

class CinnamonUserCommandDeclaration(
    val declarationWrapper: CinnamonUserCommandDeclarationWrapper,
    name: String,
    nameLocalizations: Map<Locale, String>? = null,
    defaultMemberPermissions: Permissions?,
    dmPermission: Boolean?,
    executor: UserCommandExecutor // User/Message commands always requires an executor, that's why it is not nullable!
) : UserCommandDeclaration(name, nameLocalizations, defaultMemberPermissions, dmPermission, executor)