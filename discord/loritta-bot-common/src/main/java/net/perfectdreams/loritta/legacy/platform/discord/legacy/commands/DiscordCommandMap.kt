package net.perfectdreams.loritta.legacy.platform.discord.legacy.commands

import net.perfectdreams.loritta.legacy.commands.vanilla.discord.ChannelInfoCommand
import net.perfectdreams.loritta.legacy.dao.ServerConfig
import net.perfectdreams.loritta.legacy.events.LorittaMessageEvent
import net.perfectdreams.loritta.legacy.utils.Constants
import net.perfectdreams.loritta.legacy.utils.DateUtils
import net.perfectdreams.loritta.legacy.utils.LorittaPermission
import net.perfectdreams.loritta.legacy.utils.LorittaUser
import net.perfectdreams.loritta.legacy.utils.LorittaUtils
import net.perfectdreams.loritta.legacy.utils.LorittaUtilsKotlin
import net.perfectdreams.loritta.legacy.utils.MessageUtils
import net.perfectdreams.loritta.legacy.utils.escapeMentions
import net.perfectdreams.loritta.legacy.utils.extensions.await
import net.perfectdreams.loritta.legacy.utils.extensions.localized
import net.perfectdreams.loritta.legacy.utils.extensions.referenceIfPossible
import net.perfectdreams.loritta.legacy.utils.loritta
import net.perfectdreams.loritta.legacy.utils.lorittaShards
import net.perfectdreams.loritta.legacy.utils.stripCodeMarks
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.perfectdreams.loritta.common.api.commands.Command
import net.perfectdreams.loritta.common.api.commands.CommandContext
import net.perfectdreams.loritta.common.api.commands.CommandException
import net.perfectdreams.loritta.common.api.commands.CommandMap
import net.perfectdreams.loritta.common.api.commands.SilentCommandException
import net.perfectdreams.loritta.common.messages.LorittaReply
import net.perfectdreams.loritta.legacy.commands.vanilla.action.*
import net.perfectdreams.loritta.legacy.commands.vanilla.administration.*
import net.perfectdreams.loritta.legacy.commands.vanilla.discord.GuildBannerCommand
import net.perfectdreams.loritta.legacy.commands.vanilla.discord.RoleInfoCommand
import net.perfectdreams.loritta.legacy.commands.vanilla.economy.*
import net.perfectdreams.loritta.legacy.commands.vanilla.`fun`.*
import net.perfectdreams.loritta.legacy.commands.vanilla.images.*
import net.perfectdreams.loritta.legacy.commands.vanilla.magic.LoriToolsCommand
import net.perfectdreams.loritta.legacy.commands.vanilla.misc.DiscordBotListCommand
import net.perfectdreams.loritta.legacy.commands.vanilla.misc.DiscordBotListStatusCommand
import net.perfectdreams.loritta.legacy.commands.vanilla.misc.DiscordBotListTopCommand
import net.perfectdreams.loritta.legacy.commands.vanilla.misc.DiscordBotListTopLocalCommand
import net.perfectdreams.loritta.legacy.commands.vanilla.roblox.RbGameCommand
import net.perfectdreams.loritta.legacy.commands.vanilla.roblox.RbUserCommand
import net.perfectdreams.loritta.legacy.commands.vanilla.social.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.locale.LocaleStringData
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.utils.CommandCooldownManager
import net.perfectdreams.loritta.legacy.utils.CommandUtils
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.legacy.utils.metrics.Prometheus
import java.sql.Connection
import java.util.concurrent.CancellationException

class DiscordCommandMap(val discordLoritta: LorittaDiscord) : CommandMap<Command<CommandContext>> {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	val commands = mutableListOf<Command<CommandContext>>()

