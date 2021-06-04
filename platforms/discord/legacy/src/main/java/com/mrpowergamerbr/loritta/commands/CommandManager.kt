package com.mrpowergamerbr.loritta.commands

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.*
import com.mrpowergamerbr.loritta.commands.vanilla.administration.*
import com.mrpowergamerbr.loritta.commands.vanilla.discord.*
import com.mrpowergamerbr.loritta.commands.vanilla.economy.*
import com.mrpowergamerbr.loritta.commands.vanilla.images.*
import com.mrpowergamerbr.loritta.commands.vanilla.magic.*
import com.mrpowergamerbr.loritta.commands.vanilla.minecraft.*
import com.mrpowergamerbr.loritta.commands.vanilla.misc.*
import com.mrpowergamerbr.loritta.commands.vanilla.music.LyricsCommand
import com.mrpowergamerbr.loritta.commands.vanilla.pokemon.PokedexCommand
import com.mrpowergamerbr.loritta.commands.vanilla.social.*
import com.mrpowergamerbr.loritta.commands.vanilla.undertale.UndertaleBattleCommand
import com.mrpowergamerbr.loritta.commands.vanilla.undertale.UndertaleBoxCommand
import com.mrpowergamerbr.loritta.commands.vanilla.utils.*
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.DateUtils
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.localized
import com.mrpowergamerbr.loritta.utils.extensions.referenceIfPossible
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.locale.LocaleStringData
import net.perfectdreams.loritta.dao.servers.moduleconfigs.MiscellaneousConfig
import net.perfectdreams.loritta.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.utils.*
import net.perfectdreams.loritta.utils.metrics.Prometheus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.sql.Connection
import java.util.*
import java.util.concurrent.CancellationException
import java.util.jar.JarFile

class CommandManager(loritta: Loritta) {
	companion object {
		val logger = KotlinLogging.logger {}
	}

	var commandMap: MutableList<AbstractCommand> = ArrayList()

