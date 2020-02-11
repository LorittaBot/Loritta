package net.perfectdreams.loritta.platform.nodejs.commands

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import eris.Message
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.CommandMap
import net.perfectdreams.loritta.platform.nodejs.LorittaNodeJS
import net.perfectdreams.loritta.platform.nodejs.entities.ErisMessage

class JSCommandMap(val loritta: LorittaNodeJS) : CommandMap<Command<CommandContext>> {
	val commands = mutableListOf<Command<CommandContext>>()

	override fun register(command: Command<CommandContext>) {
		commands.add(command)
	}

	override fun unregister(command: Command<CommandContext>) {
		commands.remove(command)
	}

	suspend fun dispatch(message: Message): Boolean {
		val rawMessage = message.content

		// É necessário remover o new line para comandos como "+eval", etc
		val rawArguments = rawMessage.replace("\n", "").split(" ")

		// Primeiro os comandos vanilla da Loritta(tm)
		for (command in commands) {
			if (dispatch(command, rawArguments, message, BaseLocale("default")))
				return true
		}

		return false
	}

	suspend fun dispatch(command: Command<CommandContext>, rawArguments: List<String>, message: Message, locale: BaseLocale): Boolean {
		val content = message.content

		var prefix = "!!"

		val labels = command.labels.toMutableList()

		// Comandos com espaços na label, yeah!
		var valid = false

		val checkArguments = rawArguments.toMutableList()
		val rawArgument0 = checkArguments.getOrNull(0)
		var removeArgumentCount = 0
		val byMention = false

		if (byMention) {
			removeArgumentCount++
			checkArguments.removeAt(0)
			prefix = ""
		}

		for (label in labels) {
			val subLabels = label.split(" ")

			removeArgumentCount = if (byMention) { 1 } else { 0 }
			var validLabelCount = 0

			for ((index, subLabel) in subLabels.withIndex()) {
				val rawArgumentAt = checkArguments.getOrNull(index) ?: break

				val subLabelPrefix = if (index == 0)
					prefix
				else
					""

				if (rawArgumentAt.equals(subLabelPrefix + subLabel, true)) { // ignoreCase = true ~ Permite usar "+cOmAnDo"
					validLabelCount++
					removeArgumentCount++
				}
			}

			if (validLabelCount == subLabels.size) {
				valid = true
				break
			}
		}

		if (valid) {
			val rawArgs = content.split(" ").toMutableList()

			repeat(removeArgumentCount) {
				rawArgs.removeAt(0)
			}

			val context = JSCommandContext(
					loritta,
					command,
					rawArgs,
					ErisMessage(message),
					locale
			)

			command.executor.invoke(context)
		}
		return false
	}
}