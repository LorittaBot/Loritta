package net.perfectdreams.loritta.cinnamon.discord.commands

import dev.kord.common.entity.Snowflake
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.commands.CommandManager
import net.perfectdreams.discordinteraktions.common.commands.CommandRegistry
import net.perfectdreams.discordinteraktions.common.commands.slash.SlashCommandExecutor
import net.perfectdreams.discordinteraktions.declarations.commands.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.commands.slash.SlashCommandDeclarationBuilder
import net.perfectdreams.discordinteraktions.declarations.commands.slash.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.declarations.commands.slash.slashCommand
import net.perfectdreams.discordinteraktions.declarations.commands.wrappers.SlashCommandDeclarationWrapper
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import java.util.concurrent.atomic.AtomicInteger

class CommandRegistry(
    val loritta: LorittaCinnamon,
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

    suspend fun convertToInteraKTions(locale: I18nContext) {
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
        locale: I18nContext,
        signature: AtomicInteger
    ): Pair<SlashCommandDeclarationWrapper, List<SlashCommandExecutor>> {
        val executors = mutableListOf<SlashCommandExecutor>()

        val declaration = convertCommandDeclarationToSlashCommand(
            declaration,
            declarationExecutor,
            locale,
            signature,
            executors
        )

        return Pair(
            object: SlashCommandDeclarationWrapper {
                override fun declaration() = declaration
            },
            executors
        )
    }

    fun convertCommandDeclarationToSlashCommand(
        declaration: CommandDeclarationBuilder,
        declarationExecutor: CommandExecutorDeclaration?,
        locale: I18nContext,
        signature: AtomicInteger,
        createdExecutors: MutableList<SlashCommandExecutor>
    ): SlashCommandDeclaration {
        if (declarationExecutor != null) {
            val executor = executors.firstOrNull { declarationExecutor.parent == it::class }
                ?: throw UnsupportedOperationException("The command executor wasn't found! Did you register the command executor?")
            val rootSignature = signature.getAndIncrement()

            val interaKTionsExecutor = SlashCommandExecutorWrapper(
                loritta,
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

    private fun SlashCommandDeclarationBuilder.addSubcommandGroups(declaration: CommandDeclarationBuilder, signature: AtomicInteger, createdExecutors: MutableList<SlashCommandExecutor>, i18nContext: I18nContext) {
        for (group in declaration.subcommandGroups) {
            subcommandGroup(group.labels.first(), i18nContext.get(declaration.description).shortenWithEllipsis()) {
                for (subcommand in group.subcommands) {
                    subcommands.add(
                        convertCommandDeclarationToSlashCommand(
                            subcommand,
                            subcommand.executor!!,
                            i18nContext,
                            signature,
                            createdExecutors
                        )
                    )
                }
            }
        }
    }

    fun buildDescription(i18nContext: I18nContext, declaration: CommandDeclarationBuilder) = buildString {
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
        append(declaration.category.getLocalizedName(i18nContext))
        append("」")
        // Looks better without this whitespace
        // append(" ")
        append(i18nContext.get(declaration.description))
    }.shortenWithEllipsis()
}