	init {
		commandMap.add(RollCommand())
		commandMap.add(FaustaoCommand())
		commandMap.add(CaraCoroaCommand())
		commandMap.add(PedraPapelTesouraCommand())
		commandMap.add(VaporondaCommand())
		commandMap.add(QualidadeCommand())
		commandMap.add(VaporQualidadeCommand())
		commandMap.add(TretaNewsCommand())
		commandMap.add(MagicBallCommand())
		commandMap.add(NyanCatCommand())
		commandMap.add(PrimeirasPalavrasCommand())
		commandMap.add(InverterCommand())
		// commandMap.add(SpinnerCommand())
		commandMap.add(LavaCommand())
		commandMap.add(LavaReversoCommand())
		commandMap.add(ShipCommand())
		commandMap.add(AvaliarWaifuCommand())
		commandMap.add(RazoesCommand())
		commandMap.add(DeusCommand())
		commandMap.add(PerfeitoCommand())
		commandMap.add(TrumpCommand())
		commandMap.add(CepoCommand())
		commandMap.add(DeusesCommand())
		commandMap.add(GangueCommand())
		commandMap.add(AmigosCommand())
		commandMap.add(DiscordiaCommand())
		commandMap.add(AmizadeCommand())
		commandMap.add(PerdaoCommand())
		commandMap.add(RipVidaCommand())
		commandMap.add(JoojCommand())
		commandMap.add(OjjoCommand())
		commandMap.add(TwitchCommand())

		// =======[ IMAGENS ]======
		commandMap.add(GetOverHereCommand())
		commandMap.add(ManiaTitleCardCommand())
		commandMap.add(LaranjoCommand())
		commandMap.add(TriggeredCommand())
		commandMap.add(GumballCommand())
		commandMap.add(ContentAwareScaleCommand())
		commandMap.add(SwingCommand())
		commandMap.add(DemonCommand())
		commandMap.add(KnuxThrowCommand())
		commandMap.add(TextCraftCommand())
		commandMap.add(DrawnMaskCommand())

		// =======[ DIVERSÃO ]======
		commandMap.add(BemBoladaCommand())
		commandMap.add(TodoGrupoTemCommand())
		commandMap.add(TioDoPaveCommand())
		commandMap.add(VemDeZapCommand())

		// =======[ MISC ]======
		commandMap.add(AjudaCommand())
		commandMap.add(PingCommand())
		commandMap.add(SayCommand())
		commandMap.add(EscolherCommand())
		commandMap.add(LanguageCommand())
		commandMap.add(PatreonCommand())

		// =======[ SOCIAL ]======
		commandMap.add(PerfilCommand())
		commandMap.add(BackgroundCommand())
		commandMap.add(SobreMimCommand())
		commandMap.add(RepCommand())
		commandMap.add(RankCommand())
		commandMap.add(EditarXPCommand())
		commandMap.add(AfkCommand())
		commandMap.add(MarryCommand())
		commandMap.add(DivorceCommand())
		commandMap.add(GenderCommand())

		// =======[ UTILS ]=======
		commandMap.add(TranslateCommand())
		commandMap.add(WikipediaCommand())
		commandMap.add(MoneyCommand())
		commandMap.add(ColorInfoCommand())
		commandMap.add(LembrarCommand())
		commandMap.add(DicioCommand())
		commandMap.add(TempoCommand())
		commandMap.add(PackageInfoCommand())
		commandMap.add(AnagramaCommand())
		commandMap.add(CalculadoraCommand())
		commandMap.add(MorseCommand())
		commandMap.add(OCRCommand())
		commandMap.add(EncodeCommand())
		commandMap.add(LyricsCommand())

		// =======[ DISCORD ]=======
		try {
			commandMap.add(createBotinfoCommand())
		} catch (e: Exception) {
			// TODO: Fix this
			logger.warn(e) { "Exception while registering botinfo, are you running Loritta within a IDE?" }
		}
		commandMap.add(AvatarCommand())
		commandMap.add(ServerIconCommand())
		commandMap.add(EmojiCommand())
		commandMap.add(ServerInfoCommand())
		commandMap.add(InviteCommand())
		commandMap.add(UserInfoCommand())
		commandMap.add(InviteInfoCommand())
		commandMap.add(AddEmojiCommand())
		commandMap.add(RemoveEmojiCommand())
		commandMap.add(EmojiInfoCommand())

		// =======[ MINECRAFT ]========
		commandMap.add(OfflineUUIDCommand())
		commandMap.add(McAvatarCommand())
		commandMap.add(McUUIDCommand())
		commandMap.add(McStatusCommand())
		commandMap.add(McHeadCommand())
		commandMap.add(McBodyCommand())
		commandMap.add(SpigotMcCommand())
		commandMap.add(McConquistaCommand())
		commandMap.add(McSkinCommand())
		commandMap.add(McMoletomCommand())

		// =======[ UNDERTALE ]========
		commandMap.add(UndertaleBoxCommand())
		commandMap.add(UndertaleBattleCommand())

		// =======[ POKÉMON ]========
		commandMap.add(PokedexCommand())

		// =======[ ANIME ]========
		// commandMap.add(MALAnimeCommand())
		// commandMap.add(MALMangaCommand())

		// =======[ ADMIN ]========
		commandMap.add(RoleIdCommand())
		commandMap.add(MuteCommand())
		commandMap.add(UnmuteCommand())
		commandMap.add(SlowModeCommand())
		commandMap.add(KickCommand())
		commandMap.add(BanCommand())
		commandMap.add(UnbanCommand())
		commandMap.add(WarnCommand())
		commandMap.add(WarnListCommand())
		commandMap.add(QuickPunishmentCommand())
		commandMap.add(LockCommand())
		commandMap.add(UnlockCommand())

		// =======[ MAGIC ]========
		commandMap.add(ReloadCommand())
		commandMap.add(ServerInvitesCommand())
		commandMap.add(LorittaBanCommand())
		commandMap.add(LorittaUnbanCommand())
		commandMap.add(LoriServerListConfigCommand())
		commandMap.add(EvalKotlinCommand())
		if (loritta.config.loritta.environment == EnvironmentType.CANARY)
			commandMap.add(AntiRaidCommand())

		// =======[ ECONOMIA ]========
		commandMap.add(LoraffleCommand())
		commandMap.add(DailyCommand())
		commandMap.add(PagarCommand())
		commandMap.add(SonhosCommand())
		commandMap.add(LigarCommand())
	}

	private fun createBotinfoCommand(): BotInfoCommand {
		val path = this::class.java.protectionDomain.codeSource.location.path
		val jar = JarFile(path)
		val manifest = jar.manifest
		val mainAttributes = manifest.mainAttributes
		return BotInfoCommand(BuildInfo(mainAttributes))
	}

