package net.perfectdreams.loritta.platform.discord.commands

import com.github.benmanes.caffeine.cache.Caffeine
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.localized
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.ExecutedCommandsLog
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.FeatureFlags
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit

class DiscordCommandMap(val discordLoritta: LorittaDiscord) : CommandMap<Command<CommandContext>> {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	val commands = mutableListOf<Command<CommandContext>>()
	private val userCooldown = Caffeine.newBuilder().expireAfterAccess(30L, TimeUnit.SECONDS)
			.maximumSize(100)
			.build<Long, Long>().asMap()

	override fun register(command: Command<CommandContext>) {
		logger.info { "Registering $command with ${command.labels}" }
		commands.add(command)
	}

	override fun unregister(command: Command<CommandContext>) {
		commands.remove(command)
	}

	suspend fun dispatch(ev: LorittaMessageEvent, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, locale: BaseLocale, legacyLocale: LegacyBaseLocale, lorittaUser: LorittaUser): Boolean {
		val rawMessage = ev.message.contentRaw

		// É necessário remover o new line para comandos como "+eval", etc
		val rawArguments = rawMessage.replace("\n", "").split(" ")

		// Primeiro os comandos vanilla da Loritta(tm)
		for (command in commands.filter { !legacyServerConfig.disabledCommands.contains(it.commandName) }) {
			if (dispatch(command, rawArguments, ev, serverConfig, legacyServerConfig, locale, legacyLocale, lorittaUser))
				return true
		}

		return false
	}

