package com.mrpowergamerbr.loritta.commands

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

data class CommandArguments(private val arguments: List<CommandArgument>) {
	fun build(locale: BaseLocale): String {
		val builder = StringBuilder()
		for (argument in arguments) {
			if (argument.optional)
				builder.append('[')
			else
				builder.append('<')
			builder.append(argument.type.localized(locale))
			if (argument.optional)
				builder.append(']')
			else
				builder.append('>')
			builder.append(' ')
		}
		return builder.toString().trim()
	}
}

data class CommandArgument(val type: ArgumentType, val optional: Boolean)

enum class ArgumentType {
	TEXT;

	fun localized(locale: BaseLocale) {
		when (this) {
			TEXT -> locale.commands.arguments.text
		}
	}
}

fun arguments(block: CommandArgumentsBuilder.() -> Unit): CommandArguments = CommandArgumentsBuilder().apply(block).build()

class CommandArgumentsBuilder {
	private val arguments = mutableListOf<CommandArgument>()

	fun argument(type: ArgumentType, optional: Boolean) {
		arguments.add(CommandArgument(type, optional))
	}

	fun build(): CommandArguments = CommandArguments(arguments)
}