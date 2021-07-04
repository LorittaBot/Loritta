package net.perfectdreams.loritta.platform.interaktions.commands

import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.api.entities.Snowflake
import net.perfectdreams.discordinteraktions.common.commands.CommandManager
import net.perfectdreams.discordinteraktions.common.commands.CommandRegistry
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutor
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclarationBuilder
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.slashCommand
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.platform.interaktions.LorittaInteraKTions
import net.perfectdreams.loritta.platform.interaktions.utils.shortenWithEllipsis
import java.util.concurrent.atomic.AtomicInteger

class CommandManager(
    val loritta: LorittaInteraKTions,
    val interaKTionsManager: CommandManager,
    val interaKTionsRegistry: CommandRegistry
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

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

        if (loritta.interactionsConfig.registerGlobally) {
            interaKTionsRegistry.updateAllGlobalCommands(true)
        } else {
            for (guildId in loritta.interactionsConfig.guildsToBeRegistered) {
                interaKTionsRegistry.updateAllCommandsInGuild(Snowflake(guildId), true)
            }
        }
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
                loritta.emotes,
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

            return slashCommand(declaration.labels.first(), buildDescription(locale, declaration)) {
                this.executor = interaKTionsExecutorDeclaration

                if (declaration.subcommands.isNotEmpty() || declaration.subcommandGroups.isNotEmpty())
                    logger.warn { "Executor ${executor::class.simpleName} is set to ${declaration.labels.first()}'s root, but the command has subcommands and/or subcommand groups! Due to Discord's limitations the root command won't be usable!" }

                addSubcommandGroups(declaration, signature, createdExecutors, locale)

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
            return slashCommand(declaration.labels.first(), buildDescription(locale, declaration)) {
                addSubcommandGroups(declaration, signature, createdExecutors, locale)

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

    private fun SlashCommandDeclarationBuilder.addSubcommandGroups(declaration: CommandDeclarationBuilder, signature: AtomicInteger, createdExecutors: MutableList<SlashCommandExecutor>, locale: BaseLocale) {
        for (group in declaration.subcommandGroups) {
            subcommandGroup(group.labels.first(), locale[declaration.description!!].shortenWithEllipsis()) {
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
            CommandCategory.DISCORD -> "\uD83E\uDD19"
            CommandCategory.MISC -> "\uD83E\uDDF6"
            CommandCategory.MODERATION -> TODO()
            CommandCategory.UTILS -> "\uD83D\uDEE0️"
            CommandCategory.SOCIAL -> TODO()
            CommandCategory.ACTION -> TODO()
            CommandCategory.ECONOMY -> "\uD83D\uDCB8"
            CommandCategory.VIDEOS -> "\uD83C\uDFAC"
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