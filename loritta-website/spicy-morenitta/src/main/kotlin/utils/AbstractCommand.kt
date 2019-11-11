package utils

class AbstractCommand(
		val name: String,
		val label: String,
		val aliases: Array<String>,
		val category: CommandCategory,
		val description: String,
		val usage: String?,
		val detailedUsage: Map<String, String>,
		val example: Array<String>,
		val extendedExamples: Map<String, String>,
		val requiredUserPermissions: Array<String>,
		val requiredBotPermissions: Array<String>
)