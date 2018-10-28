package com.mrpowergamerbr.loritta.commands

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.Instant

abstract class AbstractCommand(open val label: String, var aliases: List<String> = listOf(), var category: CommandCategory, var lorittaPermissions: List<LorittaPermission> = listOf(), val onlyOwner: Boolean = false) {
	companion object {
		val logger = LoggerFactory.getLogger(AbstractCommand::class.java)
	}

	val cooldown: Int
		get() = if (needsToUploadFiles()) 10000 else 5000

	var executedCount = 0

	open fun getDescription(locale: BaseLocale): String {
		return "Insira descrição do comando aqui!"
	}

	open fun getUsage(): String? {
		return null
	}

	open fun getDetailedUsage(): Map<String, String> {
		return mapOf()
	}

	@Deprecated("Please use getExample(locale)")
	open fun getExample(): List<String> {
		return getExample(loritta.getLocaleById("default"))
	}

	open fun getExample(locale: BaseLocale): List<String> {
		return listOf()
	}

	open fun getExtendedExamples(): Map<String, String> {
		return mapOf()
	}

	open fun hasCommandFeedback(): Boolean {
		return true
	}

	open fun getExtendedDescription(): String? {
		return null
	}

	open fun needsToUploadFiles(): Boolean {
		return false
	}

	open fun canUseInPrivateChannel(): Boolean {
		return true
	}

	/**
	 * Returns the required permissions needed for the user to use this command
	 *
	 * @return the required permissions list
	 */
	open fun getDiscordPermissions(): List<Permission> {
		return listOf()
	}

	/**
	 * Returns the required permissions needed for me to use this command
	 *
	 * @return the required permissions list
	 */
	open fun getBotPermissions(): List<Permission> {
		return listOf()
	}

	/**
	 * Retorna se o comando precisa ter o sistema de música ativado

	 * @return Se o comando precisa ter o sistema de música ativado
	 */
	open fun requiresMusicEnabled(): Boolean {
		return false
	}

	/**
	 * What the command should do when it is executed
	 *
	 * @param context the context of the command
	 * @param locale  the language the command should use
	 */
	abstract suspend fun run(context: CommandContext, locale: BaseLocale)

	/**
	 * Sends an embed explaining what the command does
	 *
	 * @param context the context of the command
	 */
	suspend fun explain(context: CommandContext) {
		val conf = context.config
		val ev = context.event
		val locale = context.locale

		if (conf.explainOnCommandRun) {
			val rawArguments = context.message.contentRaw.split(" ")
			var commandLabel = rawArguments[0]
			if (rawArguments.getOrNull(1) != null && (rawArguments[0] == "<@${Loritta.config.clientId}>" || rawArguments[0] == "<@!${Loritta.config.clientId}>")) {
				// Caso o usuário tenha usado "@Loritta comando", pegue o segundo argumento (no caso o "comando") em vez do primeiro (que é a mention da Lori)
				commandLabel = rawArguments[1]
			}
			commandLabel = commandLabel.toLowerCase()

			val embed = EmbedBuilder()
			embed.setColor(Color(0, 193, 223))
			embed.setTitle("\uD83E\uDD14 `$commandLabel`")

			val usage = if (getUsage() != null) " `${getUsage()}`" else ""

			var cmdInfo = getDescription(context.locale) + "\n\n"

			cmdInfo += "\uD83D\uDC81 **" + locale["HOW_TO_USE"] + ":** " + commandLabel + usage + "\n"

			if (!this.getDetailedUsage().isEmpty()) {
				for ((key, value) in this.getDetailedUsage()) {
					cmdInfo += "${Constants.LEFT_PADDING} `$key` - $value\n"
				}
			}

			cmdInfo += "\n"

			// Criar uma lista de exemplos
			val examples = ArrayList<String>()
			for (example in this.getExample()) { // Adicionar todos os exemplos simples
				examples.add(commandLabel + if (example.isEmpty()) "" else " `$example`")
			}
			for ((key, value) in this.getExtendedExamples()) { // E agora vamos adicionar os exemplos mais complexos/extendidos
				examples.add(commandLabel + if (key.isEmpty()) "" else " `$key` - **$value**")
			}

			if (examples.isEmpty()) {
				cmdInfo += "\uD83D\uDCD6 **" + context.locale["EXAMPLE"] + ":**\n" + commandLabel
			} else {
				cmdInfo += "\uD83D\uDCD6 **" + context.locale["EXAMPLE"] + (if (this.getExample().size == 1) "" else "s") + ":**"
				for (example in examples) {
					cmdInfo += "\n" + example
				}
			}

			val aliases = mutableSetOf<String>()
			aliases.add(this.label)
			aliases.addAll(this.aliases)

			val onlyUnusedAliases = aliases.filter { it != commandLabel.replaceFirst(context.config.commandPrefix, "") }
			if (onlyUnusedAliases.isNotEmpty()) {
				cmdInfo += "\n\n\uD83D\uDD00 **${context.locale["CommandAliases"]}:**\n${onlyUnusedAliases.joinToString(", ", transform = { context.config.commandPrefix + it })}"
			}

			embed.setDescription(cmdInfo)
			embed.setAuthor("${context.userHandle.name}#${context.userHandle.discriminator}", null, ev.author.effectiveAvatarUrl)
			embed.setFooter(context.locale[this.category.fancyTitle], "${Loritta.config.websiteUrl}assets/img/loritta_gabizinha_v1.png") // Mostrar categoria do comando
			embed.setTimestamp(Instant.now())

			if (conf.explainInPrivate) {
				ev.author.openPrivateChannel().queue {
					it.sendMessage(embed.build()).queue()
				}
			} else {
				context.sendMessage(context.getAsMention(true), embed.build())
			}
		}
	}
}