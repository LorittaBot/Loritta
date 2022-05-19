package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

/**
 * Base class of every application declaration, because all interactions share a [name]
 */
sealed class ApplicationCommandDeclaration

class SlashCommandDeclaration(
    val name: String,
    val description: StringI18nData,
    val category: CommandCategory,
    val executor: SlashCommandExecutorDeclaration? = null,
    val subcommands: List<SlashCommandDeclaration>,
    val subcommandGroups: List<SlashCommandGroupDeclaration>
) : ApplicationCommandDeclaration()

class SlashCommandGroupDeclaration(
    val name: String,
    val description: StringI18nData,
    val subcommands: List<SlashCommandDeclaration>
) : ApplicationCommandDeclaration()

class UserCommandDeclaration(
    val name: StringI18nData,
    val executor: UserCommandExecutorDeclaration // User/Message commands always requires an executor, that's why it is not nullable!
) : ApplicationCommandDeclaration()

class MessageCommandDeclaration(
    val name: String,
    val executor: MessageCommandExecutorDeclaration // User/Message commands always requires an executor, that's why it is not nullable!
) : ApplicationCommandDeclaration()