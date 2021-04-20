package net.perfectdreams.loritta.platform.interaktions.commands

import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.commands.CommandManager
import net.perfectdreams.discordinteraktions.commands.SlashCommandArguments
import net.perfectdreams.discordinteraktions.commands.SlashCommandExecutor
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclarationBuilder
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.options.CommandOptions
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOption
import net.perfectdreams.loritta.common.commands.options.CommandOptionType
import net.perfectdreams.loritta.common.commands.options.ListCommandOption
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.platform.interaktions.LorittaInteraKTions
import net.perfectdreams.loritta.platform.interaktions.entities.InteraKTionsMessageChannel
import net.perfectdreams.loritta.platform.interaktions.utils.shortenWithEllipsis
import java.util.concurrent.atomic.AtomicInteger
import kotlin.UnsupportedOperationException

class CommandManager(val loritta: LorittaInteraKTions, val interaKTionsManager: CommandManager) {
    val declarations = mutableListOf<CommandDeclarationBuilder>()
    val executors = mutableListOf<CommandExecutor>()

    fun register(declaration: CommandDeclaration, vararg executors: CommandExecutor) {
        declarations.add(declaration.declaration())
        this.executors.addAll(executors)
    }

    suspend fun convertToInteraKTions(locale: BaseLocale) {
        val commandSignatureIndex = AtomicInteger()

        for (declaration in declarations) {
            val declarationExecutor = declaration.executor

            val (declaration, executors) = convertCommandDeclarationToInteraKTions(
                declaration,
                declarationExecutor,
                locale,
                commandSignatureIndex
            )

            interaKTionsManager.register(
                declaration,
                *executors.toTypedArray()
            )
        }

        interaKTionsManager.updateAllCommandsInGuild(Snowflake(297732013006389252L), true)
    }

    fun convertCommandDeclarationToInteraKTions(
        declaration: CommandDeclarationBuilder,
        declarationExecutor: CommandExecutorDeclaration?,
        locale: BaseLocale,
        signature: AtomicInteger
    ): Pair<SlashCommandDeclaration, List<SlashCommandExecutor>> {
        val executors = mutableListOf<SlashCommandExecutor>()

        val declaration = convertCommandDeclarationToSlashCommand(
            declaration,
            declarationExecutor,
            locale,
            signature,
            executors
        )

        return Pair(
            object: SlashCommandDeclaration {
                override fun declaration() = declaration
            },
            executors
        )
    }

    fun convertCommandDeclarationToSlashCommand(
        declaration: CommandDeclarationBuilder,
        declarationExecutor: CommandExecutorDeclaration?,
        locale: BaseLocale,
        signature: AtomicInteger,
        createdExecutors: MutableList<SlashCommandExecutor>
    ): SlashCommandDeclarationBuilder {
        if (declarationExecutor != null) {
            val executor = executors.firstOrNull { declarationExecutor.parent == it::class }
                ?: throw UnsupportedOperationException("The command executor wasn't found! Did you register the command executor?")
            val rootSignature = signature.getAndIncrement()

            val interaKTionsExecutor = SlashCommandExecutorWrapper(
                loritta,
                locale,
                declaration,
                declarationExecutor,
                executor,
                rootSignature
            )

            // Register all the command options with Discord InteraKTions
            val interaKTionsOptions = SlashCommandOptionsWrapper(
                declarationExecutor,
                locale
            )

            val interaKTionsExecutorDeclaration = object : SlashCommandExecutorDeclaration(rootSignature) {
                override val options = interaKTionsOptions
            }

            createdExecutors.add(interaKTionsExecutor)

            return slashCommand(declaration.labels.first()) {
                description = buildDescription(locale, declaration)

                this.executor = interaKTionsExecutorDeclaration

                for (group in declaration.subcommandGroups) {
                    subcommandGroup(group.labels.first()) {
                        description = locale[declaration.description!!].shortenWithEllipsis()

                        for (subcommand in group.subcommands) {
                            subcommands.add(
                                convertCommandDeclarationToSlashCommand(
                                    subcommand,
                                    subcommand.executor!!,
                                    locale,
                                    signature,
                                    createdExecutors
                                )
                            )
                        }
                    }
                }

                for (subcommand in declaration.subcommands) {
                    subcommands.add(
                        convertCommandDeclarationToSlashCommand(
                            subcommand,
                            subcommand.executor!!,
                            locale,
                            signature,
                            createdExecutors
                        )
                    )
                }
            }
        } else {
            return slashCommand(declaration.labels.first()) {
                description = buildDescription(locale, declaration)

                for (group in declaration.subcommandGroups) {
                    subcommandGroup(group.labels.first()) {
                        description = locale[declaration.description!!].shortenWithEllipsis()

                        for (subcommand in group.subcommands) {
                            subcommands.add(
                                convertCommandDeclarationToSlashCommand(
                                    subcommand,
                                    subcommand.executor!!,
                                    locale,
                                    signature,
                                    createdExecutors
                                )
                            )
                        }
                    }
                }

                for (subcommand in declaration.subcommands) {
                    subcommands.add(
                        convertCommandDeclarationToSlashCommand(
                            subcommand,
                            subcommand.executor,
                            locale,
                            signature,
                            createdExecutors
                        )
                    )
                }
            }
        }
    }

    fun buildDescription(locale: BaseLocale, declaration: CommandDeclarationBuilder) = buildString {
        // It looks like this
        // "「Emoji Category」 Description"
        append("「")
        // Unicode emojis reflecting every category
        val emoji = when (declaration.category) {
            CommandCategory.FUN -> "\uD83D\uDE02"
            CommandCategory.IMAGES -> "\uD83D\uDDBC️"
            CommandCategory.MINECRAFT -> "⛏️"
            CommandCategory.POKEMON -> TODO()
            CommandCategory.UNDERTALE -> "❤️"
            CommandCategory.ROBLOX -> TODO()
            CommandCategory.ANIME -> TODO()
            CommandCategory.DISCORD -> TODO()
            CommandCategory.MISC -> "\uD83E\uDDF6"
            CommandCategory.MODERATION -> TODO()
            CommandCategory.UTILS -> "\uD83D\uDEE0️"
            CommandCategory.SOCIAL -> TODO()
            CommandCategory.ACTION -> TODO()
            CommandCategory.ECONOMY -> TODO()
            CommandCategory.VIDEOS -> TODO()
            CommandCategory.FORTNITE -> TODO()
            CommandCategory.MAGIC -> TODO()
        }
        append(emoji)
        append(" ")
        append(declaration.category.getLocalizedName(locale))
        append("」")
        append(" ")
        append(locale[declaration.description!!])
    }.shortenWithEllipsis()
}