	suspend fun dispatch(command: Command<CommandContext>, rawArguments: List<String>, ev: LorittaMessageEvent, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, locale: BaseLocale, legacyLocale: LegacyBaseLocale, lorittaUser: LorittaUser): Boolean {
		val message = ev.message.contentDisplay
		val user = ev.author

		var prefix = serverConfig.commandPrefix

		val labels = command.labels.toMutableList()

		// Comandos com espaços na label, yeah!
		var valid = false

		val checkArguments = rawArguments.toMutableList()
		val rawArgument0 = checkArguments.getOrNull(0)
		var removeArgumentCount = 0
		val byMention = (rawArgument0 == "<@${discordLoritta.discordConfig.discord.clientId}>" || rawArgument0 == "<@!${discordLoritta.discordConfig.discord.clientId}>")

		if (byMention) {
			removeArgumentCount++
			checkArguments.removeAt(0)
			prefix = ""
		}

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
			val isPrivateChannel = ev.isFromType(ChannelType.PRIVATE)
			val start = System.currentTimeMillis()

			val args = message.replace("@${ev.guild?.selfMember?.effectiveName ?: ""}", "").stripCodeMarks().split(Constants.WHITE_SPACE_MULTIPLE_REGEX).toMutableList()
			val rawArgs = ev.message.contentRaw.stripCodeMarks().split(Constants.WHITE_SPACE_MULTIPLE_REGEX).toMutableList()
			val strippedArgs = ev.message.contentStripped.stripCodeMarks().split(Constants.WHITE_SPACE_MULTIPLE_REGEX).toMutableList()

			repeat(removeArgumentCount) {
				args.removeAt(0)
				rawArgs.removeAt(0)
				strippedArgs.removeAt(0)
			}

			val context = DiscordCommandContext(
					loritta,
					command,
					rawArgs,
					ev.message,
					locale,
					serverConfig,
					lorittaUser
			)

			if (ev.message.isFromType(ChannelType.TEXT)) {
				logger.info("(${ev.message.guild.name} -> ${ev.message.channel.name}) ${ev.author.name}#${ev.author.discriminator} (${ev.author.id}): ${ev.message.contentDisplay}")
			} else {
				logger.info("(Direct Message) ${ev.author.name}#${ev.author.discriminator} (${ev.author.id}): ${ev.message.contentDisplay}")
			}

			try {
				legacyServerConfig.lastCommandReceivedAt = System.currentTimeMillis()
				com.mrpowergamerbr.loritta.utils.loritta.serversColl.updateOne(
						Filters.eq("_id", legacyServerConfig.guildId),
						Updates.set("lastCommandReceivedAt", legacyServerConfig.lastCommandReceivedAt)
				)

				if (serverConfig.blacklistedChannels.contains(ev.channel.idLong) && !lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
					if (serverConfig.warnIfBlacklisted) {
						if (serverConfig.blacklistedChannels.isNotEmpty() && ev.guild != null && ev.member != null && ev.textChannel != null) {
							val generatedMessage = MessageUtils.generateMessage(
									serverConfig.blacklistedWarning ?: "???",
									listOf(ev.member, ev.textChannel),
									ev.guild
							)
							ev.textChannel.sendMessage(generatedMessage!!).queue()
						}
					}
					return true // Ignorar canais bloqueados (return true = fast break, se está bloqueado o canal no primeiro comando que for executado, os outros obviamente também estarão)
				}

				// Cooldown
				val diff = System.currentTimeMillis() - userCooldown.getOrDefault(ev.author.idLong, 0L)

				if (1250 > diff && !loritta.config.isOwner(ev.author.id)) { // Tá bom, é alguém tentando floodar, vamos simplesmente ignorar
					userCooldown.put(ev.author.idLong, System.currentTimeMillis()) // E vamos guardar o tempo atual
					return true
				}

				var cooldown = command.cooldown
				val donatorPaid = com.mrpowergamerbr.loritta.utils.loritta.getActiveMoneyFromDonations(ev.author.idLong)
				val guildPaid = transaction(Databases.loritta) {
					loritta.getOrCreateServerConfig(ev.author.idLong).donationKey?.value
				} ?: 0.0

				if (donatorPaid >= 39.99 || guildPaid >= 59.99) {
					cooldown /= 2
				}

				if (cooldown > diff && !loritta.config.isOwner(ev.author.id)) {
					val fancy = DateUtils.formatDateDiff((cooldown - diff) + System.currentTimeMillis(), legacyLocale)
					context.reply(
							LorittaReply(
									locale["commands.pleaseWaitCooldown", fancy, "\uD83D\uDE45"],
									"\uD83D\uDD25"
							)
					)
					return true
				}

				userCooldown[ev.author.idLong] = System.currentTimeMillis()

				command.executedCount++

				if (command.hasCommandFeedback) {
					if (FeatureFlags.IMPROVED_TYPING_SEND) {
						if (command.sendTypingStatus)
							ev.channel.sendTyping().await()
					} else {
						ev.channel.sendTyping().await()
					}
				}

				// Se estamos dentro de uma guild... (Já que mensagens privadas não possuem permissões)
				if (!isPrivateChannel && ev.guild != null && ev.member != null && ev.textChannel != null && command is DiscordCommand) {
					// Verificar se a Loritta possui todas as permissões necessárias
					val botPermissions = command.botRequiredPermissions.toMutableList()
					botPermissions.add(Permission.MESSAGE_EMBED_LINKS)
					botPermissions.add(Permission.MESSAGE_EXT_EMOJI)
					botPermissions.add(Permission.MESSAGE_ADD_REACTION)
					botPermissions.add(Permission.MESSAGE_HISTORY)
					val missingPermissions = ArrayList<Permission>(botPermissions.filterNot { ev.guild.selfMember.hasPermission(ev.textChannel, it) })

					if (missingPermissions.isNotEmpty()) {
						// oh no
						val required = missingPermissions.joinToString(", ", transform = { "`" + it.localized(locale) + "`" })
						context.reply(
								LorittaReply(
										locale["commands.loriDoesntHavePermissionDiscord", required, "\uD83D\uDE22", "\uD83D\uDE42"],
										Constants.ERROR
								)
						)
						return true
					}
				}

				/* if (!isPrivateChannel && ev.member != null && ev.textChannel != null) {
					val missingPermissions = command.lorittaPermissions.filterNot { lorittaUser.hasPermission(it) }

					if (missingPermissions.isNotEmpty()) {
						// oh no
						val required = missingPermissions.joinToString(", ", transform = { "`" + legacyLocale["LORIPERMISSION_${it.name}"] + "`"})
						var message = legacyLocale["LORIPERMISSION_MissingPermissions", required]

						if (ev.member.hasPermission(Permission.ADMINISTRATOR) || ev.member.hasPermission(Permission.MANAGE_SERVER)) {
							message += " ${legacyLocale["LORIPERMISSION_MissingPermCanConfigure", loritta.instanceConfig.loritta.website.url]}"
						}
						ev.textChannel.sendMessage(Constants.ERROR + " **|** ${ev.member.asMention} $message").queue()
						return true
					}
				} */

				if (args.isNotEmpty() && args[0] == "🤷") { // Usar a ajuda caso 🤷 seja usado
					context.explain()
					return true
				} /*

				if (LorittaUtilsKotlin.handleIfBanned(context, lorittaUser.profile)) {
					return true
				} */

				if (command.onlyOwner && !loritta.config.isOwner(user.id)) {
					context.reply(
							LorittaReply(
									locale["commands.commandOnlyForOwner"],
									Constants.ERROR
							)
					)
					return true
				}

				if (command is DiscordCommand) {
					val missingRequiredPermissions = command.userRequiredPermissions.filterNot { ev.message.member!!.hasPermission(ev.message.textChannel, it) }

					if (missingRequiredPermissions.isNotEmpty()) {
						val required = missingRequiredPermissions.joinToString(", ", transform = { "`" + it.localized(locale) + "`" })
						context.reply(
								LorittaReply(
										locale["commands.userDoesntHavePermissionDiscord", required],
										Constants.ERROR
								)
						)
						return true
					}
				}

				if (context.isPrivateChannel && !command.canUseInPrivateChannel) {
					context.reply(
							LorittaReply(
									message = legacyLocale["CANT_USE_IN_PRIVATE"],
									prefix = Constants.ERROR
							)
					)
					return true
				}

				/* if (command.needsToUploadFiles()) {
					if (!LorittaUtils.canUploadFiles(context)) {
						return true
					}
				}

				*/

				if (command is DiscordCommand) {
					if (command.requiresMusic) {
						/* if (!context.legacyConfig.musicConfig.isEnabled) {
						val canManage = context.handle.hasPermission(Permission.MANAGE_SERVER) || context.handle.hasPermission(Permission.ADMINISTRATOR)
						context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + legacyLocale["DJ_LORITTA_DISABLED"] + " \uD83D\uDE1E" + if (canManage) legacyLocale["DJ_LORITTA_HOW_TO_ENABLE", "${loritta.instanceConfig.loritta.website.url}dashboard"] else "")
						return true
					} */
					}
				}

				// Vamos pegar uma mensagem aleatória de doação, se não for nula, iremos enviar ela :3
				/* DonateUtils.getRandomDonationMessage(
						locale,
						lorittaUser.profile,
						donatorPaid,
						guildPaid
				)?.let { context.reply(it) }

				if (!context.isPrivateChannel && ev.guild != null) {
					val nickname = ev.guild.selfMember.nickname

					if (nickname != null) {
						// #LoritaTambémTemSentimentos
						val hasBadNickname = MiscUtils.hasInappropriateWords(nickname)

						if (hasBadNickname) {
							context.reply(
									LoriReply(
											legacyLocale["LORITTA_BadNickname"],
											"<:lori_triste:370344565967814659>"
									)
							)
							if (ev.guild.selfMember.hasPermission(Permission.NICKNAME_CHANGE)) {
								ev.guild.modifyNickname(ev.guild.selfMember, null).queue()
							} else {
								return true
							}
						}
					}
				} */

				transaction(Databases.loritta) {
					lorittaUser.profile.lastCommandSentAt = System.currentTimeMillis()

					ExecutedCommandsLog.insert {
						it[userId] = lorittaUser.user.idLong
						it[guildId] = if (ev.message.isFromGuild) ev.message.guild.idLong else null
						it[channelId] = ev.message.channel.idLong
						it[sentAt] = System.currentTimeMillis()
						it[ExecutedCommandsLog.command] = command.commandName ?: "UnknownCommand"
						it[ExecutedCommandsLog.message] = ev.message.contentRaw
					}
				}

				command.executor.invoke(context)

				if (!isPrivateChannel && ev.guild != null) {
					if (ev.guild.selfMember.hasPermission(ev.textChannel!!, Permission.MESSAGE_MANAGE) && (serverConfig.deleteMessageAfterCommand)) {
						ev.message.textChannel.retrieveMessageById(ev.messageId).queue {
							// Nós iremos pegar a mensagem novamente, já que talvez ela tenha sido deletada
							it.delete().queue()
						}
					}
				}

				val end = System.currentTimeMillis()
				if (ev.message.isFromType(ChannelType.TEXT)) {
					logger.info("(${ev.message.guild.name} -> ${ev.message.channel.name}) ${ev.author.name}#${ev.author.discriminator} (${ev.author.id}): ${ev.message.contentDisplay} - OK! Processado em ${end - start}ms")
				} else {
					logger.info("(Direct Message) ${ev.author.name}#${ev.author.discriminator} (${ev.author.id}): ${ev.message.contentDisplay} - OK! Processado em ${end - start}ms")
				}
				return true
			} catch (e: Exception) {
				if (e is CancellationException) {
					logger.error(e) { "RestAction in command ${command::class.simpleName} has been cancelled" }
					return true
				}

				if (e is ErrorResponseException) {
					if (e.errorCode == 40005) { // Request entity too large
						if (ev.isFromType(ChannelType.PRIVATE) || (ev.isFromType(ChannelType.TEXT) && ev.textChannel != null && ev.textChannel.canTalk()))
							context.reply(
									LorittaReply(
											locale["commands.imageTooLarge", "8MB", Emotes.LORI_TEMMIE],
											"\uD83E\uDD37"
									)
							)
						return true
					}
				}

				if (e is SilentCommandException)
					return true

				if (e is CommandException) {
					context.reply(
							LorittaReply(
									e.reason,
									e.prefix
							)
					)
					return true
				}

				logger.error("Exception ao executar comando ${command.javaClass.simpleName}", e)

				// Avisar ao usuário que algo deu muito errado
				val mention = "${ev.author.asMention} "
				var reply = "\uD83E\uDD37 **|** " + mention + locale["commands.errorWhileExecutingCommand", Emotes.LORI_RAGE, Emotes.LORI_CRYING]

				if (!e.message.isNullOrEmpty())
					reply += " `${e.message!!.escapeMentions()}`"

				if (ev.isFromType(ChannelType.PRIVATE) || (ev.isFromType(ChannelType.TEXT) && ev.textChannel != null && ev.textChannel.canTalk()))
					ev.channel.sendMessage(reply).queue()

				return true
			}
		}
		return false
	}
}