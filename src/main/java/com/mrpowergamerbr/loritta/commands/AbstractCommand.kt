package com.mrpowergamerbr.loritta.commands

import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import org.slf4j.LoggerFactory

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

	open fun getExample(): List<String> {
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
	fun explain(context: CommandContext) {
	}
}