package net.perfectdreams.loritta.api.commands

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

data class CommandArguments(val arguments: List<CommandArgument>) {
	fun build(locale: BaseLocale): String {
		val builder = StringBuilder()
		for (argument in arguments) {
			argument.build(builder, locale)
			builder.append(' ')
		}
		return builder.toString().trim()
	}
}

data class CommandArgument(
		val type: ArgumentType,
		val optional: Boolean,
		val defaultValue: String? = null,
		val text: String? = null,
		val explanation: String? = null
) {
	fun build(locale: BaseLocale): String {
		return build(StringBuilder(), locale).toString()
	}

	fun build(builder: StringBuilder, locale: BaseLocale): StringBuilder {
		if (this.optional)
			builder.append('[')
		else
			builder.append('<')
		builder.append(this.text ?: this.type.localized(locale))
		if (this.optional)
			builder.append(']')
		else
			builder.append('>')
		return builder
	}
}

enum class ArgumentType {
	TEXT,
	NUMBER,
	USER,
	ROLE,
	COLOR,
	EMOTE,
	IMAGE;

	fun localized(locale: BaseLocale): String {
		return when (this) {
			TEXT   -> locale["commands.arguments.text"]
			NUMBER -> locale["commands.arguments.number"]
			USER   -> locale["commands.arguments.user"]
			EMOTE  -> locale["commands.arguments.emote"]
			IMAGE  -> locale["commands.arguments.image"]
			ROLE   -> locale["commands.arguments.role"]
			COLOR  -> locale["commands.argument.color"]
		}
	}
}

fun arguments(block: CommandArgumentsBuilder.() -> Unit): CommandArguments = CommandArgumentsBuilder().apply(block).build()

class CommandArgumentsBuilder {
	private val arguments = mutableListOf<CommandArgument>()

	fun argument(type: ArgumentType, block: CommandArgumentBuilder.() -> Unit) = arguments.add(CommandArgumentBuilder().apply(block).build(type))

	fun build(): CommandArguments = CommandArguments(arguments)
}

class CommandArgumentBuilder {
	var optional = false
	var defaultValue: String? = null
	var text: String? = null
	var explanation: Any? = null

	fun build(type: ArgumentType): CommandArgument = CommandArgument(type, optional, defaultValue, text, explanation?.toString())
}