	init {
		registerAll(
			// ===[ MAGIC ]===
			LoriToolsCommand(discordLoritta),

			// ===[ ROLEPLAY ]===
			AttackCommand(discordLoritta),
			DanceCommand(discordLoritta),
			HeadPatCommand(discordLoritta),
			HighFiveCommand(discordLoritta),
			HugCommand(discordLoritta),
			KissCommand(discordLoritta),
			SlapCommand(discordLoritta),

			// ===[ ECONOMY ]===
			SonhosTopCommand(discordLoritta),
			SonhosTopLocalCommand(discordLoritta),
			TransactionsCommand(discordLoritta),
			BrokerCommand(discordLoritta),
			BrokerBuyStockCommand(discordLoritta),
			BrokerSellStockCommand(discordLoritta),
			BrokerPortfolioCommand(discordLoritta),
			GuessNumberCommand(discordLoritta),
			ScratchCardCommand(discordLoritta),
			ScratchCardTopCommand(discordLoritta),

			// ===[ SOCIAL ]===
			BomDiaECiaStatusCommand(discordLoritta),
			BomDiaECiaTopCommand(discordLoritta),
			BomDiaECiaTopLocalCommand(discordLoritta),
			RepTopCommand(discordLoritta),
			XpNotificationsCommand(discordLoritta),
			RepListCommand(discordLoritta),
			DashboardCommand(discordLoritta),
			RenameChannelCommand(discordLoritta),
			RenameEmojiCommand(discordLoritta),

			// ===[ ADMIN ]===
			BanInfoCommand(discordLoritta),
			ClearCommand(discordLoritta),
			UnwarnCommand(discordLoritta),

			// ===[ MISC ]===
			FanArtsCommand(discordLoritta),
			DiscordBotListCommand(discordLoritta),
			DiscordBotListStatusCommand(discordLoritta),
			DiscordBotListTopCommand(discordLoritta),
			DiscordBotListTopLocalCommand(discordLoritta),

			// ===[ DISCORD ]===
			ChannelInfoCommand(discordLoritta),
			GuildBannerCommand(discordLoritta),
			RoleInfoCommand(discordLoritta),

			// ===[ FUN ]===
			GiveawayCommand(discordLoritta),
			GiveawayEndCommand(discordLoritta),
			GiveawayRerollCommand(discordLoritta),
			GiveawaySetupCommand(discordLoritta),
			CancelledCommand(discordLoritta),
			HungerGamesCommand(discordLoritta),

			// ===[ IMAGES ]===
			ArtCommand(discordLoritta),
			AtaCommand(discordLoritta),
			BobBurningPaperCommand(discordLoritta),
			BolsoDrakeCommand(discordLoritta),
			BolsoFrameCommand(discordLoritta),
			Bolsonaro2Command(discordLoritta),
			BolsonaroCommand(discordLoritta),
			BriggsCoverCommand(discordLoritta),
			BuckShirtCommand(discordLoritta),
			CanellaDvdCommand(discordLoritta),
			ChicoAtaCommand(discordLoritta),
			DrakeCommand(discordLoritta),
			GessyAtaCommand(discordLoritta),
			LoriAtaCommand(discordLoritta),
			LoriDrakeCommand(discordLoritta),
			LoriSignCommand(discordLoritta),
			PassingPaperCommand(discordLoritta),
			PepeDreamCommand(discordLoritta),
			QuadroCommand(discordLoritta),
			RomeroBrittoCommand(discordLoritta),
			SAMCommand(discordLoritta),
			StudiopolisTvCommand(discordLoritta),
			SustoCommand(discordLoritta),
			CarlyAaahCommand(discordLoritta),
			PetPetCommand(discordLoritta),
			EdnaldoTvCommand(discordLoritta),
			EdnaldoBandeiraCommand(discordLoritta),
			RipTvCommand(discordLoritta),
			AttackOnHeartCommand(discordLoritta),
			ToBeContinuedCommand(discordLoritta),
			TerminatorCommand(discordLoritta),
			MorrePragaCommand(discordLoritta),
			CortesFlowCommand(discordLoritta),
			CocieloChavesCommand(discordLoritta),
			AsciiCommand(discordLoritta),
			AtendenteCommand(discordLoritta),
			DrawnWordCommand(discordLoritta),
			EmojiMashupCommand(discordLoritta),
			TristeRealidadeCommand(discordLoritta),

			// ===[ ROBLOX ]===
			RbUserCommand(discordLoritta),
			RbGameCommand(discordLoritta)
		)
	}

	override fun register(command: Command<CommandContext>) {
		logger.info { "Registering $command with ${command.labels}" }
		commands.add(command)
	}

	override fun unregister(command: Command<CommandContext>) {
		logger.info { "Unregistering $command..." }
		commands.remove(command)
	}

	suspend fun dispatch(ev: LorittaMessageEvent, rawArguments: List<String>, serverConfig: ServerConfig, locale: BaseLocale, lorittaUser: LorittaUser): Boolean {
		// We order by more spaces in the first label -> less spaces, to avoid other commands taking precedence over other commands
		// I don't like how this works, we should create a command tree instead of doing this
		for (command in commands.sortedByDescending { it.labels.first().count { it.isWhitespace() }}) {
			val shouldBeProcessed = if (command is DiscordCommand)
				command.commandCheckFilter?.invoke(ev, rawArguments, serverConfig, locale, lorittaUser) ?: true
			else true

			if (shouldBeProcessed && dispatch(command, rawArguments, ev, serverConfig, locale, lorittaUser))
				return true
		}

		return false
	}

