package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.discordinteraktions.common.commands.MessageCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.commands.UserCommandExecutorDeclaration
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

/**
 * Base class of every application declaration, because all interactions share a [name]
 */
sealed class ApplicationCommandDeclaration(
    val name: String
)

class SlashCommandDeclaration(
    name: String,
    val description: StringI18nData,
    val executor: SlashCommandExecutorDeclaration? = null,
    val subcommands: List<SlashCommandDeclaration>,
    val subcommandGroups: List<SlashCommandGroupDeclaration>
) : ApplicationCommandDeclaration(name)

class SlashCommandGroupDeclaration(
    name: String,
    val description: StringI18nData,
    val subcommands: List<SlashCommandDeclaration>
) : ApplicationCommandDeclaration(name)

class UserCommandDeclaration(
    name: String,
    val executor: UserCommandExecutorDeclaration // User/Message commands always requires an executor, that's why it is not nullable!
) : ApplicationCommandDeclaration(name)

class MessageCommandDeclaration(
    name: String,
    val executor: MessageCommandExecutorDeclaration // User/Message commands always requires an executor, that's why it is not nullable!
) : ApplicationCommandDeclaration(name)