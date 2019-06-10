package net.perfectdreams.loritta.platform.amino.commands

import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.remove
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.perfectdreams.aminoreapi.events.message.MessageReceivedEvent
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.commands.LorittaCommandManager
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.platform.amino.AminoLoritta
import net.perfectdreams.loritta.platform.amino.entities.AminoUser
import net.perfectdreams.loritta.utils.Emotes
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class AminoCommandManager(val aminoLoritta: AminoLoritta) : LorittaCommandManager(aminoLoritta) {
    init {
        contextManager.registerContext<User>(
                { clazz: KClass<*> -> clazz.isSubclassOf(User::class) || clazz == User::class },
                { sender, clazz, stack ->
                    val link = stack.pop() // Ok, será que isto é uma URL?

                    if (link.startsWith("user:")) {
                        return@registerContext AminoUser(link.replace("user:", ""))
                    }

                    return@registerContext null
                }
        )
    }

    suspend fun dispatch(event: MessageReceivedEvent, locale: BaseLocale, legacyLocale: LegacyBaseLocale): Boolean {
        val rawMessage = event.message.content

        // É necessário remover o new line para comandos como "+eval", etc
        val rawArguments = rawMessage.replace("\n", "").split(" ")

        // Primeiro os comandos vanilla da Loritta(tm)
        for (command in getRegisteredCommands()) {
            if (verifyAndDispatch(event, rawMessage, command, rawArguments, locale, legacyLocale))
                return true
        }

        return false
    }

    suspend fun verifyAndDispatch(event: MessageReceivedEvent, _rawMessage: String, command: LorittaCommand, rawArguments: List<String>, locale: BaseLocale, legacyLocale: LegacyBaseLocale): Boolean {
        for (subCommand in command.subcommands) {
            if (dispatch(event, _rawMessage, subCommand as LorittaCommand, rawArguments.drop(1).toMutableList(), locale, legacyLocale, true))
                return true
        }

        if (dispatch(event, _rawMessage, command, rawArguments, locale, legacyLocale, false))
            return true

        return false
    }

    suspend fun dispatch(event: MessageReceivedEvent, _rawMessage: String, command: LorittaCommand, rawArguments: List<String>, locale: BaseLocale, legacyLocale: LegacyBaseLocale, isSubcommand: Boolean): Boolean {
        val message = _rawMessage

        val labels = command.labels.toMutableList()

        // println("Labels de $command: $labels")
        // if (cmdOptions.enableCustomAliases) // Adicionar labels customizadas no painel
        // 	labels.addAll(cmdOptions.aliases)

        // ignoreCase = true ~ Permite usar "+cOmAnDo"
        val valid = labels.any { rawArguments[0].equals("/" + it, true) }

        if (valid) {
            val start = System.currentTimeMillis()

            val args = message.stripCodeMarks().split(" ").toTypedArray().remove(0)
            val rawArgs = message.stripCodeMarks().split(" ").toTypedArray().remove(0)
            val strippedArgs = message.stripCodeMarks().split(" ").toTypedArray().remove(0)

            val legacyLocale = legacyLocale

            val context = AminoCommandContext(aminoLoritta, event, locale, legacyLocale, command, args)

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