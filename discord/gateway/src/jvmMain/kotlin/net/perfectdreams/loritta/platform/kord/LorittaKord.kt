package net.perfectdreams.loritta.platform.kord

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.commands.`fun`.CoinFlipExecutor
import net.perfectdreams.loritta.commands.`fun`.RateWaifuExecutor
import net.perfectdreams.loritta.commands.`fun`.declarations.CoinFlipCommand
import net.perfectdreams.loritta.commands.`fun`.declarations.RateWaifuCommand
import net.perfectdreams.loritta.commands.misc.PingAyayaExecutor
import net.perfectdreams.loritta.commands.misc.PingExecutor
import net.perfectdreams.loritta.commands.misc.declarations.PingCommand
import net.perfectdreams.loritta.commands.utils.HelpExecutor
import net.perfectdreams.loritta.commands.utils.declarations.HelpCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.options.CommandOptionType
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclarationBuilder
import net.perfectdreams.loritta.common.commands.options.CommandOption
import net.perfectdreams.loritta.common.locale.LocaleManager
import net.perfectdreams.loritta.common.utils.ConfigUtils
import net.perfectdreams.loritta.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.discord.LorittaDiscord
import net.perfectdreams.loritta.discord.LorittaDiscordConfig
import net.perfectdreams.loritta.platform.kord.commands.KordCommandContext
import net.perfectdreams.loritta.platform.kord.entities.KordMessageChannel
import net.perfectdreams.loritta.platform.kord.entities.KordUser
import java.io.File

class LorittaKord(config: LorittaConfig, discordConfig: LorittaDiscordConfig): LorittaDiscord(config, discordConfig) {
    val commandManager = CommandManager()
    val localeManager = LocaleManager(
        ConfigUtils.localesFolder
    )

    fun parseArgs(content: String, args: List<CommandOption<*>>): MutableMap<CommandOption<*>, Any?> {
        // --ayaya_count=5
        val regex = Regex("--([A-z_]+)=([A-z0-9_]+)")
        val matchedArgs = regex.findAll(content)

        val argsResults = mutableMapOf<CommandOption<*>, Any?>()

        matchedArgs.forEach {
            val name = it.groupValues[1]
            val value = it.groupValues[2]

            val result = args.firstOrNull { it.name == name } ?: return@forEach

            val convertedArgument = when (result.type) {
                is CommandOptionType.String, CommandOptionType.NullableString -> value
                is CommandOptionType.Integer -> value.toInt()
                is CommandOptionType.NullableInteger -> value.toIntOrNull()
                else -> throw UnsupportedOperationException("I don't know how to convert ${result.type}!")
            }

            argsResults[result] = convertedArgument
        }

        return argsResults
    }

    suspend fun executeCommand(event: MessageCreateEvent, content: String, declaration: CommandDeclarationBuilder): Boolean {
        val split = content.split(" ")

        val label = declaration.labels.first()

        if (label == split[0]) {
            for (subcommand in declaration.subcommands) {
                if (executeCommand(event, split.drop(1).joinToString(" "), subcommand))
                    return true
            }

            val executor = commandManager.executors.first {
                it::class == declaration.executor?.parent
            }

            val args = parseArgs(split.drop(1).joinToString(" "), declaration.executor?.options?.arguments ?: listOf())

            executor.execute(
                KordCommandContext(
                    this,
                    localeManager.getLocaleById("default"),
                    KordUser(event.message.author!!),
                    KordMessageChannel(event.message.getChannel())
                ),
                CommandArguments(args)
            )
            return true
        }

        return false
    }

    fun start() {
        localeManager.loadLocales()

        commandManager.register(
            PingCommand,
            PingExecutor(),
            PingAyayaExecutor(emotes)
        )

        commandManager.register(
            CoinFlipCommand,
            CoinFlipExecutor(emotes, random)
        )

        commandManager.register(
            RateWaifuCommand,
            RateWaifuExecutor(emotes)
        )

        commandManager.register(
            HelpCommand,
            HelpExecutor(emotes)
        )

        runBlocking {
            val client = Kord(discordConfig.token)

            client.on<MessageCreateEvent> {
                try {
                    if (this.message.content.startsWith("!!")) {
                        val contentWithoutPrefix = this.message.content.removePrefix("!!")

                        for (declaration in commandManager.declarations) {
                            if (executeCommand(this, contentWithoutPrefix, declaration))
                                return@on
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            client.login()
        }
    }
}