package com.mrpowergamerbr.loritta.commands

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.core.exceptions.ErrorResponseException
import net.dv8tion.jda.core.utils.PermissionUtil
import java.awt.Color
import java.time.Instant
import java.util.*

open abstract class CommandBase {
	open abstract fun getLabel(): String

	open fun getDescription(): String {
		return getDescription(LorittaLauncher.loritta.getLocaleById("default"))
	}

	fun getDescription(context: CommandContext): String {
		// TODO: Temporário
		val description = getDescription(context.locale)
		if (description == "Insira descrição do comando aqui!") {
			return description
		}
		return description
	}

	open fun getDescription(locale: BaseLocale): String {
		return "Insira descrição do comando aqui!"
	}

	open fun getCategory(): CommandCategory {
		return CommandCategory.MISC
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

	open fun getAliases(): List<String> {
		return listOf()
	}

	open fun hasCommandFeedback(): Boolean {
		return true
	}

	open abstract fun run(context: CommandContext)

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
	 * Retorna as permissões necessárias para utilizar este comando
	 *
	 * @return A lista de permissões necessárias
	 */
	open fun getDiscordPermissions(): List<Permission> {
		return listOf()
	}

	/**
	 * Retorna as permissões necessárias para eu poder usar este comando
	 *
	 * @return A lista de permissões necessárias
	 */
	open fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_EMBED_LINKS)
	}

	/**
	 * Retorna se somente o dono do bot pode executar este comando

	 * @return Se somente o dono do bot pode usar este comando
	 */
	open fun onlyOwner(): Boolean {
		return false
	}

	/**
	 * Retorna se o comando precisa ter o sistema de música ativado

	 * @return Se o comando precisa ter o sistema de música ativado
	 */
	open fun requiresMusicEnabled(): Boolean {
		return false
	}

	/**
	 * Retorna se o comando só funciona em uma instância de música
	 */
	open fun onlyInMusicInstance(): Boolean {
		return false
	}

	fun handle(ev: MessageReceivedEvent, conf: ServerConfig, locale: BaseLocale, profile: LorittaProfile): Boolean {
		if (conf.blacklistedChannels.contains(ev.channel.id))
			return true // Ignorar canais bloqueados (return true = fast break, se está bloqueado o canal no primeiro comando que for executado, os outros obviamente também estarão)
		val message = ev.message.content
		var rawMessage = ev.message.rawContent
		var run = false
		var byMention = false
		var label = conf.commandPrefix + getLabel()
		if (rawMessage.startsWith("<@" + Loritta.config.clientId + "> ") || rawMessage.startsWith("<@!" + Loritta.config.clientId + "> ")) {
			byMention = true
			rawMessage = rawMessage.replaceFirst("<@" + Loritta.config.clientId + "> ", "")
			rawMessage = rawMessage.replaceFirst("<@!" + Loritta.config.clientId + "> ", "")
			label = getLabel()
		}
		run = rawMessage.replace("\n", " ").split(" ")[0].equals(label, ignoreCase = true)
		if (!run) {
			for (alias in this.getAliases()) {
				label = conf.commandPrefix + alias
				if (rawMessage.startsWith(label)) {
					run = true
					break
				}
				if (byMention) {
					label = alias
					if (rawMessage.startsWith(label)) {
						run = true
						break
					}
				}
			}
		}
		if (run) {
			try {
				if (requiresMusicEnabled() || onlyInMusicInstance()) {
					// Ignorar comandos de música em uma instância não somente para música
					if (!loritta.isMusicOnly) {
						return true
					}
				}

				// Carregar as opções de comandos
				val cmdOptions = conf.getCommandOptionsFor(this)

				if (cmdOptions.override && cmdOptions.blacklistedChannels.contains(ev.channel.id))
					return true // Ignorar canais bloqueados

				// Cooldown
				val diff = System.currentTimeMillis() - loritta.userCooldown.getOrDefault(ev.author.id, 0L) as Long

				if (1250 > diff && ev.author.id != Loritta.config.ownerId) { // Tá bom, é alguém tentando floodar, vamos simplesmente ignorar
					loritta.userCooldown.put(ev.author.id, System.currentTimeMillis()) // E vamos guardar o tempo atual
					return true
				}

				if (conf != loritta.dummyServerConfig && !ev.textChannel.canTalk()) { // Se a Loritta não pode falar no canal de texto, avise para o dono do servidor para dar a permissão para ela
					LorittaUtils.warnOwnerNoPermission(ev.guild, ev.textChannel, conf)
					return true
				}

				if (hasCommandFeedback()) {
					ev.channel.sendTyping().complete()
				}

				if (5000 > diff && ev.author.id != Loritta.config.ownerId) {
					ev.channel.sendMessage("\uD83D\uDD25 **|** " + ev.member.asMention + " " + locale.get("PLEASE_WAIT_COOLDOWN")).complete()
					return true
				}

				loritta.userCooldown.put(ev.author.id, System.currentTimeMillis())

				LorittaUtilsKotlin.trackCommands(ev.message)

				// Se estamos dentro de uma guild... (Já que mensagens privadas não possuem permissões)
				if (ev.isFromType(ChannelType.TEXT)) {
					// Verificar se a Loritta possui todas as permissões necessárias
					var botPermissions = ArrayList<Permission>(getBotPermissions())
					botPermissions.add(Permission.MESSAGE_EMBED_LINKS)
					botPermissions.add(Permission.MESSAGE_EXT_EMOJI)
					botPermissions.add(Permission.MESSAGE_ADD_REACTION)
					val missingPermissions = ArrayList<Permission>(botPermissions.filterNot { ev.guild.selfMember.hasPermission(ev.textChannel, it) })

					if (missingPermissions.isNotEmpty()) {
						// oh no
						var required = ""
						missingPermissions.forEach {
							val permissionTranslation = locale.get("PERMISSION_${it.name}")
							if (required.isNotEmpty()) {
								required += ", " + permissionTranslation
							} else {
								required += permissionTranslation
							}
						}
						ev.textChannel.sendMessage(LorittaUtils.ERROR + " **|** ${ev.member.asMention} ${locale.get("PERMISSION_I_NEED_PERMISSION", required)}").complete()
						return true
					}
				}

				var args = message.stripCodeMarks().split(" ").toTypedArray().remove(0)
				var rawArgs = ev.message.rawContent.stripCodeMarks().split(" ").toTypedArray().remove(0)
				var strippedArgs = ev.message.strippedContent.stripCodeMarks().split(" ").toTypedArray().remove(0)
				if (byMention) {
					args = args.remove(0)
					rawArgs = rawArgs.remove(0)
					strippedArgs = strippedArgs.remove(0)
				}
				val context = CommandContext(conf, ev, this, args, rawArgs, strippedArgs)
				if (args.size >= 1 && args[0] == "🤷") { // Usar a ajuda caso 🤷 seja usado
					explain(context)
					return true
				}
				if (LorittaUtils.handleIfBanned(context, profile)) {
					return true
				}
				if (!context.canUseCommand()) {
					context.sendMessage("\uD83D\uDE45 **|** " + context.getAsMention(true) + "**" + context.locale.NO_PERMISSION.f() + "**")
					return true
				}
				if (context.isPrivateChannel && !canUseInPrivateChannel()) {
					context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.CANT_USE_IN_PRIVATE.f())
					return true
				}
				if (needsToUploadFiles()) {
					if (!LorittaUtils.canUploadFiles(context)) {
						return true
					}
				}
				if (requiresMusicEnabled()) {
					if (!context.config.musicConfig.isEnabled) {
						val canManage = context.handle.hasPermission(Permission.MANAGE_SERVER) || context.handle.hasPermission(Permission.ADMINISTRATOR)
						context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + context.locale.get("DJ_LORITTA_DISABLED") + " \uD83D\uDE1E" + if (canManage) context.locale.get("DJ_LORITTA_HOW_TO_ENABLE", "https://loritta.website/auth") else "")
						return true
					}
				}
				run(context)

				val cmdOpti = context.config.getCommandOptionsFor(this)
				if (conf.deleteMessageAfterCommand || (cmdOpti.override && cmdOpti.deleteMessageAfterCommand)) {
					val message = ev.message.textChannel.history.getMessageById(ev.messageId)
					if (message != null) { // Nós iremos pegar a mensagem novamente, já que talvez ela tenha sido deletada
						ev.message.delete().complete()
					}
				}
				return true
			} catch (e: Exception) {
				e.printStackTrace()
				LorittaUtilsKotlin.sendStackTrace(ev.message, e)

				// Avisar ao usuário que algo deu muito errado
				val mention = if (conf.mentionOnCommandOutput) "${ev.author.asMention} " else ""

				ev.channel.sendMessage("\uD83E\uDD37 **|** " + mention + locale.get("ERROR_WHILE_EXECUTING_COMMAND")).complete()
			}
		}
		return false
	}

	fun explain(context: CommandContext) {
		val conf = context.config
		val ev = context.event
		if (conf.explainOnCommandRun) {
			val embed = EmbedBuilder()
			embed.setColor(Color(0, 193, 223))
			embed.setTitle("\uD83E\uDD14 " + context.locale.HOW_TO_USE + "... `" + conf.commandPrefix + this.getLabel() + "`")

			val usage = if (getUsage() != null) " `${getUsage()}`" else ""

			var cmdInfo = getDescription(context) + "\n\n"

			cmdInfo += "**" + context.locale.HOW_TO_USE + ":** " + conf.commandPrefix + this.getLabel() + usage + "\n"

			if (!this.getDetailedUsage().isEmpty()) {
				for ((key, value) in this.getDetailedUsage()) {
					cmdInfo += "`$key` - $value\n"
				}
			}

			cmdInfo += "\n"

			// Criar uma lista de exemplos
			val examples = ArrayList<String>()
			for (example in this.getExample()) { // Adicionar todos os exemplos simples
				examples.add(conf.commandPrefix + this.getLabel() + if (example.isEmpty()) "" else " `$example`")
			}
			for ((key, value) in this.getExtendedExamples()) { // E agora vamos adicionar os exemplos mais complexos/extendidos
				examples.add(conf.commandPrefix + this.getLabel() + if (key.isEmpty()) "" else " `$key` - **$value**")
			}

			if (examples.isEmpty()) {
				cmdInfo += "**" + context.locale.EXAMPLE + ":**\n" + conf.commandPrefix + this.getLabel()
			} else {
				cmdInfo += "**" + context.locale.EXAMPLE + (if (this.getExample().size == 1) "" else "s") + ":**\n"
				for (example in examples) {
					cmdInfo += example + "\n"
				}
			}
			embed.setDescription(cmdInfo)
			embed.setFooter(ev.author.name + "#" + ev.author.discriminator, ev.author.effectiveAvatarUrl) // Adicionar quem executou o comando
			embed.setTimestamp(Instant.now())

			if (conf.explainInPrivate) {
				ev.author.openPrivateChannel().complete().sendMessage(embed.build()).complete()
			} else {
				ev.channel.sendMessage(embed.build()).complete()
			}
		}
	}

	open fun onCommandReactionFeedback(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {} // Quando alguém usa uma reaction na mensagem

	open fun onCommandMessageReceivedFeedback(context: CommandContext, e: MessageReceivedEvent, msg: Message) {} // Quando uma mensagem é recebida
}