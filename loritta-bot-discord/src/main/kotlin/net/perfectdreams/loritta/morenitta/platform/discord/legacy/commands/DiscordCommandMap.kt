package net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands

import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.dv8tion.jda.api.utils.TimeFormat
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.locale.LocaleStringData
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.api.commands.*
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.RenameChannelCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.RenameEmojiCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.UnwarnCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.economy.*
import net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`.GiveawayCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`.GiveawayEndCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`.GiveawayRerollCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.AsciiCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.CocieloChavesCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.EmojiMashupCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.MorrePragaCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.magic.LoriToolsCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.misc.DiscordBotListStatusCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.misc.DiscordBotListTopCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.misc.DiscordBotListTopLocalCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.social.*
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getLocalizedName
import net.perfectdreams.loritta.morenitta.utils.extensions.referenceIfPossible
import java.sql.Connection
import java.util.concurrent.CancellationException

class DiscordCommandMap(val loritta: LorittaBot) : CommandMap<Command<CommandContext>> {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
	}

	val commands = mutableListOf<Command<CommandContext>>()

	init {
		registerAll(
			// ===[ MAGIC ]===
			LoriToolsCommand(loritta),

			// ===[ ECONOMY ]===
			TransactionsCommand(loritta),
			GuessNumberCommand(loritta),
			ScratchCardCommand(loritta),
			ScratchCardTopCommand(loritta),
			CoinFlipBetStatsCommand(loritta),

			// ===[ SOCIAL ]===
			BomDiaECiaStatusCommand(loritta),
			BomDiaECiaTopCommand(loritta),
			BomDiaECiaTopLocalCommand(loritta),
			RepTopCommand(loritta),
//			XpNotificationsCommand(loritta),
			RenameChannelCommand(loritta),
			RenameEmojiCommand(loritta),

			// ===[ ADMIN ]===
			UnwarnCommand(loritta),

			// ===[ MISC ]===
//			DiscordBotListCommand(loritta),
			DiscordBotListStatusCommand(loritta),
			DiscordBotListTopCommand(loritta),
			DiscordBotListTopLocalCommand(loritta),

			// ===[ DISCORD ]===
			// ChannelInfoCommand(loritta),
			// GuildBannerCommand(loritta),
			// RoleInfoCommand(loritta),

			// ===[ FUN ]===
			GiveawayCommand(loritta),

			// ===[ IMAGES ]===
			// ArtCommand(loritta),
			// AtaCommand(loritta),
			// BobBurningPaperCommand(loritta),
			// BolsoDrakeCommand(loritta),
			// BolsoFrameCommand(loritta),
			// Bolsonaro2Command(loritta),
			// BolsonaroCommand(loritta),
			// BriggsCoverCommand(loritta),
			// BuckShirtCommand(loritta),
			// CanellaDvdCommand(loritta),
			// ChicoAtaCommand(loritta),
			// DrakeCommand(loritta),
			// GessyAtaCommand(loritta),
			// LoriAtaCommand(loritta),
			// LoriDrakeCommand(loritta),
			// LoriSignCommand(loritta),
			// PassingPaperCommand(loritta),
			// PepeDreamCommand(loritta),
			// QuadroCommand(loritta),
			// RomeroBrittoCommand(loritta),
			// SAMCommand(loritta),
			// StudiopolisTvCommand(loritta),
			// SustoCommand(loritta),
			// PetPetCommand(loritta),
			// EdnaldoTvCommand(loritta),
			// EdnaldoBandeiraCommand(loritta),
			// RipTvCommand(loritta),
			// ToBeContinuedCommand(loritta),
			// TerminatorCommand(loritta),
			MorrePragaCommand(loritta),
			// CortesFlowCommand(loritta),
			CocieloChavesCommand(loritta),
			AsciiCommand(loritta),
			// AtendenteCommand(loritta),
			// DrawnWordCommand(loritta),
			EmojiMashupCommand(loritta)
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

	suspend fun dispatch(ev: LorittaMessageEvent, rawArguments: List<String>, serverConfig: ServerConfig, locale: BaseLocale, i18nContext: I18nContext, lorittaUser: LorittaUser): Boolean {
		// We order by more spaces in the first label -> less spaces, to avoid other commands taking precedence over other commands
		// I don't like how this works, we should create a command tree instead of doing this
		for (command in commands.sortedByDescending { it.labels.first().count { it.isWhitespace() }}) {
			val shouldBeProcessed = if (command is DiscordCommand)
				command.commandCheckFilter?.invoke(ev, rawArguments, serverConfig, locale, lorittaUser) ?: true
			else true

			if (shouldBeProcessed && dispatch(command, rawArguments, ev, serverConfig, locale, i18nContext, lorittaUser))
				return true
		}

		return false
	}

	suspend fun dispatch(command: Command<CommandContext>, rawArguments: List<String>, ev: LorittaMessageEvent, serverConfig: ServerConfig, locale: BaseLocale, i18nContext: I18nContext, lorittaUser: LorittaUser): Boolean {
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

			// This ia workaround because "+giveaway setup" is handled by the new interactions slash command framework
			// However "+giveaway" takes priority, which we do not want
			if ((validLabel == "giveaway" || validLabel == "sorteio") && rawArgs.isNotEmpty())
				return false

			val strippedArgs = MarkdownSanitizer.sanitize(rawArgs.joinToString(" ")).split(" ").toTypedArray()
			val args = strippedArgs

			val context = DiscordCommandContext(
				loritta,
				command,
				rawArgs,
				ev.message,
				locale,
				i18nContext,
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
				// Skip cooldown if the user is not a Loritta supervisor...
				if (!context.user.isLorittaSupervisor(context.loritta.lorittaShards)) {
					var commandCooldown = command.cooldown
					val donatorPaid = loritta.getActiveMoneyFromDonations(ev.author.idLong)
					val guildId = ev.guild?.idLong
					val guildPaid = guildId?.let { serverConfig.getActiveDonationKeysValue(loritta) } ?: 0.0

					val plan = UserPremiumPlans.getPlanFromValue(donatorPaid)

					if (plan.lessCooldown) {
						commandCooldown /= 2
					}

					val (cooldownStatus, cooldownTriggeredAt, cooldown) = loritta.commandCooldownManager.checkCooldown(
						ev,
						commandCooldown
					)

					if (cooldownStatus.sendMessage) {
						val fancy = TimeFormat.RELATIVE.format(cooldown + cooldownTriggeredAt)

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
				}

				if (serverConfig.blacklistedChannels.contains(ev.channel.idLong) && !lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
					if (serverConfig.warnIfBlacklisted) {
						if (serverConfig.blacklistedChannels.isNotEmpty() && ev.guild != null && ev.member != null && ev.textChannel != null) {
							val generatedMessage = MessageUtils.generateMessageOrFallbackIfInvalid(
								i18nContext,
								serverConfig.blacklistedWarning ?: "???",
								listOf(ev.member, ev.textChannel, ev.guild),
								ev.guild,
								emptyMap(),
								generationErrorMessageI18nKey = I18nKeysData.InvalidMessages.CommandDenylist
							)

							ev.textChannel.sendMessage(generatedMessage)
								.referenceIfPossible(ev.message, serverConfig, true)
								.await()
						}
					}
					return true // Ignorar canais bloqueados (return true = fast break, se está bloqueado o canal no primeiro comando que for executado, os outros obviamente também estarão)
				}

				if (!isPrivateChannel && ev.guild != null && ev.member != null) {
					// Verificar se o comando está ativado na guild atual
					if (CommandUtils.checkIfCommandIsDisabledInGuild(loritta, serverConfig, locale, ev.channel, ev.member, command.commandName))
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
						val required = missingPermissions.joinToString(", ", transform = { "`" + it.getLocalizedName(i18nContext) + "`" })
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
							message += " ${locale["commands.loriMissingPermissionCanConfigure", loritta.config.loritta.website.url]}"
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

				if (command.onlyOwner && !loritta.isOwner(user.id)) {
					context.reply(
						LorittaReply(
							locale["commands.commandOnlyForOwner"],
							Constants.ERROR
						)
					)
					return true
				}

				if (command is DiscordCommand) {
					val missingRequiredPermissions = command.userRequiredPermissions.filterNot { ev.message.member!!.hasPermission(ev.message.guildChannel, it) }

					if (missingRequiredPermissions.isNotEmpty()) {
						val required = missingRequiredPermissions.joinToString(", ", transform = { "`" + it.getLocalizedName(i18nContext) + "`" })
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

				if (ev.guild != null && (LorittaUtils.isGuildOwnerBanned(loritta, lorittaUser._profile, ev.guild) || LorittaUtils.isGuildBanned(loritta, ev.guild)))
					return true

				// We don't care about locking the row just to update the sent at field
				loritta.newSuspendedTransaction(transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED) {
					lorittaUser.profile.lastCommandSentAt = System.currentTimeMillis()
				}

				CommandUtils.trackCommandToDatabase(loritta, ev, command.commandName)

				loritta.newSuspendedTransaction {
					val profile = serverConfig.getUserDataIfExistsNested(lorittaUser.profile.userId)

					if (profile != null && !profile.isInGuild)
						profile.isInGuild = true
				}

				loritta.lorittaShards.updateCachedUserData(user)

				logger.info { "Executor Callback: ${command.executor}" }
				command.executor.invoke(context)

				if (!isPrivateChannel && ev.guild != null) {
					if (ev.guild.selfMember.hasPermission(ev.channel as GuildChannel, Permission.MESSAGE_MANAGE) && (serverConfig.deleteMessageAfterCommand)) {
						ev.message.guildChannel.deleteMessageById(ev.messageId).queue({}, {
							// We don't care if we weren't able to delete the message because it was already deleted
						})
					}
				}

				val end = System.currentTimeMillis()
				val commandLatency = end - start

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

				logger.error(e) { "Exception ao executar comando ${command.commandName}" }

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