	suspend fun dispatch(command: Command<CommandContext>, rawArguments: List<String>, ev: LorittaMessageEvent, serverConfig: ServerConfig, locale: BaseLocale, lorittaUser: LorittaUser): Boolean {
		val message = ev.message.contentDisplay
		val user = ev.author

		val labels = command.labels.toMutableList()

		// Comandos com espaços na label, yeah!
		var valid = false
		var validLabel: String? = null

		val checkArguments = rawArguments.toMutableList()
		var removeArgumentCount = 0

		for (label in labels) {
			val subLabels = label.split(" ")

			removeArgumentCount = 0
			var validLabelCount = 0

			for ((index, subLabel) in subLabels.withIndex()) {
				val rawArgumentAt = checkArguments.getOrNull(index) ?: break

				if (rawArgumentAt.equals(subLabel, true)) { // ignoreCase = true ~ Permite usar "+cOmAnDo"
					validLabelCount++
					removeArgumentCount++
				}
			}

			if (validLabelCount == subLabels.size) {
				valid = true
				validLabel = subLabels.joinToString(" ")
				break
			}
		}

		if (valid && validLabel != null) {
			val isPrivateChannel = ev.isFromType(ChannelType.PRIVATE)
			val start = System.currentTimeMillis()

			val rawArgs = rawArguments.joinToString(" ").stripCodeMarks()
				.split(Constants.WHITE_SPACE_MULTIPLE_REGEX)
				.drop(removeArgumentCount)
				.toMutableList()

			val strippedArgs = MarkdownSanitizer.sanitize(rawArgs.joinToString(" ")).split(" ").toTypedArray()
			val args = strippedArgs

			val context = DiscordCommandContext(
				loritta,
				command,
				rawArgs,
				ev.message,
				locale,
				serverConfig,
				lorittaUser,
				validLabel
			)

			CommandUtils.logMessageEvent(ev, logger)

			try {
				// Check if user is banned
				if (LorittaUtilsKotlin.handleIfBanned(context, lorittaUser.profile))
					return true

				// Cooldown
				var commandCooldown = command.cooldown
				val donatorPaid = discordLoritta.getActiveMoneyFromDonationsAsync(ev.author.idLong)
				val guildId = ev.guild?.idLong
				val guildPaid = guildId?.let { serverConfig.getActiveDonationKeysValue() } ?: 0.0

				val plan = UserPremiumPlans.getPlanFromValue(donatorPaid)

				if (plan.lessCooldown) {
					commandCooldown /= 2
				}

				val (cooldownStatus, cooldownTriggeredAt, cooldown) = discordLoritta.commandCooldownManager.checkCooldown(
					ev,
					commandCooldown
				)

				if (cooldownStatus.sendMessage) {
					val fancy = DateUtils.formatDateDiff(cooldown + cooldownTriggeredAt, locale)

					val key = when (cooldownStatus) {
						CommandCooldownManager.CooldownStatus.RATE_LIMITED_SEND_MESSAGE ->
							LocaleKeyData(
								"commands.pleaseWaitCooldown",
								listOf(
									LocaleStringData(fancy),
									LocaleStringData("\uD83D\uDE45")
								)
							)
						CommandCooldownManager.CooldownStatus.RATE_LIMITED_SEND_MESSAGE_REPEATED ->
							LocaleKeyData(
								"commands.pleaseWaitCooldownRepeated",
								listOf(
									LocaleStringData(fancy),
									LocaleStringData(Emotes.LORI_HMPF.toString())
								)
							)
						else -> throw IllegalArgumentException("Invalid Cooldown Status $cooldownStatus, marked as send but there isn't any locale keys related to it!")
					}

					context.reply(
						LorittaReply(
							locale[key],
							"\uD83D\uDD25"
						)
					)
					return true
				} else if (cooldownStatus == CommandCooldownManager.CooldownStatus.RATE_LIMITED_MESSAGE_ALREADY_SENT) return true

				if (serverConfig.blacklistedChannels.contains(ev.channel.idLong) && !lorittaUser.hasPermission(
						LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
					if (serverConfig.warnIfBlacklisted) {
						if (serverConfig.blacklistedChannels.isNotEmpty() && ev.guild != null && ev.member != null && ev.textChannel != null) {
							val generatedMessage = MessageUtils.generateMessage(
								serverConfig.blacklistedWarning ?: "???",
								listOf(ev.member, ev.textChannel),
								ev.guild
							)
							if (generatedMessage != null)
								ev.textChannel.sendMessage(generatedMessage)
									.referenceIfPossible(ev.message, serverConfig, true)
									.await()
						}
					}
					return true // Ignorar canais bloqueados (return true = fast break, se está bloqueado o canal no primeiro comando que for executado, os outros obviamente também estarão)
				}

				if (!isPrivateChannel && ev.guild != null && ev.member != null) {
					// Verificar se o comando está ativado na guild atual
					if (CommandUtils.checkIfCommandIsDisabledInGuild(serverConfig, locale, ev.channel, ev.member, command.commandName))
						return true
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

				if (!isPrivateChannel && ev.member != null && ev.textChannel != null && command is DiscordCommand) {
					val missingPermissions = command.userRequiredLorittaPermissions.filterNot { lorittaUser.hasPermission(it) }

					if (missingPermissions.isNotEmpty()) {
						// oh no
						val required = missingPermissions.joinToString(", ", transform = { "`" + locale["commands.loriPermission${it.name}"] + "`"})
						var message = locale["commands.loriMissingPermission", required]

						if (ev.member.hasPermission(Permission.ADMINISTRATOR) || ev.member.hasPermission(Permission.MANAGE_SERVER)) {
							message += " ${locale["commands.loriMissingPermissionCanConfigure", loritta.instanceConfig.loritta.website.url]}"
						}
						context.reply(
							LorittaReply(
								message,
								Constants.ERROR
							)
						)
						return true
					}
				}

				if (args.isNotEmpty() && args[0] == "🤷") { // Usar a ajuda caso 🤷 seja usado
					context.explain()
					return true
				}

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
							message = locale["commands.cantUseInPrivate"],
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

				if (ev.guild != null && (LorittaUtils.isGuildOwnerBanned(lorittaUser._profile, ev.guild) || LorittaUtils.isGuildBanned(ev.guild)))
					return true

				// We don't care about locking the row just to update the sent at field
				loritta.newSuspendedTransaction(transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED) {
					lorittaUser.profile.lastCommandSentAt = System.currentTimeMillis()
				}

				CommandUtils.trackCommandToDatabase(ev, command.commandName)

				loritta.newSuspendedTransaction {
					val profile = serverConfig.getUserDataIfExistsNested(lorittaUser.profile.userId)

					if (profile != null && !profile.isInGuild)
						profile.isInGuild = true
				}

				lorittaShards.updateCachedUserData(user)

				logger.info { "Executor Callback: ${command.executor}" }
				command.executor.invoke(context)

				if (!isPrivateChannel && ev.guild != null) {
					if (ev.guild.selfMember.hasPermission(ev.textChannel!!, Permission.MESSAGE_MANAGE) && (serverConfig.deleteMessageAfterCommand)) {
						ev.message.textChannel.deleteMessageById(ev.messageId).queue({}, {
							// We don't care if we weren't able to delete the message because it was already deleted
						})
					}
				}

				val end = System.currentTimeMillis()
				val commandLatency = end - start
				Prometheus.COMMAND_LATENCY.labels(command.commandName).observe(commandLatency.toDouble())

				CommandUtils.logMessageEventComplete(ev, logger, commandLatency)
				return true
			} catch (e: Exception) {
				if (e is CancellationException) {
					logger.error(e) { "RestAction in command ${command.commandName} has been cancelled" }
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
					context.reply(e.reply)
					return true
				}

				logger.error("Exception ao executar comando ${command.commandName}", e)

				// Avisar ao usuário que algo deu muito errado
				val mention = "${ev.author.asMention} "
				var reply = "\uD83E\uDD37 **|** " + mention + locale["commands.errorWhileExecutingCommand", Emotes.LORI_RAGE, Emotes.LORI_CRYING]

				if (!e.message.isNullOrEmpty())
					reply += " `${e.message!!.escapeMentions()}`"

				if (ev.isFromType(ChannelType.PRIVATE) || (ev.isFromType(ChannelType.TEXT) && ev.textChannel != null && ev.textChannel.canTalk()))
					ev.channel.sendMessage(reply)
						.referenceIfPossible(ev.message, serverConfig, true)
						.await()

				return true
			}
		}
		return false
	}
}