	suspend fun matches(ev: LorittaMessageEvent, rawArguments: List<String>, serverConfig: ServerConfig, locale: BaseLocale, lorittaUser: LorittaUser): Boolean {
		// Primeiro os comandos vanilla da Loritta(tm)
		for (command in commandMap) {
			if (matches(command, rawArguments, ev, serverConfig, locale, lorittaUser))
				return true
		}

		// Checking custom commands
		// To avoid unnecessary databases retrievals, we are going to check if the message starts with the server prefix or with Loritta's mention
		val nashornCommands = loritta.newSuspendedTransaction {
			CustomGuildCommands.select {
				CustomGuildCommands.guild eq serverConfig.id and (CustomGuildCommands.enabled eq true)
			}.toList()
		}.map {
			NashornCommand(
					it[CustomGuildCommands.label],
					it[CustomGuildCommands.code],
					it[CustomGuildCommands.codeType]
			)
		}

		for (command in nashornCommands) {
			if (matches(command, rawArguments, ev, serverConfig, locale, lorittaUser))
				return true
		}

		return false
	}

	/**
	 * Checks if the command should be handled (if all conditions are valid, like labels, etc)
	 *
	 * @param ev          the event wrapped in a LorittaMessageEvent
	 * @param legacyServerConfig        the server configuration
	 * @param legacyLocale      the language of the server
	 * @param lorittaUser the user that is executing this command
	 * @return            if the command was handled or not
	 */
	suspend fun matches(command: AbstractCommand, rawArguments: List<String>, ev: LorittaMessageEvent, serverConfig: ServerConfig, locale: BaseLocale, lorittaUser: LorittaUser): Boolean {
		val message = ev.message.contentDisplay
		val labels = mutableListOf(command.label)

		labels.addAll(command.aliases)

		// ignoreCase = true ~ Permite usar "+cOmAnDo"
		val valid = labels.any { rawArguments[0].equals(it, true) }

		if (valid) {
			val isPrivateChannel = ev.isFromType(ChannelType.PRIVATE)
			val start = System.currentTimeMillis()

			val rawArgs = rawArguments.joinToString(" ").stripCodeMarks().split(Constants.WHITE_SPACE_MULTIPLE_REGEX)
					.drop(1)
					.toTypedArray()
			val args = rawArgs
			val strippedArgs: Array<String>

			if (rawArgs.isNotEmpty()) {
				strippedArgs = MarkdownSanitizer.sanitize(rawArgs.joinToString(" ")).split(" ").toTypedArray()
			} else {
				strippedArgs = rawArgs
			}

			val context = CommandContext(serverConfig, lorittaUser, locale, ev, command, args, rawArgs, strippedArgs)

			try {
				CommandUtils.logMessageEvent(ev, logger)

				// Check if user is banned
				if (LorittaUtilsKotlin.handleIfBanned(context, lorittaUser.profile))
					return true

				// Cooldown
				var commandCooldown = command.cooldown
				val donatorPaid = loritta.getActiveMoneyFromDonationsAsync(ev.author.idLong)
				val guildId = ev.guild?.idLong
				val guildPaid = guildId?.let { serverConfig.getActiveDonationKeysValue() } ?: 0.0

				val plan = UserPremiumPlans.getPlanFromValue(donatorPaid)

				if (plan.lessCooldown) {
					commandCooldown /= 2
				}

				val (cooldownStatus, cooldownTriggeredAt, cooldown) = loritta.commandCooldownManager.checkCooldown(
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

				val miscellaneousConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<MiscellaneousConfig?>(loritta, ServerConfig::miscellaneousConfig)

				val enableBomDiaECia = miscellaneousConfig?.enableBomDiaECia ?: false

				if (serverConfig.blacklistedChannels.contains(ev.channel.idLong) && !lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
					if (!enableBomDiaECia || (enableBomDiaECia && command !is LigarCommand)) {
						if (serverConfig.warnIfBlacklisted) {
							if (serverConfig.blacklistedWarning?.isNotEmpty() == true && ev.guild != null && ev.member != null && ev.textChannel != null) {
								val generatedMessage = MessageUtils.generateMessage(
									serverConfig.blacklistedWarning ?: "???",
									listOf(ev.member, ev.textChannel, ev.guild),
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
				}

				if (command.hasCommandFeedback()) {
					// Sending typing status for every single command is costly (API limits!)
					// To avoid sending it every time, we check if we should send the typing status
					// (We only send it if the command takes a looong time to be executed)
					if (command.sendTypingStatus)
						ev.channel.sendTyping().await()
				}

				if (!isPrivateChannel && ev.guild != null && ev.member != null) {
					// Verificar se o comando está ativado na guild atual
					if (CommandUtils.checkIfCommandIsDisabledInGuild(serverConfig, locale, ev.channel, ev.member, command::class.simpleName!!))
						return true
				}

				// Se estamos dentro de uma guild... (Já que mensagens privadas não possuem permissões)
				if (!isPrivateChannel && ev.guild != null && ev.member != null && ev.textChannel != null) {
					// Verificar se a Loritta possui todas as permissões necessárias
					val botPermissions = ArrayList<Permission>(command.getBotPermissions())
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

				if (!isPrivateChannel && ev.member != null && ev.textChannel != null) {
					val missingPermissions = command.lorittaPermissions.filterNot { lorittaUser.hasPermission(it) }

					if (missingPermissions.isNotEmpty()) {
						// oh no
						val required = missingPermissions.joinToString(
							", ",
							transform = { "`" + locale["commands.loriPermission${it.name}"] + "`" })
						var message = locale["commands.loriMissingPermission", required]

						if (ev.member.hasPermission(Permission.ADMINISTRATOR) || ev.member.hasPermission(Permission.MANAGE_SERVER)) {
							message += " ${locale["commands.loriMissingPermissionCanConfigure", loritta.instanceConfig.loritta.website.url]}"
						}
						ev.textChannel.sendMessage(Constants.ERROR + " **|** ${ev.member.asMention} $message")
							.referenceIfPossible(ev.message, serverConfig, true)
							.await()
						return true
					}
				}

				if (args.isNotEmpty() && args[0] == "🤷") { // Usar a ajuda caso 🤷 seja usado
					command.explain(context)
					return true
				}

				if (context.cmd.onlyOwner && !loritta.config.isOwner(ev.author.id)) {
					context.reply(
							LorittaReply(
									locale["commands.commandOnlyForOwner"],
									Constants.ERROR
							)
					)
					return true
				}

				if (!context.canUseCommand()) {
					val requiredPermissions = command.getDiscordPermissions().filter { !ev.message.member!!.hasPermission(ev.message.textChannel, it) }
					val required = requiredPermissions.joinToString(", ", transform = { "`" + it.localized(locale) + "`" })
					context.reply(
							LorittaReply(
									locale["commands.userDoesntHavePermissionDiscord", required],
									Constants.ERROR
							)
					)
					return true
				}

				if (context.isPrivateChannel && !command.canUseInPrivateChannel()) {
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["commands.cantUseInPrivate"])
					return true
				}

				if (command.needsToUploadFiles()) {
					if (!LorittaUtils.canUploadFiles(context)) {
						return true
					}
				}

				// Vamos pegar uma mensagem aleatória de doação, se não for nula, iremos enviar ela :3
				DonateUtils.getRandomDonationMessage(
						locale,
						lorittaUser.profile,
						donatorPaid,
						guildPaid
				)?.let { context.reply(it) }

				if (!context.isPrivateChannel) {
					val nickname = context.guild.selfMember.nickname

					if (nickname != null) {
						// #LoritaTambémTemSentimentos
						val hasBadNickname = MiscUtils.hasInappropriateWords(nickname)

						if (hasBadNickname) {
							context.reply(
									LorittaReply(
										locale["commands.lorittaBadNickname"],
										"<:lori_triste:370344565967814659>"
									)
							)
							if (context.guild.selfMember.hasPermission(Permission.NICKNAME_CHANGE)) {
								context.guild.modifyNickname(context.guild.selfMember, null).queue()
							} else {
								return true
							}
						}
					}
				}

				if (ev.guild != null && (LorittaUtils.isGuildOwnerBanned(lorittaUser._profile, ev.guild) || LorittaUtils.isGuildBanned(ev.guild)))
					return true

				// We don't care about locking the row just to update the sent at field
				loritta.newSuspendedTransaction(transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED) {
					lorittaUser.profile.lastCommandSentAt = System.currentTimeMillis()
				}

				CommandUtils.trackCommandToDatabase(ev, command::class.simpleName ?: "UnknownCommand")

				loritta.newSuspendedTransaction {
					val profile = serverConfig.getUserDataIfExistsNested(lorittaUser.profile.userId)

					if (profile != null && !profile.isInGuild)
						profile.isInGuild = true
				}

				lorittaShards.updateCachedUserData(context.userHandle)

				command.run(context, context.locale)

				if (!isPrivateChannel && ev.guild != null) {
					if (ev.guild.selfMember.hasPermission(ev.textChannel!!, Permission.MESSAGE_MANAGE) && (serverConfig.deleteMessageAfterCommand)) {
						ev.message.textChannel.deleteMessageById(ev.messageId).queue({}, {
							// We don't care if we weren't able to delete the message because it was already deleted
						})
					}
				}

				val end = System.currentTimeMillis()
				val commandLatency = end - start
				Prometheus.COMMAND_LATENCY.labels(command::class.simpleName).observe(commandLatency.toDouble())

				CommandUtils.logMessageEventComplete(ev, logger, commandLatency)
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

				logger.error("Exception ao executar comando ${command.javaClass.simpleName}", e)

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
