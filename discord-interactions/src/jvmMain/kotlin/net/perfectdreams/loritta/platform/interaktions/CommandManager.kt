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
import net.perfectdreams.loritta.common.commands.options.CommandOptionType
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
                declarationExecutor!!,
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
        declarationExecutor: CommandExecutorDeclaration,
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
        declarationExecutor: CommandExecutorDeclaration,
        locale: BaseLocale,
        signature: AtomicInteger,
        createdExecutors: MutableList<SlashCommandExecutor>
    ): SlashCommandDeclarationBuilder {
        val executor = executors.firstOrNull { declarationExecutor.parent == it::class } ?: throw UnsupportedOperationException("Something went wrong!")
        val rootSignature = signature.getAndIncrement()

        val interaKTionsExecutor = object : SlashCommandExecutor() {
            override suspend fun execute(context: SlashCommandContext, args: SlashCommandArguments) {
                println("Executed something $rootSignature")

                val cinnamonArgs = args.types.map { entry ->
                    val result = declarationExecutor.options.arguments.first { it.name == entry.key.name }
                    val result2 = (result to entry.value)
                    result2
                }.toMap()

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
        val interaKTionsOptions = object: CommandOptions() {
            init {
                declarationExecutor.options.arguments.forEach {
                    val arg = when (it.type) {
                        is CommandOptionType.String -> string(
                            it.name,
                            locale[it.description].shortenWithEllipsis()
                        )

                        is CommandOptionType.NullableString -> optionalString(
                            it.name,
                            locale[it.description].shortenWithEllipsis()
                        )

                        is CommandOptionType.Integer -> integer(
                            it.name,
                            locale[it.description].shortenWithEllipsis()
                        )

                        is CommandOptionType.NullableInteger -> optionalInteger(
                            it.name,
                            locale[it.description].shortenWithEllipsis()
                        )

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

        val interaKTionsExecutorDeclaration = object : SlashCommandExecutorDeclaration(rootSignature) {
            override val options = interaKTionsOptions
        }

        createdExecutors.add(interaKTionsExecutor)

        return slashCommand(declaration.labels.first()) {
            description = locale[declaration.description!!].shortenWithEllipsis()
            this.executor = interaKTionsExecutorDeclaration

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
    }

    fun String.shortenWithEllipsis(): String {
        if (this.length >= 100)
            return this.take(97) + "..."
        return this
    }
}