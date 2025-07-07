package net.perfectdreams.loritta.morenitta.commands

import dev.minn.jda.ktx.messages.MessageCreate
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.dv8tion.jda.api.utils.TimeFormat
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.locale.LocaleStringData
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.nashorn.NashornCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.*
import net.perfectdreams.loritta.morenitta.commands.vanilla.discord.InviteCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.discord.ServerInfoCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`.*
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.*
import net.perfectdreams.loritta.morenitta.commands.vanilla.magic.*
import net.perfectdreams.loritta.morenitta.commands.vanilla.minecraft.*
import net.perfectdreams.loritta.morenitta.commands.vanilla.misc.PatreonCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.music.LyricsCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.social.*
import net.perfectdreams.loritta.morenitta.commands.vanilla.undertale.UndertaleBattleCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.undertale.UndertaleBoxCommand
import net.perfectdreams.loritta.morenitta.commands.vanilla.utils.*
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.MiscellaneousConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getLocalizedName
import net.perfectdreams.loritta.morenitta.utils.extensions.referenceIfPossible
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.sql.Connection
import java.util.concurrent.CancellationException

class CommandManager(val loritta: LorittaBot) {
	companion object {
		val logger by HarmonyLoggerFactory.logger {}
	}

	var commandMap: MutableList<AbstractCommand> = ArrayList()

	init {
		commandMap.add(VaporondaCommand(loritta))
		commandMap.add(QualidadeCommand(loritta))
		commandMap.add(VaporQualidadeCommand(loritta))
		commandMap.add(TretaNewsCommand(loritta))
		commandMap.add(MagicBallCommand(loritta))
		commandMap.add(NyanCatCommand(loritta))
		commandMap.add(PrimeirasPalavrasCommand(loritta))
		// commandMap.add(InverterCommand(loritta))
		// commandMap.add(SpinnerCommand(loritta))
		commandMap.add(LavaCommand(loritta))
		commandMap.add(LavaReversoCommand(loritta))
		commandMap.add(RazoesCommand(loritta))
		commandMap.add(DeusCommand(loritta))
		commandMap.add(PerfeitoCommand(loritta))
		// commandMap.add(TrumpCommand(loritta))
		// commandMap.add(CepoCommand(loritta))
		commandMap.add(DeusesCommand(loritta))
		commandMap.add(GangueCommand(loritta))
		commandMap.add(DiscordiaCommand(loritta))
		commandMap.add(AmizadeCommand(loritta))
		commandMap.add(PerdaoCommand(loritta))
		commandMap.add(RipVidaCommand(loritta))
		commandMap.add(JoojCommand(loritta))
		commandMap.add(OjjoCommand(loritta))
		commandMap.add(TwitchCommand(loritta))

		// =======[ IMAGENS ]======
		// commandMap.add(GetOverHereCommand(loritta))
		// commandMap.add(ManiaTitleCardCommand(loritta))
		commandMap.add(LaranjoCommand(loritta))
		commandMap.add(TriggeredCommand(loritta))
		commandMap.add(GumballCommand(loritta))
		commandMap.add(ContentAwareScaleCommand(loritta))
		commandMap.add(SwingCommand(loritta))
		commandMap.add(DemonCommand(loritta))
		// commandMap.add(KnuxThrowCommand(loritta))
		commandMap.add(TextCraftCommand(loritta))
		// commandMap.add(DrawnMaskCommand(loritta))

		// =======[ MISC ]======
		commandMap.add(SayCommand(loritta))
// 		commandMap.add(EscolherCommand(loritta))
		// commandMap.add(LanguageCommand(loritta))
		commandMap.add(PatreonCommand(loritta))

		// =======[ SOCIAL ]======
		commandMap.add(BackgroundCommand(loritta))
		commandMap.add(RepCommand(loritta))
//		commandMap.add(AfkCommand(loritta))
		commandMap.add(GenderCommand(loritta))

		// =======[ UTILS ]=======
		commandMap.add(TranslateCommand(loritta))
		commandMap.add(WikipediaCommand(loritta))
		commandMap.add(ColorInfoCommand(loritta))
		commandMap.add(TempoCommand(loritta))
//		commandMap.add(MorseCommand(loritta))
		commandMap.add(EncodeCommand(loritta))
		commandMap.add(LyricsCommand(loritta))

		// =======[ DISCORD ]=======
		// commandMap.add(ServerIconCommand(loritta))
		// commandMap.add(EmojiCommand(loritta))
		commandMap.add(ServerInfoCommand(loritta))
		commandMap.add(InviteCommand(loritta))
		// commandMap.add(InviteInfoCommand(loritta))
		// commandMap.add(EmojiInfoCommand(loritta))

		// =======[ MINECRAFT ]========
		commandMap.add(OfflineUUIDCommand(loritta))
		commandMap.add(McAvatarCommand(loritta))
		commandMap.add(McUUIDCommand(loritta))
		commandMap.add(McHeadCommand(loritta))
		commandMap.add(McBodyCommand(loritta))
		commandMap.add(SpigotMcCommand(loritta))
		commandMap.add(McConquistaCommand(loritta))
		commandMap.add(McSkinCommand(loritta))
		commandMap.add(McMoletomCommand(loritta))

		// =======[ UNDERTALE ]========
		commandMap.add(UndertaleBoxCommand(loritta))
		commandMap.add(UndertaleBattleCommand(loritta))

		// =======[ ANIME ]========
		// commandMap.add(MALAnimeCommand(loritta))
		// commandMap.add(MALMangaCommand(loritta))

		// =======[ ADMIN ]========
		commandMap.add(RoleIdCommand(loritta))
		commandMap.add(MuteCommand(loritta))
		commandMap.add(UnmuteCommand(loritta))
		commandMap.add(SlowModeCommand(loritta))
		commandMap.add(UnbanCommand(loritta))
		commandMap.add(WarnListCommand(loritta))
		commandMap.add(QuickPunishmentCommand(loritta))
		commandMap.add(LockCommand(loritta))
		commandMap.add(UnlockCommand(loritta))

		// =======[ MAGIC ]========
		commandMap.add(ReloadCommand(loritta))
		commandMap.add(ServerInvitesCommand(loritta))
		commandMap.add(LorittaBanCommand(loritta))
		commandMap.add(LorittaUnbanCommand(loritta))
		commandMap.add(LoriServerListConfigCommand(loritta))
		// TODO: Fix compilation?
		// if (loritta.config.loritta.environment == EnvironmentType.CANARY)
		// 	commandMap.add(AntiRaidCommand(loritta))
	}

	suspend fun matches(ev: LorittaMessageEvent, rawArguments: List<String>, serverConfig: ServerConfig, locale: BaseLocale, i18nContext: I18nContext, lorittaUser: LorittaUser): Boolean {
		// Primeiro os comandos vanilla da Loritta(tm)
		for (command in commandMap) {
			if (matches(command, rawArguments, ev, serverConfig, locale, i18nContext, lorittaUser))
				return true
		}

		return false
	}

	suspend fun matchesNashornCommands(ev: LorittaMessageEvent, rawArguments: List<String>, serverConfig: ServerConfig, locale: BaseLocale, i18nContext: I18nContext, lorittaUser: LorittaUser): Boolean {
		// Checking custom commands
		// To avoid unnecessary databases retrievals, we are going to check if the message starts with the server prefix or with Loritta's mention
		val nashornCommands = loritta.newSuspendedTransaction {
			CustomGuildCommands.selectAll().where {
				CustomGuildCommands.guild eq serverConfig.id and (CustomGuildCommands.enabled eq true)
			}.toList()
		}.map {
			NashornCommand(
				loritta,
				it[CustomGuildCommands.label],
				it[CustomGuildCommands.code],
				it[CustomGuildCommands.codeType]
			)
		}

		for (command in nashornCommands) {
			if (matches(command, rawArguments, ev, serverConfig, locale, i18nContext, lorittaUser))
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
	suspend fun matches(command: AbstractCommand, rawArguments: List<String>, ev: LorittaMessageEvent, serverConfig: ServerConfig, locale: BaseLocale, i18nContext: I18nContext, lorittaUser: LorittaUser): Boolean {
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

			val context = CommandContext(loritta, serverConfig, lorittaUser, locale, i18nContext, ev, command, args, rawArgs, strippedArgs)

			try {
				CommandUtils.logMessageEvent(ev, logger)

				// Check if user is banned
				if (LorittaUtilsKotlin.handleIfBanned(context, lorittaUser.profile))
					return true

				// Cooldown
				// Skip cooldown if the user is not a Loritta supervisor...
				if (!context.userHandle.isLorittaSupervisor(context.loritta.lorittaShards)) {
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

				val miscellaneousConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<MiscellaneousConfig?>(loritta, ServerConfig::miscellaneousConfig)

				val enableBomDiaECia = miscellaneousConfig?.enableBomDiaECia ?: false

				if (serverConfig.blacklistedChannels.contains(ev.channel.idLong) && !lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
                    if (serverConfig.warnIfBlacklisted) {
                        if (serverConfig.blacklistedWarning?.isNotEmpty() == true && ev.guild != null && ev.member != null && ev.textChannel != null) {
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
					if (CommandUtils.checkIfCommandIsDisabledInGuild(loritta, serverConfig, locale, ev.channel, ev.member, command::class.simpleName!!))
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

				if (!isPrivateChannel && ev.member != null && ev.textChannel != null) {
					val missingPermissions = command.lorittaPermissions.filterNot { lorittaUser.hasPermission(it) }

					if (missingPermissions.isNotEmpty()) {
						// oh no
						val required = missingPermissions.joinToString(
							", ",
							transform = { "`" + locale["commands.loriPermission${it.name}"] + "`" })
						var message = locale["commands.loriMissingPermission", required]

						if (ev.member.hasPermission(Permission.ADMINISTRATOR) || ev.member.hasPermission(Permission.MANAGE_SERVER)) {
							message += " ${locale["commands.loriMissingPermissionCanConfigure", loritta.config.loritta.website.url]}"
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

				if (context.cmd.onlyOwner && !loritta.isOwner(ev.author.id)) {
					context.reply(
						LorittaReply(
							locale["commands.commandOnlyForOwner"],
							Constants.ERROR
						)
					)
					return true
				}

				if (!context.canUseCommand()) {
					val requiredPermissions = command.getDiscordPermissions().filter { !ev.message.member!!.hasPermission(ev.message.guildChannel, it) }
					val required = requiredPermissions.joinToString(", ", transform = { "`" + it.getLocalizedName(i18nContext) + "`" })
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
					loritta,
					locale,
					lorittaUser.profile
				)?.let { context.reply(it) }

				val currentGuild = context.guildOrNull
				if (currentGuild != null) {
					if (!loritta.discordSlashCommandScopeWorkaround.checkIfSlashCommandScopeIsEnabled(currentGuild, context.handle)) {
						context.sendMessage(MessageCreate { apply(loritta.discordSlashCommandScopeWorkaround.unauthMessage(currentGuild, context.handle)) })
					}
				}

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

				if (ev.guild != null && (LorittaUtils.isGuildOwnerBanned(loritta, lorittaUser._profile, ev.guild) || LorittaUtils.isGuildBanned(loritta, ev.guild)))
					return true

				// We don't care about locking the row just to update the sent at field
				loritta.newSuspendedTransaction(transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED) {
					lorittaUser.profile.lastCommandSentAt = System.currentTimeMillis()
				}

				CommandUtils.trackCommandToDatabase(loritta, ev, command::class.simpleName ?: "UnknownCommand")

				loritta.newSuspendedTransaction {
					val profile = serverConfig.getUserDataIfExistsNested(lorittaUser.profile.userId)

					if (profile != null && !profile.isInGuild)
						profile.isInGuild = true
				}

				loritta.lorittaShards.updateCachedUserData(context.userHandle)

				command.run(context, context.locale)

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

				logger.error(e) { "Exception ao executar comando ${command.javaClass.simpleName}" }

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
