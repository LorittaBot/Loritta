package net.perfectdreams.loritta.platform.frontend.commands

import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.CommandMap
import net.perfectdreams.loritta.platform.frontend.entities.JSMessage

class JSCommandMap : CommandMap<Command<CommandContext>> {
	val commands = mutableMapOf<String, Command<CommandContext>>()

	override fun register(command: Command<CommandContext>) {
		for (label in command.labels)
			commands[label] = command
	}

	override fun unregister(command: Command<CommandContext>) {
		for (label in command.labels)
			commands.remove(label)
	}

	suspend fun dispatch(message: JSMessage): Boolean {
		val rawMessage = message.content

		// É necessário remover o new line para comandos como "+eval", etc
		val rawArguments = rawMessage.replace("\n", "").split(" ")

		// Primeiro os comandos vanilla da Loritta(tm)
		for (command in commands.values) {
			if (dispatch(command, rawArguments, message))
				return true
		}

		return false
	}

	suspend fun dispatch(command: Command<CommandContext>, rawArguments: List<String>, message: JSMessage): Boolean {
		val content = message.content
		val channel = message.channel
		val prefix = "+"

		val labels = command.labels.toMutableList()

		// Comandos com espaços na label, yeah!
		var valid = false

		val checkArguments = rawArguments.toMutableList()
		val rawArgument0 = checkArguments.getOrNull(0)
		var removeArgumentCount = 0
		val byMention = false

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
			val args = rawArguments.toMutableList()

			repeat(removeArgumentCount) {
				args.removeAt(0)
			}

			val context = JSCommandContext(
					args,
					message
			)

			try {
				command.executor.invoke(context)
				return true
			} catch (e: Exception) {
				// Avisar ao usuário que algo deu muito errado
				val mention = "${message.author.asMention}, algo deu muito errado, sorry ;w;"
				channel.sendMessage(mention)
				return true
			}
		}
		return false
	}
}