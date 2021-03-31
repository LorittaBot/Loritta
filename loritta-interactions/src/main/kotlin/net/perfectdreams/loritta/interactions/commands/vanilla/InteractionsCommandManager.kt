package net.perfectdreams.loritta.interactions.commands.vanilla

import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.perfectdreams.discordinteraktions.commands.SlashCommand
import net.perfectdreams.discordinteraktions.context.SlashCommandContext
import net.perfectdreams.discordinteraktions.declarations.slash.CommandOption
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandGroupDeclaration
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.CommandManager
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.api.commands.declarations.IntegerCommandChoice
import net.perfectdreams.loritta.api.commands.declarations.StringCommandChoice
import net.perfectdreams.loritta.interactions.LorittaInteractions
import net.perfectdreams.loritta.interactions.internal.commands.DummyMessage
import net.perfectdreams.loritta.interactions.internal.commands.InteraKTionsChannel
import net.perfectdreams.loritta.interactions.internal.commands.InteractionsCommandContext
import net.perfectdreams.loritta.interactions.internal.commands.InteractionsOptionsManager
import net.perfectdreams.loritta.utils.locale.BaseLocale

class InteractionsCommandManager(val m: LorittaInteractions) : CommandManager<LorittaCommand<CommandContext>> {
    val commands = mutableListOf<LorittaCommand<CommandContext>>()

    override fun register(command: LorittaCommand<CommandContext>) {
        commands.add(command)
    }

    override fun unregister(command: LorittaCommand<CommandContext>) {
        commands.remove(command)
    }

    private fun convertDeclarationToInteraKTionsDeclaration(
        locale: BaseLocale,
        lorittaDeclaration: CommandDeclaration,
        declarations: MutableMap<CommandDeclaration, SlashCommandDeclaration>
    ): SlashCommandDeclaration {
        if (declarations.containsKey(lorittaDeclaration))
            return declarations.get(lorittaDeclaration)!!

        val options = object: SlashCommandDeclaration.Options() {}

        for (lorittaGroup in lorittaDeclaration.options.subcommandGroups) {
            val group = object: SlashCommandGroupDeclaration(
                lorittaGroup.name,
                locale[lorittaGroup.description]
            ) {}

            lorittaGroup.subcommands.forEach {
                group.subcommands.add(
                    convertDeclarationToInteraKTionsDeclaration(
                        locale,
                        it,
                        declarations
                    )
                )
            }

            options.subcommandGroups.add(group)
        }

        for (subcommand in lorittaDeclaration.options.subcommands) {
            options.subcommands.add(
                convertDeclarationToInteraKTionsDeclaration(
                    locale,
                    subcommand,
                    declarations
                )
            )
        }

        for (option in lorittaDeclaration.options.arguments) {
            options.arguments.add(
                CommandOption<Any>(
                    option.type,
                    option.name,
                    locale[option.description],
                    option.required,
                    option.choices.map {
                        if (it is StringCommandChoice) {
                            net.perfectdreams.discordinteraktions.declarations.slash.StringCommandChoice(
                                it.name,
                                it.value
                            )
                        } else if (it is IntegerCommandChoice) {
                            net.perfectdreams.discordinteraktions.declarations.slash.IntegerCommandChoice(
                                it.name,
                                it.value
                            )
                        } else throw RuntimeException()
                    }
                )
            )
        }

        // Convert Loritta Command Type to Discord InteraKTions Command Type
        val declaration = object: SlashCommandDeclaration(
            lorittaDeclaration.name,
            // There is a 100 chars limit for the description
            locale[lorittaDeclaration.description].take(100)
        ) {
            override val options = options
        }

        declarations[lorittaDeclaration] = declaration

        return declaration
    }

    suspend fun registerDiscord() {
        val locale = m.localeManager.getLocaleById("default")

        // This is to avoid having multiple declarations created, which causes issues with the same declaration having multiple objects
        val declarations = mutableMapOf<CommandDeclaration, SlashCommandDeclaration>()

        val interaktionsCommands = commands.map {
            // Now we need to get the child declaration from here...
            SlashCommandWrapper(
                it,
                convertDeclarationToInteraKTionsDeclaration(locale, it.declaration, declarations),
                convertDeclarationToInteraKTionsDeclaration(locale, it.rootDeclaration, declarations)
            )
        }

        for (command in interaktionsCommands) {
            // Trying to register directly causes a "NoClassDefFoundError" because of the anonymous class (but why?)
            m.server.commandManager.register(command)
        }

        m.server.commandManager.updateAllCommandsInGuild(
            Snowflake(297732013006389252L),
            deleteUnknownCommands = true
        )
    }

    inner class SlashCommandWrapper(val command: LorittaCommand<CommandContext>, declaration: SlashCommandDeclaration, rootDeclaration: SlashCommandDeclaration) : SlashCommand(declaration, rootDeclaration) {
        override suspend fun executes(context: SlashCommandContext) {
            GlobalScope.launch {
                // Weird workaround:tm:, automatic defer
                delay(2_000)
                if (!context.isDeferred) {
                    println("Command $command took too long to defer! Deferring automatically...")
                    context.defer()
                }
            }

            command.executes(
                InteractionsCommandContext(
                    context,
                    m,
                    command,
                    InteractionsOptionsManager(context),
                    DummyMessage(
                        InteraKTionsChannel(
                            context
                        )
                    ),
                    m.localeManager.getLocaleById("default")
                )
            )
        }
    }
}