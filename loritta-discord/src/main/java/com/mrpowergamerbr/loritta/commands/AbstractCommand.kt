package com.mrpowergamerbr.loritta.commands

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.extensions.localized
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.utils.Emotes
import java.awt.Color
import java.time.Instant

abstract class AbstractCommand(open val label: String, var aliases: List<String> = listOf(), var category: CommandCategory, var lorittaPermissions: List<LorittaPermission> = listOf(), val onlyOwner: Boolean = false) {
	@Transient
	internal val logger = KotlinLogging.logger {}

	val cooldown: Int
		get() {
			val customCooldown = loritta.config.loritta.commands.commandsCooldown[this::class.simpleName]

			if (customCooldown != null)
				return customCooldown

			return if (needsToUploadFiles())
				loritta.config.loritta.commands.imageCooldown
			else
				loritta.config.loritta.commands.cooldown
		}

	open val sendTypingStatus: Boolean
		get() {
			return cooldown > loritta.config.loritta.commands.imageCooldown
		}

	open fun getDescriptionKey() = Command.MISSING_DESCRIPTION_KEY

	open fun getDescription(locale: BaseLocale) = locale.get(getDescriptionKey())

	open fun getUsage(): CommandArguments {
		return arguments {}
	}

	open fun getDetailedUsage(): Map<String, String> {
		return mapOf()
	}

	@Deprecated("Please use getExamples(locale)")
	open fun getExamples(): List<String> {
		return getExamples(loritta.getLocaleById("default"))
	}

	open fun getExamples(locale: BaseLocale): List<String> {
		return listOf()
	}

	open fun getExtendedExamples(): Map<String, String> {
		return mapOf()
	}

	open fun hasCommandFeedback(): Boolean {
		return true
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
	 * Retorna um valor boolean para verificar se o usuário pode rodar o comando
	 *
	 * @return um valor boolean para verificar se o usuário pode rodar o comando
	 */
	open fun canHandle(context: CommandContext): Boolean {
		return true
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
		val serverConfig = context.config
		val user = context.userHandle
		val locale = context.locale
		val discordMessage = context.message
		val commandDescription = getDescription(locale)

		val executedCommandLabel = this.label
		val commandLabel = "${serverConfig.commandPrefix}${executedCommandLabel}"

		val embed = EmbedBuilder()
				.setColor(Constants.LORITTA_AQUA)
				.setAuthor(locale["commands.explain.clickHereToSeeAllMyCommands"], "${loritta.instanceConfig.loritta.website.url}commands", discordMessage.jda.selfUser.effectiveAvatarUrl)
				.setTitle("${Emotes.LORI_HM} `${serverConfig.commandPrefix}${executedCommandLabel}`")
				.setFooter("${user.name + "#" + user.discriminator} • ${this.category.getLocalizedName(locale)}", user.effectiveAvatarUrl)
				.setTimestamp(Instant.now())

		val commandArguments = this.getUsage()
		val description = buildString {
			this.append(commandDescription)
			this.append('\n')
			this.append('\n')
			this.append("${Emotes.LORI_SMILE} **${locale["commands.explain.howToUse"]}** ")
			this.append('`')
			this.append(serverConfig.commandPrefix)
			this.append(label)
			this.append('`')
			this.append(' ')
			for ((index, argument) in commandArguments.arguments.withIndex()) {
				// <argumento> - Argumento obrigatório
				// [argumento] - Argumento opcional
				this.append("**")
				this.append('`')
				argument.build(this, locale)
				this.append('`')
				this.append("**")
				if (index != commandArguments.arguments.size - 1)
					this.append(' ')
			}
		}

		embed.setDescription(description)
		// Criar uma lista de exemplos
		val examples = ArrayList<String>()
		for (example in this.getExamples()) { // Adicionar todos os exemplos simples
			examples.add("`" + commandLabel + "`" + if (example.isEmpty()) "" else " **`$example`**")
		}
		if (this.getExamples(locale).isNotEmpty()) {
			examples.clear()
			for (example in this.getExamples(locale)) { // Adicionar todos os exemplos simples
				examples.add("`" + commandLabel + "`" + if (example.isEmpty()) "" else " **`$example`**")
			}
		}
		for ((key, value) in this.getExtendedExamples()) { // E agora vamos adicionar os exemplos mais complexos/extendidos
			examples.add("`" + commandLabel + "`" + if (key.isEmpty()) "" else " `$key` - **$value**")
		}
		if (examples.isNotEmpty()) {
			embed.addField(
					"\uD83D\uDCD6 ${locale["commands.explain.examples"]}",
					examples.joinToString("\n", transform = { "$it" }),
					false
			)
		}

		val botPermissions = getBotPermissions()
		val discordPermissions = getDiscordPermissions()

		if (botPermissions.isNotEmpty() || discordPermissions.isNotEmpty()) {
			var field = ""
			if (discordPermissions.isNotEmpty()) {
				field += "\uD83D\uDC81 ${locale["commands.explain.youNeedToHavePermission", discordPermissions.joinToString(", ", transform = { "`${it.localized(locale)}`" })]}\n"
			}
			if (botPermissions.isNotEmpty()) {
				field += "<:loritta:331179879582269451> ${locale["commands.explain.loriNeedToHavePermission", botPermissions.joinToString(", ", transform = { "`${it.localized(locale)}`" })]}\n"
			}
			embed.addField(
					"\uD83D\uDCDB ${locale["commands.explain.permissions"]}",
					field,
					false
			)
		}

		val labels = this.aliases + this.label
		val otherAlternatives = labels.filter { it != executedCommandLabel }

		if (otherAlternatives.isNotEmpty()) {
			embed.addField(
					"\uD83D\uDD00 ${locale["commands.explain.aliases"]}",
					otherAlternatives.joinToString(transform = { "`${serverConfig.commandPrefix}$it`" }),
					false
			)
		}

		val message = context.sendMessage(context.getAsMention(true), embed.build())
		message.addReaction("❓").queue()
		message.onReactionAddByAuthor(context) {
			if (it.reactionEmote.isEmote("❓")) {
				message.delete().queue()
				explainArguments(context)
			}
		}
	}

	/**
	 * Sends an embed explaining how the argument works
	 *
	 * @param context the context of the command
	 */
	suspend fun explainArguments(context: CommandContext) {
		val embed = EmbedBuilder()
		embed.setColor(Color(0, 193, 223))
		embed.setTitle("\uD83E\uDD14 Como os argumentos funcionam?")
		embed.addField(
				"Estilos de Argumentos",
				"""
					`<argumento>` - Argumento obrigatório
					`[argumento]` - Argumento opcional
				""".trimIndent(),
				false
		)

		embed.addField(
				"Tipos de Argumentos",
				"""
					`texto` - Um texto qualquer
					`usuário` - Menção, nome de um usuário ou ID de um usuário
					`imagem` - URL da imagem,  menção, nome de um usuário, ID de um usuário e, caso nada tenha sido encontrado, será pego a primeira imagem encontrada nas últimas 25 mensagens.
				""".trimIndent(),
				false
		)

		val message = context.sendMessage(context.getAsMention(true), embed.build())
		message.addReaction("❓").queue()
		message.onReactionAddByAuthor(context) {
			if (it.reactionEmote.isEmote("❓")) {
				message.delete().queue()
				explain(context)
			}
		}
	}
}