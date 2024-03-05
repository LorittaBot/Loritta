package net.perfectdreams.loritta.morenitta.commands

import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.locale.LocaleStringData
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.api.commands.Command
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.addReaction
import net.perfectdreams.loritta.morenitta.utils.extensions.getLocalizedName
import net.perfectdreams.loritta.morenitta.utils.extensions.isEmote
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor
import java.awt.Color
import java.time.Instant

abstract class AbstractCommand(val loritta: LorittaBot, open val label: String, var aliases: List<String> = listOf(), var category: net.perfectdreams.loritta.common.commands.CommandCategory, var lorittaPermissions: List<LorittaPermission> = listOf(), val onlyOwner: Boolean = false) {
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

	open fun getDescriptionKey() = Command.MISSING_DESCRIPTION_KEY

	open fun getDescription(locale: BaseLocale) = locale.get(getDescriptionKey())

	open fun getUsage(): net.perfectdreams.loritta.common.commands.CommandArguments {
		return arguments {}
	}

	open fun getExamplesKey(): LocaleKeyData? = null

	@Deprecated("Please use getExamples(locale)")
	open fun getExamples(): List<String> {
		return getExamples(loritta.localeManager.getLocaleById("default"))
	}

	open fun getExamples(locale: BaseLocale): List<String> {
		return listOf()
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
		val commandLabelWithPrefix = "${serverConfig.commandPrefix}${executedCommandLabel}"

		val embed = EmbedBuilder()
				.setColor(Constants.LORITTA_AQUA)
				.setAuthor(locale["commands.explain.clickHereToSeeAllMyCommands"], "${loritta.config.loritta.website.url}commands", discordMessage.jda.selfUser.effectiveAvatarUrl)
				.setTitle("${Emotes.LORI_HM} `${serverConfig.commandPrefix}${executedCommandLabel}`")
				.setFooter("${user.name} • ${this.category.getLocalizedName(locale)}", user.effectiveAvatarUrl)
				.setTimestamp(Instant.now())

		val commandArguments = this.getUsage()
		val description = buildString {
			// Builds the "How to Use" string
			this.append(commandDescription)
			this.append('\n')
			this.append('\n')
			this.append("${Emotes.LORI_SMILE} **${locale["commands.explain.howToUse"]}**")
			this.append(" `")
			this.append(serverConfig.commandPrefix)
			this.append(label)
			this.append('`')

			// Only add the arguments if the list is not empty (to avoid adding a empty "` `")
			if (commandArguments.arguments.isNotEmpty()) {
				this.append("**")
				this.append('`')
				this.append(' ')
				for ((index, argument) in commandArguments.arguments.withIndex()) {
					argument.build(this, locale)

					if (index != commandArguments.arguments.size - 1)
						this.append(' ')
				}
				this.append('`')
				this.append("**")

				// If we have arguments with explanations, let's show them!
				val argumentsWithExplanations = commandArguments.arguments.filter { it.explanation != null }

				if (argumentsWithExplanations.isNotEmpty()) {
					this.append('\n')
					// Same thing again, but with a *twist*!
					for ((index, argument) in argumentsWithExplanations.withIndex()) {
						this.append("**")
						this.append('`')
						argument.build(this, locale)
						this.append('`')
						this.append("**")
						this.append(' ')

						when (val explanation = argument.explanation) {
							is LocaleKeyData -> {
								this.append(locale.get(explanation))
							}
							is LocaleStringData -> {
								this.append(explanation.text)
							}
							else -> throw IllegalArgumentException("I don't know how to process a $argument!")
						}

						this.append('\n')
					}
				}
			}
		}

		embed.setDescription(description)

		// Create example list
		val examplesKey = getExamplesKey()
		val examples = ArrayList<String>()

		if (examplesKey != null) {
			val examplesAsString = locale.getList(examplesKey)

			for (example in examplesAsString) {
				val split = example.split("|-|")
						.map { it.trim() }

				if (split.size == 2) {
					// If the command has a extended description
					// "12 |-| Gira um dado de 12 lados"
					// A extended description can also contain "nothing", but contains a extended description
					// "|-| Gira um dado de 6 lados"
					val (commandExample, explanation) = split

					examples.add("\uD83D\uDD39 **$explanation**")
					examples.add("`" + commandLabelWithPrefix + "`" + (if (commandExample.isEmpty()) "" else "**` $commandExample`**"))
				} else {
					val commandExample = split[0]

					examples.add("`" + commandLabelWithPrefix + "`" + if (commandExample.isEmpty()) "" else "**` $commandExample`**")
				}
			}
		}

		if (examples.isNotEmpty()) {
			embed.addField(
					"\uD83D\uDCD6 ${locale["commands.explain.examples"]}",
					examples.joinToString("\n", transform = { it }),
					false
			)
		}

		val botPermissions = getBotPermissions()
		val discordPermissions = getDiscordPermissions()

		if (botPermissions.isNotEmpty() || discordPermissions.isNotEmpty()) {
			var field = ""
			if (discordPermissions.isNotEmpty()) {
				field += "\uD83D\uDC81 ${locale["commands.explain.youNeedToHavePermission", discordPermissions.joinToString(", ", transform = { "`${it.getLocalizedName(context.i18nContext)}`" })]}\n"
			}
			if (botPermissions.isNotEmpty()) {
				field += "<:loritta:331179879582269451> ${locale["commands.explain.loriNeedToHavePermission", botPermissions.joinToString(", ", transform = { "`${it.getLocalizedName(context.i18nContext)}`" })]}\n"
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
			if (it.emoji.isEmote("❓")) {
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
			if (it.emoji.isEmote("❓")) {
				message.delete().queue()
				explain(context)
			}
		}
	}
}