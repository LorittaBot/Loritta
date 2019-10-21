package com.mrpowergamerbr.loritta.commands

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.extensions.localized
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import org.bson.codecs.pojo.annotations.BsonIgnore
import java.awt.Color
import java.time.Instant

abstract class AbstractCommand(open val label: String, var aliases: List<String> = listOf(), var category: CommandCategory, var lorittaPermissions: List<LorittaPermission> = listOf(), val onlyOwner: Boolean = false) {
	@Transient
	@get:BsonIgnore
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

	var executedCount = 0

	open fun getDescription(locale: LegacyBaseLocale): String {
		return "Insira descrição do comando aqui!"
	}

	@Deprecated("Please use getUsage(locale)")
	open fun getUsage(): String? {
		return null
	}

	open fun getUsage(locale: LegacyBaseLocale): CommandArguments {
		return arguments {}
	}

	open fun getDetailedUsage(): Map<String, String> {
		return mapOf()
	}

	@Deprecated("Please use getExamples(locale)")
	open fun getExamples(): List<String> {
		return getExamples(loritta.getLegacyLocaleById("default"))
	}

	open fun getExamples(locale: LegacyBaseLocale): List<String> {
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
	abstract suspend fun run(context: CommandContext, locale: LegacyBaseLocale)

	/**
	 * Sends an embed explaining what the command does
	 *
	 * @param context the context of the command
	 */
	suspend fun explain(context: CommandContext) {
		val conf = context.config
		val ev = context.event
		val locale = context.legacyLocale

		if (conf.explainOnCommandRun) {
			val rawArguments = context.message.contentRaw.split(" ")
			var commandLabel = rawArguments[0]
			if (rawArguments.getOrNull(1) != null && (rawArguments[0] == "<@${loritta.discordConfig.discord.clientId}>" || rawArguments[0] == "<@!${loritta.discordConfig.discord.clientId}>")) {
				// Caso o usuário tenha usado "@Loritta comando", pegue o segundo argumento (no caso o "comando") em vez do primeiro (que é a mention da Lori)
				commandLabel = rawArguments[1]
			}
			commandLabel = commandLabel.toLowerCase()

			val embed = EmbedBuilder()
			embed.setColor(Color(0, 193, 223))
			embed.setTitle("\uD83E\uDD14 `$commandLabel`")

			val commandArguments = getUsage(locale)
			val usage = when {
				commandArguments.arguments.isNotEmpty() -> " `${commandArguments.build(context.locale)}`"
				getUsage() != null -> " `${getUsage()}`"
				else -> ""
			}

			var cmdInfo = getDescription(context.legacyLocale) + "\n\n"

			cmdInfo += "\uD83D\uDC81 **" + locale["HOW_TO_USE"] + ":** " + commandLabel + usage + "\n"

			for (argument in commandArguments.arguments) {
				if (argument.explanation != null) {
					cmdInfo += "${Constants.LEFT_PADDING} `${argument.build(context.locale)}` - "
					if (argument.defaultValue != null) {
						cmdInfo += "(Padrão: ${argument.defaultValue}) "
					}
					cmdInfo += "${argument.explanation}\n"
				}
			}

			cmdInfo += "\n"

			// Criar uma lista de exemplos
			val examples = ArrayList<String>()
			for (example in this.getExamples()) { // Adicionar todos os exemplos simples
				examples.add(commandLabel + if (example.isEmpty()) "" else " `$example`")
			}
			if (this.getExamples(context.legacyLocale).isNotEmpty()) {
				examples.clear()
				for (example in this.getExamples(context.legacyLocale)) { // Adicionar todos os exemplos simples
					examples.add(commandLabel + if (example.isEmpty()) "" else " `$example`")
				}
			}
			for ((key, value) in this.getExtendedExamples()) { // E agora vamos adicionar os exemplos mais complexos/extendidos
				examples.add(commandLabel + if (key.isEmpty()) "" else " `$key` - **$value**")
			}

			if (examples.isEmpty()) {
				embed.addField(
						"\uD83D\uDCD6 " + context.legacyLocale["EXAMPLE"],
						commandLabel,
						false
				)
			} else {
				var exampleList = ""
				for (example in examples) {
					exampleList += example + "\n"
				}
				embed.addField(
						"\uD83D\uDCD6 " + context.legacyLocale["EXAMPLE"] + (if (this.getExamples().size == 1) "" else "s"),
						exampleList,
						false
				)
			}

			if (getBotPermissions().isNotEmpty() || getDiscordPermissions().isNotEmpty()) {
				var field = ""
				if (getDiscordPermissions().isNotEmpty()) {
					field += "\uD83D\uDC81 Você precisa ter permissão para ${getDiscordPermissions().joinToString(", ", transform = { "`${it.localized(context.locale)}`" })} para utilizar este comando!\n"
				}
				if (getBotPermissions().isNotEmpty()) {
					field += "<:loritta:331179879582269451> Eu preciso de permissão para ${getBotPermissions().joinToString(", ", transform = { "`${it.localized(context.locale)}`" })} para poder executar este comando!\n"
				}
				embed.addField(
						"\uD83D\uDCDB Permissões",
						field,
						false
				)
			}

			val aliases = mutableSetOf<String>()
			aliases.add(this.label)
			aliases.addAll(this.aliases)

			val onlyUnusedAliases = aliases.filter { it != commandLabel.replaceFirst(context.config.commandPrefix, "") }
			if (onlyUnusedAliases.isNotEmpty()) {
				embed.addField(
						"\uD83D\uDD00 ${context.legacyLocale["CommandAliases"]}",
						onlyUnusedAliases.joinToString(", ", transform = { "`" + context.config.commandPrefix + it + "`" }),
						true
				)
			}

			embed.setDescription(cmdInfo)
			embed.setAuthor("${context.userHandle.name}#${context.userHandle.discriminator}", null, ev.author.effectiveAvatarUrl)
			embed.setFooter(category.getLocalizedName(context.locale), "${loritta.instanceConfig.loritta.website.url}assets/img/loritta_gabizinha_v1.png") // Mostrar categoria do comando
			embed.setTimestamp(Instant.now())

			if (conf.explainInPrivate) {
				ev.author.openPrivateChannel().queue {
					it.sendMessage(embed.build()).queue()
				}
			} else {
				val message = context.sendMessage(context.getAsMention(true), embed.build())
				message.addReaction("❓").queue()
				message.onReactionAddByAuthor(context) {
					if (it.reactionEmote.isEmote("❓")) {
						message.delete().queue()
						explainArguments(context)
					}
				}
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