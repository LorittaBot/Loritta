package net.perfectdreams.loritta.cinnamon.platform.commands

import dev.kord.common.entity.Snowflake
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.commands.CommandManager
import net.perfectdreams.discordinteraktions.common.commands.CommandRegistry
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutor
import net.perfectdreams.discordinteraktions.common.commands.slashCommand
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandOptionsWrapper
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickBaseExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.ButtonClickWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.SelectMenuBaseExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.SelectMenuExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.components.SelectMenuWithDataExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.buttons.ButtonClickWithDataExecutorWrapper
import net.perfectdreams.loritta.cinnamon.platform.components.selects.SelectMenuWithDataExecutorWrapper

class CommandRegistry(
    val loritta: LorittaCinnamon,
    val interaKTionsManager: CommandManager,
    val interaKTionsRegistry: CommandRegistry
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val MAX_COMMAND_DESCRIPTION_LENGTH = 100
    }

    val declarations = mutableListOf<SlashCommandDeclarationBuilder>()
    val executors = mutableListOf<net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor>()

    val selectMenusDeclarations = mutableListOf<SelectMenuExecutorDeclaration>()
    val selectMenusExecutors = mutableListOf<SelectMenuBaseExecutor>()

    val buttonsDeclarations = mutableListOf<ButtonClickExecutorDeclaration>()
    val buttonsExecutors = mutableListOf<ButtonClickBaseExecutor>()

    fun register(declaration: net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper, vararg executors: net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor) {
        declarations.add(declaration.declaration())
        this.executors.addAll(executors)
    }

    fun register(declaration: ButtonClickExecutorDeclaration, executor: ButtonClickBaseExecutor) {
        buttonsDeclarations.add(declaration)
        buttonsExecutors.add(executor)
    }

    fun register(declaration: SelectMenuExecutorDeclaration, executor: SelectMenuBaseExecutor) {
        selectMenusDeclarations.add(declaration)
        selectMenusExecutors.add(executor)
    }

    suspend fun convertToInteraKTions(locale: I18nContext) {
        convertCommandsToInteraKTions(locale)
        convertSelectMenusToInteraKTions()
        convertButtonsToInteraKTions()

        if (loritta.interactionsConfig.registerGlobally) {
            interaKTionsRegistry.updateAllGlobalCommands(true)
        } else {
            for (guildId in loritta.interactionsConfig.guildsToBeRegistered) {
                interaKTionsRegistry.updateAllCommandsInGuild(Snowflake(guildId), true)
            }
        }
    }

    private fun convertCommandsToInteraKTions(locale: I18nContext) {
        for (declaration in declarations) {
            val declarationExecutor = declaration.executor

            val (declaration, executors) = convertCommandDeclarationToInteraKTions(
                declaration,
                declarationExecutor,
                locale
            )

            interaKTionsManager.register(
                declaration,
                *executors.toTypedArray()
            )
        }
    }

    private fun convertSelectMenusToInteraKTions() {
        for (declaration in selectMenusDeclarations) {
            val executor = selectMenusExecutors.firstOrNull { declaration.parent == it::class }
                ?: throw UnsupportedOperationException("The select menu executor wasn't found! Did you register the select menu executor?")

            if (executor is SelectMenuWithDataExecutor) {
                val interaKTionsExecutor = SelectMenuWithDataExecutorWrapper(
                    loritta,
                    declaration,
                    executor
                )

                val interaKTionsExecutorDeclaration = object : net.perfectdreams.discordinteraktions.common.components.SelectMenuExecutorDeclaration(
                    declaration::class,
                    declaration.id.value
                ) {}

                interaKTionsManager.register(
                    interaKTionsExecutorDeclaration,
                    interaKTionsExecutor
                )
            }
        }
    }

    private fun convertButtonsToInteraKTions() {
        for (declaration in buttonsDeclarations) {
            val executor = buttonsExecutors.firstOrNull { declaration.parent == it::class }
                ?: throw UnsupportedOperationException("The button click executor wasn't found! Did you register the button click executor?")

            val interaKTionsExecutor = ButtonClickWithDataExecutorWrapper(
                loritta,
                declaration,
                executor as ButtonClickWithDataExecutor
            )

            val interaKTionsExecutorDeclaration = object : net.perfectdreams.discordinteraktions.common.components.ButtonClickExecutorDeclaration(
                declaration::class,
                declaration.id.value
            ) {}

            interaKTionsManager.register(
                interaKTionsExecutorDeclaration,
                interaKTionsExecutor
            )
        }
    }

    private fun convertCommandDeclarationToInteraKTions(
        declaration: SlashCommandDeclarationBuilder,
        declarationExecutor: SlashCommandExecutorDeclaration?,
        locale: I18nContext
    ): Pair<SlashCommandDeclarationWrapper, List<SlashCommandExecutor>> {
        val executors = mutableListOf<SlashCommandExecutor>()

        val declaration = convertCommandDeclarationToSlashCommand(
            declaration,
            declarationExecutor,
            locale,
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
        declaration: SlashCommandDeclarationBuilder,
        declarationExecutor: SlashCommandExecutorDeclaration?,
        locale: I18nContext,
        createdExecutors: MutableList<SlashCommandExecutor>
    ): SlashCommandDeclaration {
        if (declarationExecutor != null) {
            val executor = executors.firstOrNull { declarationExecutor.parent == it::class }
                ?: throw UnsupportedOperationException("The command executor ${declarationExecutor.parent} wasn't found! Did you register the command executor?")

            val interaKTionsExecutor = SlashCommandExecutorWrapper(
                loritta,
                declaration,
                declarationExecutor,
                executor
            )

            // Register all the command options with Discord InteraKTions
            val interaKTionsOptions = SlashCommandOptionsWrapper(
                declarationExecutor,
                locale
            )

            val interaKTionsExecutorDeclaration = object : net.perfectdreams.discordinteraktions.common.commands.SlashCommandExecutorDeclaration(declarationExecutor::class) {
                override val options = interaKTionsOptions
            }

            createdExecutors.add(interaKTionsExecutor)

            return slashCommand(declaration.labels.first(), buildDescription(locale, declaration)) {
                this.executor = interaKTionsExecutorDeclaration

                if (declaration.subcommands.isNotEmpty() || declaration.subcommandGroups.isNotEmpty())
                    logger.warn { "Executor ${executor::class.simpleName} is set to ${declaration.labels.first()}'s root, but the command has subcommands and/or subcommand groups! Due to Discord's limitations the root command won't be usable!" }

                addSubcommandGroups(declaration, createdExecutors, locale)

                for (subcommand in declaration.subcommands) {
                    subcommands.add(
                        convertCommandDeclarationToSlashCommand(
                            subcommand,
                            subcommand.executor!!,
                            locale,
                            createdExecutors
                        )
                    )
                }
            }
        } else {
            return slashCommand(declaration.labels.first(), buildDescription(locale, declaration)) {
                addSubcommandGroups(declaration, createdExecutors, locale)

                for (subcommand in declaration.subcommands) {
                    subcommands.add(
                        convertCommandDeclarationToSlashCommand(
                            subcommand,
                            subcommand.executor,
                            locale,
                            createdExecutors
                        )
                    )
                }
            }
        }
    }

    private fun net.perfectdreams.discordinteraktions.common.commands.SlashCommandDeclarationBuilder.addSubcommandGroups(declaration: SlashCommandDeclarationBuilder, createdExecutors: MutableList<SlashCommandExecutor>, i18nContext: I18nContext) {
        for (group in declaration.subcommandGroups) {
            subcommandGroup(group.labels.first(), i18nContext.get(declaration.description).shortenWithEllipsis(MAX_COMMAND_DESCRIPTION_LENGTH)) {
                for (subcommand in group.subcommands) {
                    subcommands.add(
                        convertCommandDeclarationToSlashCommand(
                            subcommand,
                            subcommand.executor!!,
                            i18nContext,
                            createdExecutors
                        )
                    )
                }
            }
        }
    }

    fun buildDescription(i18nContext: I18nContext, declaration: SlashCommandDeclarationBuilder) = buildString {
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
            CommandCategory.SOCIAL -> "\uD83D\uDDE3️"
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
    }.shortenWithEllipsis(MAX_COMMAND_DESCRIPTION_LENGTH)
}