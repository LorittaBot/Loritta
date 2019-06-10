package net.perfectdreams.loritta.platform.console.commands

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.remove
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.commands.LorittaCommandManager
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.platform.console.ConsoleLoritta
import net.perfectdreams.loritta.platform.console.entities.ConsoleUser
import net.perfectdreams.loritta.utils.Emotes
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class ConsoleCommandManager(val consoleLoritta: ConsoleLoritta) : LorittaCommandManager(consoleLoritta) {
    init {
        contextManager.registerContext<User>(
                { clazz: KClass<*> -> clazz.isSubclassOf(User::class) || clazz == User::class },
                { sender, clazz, stack ->
                    val link = stack.pop() // Ok, será que isto é uma URL?

                    if (link.startsWith("user:")) {
                        return@registerContext ConsoleUser(link.replace("user:", ""))
                    }

                    return@registerContext null
                }
        )
    }

    suspend fun dispatch(_rawMessage: String, locale: BaseLocale, legacyLocale: LegacyBaseLocale): Boolean {
        val rawMessage = _rawMessage

        // É necessário remover o new line para comandos como "+eval", etc
        val rawArguments = rawMessage.replace("\n", "").split(" ")

        // Primeiro os comandos vanilla da Loritta(tm)
        for (command in getRegisteredCommands()) {
            if (verifyAndDispatch(rawMessage, command, rawArguments, locale, legacyLocale))
                return true
        }

        return false
    }

    suspend fun verifyAndDispatch(_rawMessage: String, command: LorittaCommand, rawArguments: List<String>, locale: BaseLocale, legacyLocale: LegacyBaseLocale): Boolean {
        for (subCommand in command.subcommands) {
            if (dispatch(_rawMessage, subCommand as LorittaCommand, rawArguments.drop(1).toMutableList(), locale, legacyLocale, true))
                return true
        }

        if (dispatch(_rawMessage, command, rawArguments, locale, legacyLocale, false))
            return true

        return false
    }

    suspend fun dispatch(_rawMessage: String, command: LorittaCommand, rawArguments: List<String>, locale: BaseLocale, legacyLocale: LegacyBaseLocale, isSubcommand: Boolean): Boolean {
        val message = _rawMessage

        val labels = command.labels.toMutableList()

        // println("Labels de $command: $labels")
        // if (cmdOptions.enableCustomAliases) // Adicionar labels customizadas no painel
        // 	labels.addAll(cmdOptions.aliases)

        // ignoreCase = true ~ Permite usar "+cOmAnDo"
        val valid = labels.any { rawArguments[0].equals(it, true) }

        if (valid) {
            val start = System.currentTimeMillis()

            val args = message.stripCodeMarks().split(" ").toTypedArray().remove(0)
            val rawArgs = message.stripCodeMarks().split(" ").toTypedArray().remove(0)
            val strippedArgs = message.stripCodeMarks().split(" ").toTypedArray().remove(0)

            val legacyLocale = legacyLocale

            val context = ConsoleCommandContext(consoleLoritta, locale, legacyLocale, command, args)

            try {
                command.executedCount++

                if (args.isNotEmpty() && args[0] == "🤷") { // Usar a ajuda caso 🤷 seja usado
                    context.explain()
                    return true
                }

                val result = execute(context, command, rawArgs)
                return result
            } catch (e: Exception) {
                logger.error("Exception ao executar comando ${command.javaClass.simpleName}", e)

                // Avisar ao usuário que algo deu muito errado
                var reply = "\uD83E\uDD37 **|** " + locale["commands.errorWhileExecutingCommand", Emotes.LORI_RAGE, Emotes.LORI_CRYING]

                if (!e.message.isNullOrEmpty())
                    reply += " `${e.message!!.escapeMentions()}`"
                return true
            }
        }
        return false
    }
}