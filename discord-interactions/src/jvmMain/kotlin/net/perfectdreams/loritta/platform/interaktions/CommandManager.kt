package net.perfectdreams.loritta.platform.interaktions

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
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOption
import net.perfectdreams.loritta.common.commands.options.CommandOptionType
import net.perfectdreams.loritta.common.commands.options.ListCommandOption
import net.perfectdreams.loritta.common.commands.options.ListCommandOptionBuilder
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.platform.interaktions.commands.InteraKTionsCommandContext
import net.perfectdreams.loritta.platform.interaktions.entities.InteraKTionsMessageChannel
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

            val interaKTionsExecutor = object : SlashCommandExecutor() {
                override suspend fun execute(context: SlashCommandContext, args: SlashCommandArguments) {
                    println("Executed something $rootSignature")

                    // Map Cinnamon Arguments to Discord InteraKTions Arguments
                    val cinnamonArgs = mutableMapOf<CommandOption<*>, Any?>()
                    val interaKTionsArgumentEntries = args.types.entries

                    declarationExecutor.options.arguments.forEach {
                        if (it.type is CommandOptionType.StringList) {
                            // Special case: Lists
                            val listsValues = interaKTionsArgumentEntries.filter { opt -> it.name.startsWith(it.name) }
                            cinnamonArgs[it] = mutableListOf<String>().also {
                                it.addAll(listsValues.map { it.value as String })
                            }
                        } else {
                            val interaKTionArgument =
                                interaKTionsArgumentEntries.firstOrNull { opt -> it.name == opt.key.name }
                            // If the value is null but it *wasn't* meant to be null, we are going to throw a exception!
                            // (This should NEVER happen!)
                            if (interaKTionArgument?.value == null && it.type !is CommandOptionType.Nullable)
                                throw UnsupportedOperationException("Argument ${interaKTionArgument?.key} valie is null, but the type of the argument is ${it.type}! Bug?")

                            cinnamonArgs[it] = interaKTionArgument?.value
                        }
                    }

                    executor.execute(
                        InteraKTionsCommandContext(
                            loritta,
                            locale,
                            InteraKTionsMessageChannel(context)
                        ),
                        CommandArguments(cinnamonArgs)
                    )
                }

                override fun signature() = rootSignature
            }

            // Register all the command options with Discord InteraKTions
            val interaKTionsOptions = object : CommandOptions() {
                init {
                    declarationExecutor.options.arguments.forEach {
                        if (it is ListCommandOption<*>) {
                            // String List is a special case due to Slash Commands not supporting varargs right now
                            // As a alternative, we will create from 1 up to 25 options
                            val requiredOptions = (it.minimum ?: 0).coerceAtMost(25)
                            val optionalOptions = ((it.maximum ?: 25) - requiredOptions).coerceAtMost(25)

                            var idx = 1

                            repeat(requiredOptions) { _ ->
                                string("${it.name}$idx", locale[it.description])
                                    .register()
                                idx++
                            }

                            repeat(optionalOptions) { _ ->
                                optionalString("${it.name}$idx", locale[it.description])
                                    .register()
                                idx++
                            }
                        } else {
                            val arg = when (it.type) {
                                is CommandOptionType.String -> string(
                                    it.name,
                                    locale[it.description].shortenWithEllipsis()
                                ).also { option ->
                                    it.choices.take(25).forEach {
                                        option.choice(it.value as String, locale[it.name])
                                    }
                                }

                                is CommandOptionType.NullableString -> optionalString(
                                    it.name,
                                    locale[it.description].shortenWithEllipsis()
                                ).also { option ->
                                    it.choices.take(25).forEach {
                                        option.choice(it.value as String, locale[it.name])
                                    }
                                }

                                is CommandOptionType.Integer -> integer(
                                    it.name,
                                    locale[it.description].shortenWithEllipsis()
                                ).also { option ->
                                    it.choices.take(25).forEach {
                                        option.choice(it.value as Int, locale[it.name])
                                    }
                                }

                                is CommandOptionType.NullableInteger -> optionalInteger(
                                    it.name,
                                    locale[it.description].shortenWithEllipsis()
                                ).also { option ->
                                    it.choices.take(25).forEach {
                                        option.choice(it.value as Int, locale[it.name])
                                    }
                                }

                                is CommandOptionType.Bool -> boolean(
                                    it.name,
                                    locale[it.description].shortenWithEllipsis()
                                )

                                is CommandOptionType.NullableBool -> optionalBoolean(
                                    it.name,
                                    locale[it.description].shortenWithEllipsis()
                                )

                                else -> throw UnsupportedOperationException("Unsupported option type ${it.type}")
                            }

                            arg.register()
                        }
                    }
                }
            }

            val interaKTionsExecutorDeclaration = object : SlashCommandExecutorDeclaration(rootSignature) {
                override val options = interaKTionsOptions
            }

            createdExecutors.add(interaKTionsExecutor)

            return slashCommand(declaration.labels.first()) {
                description = locale[declaration.description!!].shortenWithEllipsis()
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
                description = locale[declaration.description!!].shortenWithEllipsis()

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

    fun String.shortenWithEllipsis(): String {
        if (this.length >= 100)
            return this.take(97) + "..."
        return this
    }
}